package kademlia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import listeners.BlurImageRequestListener;
import listeners.BlurResultRequestListener;
import listeners.FindAnythingResponseListener;
import listeners.FindNodeRequestListener;
import listeners.FindNodeResponseListener;
import listeners.FindValueRequestListener;
import listeners.FindValueResponseListener;
import listeners.FindValueResponseListener.NonConsistentValueException;
import listeners.StoreRequestListener;
import listeners.StoreResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.FindValueRequest;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.StoreRequest;
import protos.KademliaProtos.TaskResult;
import util.Constants;
import utils.HashTableValueUtils;
import utils.KademliaUtils;
import utils.StatisticsUtils;
import algorithm.BlurAlgorithm;
import buckets.KBuckets;
import factories.FindNodeRequestFactory;
import factories.FindValueRequestFactory;
import factories.MessageContainerFactory;
import factories.StoreRequestFactory;

public class KademliaNodeWorker {
	private MessageManager messageManager;
	private KademliaNode node;
	
	private KBuckets kbuckets;
	private ConcurrentHashMap<KademliaId, HashTableValueWrapper> localHashMap;
	private KademliaNodeTaskManager taskManager;
	
	// Listeners
	private FindNodeResponseListener findNodeResponseListener;
	private FindValueResponseListener findValueResponseListener;
	private StoreRequestListener storeRequestListener;
	
	public KademliaNodeWorker(BootstrapConnectResponse bootstrapResponse, MessageManager messageManager) {
		this.node = bootstrapResponse.getYou();
		this.kbuckets = new KBuckets(node.getId(), bootstrapResponse.getOthersList());
		this.kbuckets.add(node);
		this.messageManager = messageManager;
		this.localHashMap = new ConcurrentHashMap<KademliaId, HashTableValueWrapper>();
		this.taskManager = new KademliaNodeTaskManager(this);
		registerListeners();
	}

	private void registerListeners() {
		findNodeResponseListener = new FindNodeResponseListener(this);
		findValueResponseListener = new FindValueResponseListener(this);
		storeRequestListener = new StoreRequestListener(this);
		
		messageManager.registerListener(MessageType.NODE_FIND_NODE_REQUEST, new FindNodeRequestListener(this));
		messageManager.registerListener(MessageType.NODE_FIND_NODE_RESPONSE, findNodeResponseListener);
		messageManager.registerListener(MessageType.NODE_FIND_VALUE_RESPONSE, findValueResponseListener);
		messageManager.registerListener(MessageType.NODE_FIND_VALUE_REQUEST, new FindValueRequestListener(this));		
		messageManager.registerListener(MessageType.NODE_STORE_REQUEST, storeRequestListener);
		messageManager.registerListener(MessageType.NODE_STORE_RESPONSE, new StoreResponseListener(this));
		
		messageManager.registerListener(MessageType.BLUR_IMAGE_REQUEST, new BlurImageRequestListener(taskManager));
		messageManager.registerListener(MessageType.BLUR_RESULT_REQUEST, new BlurResultRequestListener(taskManager));
	}

	public void run() {
		//System.out.println(KademliaUtils.idToString(node.getId()));
		new Thread(new KademliaRepublisher(this)).start();
		taskManager.run();
	}

	public void testStore(int id, String val) {
		KademliaId key = KademliaUtils.generateId(id);
		HashTableValue value = HashTableValue.newBuilder().setTmp(val).build();
		store(key, value);
		//System.out.println("Sent value: "+value.getTmp());
	}
	
	public void testGet(int id) {
		KademliaId key = KademliaUtils.generateId(id);
		HashTableValue val = findValue(key);
		if (val == null) System.out.println("Got NULL");
		else System.out.println("Got value: ["+id+"] = "+val.getTmp());
		//System.out.println("I have "+localHashMap.size());
	}

	public KademliaNode getNode() {
		return node;
	}
	
	private void sendMessageToNodes(List<KademliaNode> nodes, MessageContainer message) {
		for (KademliaNode receiver : nodes) {
			messageManager.sendMessage(receiver, message);
		}
	}
	
