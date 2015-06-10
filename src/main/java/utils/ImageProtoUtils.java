package utils;

import java.util.List;

import protos.KademliaProtos.BlurArea;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageRow;
import protos.KademliaProtos.Pixel;
import protos.KademliaProtos.TaskResult;

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

	public static ImageProto assembleImage(List<TaskResult> imageParts,
			int height, int width) {
		ImageProto.Builder builder = makeEmptyImageProtoBuilder(height, width);

		for (TaskResult taskResult : imageParts) {
			populateImageProtoBuilder(builder, taskResult);
		}

		return builder.build();
	}

	private static void populateImageProtoBuilder(ImageProto.Builder builder,
			TaskResult taskResult) {
		BlurArea area = taskResult.getWholeImageBlurArea();
		ImageProto bluredImage = taskResult.getBluredImage();
		for (int y = area.getTop(); y < area.getBottom(); y++) {
			for (int x = area.getLeft(); x < area.getRight(); x++) {
				builder.getRowsBuilder(y).setPixels(x,
						bluredImage.getRows(y).getPixels(x));
			}
		}
	}

	private static ImageProto.Builder makeEmptyImageProtoBuilder(int height,
			int width) {
		ImageProto.Builder builder = ImageProto.newBuilder();
		for (int y = 0; y < height; y++) {
			ImageRow.Builder row = ImageRow.newBuilder();
			for (int x = 0; x < width; x++) {
				row.addPixels(Pixel.newBuilder());
			}
			builder.addRows(row);
		}
		return builder;
	}
}
