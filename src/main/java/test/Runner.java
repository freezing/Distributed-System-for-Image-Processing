package test;

import java.io.IOException;

import protos.KademliaProtos.KademliaNode;
import client.Client;
import bootstrap.BootstrapServer;
import kademlia.KademliaNodeStarter;

public class Runner {

	private static final int SERVER_PORT = 19803;
	private static final int N = 3;

	public static void main(String[] args) throws IOException, InterruptedException {
		new Thread(new Runnable(){
			public void run() {
				try {
				final BootstrapServer bs = new BootstrapServer(SERVER_PORT);
				bs.run();
			} catch (RuntimeException e) {
				System.out.println("Bootstrap already running!");
			}
		}
		}).start();
		
		Thread.sleep(2000);
		
		new Thread(new Runnable() {
			public void run() {
			
			final KademliaNodeStarter nodes[] = new KademliaNodeStarter[N];
			int i = 0;
			for (; i<N; i++) {
				nodes[i] = new KademliaNodeStarter(21000+i, "localhost", SERVER_PORT);
				nodes[i].run();
			}
		}}).start();
		
		Thread.sleep(2000);
		
		new Thread(new Runnable() {
			public void run() {
			
			KademliaNode node = KademliaNode.newBuilder()
					.setAddress("localhost")
					.setPort(21000)
					.build();
			
				Client client;
				try {
					client = new Client("/home/nikola/Desktop/kids/80x40.png", 5, 23000, node, N);
					client.run();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}).start();
	}
}
