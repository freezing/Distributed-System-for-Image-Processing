package factories;

import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.StoreRequest;

import com.google.protobuf.InvalidProtocolBufferException;

public class StoreRequestFactory {
	public static StoreRequest parse(byte[] message) {
		try {
			return StoreRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static StoreRequest make(KademliaId key, HashTableValue value) {
		return StoreRequest.newBuilder()
				.setKey(key)
				.setValue(value)
				.build();
	}
}
