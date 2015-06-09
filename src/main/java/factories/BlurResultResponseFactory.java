package factories;

import protos.KademliaProtos.BlurResultResponse;
import protos.KademliaProtos.ImageProto;

import com.google.protobuf.InvalidProtocolBufferException;

public class BlurResultResponseFactory {
	public static BlurResultResponse make(byte[] message) {
		try {
			return BlurResultResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static BlurResultResponse make(float percentage, ImageProto image) {
		BlurResultResponse.Builder builder = BlurResultResponse.newBuilder();
		
		if (image != null) {
			builder.setImage(image);
		}
		
		builder.setPercentage(percentage);
		
		return builder.build();
	}
}
