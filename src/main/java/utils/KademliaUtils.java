package utils;

import java.util.BitSet;
import java.util.Random;

import com.google.protobuf.ByteString;

import protos.KademliaProtos.KademliaId;
import sha.Sha;
import util.Constants;

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
	
	public static int compare(KademliaId a, KademliaId b) {
		byte[] aBytes = a.toByteArray();
		byte[] bBytes = b.toByteArray();
		for (int i = 0; i < aBytes.length; i++) {
			if (aBytes[i] > bBytes[i]) return 1;
			else if (aBytes[i] < bBytes[i]) return -1;
		}
		return 0;
	}
	
	public static KademliaId generateId(int id) {
		byte[] bytes = Sha.getInstance().digest(id);
		return KademliaId.newBuilder()
				.setData(ByteString.copyFrom(bytes))
				.build();
	}
	
	public static KademliaId randomId() {
		Random random = new Random();
		byte[] bytes = new byte[Constants.ID_SIZE_IN_BYTES];
		random.nextBytes(bytes);
		return KademliaId.newBuilder()
				.setData(ByteString.copyFrom(bytes))
				.build();
	}
	
	public static String idToString(KademliaId id) {
		byte[] data = id.getData().toByteArray();
		String result = "[";
		for (int i = 0; i < data.length; i++) {
			if (i > 0) result += "-";
			String tmp = Integer.toHexString(data[i]);
			if (tmp.length() > 2) tmp = tmp.substring(tmp.length()-2);
			else if (tmp.length() == 1) tmp = "0"+tmp;
			result += tmp;
		}
		result += "]";
		return result;
	}
}
