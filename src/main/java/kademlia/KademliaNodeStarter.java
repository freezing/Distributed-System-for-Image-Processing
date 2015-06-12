package kademlia;

import listeners.BootstrapConnectResponseListener;
import network.MessageManager;
import network.MessageType;
import network.TCPMessageManager;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;
import factories.MessageContainerFactory;

public class KademliaNodeStarter implements Runnable {
	private MessageManager messageManager;
	private TCPMessageManager tcpMessageManager;
	
	private BootstrapConnectResponse bootstrapResponse = null;
	private BootstrapConnectResponseListener bootstrapListener;
	private KademliaNode bootstrapNode;
	private int myPort;
	KademliaNodeWorker worker;
	
	public KademliaNodeStarter(int myPort, String bootstrapIp, int bootstrapPort) {
		this.myPort = myPort;
		bootstrapNode = KademliaNode.newBuilder().setAddress(bootstrapIp).setPort(bootstrapPort).build();
		messageManager = new MessageManager(myPort);
		tcpMessageManager = new TCPMessageManager(myPort);
		registerListeners();
	}
	
	private void registerListeners() {
		bootstrapListener = new BootstrapConnectResponseListener(this);
		messageManager.registerListener(MessageType.BOOTSTRAP_CONNECT_RESPONSE, bootstrapListener);
	}
	
	public void run() {
		messageManager.startListening();
		BootstrapConnectRequest bootstrapConnectRequest = BootstrapConnectRequest.newBuilder()
				.setPort(myPort).build();
		messageManager.sendMessage(bootstrapNode, MessageContainerFactory.make(null, bootstrapConnectRequest));
		
		tcpMessageManager.startListening();
		
		synchronized (bootstrapListener) {
			try {
				bootstrapListener.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (bootstrapResponse == null) throw new NullPointerException();
		
		worker = new KademliaNodeWorker(bootstrapResponse, messageManager, tcpMessageManager);
		worker.findNode(worker.getNode().getId());
		worker.findNode(KademliaUtils.randomId());
		worker.findNode(KademliaUtils.randomId());
		worker.findNode(KademliaUtils.randomId());

		worker.run();
	}
	
	public void setBootstrapResponse(BootstrapConnectResponse bootstrapResponse) {
		this.bootstrapResponse = bootstrapResponse;
	}
}
