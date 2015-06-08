package utils;

import java.util.BitSet;

import com.google.protobuf.ByteString;

import protos.KademliaProtos.KademliaId;

public class KademliaUtils {
	public static int xorDistance(KademliaId a, KademliaId b) {
		KademliaId id = XOR(a, b);
		return cardinality(id);
	}
	
	public static int cardinality(KademliaId id) {
		byte[] bytes = id.getData().toByteArray();
		BitSet bitSet = BitSet.valueOf(bytes);
		return bitSet.cardinality();
	}

	public static KademliaId XOR(KademliaId a, KademliaId b) {
		byte[] first = a.getData().toByteArray();
		byte[] second = b.getData().toByteArray();
		
		if (first.length != second.length) {
			throw new IllegalStateException("Different lengths: " + first.length + " and " + second.length);
		}
		
		byte[] xor = new byte[first.length];
		for (int i = 0; i < first.length; i++) {
			xor[i] = (byte) (first[i] ^ second[i]);
		}
		
		return KademliaId.newBuilder().setData(ByteString.copyFrom(xor)).build();
	}
}
