package factories;

import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.SegmentTreeNode;

public class HashTableValueFactory {

	public static HashTableValue make(ImageTask imageTask, KademliaId myId,
			KademliaId parentId) {
		SegmentTreeNode segmentTreeNode = SegmentTreeNodeFactory.make(myId,
				parentId);
		int pendingTasks = imageTask.getFake() ? 0 : 1;
		return HashTableValue.newBuilder().setSegmentTreeNode(segmentTreeNode)
				.setUnitTask(imageTask).setFinishedTasks(0)
				.setPendingTasks(pendingTasks).build();
	}

	public static HashTableValue make(KademliaId myId, KademliaId parentId,
			KademliaId leftChildId, KademliaId rightChildId, int pendingTasks) {
		SegmentTreeNode segmentTreeNode = SegmentTreeNodeFactory.make(myId,
				parentId, leftChildId, rightChildId);
		return HashTableValue.newBuilder()
			.setFinishedTasks(0)
			.setPendingTasks(pendingTasks)
			.setSegmentTreeNode(segmentTreeNode)
			.build();
	}
}
