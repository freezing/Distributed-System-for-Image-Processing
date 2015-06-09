package kademlia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import listeners.FindNodeRequestListener;
import listeners.FindNodeResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import util.Constants;
import buckets.KBuckets;
import factories.FindNodeRequestFactory;
import factories.MessageContainerFactory;

public class KademliaNodeWorker {
	private MessageManager messageManager;
	private KademliaNode node;
	
	private KBuckets kbuckets;
	
	// Listeners
	private FindNodeResponseListener findNodeResponseListener;
	
	public KademliaNodeWorker(BootstrapConnectResponse bootstrapResponse, MessageManager messageManager) {
		this.node = bootstrapResponse.getYou();
		this.kbuckets = new KBuckets(node.getId(), bootstrapResponse.getOthersList());
		this.messageManager = messageManager;
		
		registerListeners();
	}

	private void registerListeners() {
		findNodeResponseListener = new FindNodeResponseListener(this);
		
		messageManager.registerListener(MessageType.NODE_FIND_NODE_REQUEST, new FindNodeRequestListener(this));
		messageManager.registerListener(MessageType.NODE_FIND_NODE_RESPONSE, findNodeResponseListener);
	}

	public void run() {
		
	}

	public KademliaNode getNode() {
		return node;
	}

	public void findNode(KademliaNode node) {
		List<KademliaNode> prevClosest = null;
		
		Set<KademliaId> visited = new HashSet<KademliaId>();
		while (true) {
			List<KademliaNode> closest = kbuckets.getKClosest(node);
			CountDownLatch latch = new CountDownLatch(closest.size());
			findNodeResponseListener.put(node.getId(), latch);
			
			if (prevClosest != null && prevClosest.equals(closest)) {
				break;
			}
			prevClosest = closest;
			
			for (KademliaNode receiver : closest) {
				if (!visited.contains(receiver.getId())) {
					visited.add(receiver.getId());
					FindNodeRequest request = FindNodeRequestFactory.make(node.getId());
					messageManager.sendMessage(receiver, MessageContainerFactory.make(this.node, request));
				} else {
					latch.countDown();
				}
			}
			
			try {
				latch.await(Constants.LATCH_TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addAll(List<KademliaNode> results) {
		kbuckets.addAll(results);
	}
}
