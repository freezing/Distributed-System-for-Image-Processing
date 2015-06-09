package kademlia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.StoreRequest;
import protos.KademliaProtos.TaskResult;
import util.Constants;
import utils.HashTableValueUtils;
import utils.ImageTaskUtils;
import utils.KademliaUtils;
import utils.StatisticsUtils;
import algorithm.BlurAlgorithm;
import buckets.KBuckets;
import factories.FindNodeRequestFactory;
import factories.FindValueRequestFactory;
import factories.HashTableValueFactory;
import factories.MessageContainerFactory;
import factories.StoreRequestFactory;

public class KademliaNodeWorker {
	private static final KademliaId SEGMENT_TREE_ROOT_ID = KademliaUtils.generateId(1);
	private static final Random rnd = new Random(235798231L);
	
	private MessageManager messageManager;
	private KademliaNode node;
	
	private KBuckets kbuckets;
	private ConcurrentHashMap<KademliaId, HashTableValue> localHashMap;
	
	// Listeners
	private FindNodeResponseListener findNodeResponseListener;
	private FindValueResponseListener findValueResponseListener;
	private StoreRequestListener storeRequestListener;
	
	public KademliaNodeWorker(BootstrapConnectResponse bootstrapResponse, MessageManager messageManager) {
		this.node = bootstrapResponse.getYou();
		this.kbuckets = new KBuckets(node.getId(), bootstrapResponse.getOthersList());
		this.messageManager = messageManager;
		this.localHashMap = new ConcurrentHashMap<KademliaId, HashTableValue>();
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
		
		messageManager.registerListener(MessageType.BLUR_IMAGE_REQUEST, new BlurImageRequestListener(this));
		
		messageManager.registerListener(MessageType.BLUR_RESULT_REQUEST, new BlurResultRequestListener(this));
	}

	public void run() {
		while (true) {
			// Check if there are any not finished jobs
			HashTableValue rootValue = findValue(SEGMENT_TREE_ROOT_ID);
			if (rootValue != null && !StatisticsUtils.isAllFinished(rootValue)) {
				// Access random task
				int nextRandomTaskId = rnd.nextInt(rootValue.getValidTasks()) + 1;
				HashTableValue taskValue = getTask(nextRandomTaskId);
				if (taskValue != null) {
					// Create new HashTableValue with updated timestamp
					HashTableValue.Builder taskValueBuilder = HashTableValue.newBuilder(taskValue)
						.setLastTimeTaken(System.currentTimeMillis());
					
					// Store it in the 
					store(taskValue.getSegmentTreeNode().getMyId(), taskValueBuilder.build());
					updateSegmentTreeParent(taskValue.getSegmentTreeNode().getParentId());
					
					// Start work on the task
					TaskResult result = processTask(taskValue);
					
					// And update when finished
					HashTableValue resultValue = taskValueBuilder
						.setFinishedTasks(1)
						.setResult(result)
						.build();
					store(resultValue.getSegmentTreeNode().getMyId(), resultValue);
					updateSegmentTreeParent(resultValue.getSegmentTreeNode().getParentId());
				}
			}
			Thread.yield();
		}
	}
	
	private TaskResult processTask(HashTableValue taskValue) {
		return BlurAlgorithm.blurImageTask(taskValue.getUnitTask());
	}

	private void updateSegmentTreeParent(KademliaId parentId) {
		KademliaId currentId = parentId;
		
		while (currentId != null) {
			HashTableValue parentValue = findValue(parentId);
			HashTableValue leftChildValue = findValue(parentValue.getSegmentTreeNode().getLeftChildId());
			HashTableValue rightChildValue = findValue(parentValue.getSegmentTreeNode().getLeftChildId());
			
			HashTableValue newParentValue = HashTableValueUtils.updateParent(parentValue, leftChildValue, rightChildValue);
			store(newParentValue.getSegmentTreeNode().getMyId(), newParentValue);
			currentId = parentValue.getSegmentTreeNode().getParentId();
		}
	}

	private HashTableValue getTask(int nextRandomTaskId) {
		KademliaId randomId = KademliaUtils.generateId(nextRandomTaskId);
		// Try to find task that is not taken
		while (true) {
			HashTableValue potentialValue = findValue(randomId);
			if (StatisticsUtils.isNonTakenTask(potentialValue)) {
				// Check if potential value has unfinished (and non-taken) task
				return potentialValue;
			} else {
				// Find the first parent that has jobs that are not in progress
				// and not finished
				HashTableValue parentWithNonFinishedTasks = findParentWithNonFinishedTasksNotInProgress(potentialValue);
				if (parentWithNonFinishedTasks != null) {
					return findNonFinishedTaskInTree(parentWithNonFinishedTasks);
				} else {
					// All tasks are finished (or some super parallel thing happened,
					// which will make this process run again (so we're good :D)
					return null;
				}
			}
		}
	}

