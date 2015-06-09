package utils;

import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageRow;

public class ImageProtoUtils {
	public static ImageProto subImage(ImageProto imageProto, int top, int left,
			int bottom, int right) {
		ImageProto.Builder builder = ImageProto.newBuilder();
		for (int y = top; y < bottom; y++) {
			ImageRow.Builder rowBuilder = ImageRow.newBuilder();
			for (int x = left; x < right; x++) {
				rowBuilder.addPixels(imageProto.getRows(y).getPixels(x));
			}
		}
		return builder.build();
	}
}
