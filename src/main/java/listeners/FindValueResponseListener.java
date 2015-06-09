package listeners;

import kademlia.KademliaNodeWorker;
import protos.KademliaProtos.FindNodeResponse;
import protos.KademliaProtos.FindValueResponse;
import protos.KademliaProtos.KademliaNode;

import com.google.protobuf.InvalidProtocolBufferException;

public class FindValueResponseListener extends FindAnythingResponseListener {

private KademliaNodeWorker worker;
	
	public FindValueResponseListener(KademliaNodeWorker worker) {
		super();
		this.worker = worker;
	}

	@Override
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindValueResponse response = parseResponse(message);
		worker.addAllToKBuckets(response.getResultsList());
		//response.getValueResult()		
		worker.addToKBuckets(sender);
		latchCountDown(response.getSearchId());
	}

	private FindValueResponse parseResponse(byte[] message) {
		try {
			return FindValueResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

}
