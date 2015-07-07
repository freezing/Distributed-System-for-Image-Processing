package kademlia;

import java.io.IOException;
import java.util.Random;

import bootstrap.BootstrapServer;
import test.Debug;

public class KademliaNodeTester {
	
	public static void testStore(KademliaNodeStarter starter) {
		//starter.run();
		
		//for (int i=5000; i<6000; i++) {
		for (int i=1; i<=150; i++) {
			starter.worker.testStore(i, (i+5000)+"");
		}
//		worker.testStore(10, "ABC");
//		worker.testStore(100, "DADSD");
//		worker.testStore(4545, "adsdasd");
//		worker.testStore(5766, "sdsdsad");
//		worker.testStore(21323, "zcxczx");
	}
	
	public static void testGet(KademliaNodeStarter starter) {
		//starter.run();
		/*for (int i = 5000; i < 6000; i++) {
			worker.testGet(i);
		}*/
		int i = 30;
		while (i > 0) {
			i--;
			Random rand = new Random();
			//starter.worker.testGet(rand.nextInt(1000)+5000);
			starter.worker.testGet(i);
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		/*worker.testGet(10);
		worker.testGet(100);
		worker.testGet(4545);
		worker.testGet(5766);*/
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		/*Scanner scanner = new Scanner(System.in);
		System.out.print("How many? ");
		int howMany = scanner.nextInt();
		System.out.print("Where from? ");
		int whereFrom = scanner.nextInt();
		for (int i=0; i<howMany; i++) {
			new KademliaNodeRunner(20000+whereFrom+i, "localhost", 19803).run();
		}*/
		//new BootstrapServer(19803).run();
		KademliaNodeStarter first = new KademliaNodeStarter(20000, "localhost", 19803);
		first.run();
		KademliaNodeStarter t = first;
		for (int i=1; i<100; i++) {
			KademliaNodeStarter s = new KademliaNodeStarter(20000+i, "localhost", 19803);
			s.run();
			if (Math.random() > 0.1) t = s;
		}
		testStore(t);
		Thread.sleep(1000);
		testGet(first);
		
		
		/*long start = System.currentTimeMillis();
		int i = 0;
		for (; i<50; i++) {
			new KademliaNodeStarter(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(2000);
		testStore(new KademliaNodeStarter(20000+i++, "localhost", 19803));
		System.out.println(System.currentTimeMillis()-start);
		
		for (; i<500; i++) {
			new KademliaNodeStarter(20000+i, "localhost", 19803).run();
		}
		Debug.println(1000, "done");
		Thread.sleep(20000);
		
		Thread.sleep(2000);
		final int ii = i+5;
		new Thread(new Runnable() {
			public void run() {
				testGet(new KademliaNodeStarter(30000, "localhost", 19803));
			}
		}).run();
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				testGet(new KademliaNodeStarter(30001, "localhost", 19803));
			}
		}).run();
		new Thread(new Runnable() {
			public void run() {
				testGet(new KademliaNodeStarter(30002, "localhost", 19803));
			}
		}).run();
		testGet(new KademliaNodeStarter(20000+i++, "localhost", 19803));
		/*for (; i<70; i++) {
			new KademliaNodeRunner(20000+i, "localhost", 19803).run();
		}
		Thread.sleep(5000);*/
	//	Thread.sleep(5000);
	/*	KademliaNodeStarter runner = new KademliaNodeStarter(20000+i++, "localhost", 19803);
		Thread.sleep(5000);
		runner.testGet();*/
	}

}
