package algorithm;

import protos.KademliaProtos.BlurArea;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageRow;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.Pixel;
import protos.KademliaProtos.TaskResult;

public class BlurAlgorithm {

	public static TaskResult blurImageTask(ImageTask unitTask) {
		ImageProto bluredImage = blurImage(unitTask.getSubImage(),
				unitTask.getBlurArea(), unitTask.getRadius());
		return TaskResult.newBuilder()
				.setWholeImageBlurArea(unitTask.getWholeImageBlurArea())
				.setBluredImage(bluredImage).build();
	}

	private static ImageProto blurImage(ImageProto subImage, BlurArea blurArea,
			int radius) {
		ImageProto.Builder blurBuilder = ImageProto.newBuilder();
		
		for (int y = blurArea.getTop(); y < blurArea.getBottom(); y++) {
			ImageRow.Builder blurRow = ImageRow.newBuilder();
			for (int x = blurArea.getLeft(); x < blurArea.getRight(); x++) {
				int red = 0;
				int green = 0;
				int blue = 0;
				for (int ky = -radius; ky <= radius; ky++) {
					for (int kx = -radius; kx <= radius; kx++) {
						Pixel pixel = getPixel(subImage, y + ky, x + kx);
						if (pixel != null) {
							red += pixel.getRed();
							green += pixel.getGreen();
							blue += pixel.getBlue();
						}
					}
				}
				red /= Math.pow(2 * radius + 1, 2);
				green /= Math.pow(2 * radius + 1, 2);
				blue /= Math.pow(2 * radius + 1, 2);
				
				Pixel blurPixel = Pixel.newBuilder()
						.setRed(red)
						.setGreen(green)
						.setBlue(blue)
						.build();
				blurRow.addPixels(blurPixel);
			}
			blurBuilder.addRows(blurRow.build());
		}
		return blurBuilder.build();
	}

	private static Pixel getPixel(ImageProto subImage, int y, int x) {
		if (y < 0 || y >= subImage.getRowsCount() || x < 0 || x >= subImage.getRows(y).getPixelsCount()) {
			return null;
		}
		return subImage.getRows(y).getPixels(x);
	}

}