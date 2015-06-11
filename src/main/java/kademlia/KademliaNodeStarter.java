package kademlia;

import java.util.Random;

import listeners.BootstrapConnectResponseListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaNode;
import utils.KademliaUtils;
import factories.MessageContainerFactory;

public class KademliaNodeStarter implements Runnable {
	private MessageManager messageManager;
	private BootstrapConnectResponse bootstrapResponse = null;
	private BootstrapConnectResponseListener bootstrapListener;
	private KademliaNode bootstrapNode;
	private int myPort;
	private KademliaNodeWorker worker;
	
	public KademliaNodeStarter(int myPort, String bootstrapIp, int bootstrapPort) {
		this.myPort = myPort;
		bootstrapNode = KademliaNode.newBuilder().setAddress(bootstrapIp).setPort(bootstrapPort).build();
		messageManager = new MessageManager(myPort);
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
		
		synchronized (bootstrapListener) {
			try {
				bootstrapListener.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (bootstrapResponse == null) throw new NullPointerException();
		
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
//		worker.testStore(10, "ABC");
//		worker.testStore(100, "DADSD");
//		worker.testStore(4545, "adsdasd");
//		worker.testStore(5766, "sdsdsad");
//		worker.testStore(21323, "zcxczx");
	}
	
	public void testGet() {
		run();
		/*for (int i = 5000; i < 6000; i++) {
			worker.testGet(i);
		}*/
		while (true) {
			Random rand = new Random();
			worker.testGet(rand.nextInt(1000)+5000);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*worker.testGet(10);
		worker.testGet(100);
		worker.testGet(4545);
		worker.testGet(5766);*/
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
		long a = System.currentTimeMillis();
		int i = 0;
		for (; i<50; i++) {
			new KademliaNodeStarter(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(2000);
		new KademliaNodeStarter(20000+i++, "localhost", 19803).testStore();
		for (; i<1000; i++) {
			new KademliaNodeStarter(20000+i, "localhost", 19803).run();
		}
		System.out.println(System.currentTimeMillis()-a);
		Thread.sleep(40000);
		
		/*for (; i<70; i++) {
			new KademliaNodeRunner(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(5000);*/
		//Thread.sleep(5000);
		KademliaNodeStarter runner = new KademliaNodeStarter(20000+i++, "localhost", 19803);
		Thread.sleep(5000);
		runner.testGet();
	}
}
