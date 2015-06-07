package network;

public interface KademliaMessageListener {
	public void messageReceived(int id, byte[] message);
}
