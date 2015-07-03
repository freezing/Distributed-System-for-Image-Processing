package listeners;

import java.util.List;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.FindNodeResponse;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

import com.google.protobuf.InvalidProtocolBufferException;

import factories.MessageContainerFactory;

public class FindNodeRequestListener implements MessageListener {
	
	private KademliaNodeWorker worker;

	public FindNodeRequestListener(KademliaNodeWorker kademliaNodeWorker) {
		worker = kademliaNodeWorker;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindNodeRequest request;
		try {
			request = FindNodeRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		List<KademliaNode> nodes = worker.getKbuckets().getKClosest(request.getSearchId());
		
		worker.addToKBuckets(sender);
		
		FindNodeResponse response = FindNodeResponse.newBuilder().addAllResults(nodes)
				.setSearchId(request.getSearchId()).build();
		MessageContainer msg = MessageContainerFactory.make(worker.getNode(), response);
		worker.sendMessage(sender, msg);
	}

}
