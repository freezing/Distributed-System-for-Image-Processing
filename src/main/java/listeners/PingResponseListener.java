package listeners;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.KademliaNode;

public class PingResponseListener implements MessageListener {

	private List<KademliaNode> nodeList;
	private CountDownLatch latch;

	public PingResponseListener(List<KademliaNode> nodeList, CountDownLatch latch) {
		this.nodeList = nodeList;
		this.latch = latch;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		nodeList.add(sender);
		
		latch.countDown();
	}
}
