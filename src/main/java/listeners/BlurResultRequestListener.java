package listeners;

import kademlia.KademliaNodeTaskManager;
import network.MessageListener;
import protos.KademliaProtos.BlurResultRequest;
import protos.KademliaProtos.BlurResultResponse;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;
import utils.StatisticsUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import factories.BlurResultResponseFactory;

public class BlurResultRequestListener implements MessageListener {
	private KademliaNodeTaskManager taskManager;

	public BlurResultRequestListener(KademliaNodeTaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void messageReceived(String ip, final KademliaNode sender, byte[] message) {
		try {
			System.out.println("BlurResultRequest listener");
			// Request is empty, it's just the type that is important
			BlurResultRequest.parseFrom(message);
			
			new Thread(new Runnable() {
				
				public void run() {
					HashTableValue rootValue = taskManager.findRootValue();
					
					ImageProto image = null;
					
					if (StatisticsUtils.isAllFinished(rootValue)) {
						// Get whole image height and width from the first task
						int firstTaskId = rootValue.getTotalTasks();
						KademliaId firstTaskKademliaId = KademliaUtils.generateId(firstTaskId);
						HashTableValue firstTaskValue = taskManager.findValue(firstTaskKademliaId);
						
						// All is finished, get image						
						image = taskManager.assembleImage(rootValue.getTotalTasks(), rootValue.getValidTasks(),
								firstTaskValue.getUnitTask().getWholeImageHeight(),
								firstTaskValue.getUnitTask().getWholeImageWidth());
					}
					
					BlurResultResponse response = BlurResultResponseFactory.make(
							StatisticsUtils.calculatePercentage(rootValue), image);
					
					taskManager.sendMessageToNode(sender, response);
				}
			}).start();;

		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
