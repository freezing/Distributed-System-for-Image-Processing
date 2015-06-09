package utils;

import java.util.ArrayList;
import java.util.List;

import protos.KademliaProtos.BlurArea;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageTask;
import util.Constants;
import factories.BlurAreaFactory;

public class ImageTaskUtils {

	public static List<ImageTask> makeUnitTasks(ImageProto imageProto, int radius) {
		List<ImageTask> tasks = new ArrayList<ImageTask>();
		for (int y = 0; y < imageProto.getHeight(); y += Constants.UNIT_TASK_IMAGE_HEIGHT) {
			for (int x = 0; x < imageProto.getWidth(); x += Constants.UNIT_TASK_IMAGE_WIDTH) {
				int bottom = Math.min(y + Constants.UNIT_TASK_IMAGE_HEIGHT, imageProto.getHeight());
				int right = Math.min(x + Constants.UNIT_TASK_IMAGE_WIDTH, imageProto.getWidth());
				
				BlurArea blurArea = BlurAreaFactory.make(y, x, bottom, right);
				ImageTask unitTask = makeUnitTask(imageProto, blurArea, radius);
				tasks.add(unitTask);
			}
		}
		return tasks;
	}

	private static ImageTask makeUnitTask(ImageProto imageProto, BlurArea blurArea, int radius) {		
		int top = Math.max(0, blurArea.getTop() - radius);
		int left = Math.max(0, blurArea.getLeft() - radius);
		int bottom = Math.min(imageProto.getHeight(), blurArea.getBottom() + radius);
		int right = Math.min(imageProto.getWidth(), blurArea.getRight() + radius);
		
		ImageProto subImage = ImageProtoUtils.subImage(imageProto, top, left, bottom, right);
		
		return ImageTask.newBuilder()
			.setBlurArea(blurArea)
			.setRadius(radius)
			.setSubImage(subImage)
			.setFake(false)
			.build();
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
