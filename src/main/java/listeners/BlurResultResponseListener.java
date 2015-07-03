package listeners;

import network.MessageListener;
import protos.KademliaProtos.BlurResultResponse;
import protos.KademliaProtos.KademliaNode;
import client.Client;
import factories.BlurResultResponseFactory;

public class BlurResultResponseListener implements MessageListener {

	private Client client;

	public BlurResultResponseListener(Client client) {
		this.client = client;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		//System.out.println("Got response");
		BlurResultResponse response = BlurResultResponseFactory.make(message);
		
		if (response.hasImage()) {
			client.setBluredImage(response.getImage());
		}
		client.setPercentageDone(response.getPercentage());
	}

}
