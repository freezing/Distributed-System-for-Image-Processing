package bootstrap;

import java.util.List;

import listeners.BootstrapConnectRequestListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

public class BootstrapServer {
	private MessageManager messageManager;
	
	public BootstrapServer(int port) {
		messageManager = new MessageManager(port);
		registerListeners();
	}
	
	private void registerListeners() {
		messageManager.registerListener(MessageType.BOOTSTRAP_CONNECT_REQUEST, new BootstrapConnectRequestListener(this));
	}

	public KademliaId getNextId() {
		// TODO: Implement
		return null;
	}
	
	public List<KademliaNode> getKRandomNodes() {
		// TODO: Implement
		return null;
	}
	
	public void sendResponse(KademliaNode receiver, MessageContainer response) {
		messageManager.sendMessage(receiver, response);
	}
}
