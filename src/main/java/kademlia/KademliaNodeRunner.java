package kademlia;

import java.util.List;
import java.util.Scanner;

import listeners.BootstrapConnectResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;
import factories.MessageContainerFactory;

public class KademliaNodeRunner implements Runnable {
	private MessageManager messageManager;
	private BootstrapConnectResponse bootstrapResponse = null;
	private KademliaNode bootstrapNode;
	private int myPort;
	
	public KademliaNodeRunner(int myPort, String bootstrapIp, int bootstrapPort) {
		this.myPort = myPort;
		bootstrapNode = KademliaNode.newBuilder().setAddress(bootstrapIp).setPort(bootstrapPort).build();
		messageManager = new MessageManager(myPort);
		registerListeners();
	}
	
	private void registerListeners() {
		messageManager.registerListener(MessageType.BOOTSTRAP_CONNECT_RESPONSE, new BootstrapConnectResponseListener(this));
	}
	
	public void run() {
		messageManager.startListening();
		BootstrapConnectRequest bootstrapConnectRequest = BootstrapConnectRequest.newBuilder()
				.setPort(myPort).build();
		messageManager.sendMessage(bootstrapNode, MessageContainerFactory.make(null, bootstrapConnectRequest));
		
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
		worker.findNode(worker.getNode().getId());
		/*List<KademliaNode> results = worker.findNode(worker.getNode().getId());
		for (KademliaNode result: results) {
			KademliaId id = KademliaUtils.XOR(result.getId(), worker.getNode().getId());
		}*/
		worker.run();
	}
	
	public void setBootstrapResponse(BootstrapConnectResponse bootstrapResponse) {
		this.bootstrapResponse = bootstrapResponse;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("How many? ");
		int howMany = scanner.nextInt();
		System.out.print("Where from? ");
		int whereFrom = scanner.nextInt();
		for (int i=0; i<howMany; i++) {
			new KademliaNodeRunner(20000+whereFrom+i, "localhost", 19803).run();
		}
	}
}
