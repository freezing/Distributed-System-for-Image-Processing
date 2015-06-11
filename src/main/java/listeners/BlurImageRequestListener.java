package listeners;

import java.util.List;

import kademlia.KademliaNodeTaskManager;
import network.MessageListener;
import protos.KademliaProtos.BlurImageRequest;
import protos.KademliaProtos.ImageTask;
import protos.KademliaProtos.KademliaNode;
import utils.ImageTaskUtils;
import factories.BlurImageRequestFactory;

public class BlurImageRequestListener implements MessageListener {

	private KademliaNodeTaskManager taskManager;

	public BlurImageRequestListener(KademliaNodeTaskManager taskManager) {
		this.taskManager = taskManager;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		BlurImageRequest request = BlurImageRequestFactory.parse(message);
		final List<ImageTask> unitTasks = ImageTaskUtils.makeUnitTasks(request.getImageProto(), request.getRadius());
		
		new Thread(new Runnable() {
			public void run() {
				taskManager.setTasksReadyForDistribution(unitTasks);
			}			
		}).start();
	}
}
