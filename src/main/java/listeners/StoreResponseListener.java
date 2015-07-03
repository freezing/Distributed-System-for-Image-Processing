package listeners;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.KademliaNode;

public class StoreResponseListener implements MessageListener {
	
	private KademliaNodeWorker worker;

	public StoreResponseListener(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		worker.addToKBuckets(sender);
	}

}
