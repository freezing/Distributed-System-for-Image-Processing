package client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import listeners.BlurResultResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BlurImageRequest;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.ImageRow;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.Pixel;
import factories.BlurImageRequestFactory;
import factories.MessageContainerFactory;

public class Client {
	private MessageManager messageManager;
	
	private File imageFile;
	private int radius;
	private KademliaNode receiver;
	
	private BufferedImage bluredImage;
	
	public Client(String imagePath, int radius, int port, KademliaNode receiver) throws IOException {
		messageManager = new MessageManager(port);
		messageManager.registerListener(MessageType.BLUR_RESULT_RESPONSE, new BlurResultResponseListener(this));
		
		this.imageFile = new File(imagePath);
		this.radius = radius;
		this.receiver = receiver;
	}
	
	public void run() throws IOException {
		BufferedImage image = ImageIO.read(imageFile);
		ImageProto imageProto = makeImageProto(image);
		BlurImageRequest blurImageRequest = BlurImageRequestFactory.make(imageProto, radius);
		MessageContainer message = MessageContainerFactory.make(receiver, blurImageRequest);
		messageManager.sendMessage(receiver, message);
	}
	
	private ImageProto makeImageProto(BufferedImage image) {
		ImageProto.Builder builder = ImageProto.newBuilder().setWidth(image.getWidth())
				.setHeight(image.getHeight());
		
		for (int i = 0; i < image.getHeight(); i++) {
			ImageRow.Builder rowBuilder = ImageRow.newBuilder();
			for (int j = 0; j < image.getWidth(); j++) {
				Color color = new Color(image.getRGB(j, i));
				
				Pixel pixel = Pixel.newBuilder()
						.setRed(color.getRed())
						.setGreen(color.getGreen())
						.setBlue(color.getBlue())
						.build();
				rowBuilder.addPixels(pixel);
			}
			builder.addRows(rowBuilder.build());
		}
		return builder.build();
	}

	public void setBluredImage(ImageProto imageProto) {
		// TODO Auto-generated method stub
		// Set blured image from image proto
	}

	public static void main(String[] args) {
		
	}

	public void setPercentageDone(float percentage) {
		System.out.println("Percentage done: " + percentage);
	}
}
