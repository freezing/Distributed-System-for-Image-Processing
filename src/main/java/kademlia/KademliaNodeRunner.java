package kademlia;

import listeners.BootstrapConnectResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectResponse;

public class KademliaNodeRunner implements Runnable {
	private MessageManager messageManager;
	private BootstrapConnectResponse bootstrapResponse = null;
	
	public KademliaNodeRunner(int myPort, String bootstrapIp, int bootstraPort) {
		messageManager = new MessageManager(myPort);
		registerListeners();
	}
	
	private void registerListeners() {
		messageManager.registerListener(MessageType.BOOTSTRAP_CONNECT_RESPONSE, new BootstrapConnectResponseListener(this));
	}
	
	public void run() {
		while (bootstrapResponse == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.yield();
		}
		
		KademliaNodeWorker worker = new KademliaNodeWorker(bootstrapResponse, messageManager);
		worker.findNode(worker.getNode());
		worker.run();
	}
	
	public void setBootstrapResponse(BootstrapConnectResponse bootstrapResponse) {
		this.bootstrapResponse = bootstrapResponse;
	}

	public static void main(String[] args) {
		
	}
}
