package buckets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import protos.KademliaProtos.KademliaNode;
import util.Constants;

public class SingleKBucket {
	private List<KademliaNode> nodes;
	private long lastModified;
	private Random random = new Random();
	
	public SingleKBucket() {
		nodes = new ArrayList<KademliaNode>();
		updateLastModified();
	}
	
	public List<KademliaNode> getNodes() {
		return nodes;
	}
	
	public KademliaNode getRandomNode() {
		if (nodes.isEmpty()) return null;
		else {
			return nodes.get(random.nextInt(nodes.size()));
		}
	}
	
	public void updateLastModified() {
		lastModified = System.currentTimeMillis();
	}
	
	public boolean isFresh() {
		return ((System.currentTimeMillis()-lastModified)/1000) <= Constants.KBUCKET_FRESH_DURATION;
	}
}
