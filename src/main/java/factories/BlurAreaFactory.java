package factories;

import protos.KademliaProtos.BlurArea;

public class BlurAreaFactory {
	public static BlurArea make(int top, int left, int bottom, int right) {		
		return BlurArea.newBuilder()
				.setTop(top)
				.setLeft(left)
				.setBottom(bottom)
				.setRight(right)
				.build();
	}
}