	private HashTableValue findNonFinishedTaskInTree(
			HashTableValue value) {
		HashTableValue current = value;
		while (true) {
			if (StatisticsUtils.isNonTakenTask(current)) {
				return current;
			} else {
				// Go towards random child that has non-finished tasks
				KademliaId randomChild = null;
				KademliaId otherChild = null;
				
				if (rnd.nextBoolean()) {
					randomChild = current.getSegmentTreeNode().getLeftChildId();
					otherChild = current.getSegmentTreeNode().getRightChildId();
				} else {
					randomChild = current.getSegmentTreeNode().getRightChildId();
					otherChild = current.getSegmentTreeNode().getLeftChildId();
				}
				
				// Check if random child has non finished tasks
				HashTableValue randomChildValue = findValue(randomChild);
				if (StatisticsUtils.hasNonFinishedTasksNotInProgress(randomChildValue)) {
					current = randomChildValue;
				} else {
					HashTableValue otherChildValue = findValue(otherChild);
					if (StatisticsUtils.hasNonFinishedTasksNotInProgress(otherChildValue)) {
						current = otherChildValue;
					} else {
						// Some parallel work has made this state possible,
						// which will cause this whole process to run again (we're good again :D)
						return null;
					}
				}
			}
		}
	}

	private HashTableValue findParentWithNonFinishedTasksNotInProgress(HashTableValue value) {
		HashTableValue current = value;
		while (true) {
			KademliaId parentId = current.getSegmentTreeNode().getParentId();
			if (parentId == null) {
				return null;
			}
			HashTableValue parent = findValue(parentId);
			if (StatisticsUtils.hasNonFinishedTasksNotInProgress(parent)) {
				return parent;
			}
		}
	}

	public void testStore() {
		KademliaId key = KademliaUtils.generateId(6534);
		HashTableValue value = HashTableValue.newBuilder().setTmp("This is a test string").build();
		store(key, value);
		System.out.println("Sent value: "+value.getTmp());
	}
	
	public void testGet() {
		KademliaId key = KademliaUtils.generateId(6534);
		HashTableValue val = findValue(key);
		if (val == null) System.out.println("NULL");
		else System.out.println("Got value: "+val.getTmp());		
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
	
	public List<KademliaNode> findNode(KademliaId id) {
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

	private List<KademliaNode> findNodeOrValue(KademliaId id, FindAnythingResponseListener listener, MessageContainer message) {
		List<KademliaNode> prevClosest = null;
		
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
			System.out.println(node);
			sendStoreRequest(node, key, value);
		}
	}
	
	public void sendStoreRequest(KademliaNode receiver, KademliaId key, HashTableValue value) {
		StoreRequest request = StoreRequestFactory.make(key, value);
		MessageContainer message = MessageContainerFactory.make(getNode(), request);
		sendMessage(receiver, message);
	}

	public void putToLocalHashMap(KademliaId key, HashTableValue value) {
		System.out.println("put "+value.getTmp());
		localHashMap.put(key, value);
	}
	
	public HashTableValue getFromLocalHashMap(KademliaId key) {
		HashTableValue val = localHashMap.get(key);
		if (val != null) {
			System.out.println("get "+val.getTmp());
		}
		return val;
	}

	public void setTasksReadyForDistribution(List<ImageTask> unitTasks) {
		// First add fake tasks so that unitTasks has size of 2^A (where A is
		// some integer)
		ImageTaskUtils.extendSizeToPowerOfTwo(unitTasks);

		// Make segment tree
		int id = 2 * unitTasks.size() - 1;

		// Temporarily store all the values (id is initially set to the size of
		// nodes in the segment tree)
		HashTableValue values[] = new HashTableValue[id];

		// First create nodes that contain tasks
		id = createTaskNodes(unitTasks, id, values);

		// Then create parent nodes
		createParentNodes(id, values);
	}

	private void createParentNodes(int id, HashTableValue[] values) {
		while (id > 0) {
			// Calculate ids
			KademliaId myId = KademliaUtils.generateId(id);
			// If parent is root then his parent is null
			KademliaId parentId = id > 1 ? KademliaUtils.generateId(id / 2)
					: null;
			// Left child is calculated using formula: 2 * id
			KademliaId leftChildId = KademliaUtils.generateId(2 * id);
			// Right child is calculated using formula: 2 * id + 1
			KademliaId rightChildId = KademliaUtils.generateId(2 * id + 1);

			// Calculate number of pending tasks using children info
			int pendingTasks = values[2 * id].getValidTasks()
					+ values[2 * id + 1].getValidTasks();

			// Make HashTableValue
			HashTableValue value = HashTableValueFactory.make(myId, parentId,
					leftChildId, rightChildId, pendingTasks);

			// Insert value in DHT
			store(value.getSegmentTreeNode().getMyId(), value);
			
			// Update next id
			id--;
		}
	}

	/**
	 * 
	 * @param unitTasks
	 * @param id
	 * @return Next id.
	 */
	private int createTaskNodes(List<ImageTask> unitTasks, int id,
			HashTableValue values[]) {
		for (int i = unitTasks.size() - 1; i >= 0; i--) {
			// Calculate parent id
			int parentId = id / 2;

			// Make HashTableValue
			HashTableValue value = HashTableValueFactory.make(unitTasks.get(i),
					KademliaUtils.generateId(id),
					KademliaUtils.generateId(parentId));

			// Insert value in DHT
			store(value.getSegmentTreeNode().getMyId(), value);

			// And store in the values array
			values[id] = value;

			// Update next id value
			id--;
		}
		return id;
	}

	public HashTableValue findRootValue() {
		return findValue(SEGMENT_TREE_ROOT_ID);
	}

	public ImageProto assembleImage() {
		// TODO Auto-generated method stub
		return null;
	}
}
