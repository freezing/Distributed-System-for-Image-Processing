package factories;

import com.google.protobuf.InvalidProtocolBufferException;

import protos.KademliaProtos.StoreResponse;

public class StoreResponseFactory {
	public static StoreResponse make(String status) {
		return StoreResponse.newBuilder()
				.setStatus(status)
				.build();
	}
	
	public static StoreResponse parse(byte[] message) {
		try {
			return StoreResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
