package listeners;

import kademlia.KademliaNodeStarter;
import network.MessageListener;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaNode;

import com.google.protobuf.InvalidProtocolBufferException;

public class BootstrapConnectResponseListener implements MessageListener {
	private KademliaNodeStarter runner;
	
	public BootstrapConnectResponseListener(KademliaNodeStarter runner) {
		this.runner = runner;
	}
	
	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		if (sender != null) {
			throw new IllegalStateException("This request should always have sender as null, but received: " + sender);
		}
		
		BootstrapConnectResponse response = parseResponse(message);
		runner.setBootstrapResponse(response);
		synchronized (this) {
			this.notify();
		}
	}

	private BootstrapConnectResponse parseResponse(byte[] message) {
		try {
			return BootstrapConnectResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
