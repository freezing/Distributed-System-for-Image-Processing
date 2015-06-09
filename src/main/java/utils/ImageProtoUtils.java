package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import protos.KademliaProtos.BlurArea;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageRow;
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

	public static ImageProto assembleImage(List<TaskResult> imageParts) {
		Collections.sort(imageParts, new Comparator<TaskResult>() {
			public int compare(TaskResult a, TaskResult b) {
				if (a.getWholeImageBlurArea().getTop() < b
						.getWholeImageBlurArea().getTop()) {
					return -1;
				} else if (a.getWholeImageBlurArea().getLeft() > b
						.getWholeImageBlurArea().getLeft()) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		ImageProto.Builder builder = ImageProto.newBuilder();

		ImageRow.Builder row = null;
		for (int i = 0; i < imageParts.size(); i++) {
			if (i == 0
					|| imageParts.get(i - 1).getWholeImageBlurArea().getTop() < imageParts
							.get(i).getWholeImageBlurArea().getTop()) {
				if (row != null) {
					builder.addRows(row);
				}
				row = ImageRow.newBuilder();
			}
			// Populate row
			
		}

		// TODO Auto-generated method stub
		return builder.build();
	}
}
