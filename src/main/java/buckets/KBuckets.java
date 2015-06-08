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

	public List<KademliaNode> getKClosest(KademliaNode node) {
		List<KademliaNode> nodes = new ArrayList<KademliaNode>();
		
		for (SingleKBucket singleBucket : buckets) {
			nodes.addAll(singleBucket.getNodes());
		}
		
		Collections.sort(nodes, new Comparator<KademliaNode>() {

			public int compare(KademliaNode a, KademliaNode b) {
				byte[] first = a.getId().toByteArray();
				byte[] second = b.getId().toByteArray();
				for (int i = 0; i < first.length; i++) {
					if (first[i] < second[i]) {
						return -1;
					} else if (first[i] > second[i]) {
						return 1;
					}
				}
				return 0;
			}
			
		});
		return nodes.subList(0, Math.max(nodes.size(), Constants.K));
	}
}
