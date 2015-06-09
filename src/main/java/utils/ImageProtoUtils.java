package utils;

import java.util.List;

import protos.KademliaProtos.BlurArea;
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

	public static ImageProto subImage(ImageProto subImage, BlurArea blurArea) {
		return subImage(subImage, blurArea.getTop(), blurArea.getLeft(),
				blurArea.getBottom(), blurArea.getRight());
	}

	public static ImageProto assembleImage(List<ImageProto> imageParts) {
		// TODO Auto-generated method stub
		return null;
	}
}
