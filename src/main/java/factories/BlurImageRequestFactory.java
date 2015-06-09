package factories;

import protos.KademliaProtos.BlurImageRequest;
import protos.KademliaProtos.ImageProto;

public class BlurImageRequestFactory {
	public static BlurImageRequest make(ImageProto image, int radius) {
		return BlurImageRequest.newBuilder()
			.setImageProto(image)
			.setRadius(radius)
			.build();
	}
}
