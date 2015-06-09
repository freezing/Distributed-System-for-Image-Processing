package listeners;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import protos.KademliaProtos.StoreRequest;
import protos.KademliaProtos.StoreResponse;
import factories.MessageContainerFactory;
import factories.StoreRequestFactory;
import factories.StoreResponseFactory;

public class StoreRequestListener implements MessageListener {

	private KademliaNodeWorker worker;

	public StoreRequestListener(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		StoreRequest request = StoreRequestFactory.parse(message);
		worker.putKeyValue(request.getKey(), request.getValue());
		StoreResponse response = StoreResponseFactory.make("success");
		MessageContainer msg = MessageContainerFactory.make(worker.getNode(), response);
		worker.sendMessage(sender, msg);
	}
}
