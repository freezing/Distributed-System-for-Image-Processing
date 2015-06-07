package network;

import java.net.InetAddress;

public interface MessageListener {
	public void messageReceived(InetAddress sourceIP, int sourcePort, byte[] message);
}

