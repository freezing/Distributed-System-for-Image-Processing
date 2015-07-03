package network;

import protos.KademliaProtos.KademliaNode;


public interface MessageListener {
	public void messageReceived(String ip, KademliaNode sender, byte[] message);
}

