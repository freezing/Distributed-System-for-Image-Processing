package kademlia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.TaskResult;
import utils.HashTableValueUtils;
import utils.ImageProtoUtils;
import utils.ImageTaskUtils;
import utils.KademliaUtils;
import utils.StatisticsUtils;
import algorithm.BlurAlgorithm;
import factories.HashTableValueFactory;
import factories.MessageContainerFactory;

public class KademliaNodeTaskManager {
	
	private static final KademliaId SEGMENT_TREE_ROOT_ID = KademliaUtils.generateId(1);
	private static final Random rnd = new Random(235798231L);
	
	private KademliaNodeWorker worker;
	
	public KademliaNodeTaskManager(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	public void run() {
		while (true) {
			// Check if there are any not finished jobs
			HashTableValue rootValue = worker.findValue(SEGMENT_TREE_ROOT_ID);
			if (rootValue != null && !StatisticsUtils.isAllFinished(rootValue)) {
				// Access random task
				int nextRandomTaskId = rnd.nextInt(rootValue.getValidTasks()) + 1;
				HashTableValue taskValue = getTask(nextRandomTaskId);
				if (taskValue != null) {
					// Create new HashTableValue with updated timestamp
					HashTableValue.Builder taskValueBuilder = HashTableValue.newBuilder(taskValue)
						.setLastTimeTaken(System.currentTimeMillis());
					
					// Store it in the 
					worker.store(taskValue.getSegmentTreeNode().getMyId(), taskValueBuilder.build());
					updateSegmentTreeParent(taskValue.getSegmentTreeNode().getParentId());
					
					// Start work on the task
					TaskResult result = processTask(taskValue);
					
					// And update when finished
					HashTableValue resultValue = taskValueBuilder
						.setFinishedTasks(1)
						.setResult(result)
						.build();
					worker.store(resultValue.getSegmentTreeNode().getMyId(), resultValue);
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
			HashTableValue parentValue = worker.findValue(parentId);
			HashTableValue leftChildValue = worker.findValue(parentValue.getSegmentTreeNode().getLeftChildId());
			HashTableValue rightChildValue = worker.findValue(parentValue.getSegmentTreeNode().getLeftChildId());
			
			HashTableValue newParentValue = HashTableValueUtils.updateParent(parentValue, leftChildValue, rightChildValue);
			worker.store(newParentValue.getSegmentTreeNode().getMyId(), newParentValue);
			currentId = parentValue.getSegmentTreeNode().getParentId();
		}
	}

	private HashTableValue getTask(int nextRandomTaskId) {
		KademliaId randomId = KademliaUtils.generateId(nextRandomTaskId);
		// Try to find task that is not taken
		while (true) {
			HashTableValue potentialValue = worker.findValue(randomId);
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
				HashTableValue randomChildValue = worker.findValue(randomChild);
				if (StatisticsUtils.hasNonFinishedTasksNotInProgress(randomChildValue)) {
					current = randomChildValue;
				} else {
					HashTableValue otherChildValue = worker.findValue(otherChild);
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
			HashTableValue parent = worker.findValue(parentId);
			if (StatisticsUtils.hasNonFinishedTasksNotInProgress(parent)) {
				return parent;
			}
		}
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
			
			int totalTasks = values[2 * id].getTotalTasks()
					+ values[2 * id + 1].getTotalTasks();

			// Make HashTableValue
			HashTableValue value = HashTableValueFactory.make(myId, parentId,
					leftChildId, rightChildId, pendingTasks, totalTasks);

			// Insert value in DHT
			worker.store(value.getSegmentTreeNode().getMyId(), value);
			
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
			worker.store(value.getSegmentTreeNode().getMyId(), value);

			// And store in the values array
			values[id] = value;

			// Update next id value
			id--;
		}
		return id;
	}

	public HashTableValue findRootValue() {
		return worker.findValue(SEGMENT_TREE_ROOT_ID);
	}

	public ImageProto assembleImage(int totalParts, int validParts, int height, int width) {
		List<TaskResult> imageParts = new ArrayList<TaskResult>();
		
		// Iterate through ids of the tasks that are valid
		for (int id = totalParts; id < totalParts + validParts; id++) {
			KademliaId kademliaId = KademliaUtils.generateId(id);
			HashTableValue value = worker.findValue(kademliaId);
			if (value.hasResult()) {
				imageParts.add(value.getResult());
			} else {
				throw new IllegalStateException("This state should not be possibled!");
			}
		}
		
		return ImageProtoUtils.assembleImage(imageParts, height, width);
	}
	
	public void sendMessageToNode(KademliaNode target, Object msg) {
		MessageContainer messageContainer = MessageContainerFactory.make(
				worker.getNode(), msg);
		worker.sendMessage(target, messageContainer);
	}
}