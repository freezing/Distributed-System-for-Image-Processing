package kademlia;

import util.Constants;

public class KademliaId implements Comparable<KademliaId> {
	private byte[] data;

	public KademliaId() {
		data = new byte[Constants.ID_SIZE_IN_BYTES];
	}
	
	public KademliaId(byte[] data) {
		super();
		System.arraycopy(data, 0, this.data, 0, this.data.length);
	}
	
	public KademliaId XOR(KademliaId second) {
		KademliaId result = new KademliaId();
		for (int i=0; i<data.length; i++) {
			result.data[i] = (byte) (data[i] ^ second.data[i]);
		}
		return result;
	}

	public int compareTo(KademliaId o) {
		for (int i=0; i<data.length; i++) {
			if (data[i] > o.data[i]) return 1;
			else if (data[i] < o.data[i]) return -1;
		}
		return 0;
	}
}
