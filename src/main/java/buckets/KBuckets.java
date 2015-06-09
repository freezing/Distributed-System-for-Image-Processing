package buckets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import util.Constants;
import utils.KademliaUtils;

public class KBuckets {
	private KademliaId id;
	private SingleKBucket[] buckets;
	
	public KBuckets(KademliaId id, List<KademliaNode> nodes) {
		this.id = id;
		buckets = new SingleKBucket[Constants.ID_SIZE_IN_BITS];
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = new SingleKBucket();
		}
		addAll(nodes);
	}
	
	public void addAll(List<KademliaNode> results) {
		for (KademliaNode node : results) {
			add(node);
		}
	}

	public synchronized void add(KademliaNode node) {
		int idx = KademliaUtils.xorDistance(id, node.getId());
		
		if (contains(node, buckets[idx].getNodes())) {
			buckets[idx].getNodes().remove(node);
			buckets[idx].getNodes().add(node);
		} else if (buckets[idx].getNodes().size() < Constants.K) {
			buckets[idx].getNodes().add(node);
		} else {
			buckets[idx].getNodes().remove(0);
			buckets[idx].getNodes().add(node);			
		}
	}

	private boolean contains(KademliaNode node, List<KademliaNode> nodes) {
		for (KademliaNode entry : nodes) {
			if (node.getId().equals(entry.getId())) {
				return true;
			}
		}
		return false;
	}

	public List<KademliaNode> getKClosest(final KademliaId id) {
		List<KademliaNode> nodes = new ArrayList<KademliaNode>();
		
		for (SingleKBucket singleBucket : buckets) {
			nodes.addAll(singleBucket.getNodes());
		}
		
		Collections.sort(nodes, new Comparator<KademliaNode>() {

			public int compare(KademliaNode a, KademliaNode b) {
				KademliaId ax = KademliaUtils.XOR(a.getId(), id);
				KademliaId bx = KademliaUtils.XOR(b.getId(), id);
				return KademliaUtils.compare(ax, bx);
			}
			
		});
		return nodes.subList(0, Math.min(nodes.size(), Constants.K));
	}
}
