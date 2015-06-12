package test;

import java.io.IOException;

import kademlia.KademliaNodeStarter;

public class NikolaTest {
	public static void main(String[] args) throws InterruptedException, IOException {
		final int N = 2000;
		
		final KademliaNodeStarter nodes[] = new KademliaNodeStarter[N];
		
		long a = System.currentTimeMillis();
		int i = 0;
		for (; i<N; i++) {
			nodes[i] = new KademliaNodeStarter(20000+i, "localhost", 19803);
			final int k = i;
			nodes[i].run();
			/*
			new Thread(new Runnable() {
				
				public void run() {
					try {
						Thread.sleep(Math.abs(new Random().nextLong()) % 5000 + 10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					nodes[k].run();
				}
			}).start();*/
		}
		System.out.println("Nodes started");
	}
}
