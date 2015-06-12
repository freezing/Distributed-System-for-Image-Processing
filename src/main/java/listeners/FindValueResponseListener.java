package listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kademlia.KademliaNodeWorker;
import protos.KademliaProtos.FindValueResponse;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public class FindValueResponseListener extends FindAnythingResponseListener {
	
	private class HashTableValueWithSender {
		private HashTableValue value;
		private KademliaNode sender;
		
		public HashTableValueWithSender(HashTableValue value, KademliaNode sender) {
			this.value = value;
			this.sender = sender;
		}
		
		public HashTableValue getValue() {
			return value;
		}
		
		public KademliaId getDistance(KademliaId from) {
			return KademliaUtils.XOR(sender.getId(), from);
		}
	}
	
	private class HashTableValueBucket {
		private ArrayList<HashTableValueWithSender> buckets = new ArrayList<HashTableValueWithSender>();
		
		private KademliaNode closest = null;
		private boolean isStored = false;
		
		private int count = 1;
		
		public synchronized void incrementCount() {
			count++;
		}
		
		public synchronized boolean decrementCount() {
			count--;
			if (count == 0) return true;
			return false;
		}
		
		public synchronized void addToBucket(HashTableValueWithSender value) {
			buckets.add(value);
		}
		
		public synchronized boolean isEmpty() {
			return buckets.isEmpty();
		}
		
		public synchronized HashTableValue getBest(final KademliaId id) {
			HashTableValueWithSender value = Collections.min(buckets, new Comparator<HashTableValueWithSender>() {
				public int compare(HashTableValueWithSender o1, HashTableValueWithSender o2) {
					return KademliaUtils.compare(o1.getDistance(id), o2.getDistance(id));
				}
			});
			return value.getValue();
		}
		
		public synchronized KademliaNode getClosestNodeIfNotStored() {
			if (!isStored) return closest;
			return null;
		}
		
		public void updateClosest(KademliaId id, KademliaNode node, boolean returnedValue) {
			if (closest == null) {
				closest = node;
				isStored = returnedValue;
			} else {
				KademliaId o1 = KademliaUtils.XOR(id, node.getId());
				KademliaId o2 = KademliaUtils.XOR(id, closest.getId());
				if (KademliaUtils.compare(o1, o2) == -1) {
					closest = node;
					isStored = returnedValue;
				}
			}
		}
	}

	private KademliaNodeWorker worker;

	private Map<KademliaId,HashTableValueBucket> valueMap = new ConcurrentHashMap<KademliaId,HashTableValueBucket>();
	
	public FindValueResponseListener(KademliaNodeWorker worker) {
		super();
		this.worker = worker;
	}

	@Override
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindValueResponse response = parseResponse(message);
		
		worker.addAliveToKBuckets(sender);
		worker.addAllToKBuckets(response.getResultsList());
				
		HashTableValueBucket valueBucket = valueMap.get(response.getSearchId());
		if (valueBucket != null) {
			if (response.hasValueResult()) {
				valueBucket.updateClosest(response.getSearchId(), sender, true);
				
				valueBucket.addToBucket(new HashTableValueWithSender(response.getValueResult(), sender));
			} else {
				valueBucket.updateClosest(response.getSearchId(), sender, false);
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
	public boolean hasValue(KademliaId id) {
		if (valueMap.containsKey(id)) {
			return !valueMap.get(id).isEmpty();
		}
		return false;
	}
	
	public HashTableValue getValue(KademliaId id) {
		HashTableValueBucket valueBucket = valueMap.get(id);
		if (valueBucket != null) {
			if (valueBucket.isEmpty()) return null;
			else {
				return valueBucket.getBest(id);
			}
		}
		return null;
	}
	
	public KademliaNode getClosestNodeIfNotStored(KademliaId id) {
		HashTableValueBucket valueBucket = valueMap.get(id);
		if (valueBucket != null) {
			return valueBucket.getClosestNodeIfNotStored();
		}
		return null;
	}
	
	public synchronized void addValueExpectation(KademliaId id) {
		if (valueMap.containsKey(id)) {
			valueMap.get(id).incrementCount();
		} else {
			valueMap.put(id, new HashTableValueBucket());
		}
	}
	
	public synchronized void removeValueExpectation(KademliaId id) {
		if (valueMap.containsKey(id)) {
			if (valueMap.get(id).decrementCount()) {
				valueMap.remove(id);
			}
		}
	}

}
