package factories;

import com.google.protobuf.InvalidProtocolBufferException;

import protos.KademliaProtos.BlurImageResponse;

public class BlurImageResponseFactory {
	public static BlurImageResponse make(byte[] message) {
		try {
			return BlurImageResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
