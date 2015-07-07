package buckets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import util.Constants;
import utils.KademliaUtils;

public class KBuckets {
	private KademliaId id;
	private SingleKBucket[] buckets;
	
	private Map<KademliaNode, Long> timestamp = new HashMap<KademliaNode, Long>();
	private Map<KademliaNode, Long> refresh = new HashMap<KademliaNode, Long>();
	
	public KBuckets(KademliaId id, List<KademliaNode> nodes) {
		this.id = id;
		buckets = new SingleKBucket[Constants.ID_SIZE_IN_BITS];
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = new SingleKBucket();
		}
		for (KademliaNode node : nodes) {
			add(node);
		}
	}
	
	public synchronized void refresh(KademliaNode node) {
		long currTime = System.currentTimeMillis();
		if (refresh.containsKey(node)) {
			// Check that it didn't get reply for a long time
			// Assume it won't break too early (don't check if it exists in the map)
			long lastReply = timestamp.get(node);
			long elapsedTime = currTime - lastReply;
			if (elapsedTime > 3000) {
				// Remove it
				for (SingleKBucket bucket : buckets) {
					bucket.remove(node);
				}
			} else {
				refresh.put(node, currTime);
			}
		} else {
			refresh.put(node, currTime);
		}
	}
	
	public synchronized void add(KademliaNode node) {
		long currTime = System.currentTimeMillis();
		timestamp.put(node, currTime);
		
		int idx = KademliaUtils.xorDistance(id, node.getId());
		buckets[idx].updateLastModified();
		
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

	private synchronized boolean contains(KademliaNode node, List<KademliaNode> nodes) {
		for (KademliaNode entry : nodes) {
			if (node.getId().equals(entry.getId())) {
				return true;
			}
		}
		return false;
	}

	public synchronized List<KademliaNode> getKClosest(final KademliaId id) {
		List<KademliaNode> nodes = new ArrayList<KademliaNode>();
		
		for (SingleKBucket singleBucket : buckets) {
			nodes.addAll(singleBucket.getNodes());
		}
		
		Collections.sort(nodes, KademliaUtils.makeComparator(id));
		return nodes.subList(0, Math.min(nodes.size(), Constants.K));
	}
	
	public synchronized List<KademliaId> getRottenBucketMembers() {
		ArrayList<KademliaId> result = new ArrayList<KademliaId>();
		for (SingleKBucket singleBucket : buckets) {
			if (!singleBucket.isFresh()) {
				result.add(singleBucket.getRandomNode().getId());
			}
		}
		return result;
	}
}
