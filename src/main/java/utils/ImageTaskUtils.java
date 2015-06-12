package utils;

import java.util.ArrayList;
import java.util.List;

import protos.KademliaProtos.BlurArea;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageTask;
import test.Debug;
import util.Constants;
import factories.BlurAreaFactory;

public class ImageTaskUtils {

	public static List<ImageTask> makeUnitTasks(ImageProto imageProto,
			int radius) {
		System.out.println("Image proto height = " + imageProto.getHeight());
		List<ImageTask> tasks = new ArrayList<ImageTask>();
		for (int y = 0; y < imageProto.getHeight(); y += Constants.UNIT_TASK_IMAGE_HEIGHT) {
			for (int x = 0; x < imageProto.getWidth(); x += Constants.UNIT_TASK_IMAGE_WIDTH) {
				int bottom = Math.min(y + Constants.UNIT_TASK_IMAGE_HEIGHT,
						imageProto.getHeight());
				int right = Math.min(x + Constants.UNIT_TASK_IMAGE_WIDTH,
						imageProto.getWidth());

				BlurArea wholeImageBlurArea = BlurAreaFactory.make(y, x,
						bottom, right);
				ImageTask unitTask = makeUnitTask(imageProto,
						wholeImageBlurArea, radius, imageProto.getHeight(),
						imageProto.getWidth());
				System.out.println("Unit task whole image height = " + unitTask.getWholeImageHeight());
				tasks.add(unitTask);
			}
		}
		return tasks;
	}

	private static ImageTask makeUnitTask(ImageProto imageProto,
			BlurArea wholeImageBlurArea, int radius, int height, int width) {
		System.out.println("Make unit task height = " + height);
		int top = Math.max(0, wholeImageBlurArea.getTop() - radius);
		int left = Math.max(0, wholeImageBlurArea.getLeft() - radius);
		int bottom = Math.min(imageProto.getHeight(),
				wholeImageBlurArea.getBottom() + radius);
		int right = Math.min(imageProto.getWidth(),
				wholeImageBlurArea.getRight() + radius);

		ImageProto subImage = ImageProtoUtils.subImage(imageProto, top, left,
				bottom, right);

		BlurArea blurArea = BlurAreaFactory.make(wholeImageBlurArea.getTop()
				- top, wholeImageBlurArea.getLeft() - left,
				wholeImageBlurArea.getBottom() - top,
				wholeImageBlurArea.getRight() - left);

		return ImageTask.newBuilder().setWholeImageBlurArea(wholeImageBlurArea)
				.setBlurArea(blurArea).setRadius(radius).setSubImage(subImage)
				.setFake(false).setWholeImageHeight(height)
				.setWholeImageWidth(width).build();
	}

	public static void extendSizeToPowerOfTwo(List<ImageTask> unitTasks) {
		while (!isPowerOfTwo(unitTasks.size())) {
			unitTasks.add(makeFakeUnitTask());
		}
	}

	private static boolean isPowerOfTwo(int n) {
		return (n & -n) == n;
	}

	private static ImageTask makeFakeUnitTask() {
		return ImageTask.newBuilder().setFake(true).build();
	}
}