	private List<KademliaNode> excludeNodesFromSet(List<KademliaNode> nodes, Set<KademliaId> excludeNodes) {
		ArrayList<KademliaNode> result = new ArrayList<KademliaNode>(nodes.size());
		for (KademliaNode node : nodes) {
			if (!excludeNodes.contains(node.getId())) {
				result.add(node);
			}
		}
		return result;
	}
	
	public synchronized List<KademliaNode> findNode(KademliaId id) {
		FindNodeRequest request = FindNodeRequestFactory.make(id);
		MessageContainer message = MessageContainerFactory.make(this.node, request);
		return findNodeOrValue(id, findNodeResponseListener, message);
	}
	
	public HashTableValue findValue(KademliaId id) {
		FindValueRequest request = FindValueRequestFactory.make(id);
		MessageContainer message = MessageContainerFactory.make(this.node, request);
		findValueResponseListener.resetValue();
		findNodeOrValue(id, findValueResponseListener, message);
		try {
			return findValueResponseListener.getValue();
		} catch (NonConsistentValueException e) {
			throw new RuntimeException(e);
		}
	}

		List<KademliaNode> prevClosest = null;
		private List<KademliaNode> findNodeOrValue(KademliaId id, FindAnythingResponseListener listener, MessageContainer message) {
		
		Set<KademliaId> visited = new HashSet<KademliaId>();

		int depth = 0; 
		while (depth < Constants.MAX_FIND_DEPTH) {
			List<KademliaNode> closest = kbuckets.getKClosest(id);
			
			CountDownLatch latch = new CountDownLatch(closest.size());
			listener.put(id, latch);
			
			if (prevClosest != null && prevClosest.equals(closest)) {
				break;
			}
			prevClosest = closest;

			sendMessageToNodes(excludeNodesFromSet(closest, visited), message);
			
			try {
				latch.await(Constants.LATCH_TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (listener.hasValue()) break;
			
			depth++;
		}
		return prevClosest;
	}

	public void addAllToKBuckets(List<KademliaNode> results) {
		for (KademliaNode result: results) {
			addToKBuckets(result);
		}
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
	//	long currentTimeMS = System.currentTimeMillis();
		List<KademliaNode> closest = findNode(key);
	//	long elapsedTimeMS = System.currentTimeMillis() - currentTimeMS;
	//	if (getNode().getPort() == 20000) System.out.println("Elapsed time for findNode: " + elapsedTimeMS);
		for (KademliaNode node : closest) {
			//System.out.println(node);
			sendStoreRequest(node, key, value);
		}
	}
	
	public synchronized void sendStoreRequest(KademliaNode receiver, KademliaId key, HashTableValue value) {
		sendStoreRequest(receiver, key, value, false);
	}
	
	public void sendStoreRequest(KademliaNode receiver, KademliaId key, HashTableValue value, boolean checkDistance) {
		if (receiver.equals(node)) {
			putToLocalHashMap(key, value);
		} else if 	((!checkDistance) ||
					(KademliaUtils.compare(	KademliaUtils.XOR(node.getId(), key),
									KademliaUtils.XOR(receiver.getId(), key)) != -1)) {
				StoreRequest request = StoreRequestFactory.make(key, value);
				MessageContainer message = MessageContainerFactory.make(getNode(), request);
				sendMessage(receiver, message);
		}
	}

	public void putToLocalHashMap(KademliaId key, HashTableValue value) {
		if (localHashMap.size() == 100) {
			System.out.println("Node "+KademliaUtils.idToString(node.getId())+" reached 100 entries!");
		}
		localHashMap.put(key, new HashTableValueWrapper(value));
	}
	
	public HashTableValue getFromLocalHashMap(KademliaId key) {
		HashTableValueWrapper value = localHashMap.get(key);
		if (value != null) return value.getValue();
		return null;
	}
	
	public Collection<Entry<KademliaId, HashTableValueWrapper>> getAllLocalHashMapItems() {
		return localHashMap.entrySet();
	}

	
}
