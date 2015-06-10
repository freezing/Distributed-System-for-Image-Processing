package kademlia;

import listeners.BootstrapConnectResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;
import factories.MessageContainerFactory;

public class KademliaNodeRunner implements Runnable {
	private MessageManager messageManager;
	private BootstrapConnectResponse bootstrapResponse = null;
	private KademliaNode bootstrapNode;
	private int myPort;
	private KademliaNodeWorker worker;
	
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
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.yield();
		}
		
		worker = new KademliaNodeWorker(bootstrapResponse, messageManager);
		worker.findNode(worker.getNode().getId());
		worker.findNode(KademliaUtils.randomId());
		worker.findNode(KademliaUtils.randomId());
		worker.findNode(KademliaUtils.randomId());
		/*List<KademliaNode> results = worker.findNode(worker.getNode().getId());
		for (KademliaNode result: results) {
			KademliaId id = KademliaUtils.XOR(result.getId(), worker.getNode().getId());
		}*/
		worker.run();
	}
	
	public void testStore() {
		run();
		
		for (int i=5000; i<6000; i++) {
		worker.testStore(i, (i-5000)+"");
		}
	}
	
	public void testGet() {
		run();
		worker.testGet(5001);
		worker.testGet(5500);
		worker.testGet(5100);
		worker.testGet(5200);
		worker.testGet(5020);
	}
	
	public void setBootstrapResponse(BootstrapConnectResponse bootstrapResponse) {
		this.bootstrapResponse = bootstrapResponse;
	}

	public static void main(String[] args) throws InterruptedException {
		/*Scanner scanner = new Scanner(System.in);
		System.out.print("How many? ");
		int howMany = scanner.nextInt();
		System.out.print("Where from? ");
		int whereFrom = scanner.nextInt();
		for (int i=0; i<howMany; i++) {
			new KademliaNodeRunner(20000+whereFrom+i, "localhost", 19803).run();
		}*/
		int i = 0;
		for (; i<500; i++) {
			new KademliaNodeRunner(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(2000);
		new KademliaNodeRunner(20000+i++, "localhost", 19803).testStore();
		Thread.sleep(2000);
		for (; i<1000; i++) {
			new KademliaNodeRunner(20000+i, "localhost", 19803).run();
		}
		/*for (; i<70; i++) {
			new KademliaNodeRunner(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(5000);*/
		Thread.sleep(5000);
		KademliaNodeRunner runner = new KademliaNodeRunner(20000+i++, "localhost", 19803);
		Thread.sleep(5000);
		runner.testGet();
	}
}
