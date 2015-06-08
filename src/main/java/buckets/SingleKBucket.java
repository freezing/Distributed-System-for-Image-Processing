package buckets;

import java.util.ArrayList;
import java.util.List;

import protos.KademliaProtos.KademliaNode;

public class SingleKBucket {
	private List<KademliaNode> nodes;
	
	public SingleKBucket() {
		nodes = new ArrayList<KademliaNode>();
	}
	
	public List<KademliaNode> getNodes() {
		return nodes;
	}
}
