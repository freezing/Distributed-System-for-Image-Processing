package kademlia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import listeners.BootstrapConnectResponseListener;
import listeners.PingResponseListener;
import network.MessageManager;
import network.MessageType;
import network.TCPMessageManager;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.DeadNodeReportRequest;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.PingRequest;
import util.Constants;
import utils.KademliaUtils;
import factories.MessageContainerFactory;

public class KademliaNodeStarter implements Runnable {
	private MessageManager messageManager;
	private TCPMessageManager tcpMessageManager;
	
	private static final Random rnd = new Random();

	private BootstrapConnectResponse bootstrapResponse = null;
	private BootstrapConnectResponseListener bootstrapListener;
	private KademliaNode bootstrapNode;
	private int myPort;
	KademliaNodeWorker worker;
	
	public KademliaNodeWorker getWorker() {
		return worker;
	}
	
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
	
	private void sendPing(KademliaNode sender, KademliaNode receiver) {
		PingRequest pingRequest = PingRequest.newBuilder().setState(rnd.nextInt()).build();
		messageManager.sendMessage(receiver, MessageContainerFactory.make(sender, pingRequest));
	}
	
	public void run() {
		messageManager.startListening();
		tcpMessageManager.startListening();
		List<KademliaNode> aliveNodes = new ArrayList<KademliaNode>();

		int attempts = 5;
		while (attempts > 0) {
			attempts--;
			BootstrapConnectRequest bootstrapConnectRequest = BootstrapConnectRequest.newBuilder()
					.setPort(myPort).build();
			messageManager.sendMessage(bootstrapNode, MessageContainerFactory.make(null, bootstrapConnectRequest));
			
			synchronized (bootstrapListener) {
				try {
					bootstrapListener.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (bootstrapResponse == null) throw new NullPointerException();
			
			List<KademliaNode> nodes = bootstrapResponse.getOthersList();

			if (nodes.size() == 0) {
				aliveNodes = nodes;
				break;
			}
			
			aliveNodes.clear();
			CountDownLatch latch = new CountDownLatch(nodes.size());
			PingResponseListener pingListener = new PingResponseListener(aliveNodes, latch);
			messageManager.registerListener(MessageType.NODE_PING_RESPONSE, pingListener);
			for (KademliaNode node: nodes) {
				sendPing(bootstrapResponse.getYou(), node);
			}
			try {
				latch.await(Constants.PING_TIMEOUT_S, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			messageManager.unregisterListener(MessageType.NODE_PING_RESPONSE, pingListener);
			System.out.println(aliveNodes.size()+" are alive");
			if (aliveNodes.size() != nodes.size()) {
				ArrayList<KademliaNode> deadNodes = new ArrayList<KademliaNode>();
				for (KademliaNode node: nodes) {
					if (!aliveNodes.contains(node)) deadNodes.add(node);
				}
				DeadNodeReportRequest deadNodeReportRequest = DeadNodeReportRequest.newBuilder().addAllDeadNodes(deadNodes).build();
				messageManager.sendMessage(bootstrapNode, MessageContainerFactory.make(null, deadNodeReportRequest));
			}
			if (aliveNodes.size() > 0) break;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		worker = new KademliaNodeWorker(bootstrapResponse.getYou(), aliveNodes, messageManager, tcpMessageManager);
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
