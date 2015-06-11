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
		worker.addToKBuckets(sender);
		
		if (response.hasValueResult()) {
			synchronized(valueSet) {
				valueSet.add(response.getValueResult());
			}
		}
		
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
		synchronized(valueSet) {
			return !valueSet.isEmpty();
		}
	}
	
	public HashTableValue getValue() throws NonConsistentValueException {
		int size;
		synchronized(valueSet) {
			size = valueSet.size();
		}
		if (size == 0) return null;
		else if (size == 1) {
			return (HashTableValue)valueSet.toArray()[0];
		} else {
			if (worker.getNode().getPort() == 20000) {
				for (HashTableValue value : valueSet) {
					System.out.println("============ EXCEPTION VALUES ======================================================================");
					System.out.println(value);
					System.out.println("==================================================================================");
				}
			}
			throw new NonConsistentValueException();
		}
	}
	
	public void resetValue() {
		synchronized(valueSet) {
			valueSet.clear();
		}
	}

}
