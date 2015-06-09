package listeners;

import java.util.HashSet;
import java.util.Set;

import kademlia.KademliaNodeWorker;
import protos.KademliaProtos.FindValueResponse;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaNode;

import com.google.protobuf.InvalidProtocolBufferException;

public class FindValueResponseListener extends FindAnythingResponseListener {
	
	public static class NonConsistentValueException extends Exception {
		private static final long serialVersionUID = -6876837256450445333L;
	}

	private KademliaNodeWorker worker;

	private Set<HashTableValue> valueSet = new HashSet<HashTableValue>();
	
	public FindValueResponseListener(KademliaNodeWorker worker) {
		super();
		this.worker = worker;
	}

	@Override
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindValueResponse response = parseResponse(message);
		worker.addAllToKBuckets(response.getResultsList());
		valueSet.add(response.getValueResult());
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
	
	@Override
	public boolean hasValue() {
		return !valueSet.isEmpty();
	}
	
	public HashTableValue getValue() throws NonConsistentValueException {
		if (valueSet.isEmpty()) return null;
		else if (valueSet.size() == 1) {
			return (HashTableValue)valueSet.toArray()[0];
		} else {
			throw new NonConsistentValueException();
		}
	}
	
	public void resetValue() {
		valueSet.clear();
	}

}
