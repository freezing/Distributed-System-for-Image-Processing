package network;

public interface MessageListener {
	public void messageReceived(int source, byte[] message);
}

