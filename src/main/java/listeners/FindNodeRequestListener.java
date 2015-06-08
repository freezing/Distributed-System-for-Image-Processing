package listeners;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.KademliaNode;

public class FindNodeRequestListener implements MessageListener {

	public FindNodeRequestListener(KademliaNodeWorker kademliaNodeWorker) {
		// TODO Auto-generated constructor stub
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		// TODO Auto-generated method stub

	}

}
