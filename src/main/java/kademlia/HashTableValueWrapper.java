package kademlia;

import protos.KademliaProtos.HashTableValue;
import util.Constants;

public class HashTableValueWrapper {
	private HashTableValue value;
	private long lastUpdated;
	
	public HashTableValueWrapper(HashTableValue value) {
		this.value = value;
		lastUpdated = System.currentTimeMillis();
	}
	
	public HashTableValue getValue() {
		return value;
	}
	
	public boolean isFresh() {
		return ((System.currentTimeMillis()-lastUpdated)/1000) <= Constants.HASHTABLEVALUE_FRESH_DURATION;
	}
}
