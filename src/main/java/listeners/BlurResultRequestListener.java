package listeners;

import kademlia.KademliaNodeTaskManager;
import network.MessageListener;
import protos.KademliaProtos.BlurResultRequest;
import protos.KademliaProtos.BlurResultResponse;
import protos.KademliaProtos.HashTableValue;
import protos.KademliaProtos.ImageProto;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import utils.StatisticsUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import factories.BlurResultResponseFactory;
import factories.MessageContainerFactory;

public class BlurResultRequestListener implements MessageListener {
	private KademliaNodeTaskManager taskManager;

	public BlurResultRequestListener(KademliaNodeTaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		try {
			// Request is empty, it's just the type that is important
			BlurResultRequest.parseFrom(message);
			HashTableValue rootValue = taskManager.findRootValue();

			ImageProto image = null;

			if (StatisticsUtils.isAllFinished(rootValue)) {
				// All is finished, get image
				image = taskManager.assembleImage(rootValue.getTotalTasks(), rootValue.getValidTasks(),
						rootValue.getUnitTask().getWholeImageHeight(),
						rootValue.getUnitTask().getWholeImageWidth());
			}

			BlurResultResponse response = BlurResultResponseFactory.make(
					StatisticsUtils.calculatePercentage(rootValue), image);

			taskManager.sendMessageToNode(sender, response);

		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
