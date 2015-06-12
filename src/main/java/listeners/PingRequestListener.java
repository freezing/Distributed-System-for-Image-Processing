package listeners;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.PingResponse;
import factories.MessageContainerFactory;

public class PingRequestListener implements MessageListener {

	private KademliaNodeWorker worker;

	public PingRequestListener(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		worker.addAliveToKBuckets(sender);
		PingResponse pingResponse = PingResponse.newBuilder().setState(444).build();
		MessageContainer msg = MessageContainerFactory.make(worker.getNode(), pingResponse);
		worker.sendMessage(sender, msg);
	}
}
