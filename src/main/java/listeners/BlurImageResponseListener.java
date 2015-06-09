package listeners;

import network.MessageListener;
import protos.KademliaProtos.BlurImageResponse;
import protos.KademliaProtos.KademliaNode;
import client.Client;
import factories.BlurImageResponseFactory;

public class BlurImageResponseListener implements MessageListener {

	private Client client;

	public BlurImageResponseListener(Client client) {
		this.client = client;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		BlurImageResponse response = BlurImageResponseFactory.make(message);
		client.setBluredImage(response.getImage());		
	}

}
