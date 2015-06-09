package kademlia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import listeners.FindNodeRequestListener;
import listeners.FindNodeResponseListener;
import listeners.StoreRequestListener;
import listeners.StoreResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.StoreRequest;
import util.Constants;
import buckets.KBuckets;
import factories.FindNodeRequestFactory;
import factories.MessageContainerFactory;
import factories.StoreRequestFactory;

public class KademliaNodeWorker {
	private MessageManager messageManager;
	private KademliaNode node;
	
	private KBuckets kbuckets;
	private ConcurrentHashMap<KademliaId, HashTableValue> hashMap;
	
	// Listeners
	private FindNodeResponseListener findNodeResponseListener;
	private StoreRequestListener storeRequestListener;
	
	public KademliaNodeWorker(BootstrapConnectResponse bootstrapResponse, MessageManager messageManager) {
		this.node = bootstrapResponse.getYou();
		this.kbuckets = new KBuckets(node.getId(), bootstrapResponse.getOthersList());
		this.messageManager = messageManager;
		this.hashMap = new ConcurrentHashMap<KademliaId, HashTableValue>();
		registerListeners();
	}

	private void registerListeners() {
		findNodeResponseListener = new FindNodeResponseListener(this);
		storeRequestListener = new StoreRequestListener(this);
		
		messageManager.registerListener(MessageType.NODE_FIND_NODE_REQUEST, new FindNodeRequestListener(this));
		messageManager.registerListener(MessageType.NODE_FIND_NODE_RESPONSE, findNodeResponseListener);
		
		messageManager.registerListener(MessageType.NODE_STORE_REQUEST, storeRequestListener);
		messageManager.registerListener(MessageType.NODE_STORE_RESPONSE, new StoreResponseListener());
	}

	public void run() {
	}

	public KademliaNode getNode() {
		return node;
	}

	public List<KademliaNode> findNode(KademliaId id) {
		List<KademliaNode> prevClosest = null;
		
		Set<KademliaId> visited = new HashSet<KademliaId>();
		
		int depth = 0; 
		while (depth < Constants.MAX_FIND_DEPTH) {
			List<KademliaNode> closest = kbuckets.getKClosest(id);
			CountDownLatch latch = new CountDownLatch(closest.size());
			findNodeResponseListener.put(id, latch);
			
			if (prevClosest != null && prevClosest.equals(closest)) {
				break;
			}
			prevClosest = closest;
			
			for (KademliaNode receiver : closest) {
				if (!visited.contains(receiver.getId())) {
					visited.add(receiver.getId());
					FindNodeRequest request = FindNodeRequestFactory.make(id);
					messageManager.sendMessage(receiver, MessageContainerFactory.make(this.node, request));
				} else {
					latch.countDown();
				}
			}
			
			try {
				latch.await(Constants.LATCH_TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			depth++;
		}
		System.out.println("took: "+depth);
		return prevClosest;
	}

	public void addAllToKBuckets(List<KademliaNode> results) {
		kbuckets.addAll(results);
	}
	
	public void addToKBuckets(KademliaNode node) {
		kbuckets.add(node);
	}
	
	public KBuckets getKbuckets() {
		return kbuckets;
	}
	
	public void sendMessage(KademliaNode receiver, MessageContainer message) {
		messageManager.sendMessage(receiver, message);
	}
	
	public void store(KademliaId key, HashTableValue value) {
		List<KademliaNode> closest = findNode(key);
		for (KademliaNode node : closest) {
			store(node, key, value);
		}
	}
	
	public void store(KademliaNode receiver, KademliaId key, HashTableValue value) {
		StoreRequest request = StoreRequestFactory.make(key, value);
		MessageContainer message = MessageContainerFactory.make(getNode(), request);
		sendMessage(receiver, message);
	}

	public void putKeyValue(KademliaId key, HashTableValue value) {
		hashMap.put(key, value);
	}
	
	public void findValue(KademliaId key) {
		// TODO: Implement
	}
}
