package test;

import java.io.IOException;

import kademlia.KademliaNodeStarter;

public class NikolaTest {
	public static void main(String[] args) throws InterruptedException, IOException {
		final int N = 20;
		
		KademliaNodeStarter nodes[] = new KademliaNodeStarter[N];
		
		long a = System.currentTimeMillis();
		int i = 0;
		for (; i<N; i++) {
			nodes[i] = new KademliaNodeStarter(20000+i, "localhost", 19803);
			nodes[i].run();
		}
		System.out.println("Nodes started");
	}
}
