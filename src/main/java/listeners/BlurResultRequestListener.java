package listeners;

import kademlia.KademliaNodeWorker;
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
	private KademliaNodeWorker worker;

	public BlurResultRequestListener(KademliaNodeWorker worker) {
		this.worker = worker;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		try {
			// Request is empty, it's just the type that is important
			BlurResultRequest.parseFrom(message);
			HashTableValue rootValue = worker.findRootValue();

			ImageProto image = null;

			if (StatisticsUtils.isAllFinished(rootValue)) {
				// All is finished, get image
				image = worker.assembleImage(rootValue.getTotalTasks(), rootValue.getValidTasks());
			}

			BlurResultResponse response = BlurResultResponseFactory.make(
					StatisticsUtils.calculatePercentage(rootValue), image);

			MessageContainer messageContainer = MessageContainerFactory.make(
					worker.getNode(), response);
			worker.sendMessage(sender, messageContainer);

		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
