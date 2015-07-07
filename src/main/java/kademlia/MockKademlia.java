package kademlia;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaId;
import utils.KademliaUtils;

public class MockKademlia {	
	private Map<KademliaId, HashTableValue> hashMap = new ConcurrentHashMap<KademliaId, HashTableValue>();
	
	private MockKademlia() {
		
	}
	
	private static MockKademlia instance = null;
	
	public synchronized static MockKademlia getInstance() {
		if (instance == null) {
			instance = new MockKademlia();
		}
		return instance;
	}
	
	public synchronized void store(KademliaId key, HashTableValue value) {
		System.out.println("Storing: " + KademliaUtils.crackSha(key));
		hashMap.put(key, value);		
	}
	
	public synchronized HashTableValue findValue(KademliaId key) {
		return hashMap.get(key);
	}
}
