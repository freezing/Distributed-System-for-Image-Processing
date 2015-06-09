package listeners;

import java.util.List;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.BlurImageRequest;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.KademliaNode;
import utils.ImageTaskUtils;
import factories.BlurImageRequestFactory;

public class BlurImageRequestListener implements MessageListener {

	private KademliaNodeWorker worker;

	public BlurImageRequestListener(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		BlurImageRequest request = BlurImageRequestFactory.parse(message);
		
		List<ImageTask> unitTasks = ImageTaskUtils.makeUnitTasks(request.getImageProto(), request.getRadius());
	}
}
