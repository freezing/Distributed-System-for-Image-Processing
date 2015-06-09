package factories;

import com.google.protobuf.InvalidProtocolBufferException;

import protos.KademliaProtos.BlurImageRequest;
import protos.KademliaProtos.ImageProto;

public class BlurImageRequestFactory {
	public static BlurImageRequest make(ImageProto image, int radius) {
		return BlurImageRequest.newBuilder()
			.setImageProto(image)
			.setRadius(radius)
			.build();
	}

	public static BlurImageRequest parse(byte[] message) {
		try {
			return BlurImageRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
