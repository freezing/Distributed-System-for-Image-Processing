package client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import listeners.BlurResultResponseListener;
import network.MessageManager;
import network.MessageType;
import network.TCPMessageManager;
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
	private TCPMessageManager tcpMessageManager;
	
	private File imageFile;
	private int radius;
	private KademliaNode receiver;
	private KademliaNode clientNode;
	
	private BufferedImage bluredImage;
	private int nodeCount;
	
	private File outputFile;
	private PrintWriter writer;
	
	public Client(String imagePath, int radius, int port, KademliaNode receiver, int nodeCount) throws IOException {
		this.clientNode = KademliaNode.newBuilder().setAddress("localhost").setPort(port).build();
		messageManager = new MessageManager(port);
		tcpMessageManager = new TCPMessageManager(port);
		
		messageManager.startListening();
		tcpMessageManager.startListening();
		
		tcpMessageManager.registerListener(MessageType.BLUR_RESULT_RESPONSE, new BlurResultResponseListener(this));
		
		this.imageFile = new File(imagePath);
		this.radius = radius;
		this.receiver = receiver;
		this.nodeCount = nodeCount;
		

		
		outputFile = new File("/home/nikola/Desktop/results_" + nodeCount + ".txt");
		writer = new PrintWriter(new FileOutputStream(outputFile), true);
	}
	
	public void run() throws IOException, InterruptedException {
		BufferedImage image = ImageIO.read(imageFile);
		ImageProto imageProto = makeImageProto(image);
		BlurImageRequest blurImageRequest = BlurImageRequestFactory.make(imageProto, radius);
		MessageContainer message = MessageContainerFactory.make(receiver, blurImageRequest);
		
		tcpMessageManager.sendMessage(receiver, message);
		long startTime = System.currentTimeMillis();

		while (true) {
			if (bluredImage != null) {
				break;
			}
			
			Thread.sleep(2000);
			BlurResultRequest request = BlurResultRequest.newBuilder().setDummy("dummy").build();
			MessageContainer msg = MessageContainerFactory.make(clientNode, request);
			messageManager.sendMessage(receiver, msg);
		}
		
		long finishedTime = System.currentTimeMillis();
		
		long workTime = finishedTime - startTime;
		int height = image.getHeight();
		int width = image.getWidth();
		writer.append(workTime + " " + height + " " + width + "\n");
		
		writer.close();
		ImageIO.write(bluredImage, "png", new File("/home/nikola/Desktop/blured.png"));
		System.out.println("Done.");
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
		final String imagePath = "/home/nikola/Pictures/10644715_779768412073282_6335973577924657285_o.jpg";
		
		System.out.println("Starting client");
		KademliaNode node = KademliaNode.newBuilder()
				.setAddress("localhost")
				.setPort(20000)
				.build();
		Client client = new Client(imagePath, 5, 22000, node, 1);
		client.run();
	}
}
