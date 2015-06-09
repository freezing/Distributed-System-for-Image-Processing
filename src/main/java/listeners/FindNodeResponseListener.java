package listeners;

import com.google.protobuf.InvalidProtocolBufferException;

import kademlia.KademliaNodeWorker;
import protos.KademliaProtos.FindNodeResponse;
import protos.KademliaProtos.KademliaNode;

public class FindNodeResponseListener extends FindAnythingResponseListener {

	private KademliaNodeWorker worker;
	
	public FindNodeResponseListener(KademliaNodeWorker worker) {
		super();
		this.worker = worker;
	}

	@Override
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindNodeResponse response = parseResponse(message);
		worker.addAllToKBuckets(response.getResultsList());
		worker.addToKBuckets(sender);
		latchCountDown(response.getSearchId());
	}

	private FindNodeResponse parseResponse(byte[] message) {
		try {
			return FindNodeResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
	
	// FIND_NODE will never have a value
	public boolean hasValue() {
		return false;
	}
}
