package factories;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.SegmentTreeNode;

public class SegmentTreeNodeFactory {
	public static SegmentTreeNode make(KademliaId id, KademliaId parentId) {
		return SegmentTreeNode.newBuilder()
				.setParentId(parentId)
				.setMyId(id)
				.build();
	}

	public static SegmentTreeNode make(KademliaId myId, KademliaId parentId,
			KademliaId leftChildId, KademliaId rightChildId) {
		SegmentTreeNode.Builder builder = SegmentTreeNode.newBuilder()
				.setMyId(myId)
				.setLeftChildId(leftChildId)
				.setRightChildId(rightChildId);
		if (parentId != null) {
			builder.setParentId(parentId);
		}
		return builder.build();
	}
}
