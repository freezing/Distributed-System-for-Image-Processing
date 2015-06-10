package listeners;

import java.util.List;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.FindValueRequest;
import protos.KademliaProtos.FindValueResponse;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

import com.google.protobuf.InvalidProtocolBufferException;

import factories.MessageContainerFactory;

public class FindValueRequestListener implements MessageListener {
	
	private KademliaNodeWorker worker;

	public FindValueRequestListener(KademliaNodeWorker kademliaNodeWorker) {
		worker = kademliaNodeWorker;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindValueRequest request;
		try {
			request = FindValueRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		
		worker.addToKBuckets(sender);
		FindValueResponse response;		
		HashTableValue value = worker.getFromLocalHashMap(request.getSearchId());
		if (value != null) {
			response = FindValueResponse.newBuilder().setValueResult(value)
					.setSearchId(request.getSearchId()).build();
			System.out.println("Total in map: "+worker.getAllLocalHashMapItems().size());
		} else {
			List<KademliaNode> nodes = worker.getKbuckets().getKClosest(request.getSearchId());
			
			response = FindValueResponse.newBuilder().addAllResults(nodes)
					.setSearchId(request.getSearchId()).build();
		}
		MessageContainer msg = MessageContainerFactory.make(worker.getNode(), response);
		worker.sendMessage(sender, msg);
	}

}
