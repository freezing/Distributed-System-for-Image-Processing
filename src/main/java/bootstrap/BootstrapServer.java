package bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.protobuf.ByteString;

import listeners.BootstrapConnectRequestListener;
import network.MessageManager;
import network.MessageType;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import sha.Sha;
import util.Constants;

public class BootstrapServer {
	private List<KademliaNode> nodes;
	private MessageManager messageManager;
	private int nextId = 0;
	
	public BootstrapServer(int port) {
		messageManager = new MessageManager(port);
		nodes = new ArrayList<KademliaNode>();
		registerListeners();
	}
	
	private void registerListeners() {
		messageManager.registerListener(MessageType.BOOTSTRAP_CONNECT_REQUEST, new BootstrapConnectRequestListener(this));
	}

	public KademliaId getNextId() {
		byte[] bytes = Sha.getInstance().digest(nextId);
		System.out.println("Sha length: " + bytes.length);
		return KademliaId.newBuilder()
				.setData(ByteString.copyFrom(bytes))
				.build();
	}
	
	public List<KademliaNode> getKRandomNodes() {
		List<KademliaNode> tmp = new ArrayList<KademliaNode>();
		Collections.copy(tmp, nodes);
		Collections.shuffle(tmp);
		return tmp.subList(0, Math.min(Constants.K, tmp.size()));
	}
	
	public void addNode(KademliaNode node) {
		nodes.add(node);
	}
	
	public void sendResponse(KademliaNode receiver, MessageContainer response) {
		messageManager.sendMessage(receiver, response);
	}
}
