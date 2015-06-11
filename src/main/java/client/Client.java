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
import protos.KademliaProtos.BlurResultRequest;
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
	private KademliaNode clientNode;
	
	private BufferedImage bluredImage;
	
	public Client(String imagePath, int radius, int port, KademliaNode receiver) throws IOException {
		this.clientNode = KademliaNode.newBuilder().setAddress("localhost").setPort(port).build();
		messageManager = new MessageManager(port);
		messageManager.startListening();
		
		messageManager.registerListener(MessageType.BLUR_RESULT_RESPONSE, new BlurResultResponseListener(this));
		
		this.imageFile = new File(imagePath);
		this.radius = radius;
		this.receiver = receiver;
	}
	
	public void run() throws IOException, InterruptedException {
		BufferedImage image = ImageIO.read(imageFile);
		ImageProto imageProto = makeImageProto(image);
		BlurImageRequest blurImageRequest = BlurImageRequestFactory.make(imageProto, radius);
		MessageContainer message = MessageContainerFactory.make(receiver, blurImageRequest);
		messageManager.sendMessage(receiver, message);

		while (true) {
			if (bluredImage != null) {
				break;
			}
			
			Thread.sleep(2000);
			BlurResultRequest request = BlurResultRequest.newBuilder().setDummy("dummy").build();
			MessageContainer msg = MessageContainerFactory.make(clientNode, request);
			messageManager.sendMessage(receiver, msg);
		}
		
		ImageIO.write(bluredImage, "png", new File("/home/nikola/Desktop/blured.png"));
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
		BufferedImage bufferedImage = new BufferedImage(imageProto.getWidth(), imageProto.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		for (int y = 0; y < imageProto.getHeight(); y++) {
			for (int x = 0; x < imageProto.getWidth(); x++) {
				Pixel pixel = imageProto.getRows(y).getPixels(x);
				int rgb = new Color(pixel.getRed(), pixel.getGreen(), pixel.getBlue()).getRGB();
				bufferedImage.setRGB(x, y, rgb);
			}
		}
		bluredImage = bufferedImage;
	}

	public void setPercentageDone(float percentage) {
		System.out.println("Percentage done: " + percentage);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Starting client");
		KademliaNode node = KademliaNode.newBuilder()
				.setAddress("localhost")
				.setPort(20000)
				.build();
		Client client = new Client("/home/nikola/Pictures/1.bmp", 5, 22000, node);
		client.run();
	}
}
