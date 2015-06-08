package listeners;

import java.util.List;

import network.MessageListener;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import bootstrap.BootstrapServer;

import com.google.protobuf.InvalidProtocolBufferException;

public class BootstrapConnectRequestListener implements MessageListener {
	
	private BootstrapServer bootstrap;
	
	public BootstrapConnectRequestListener(BootstrapServer boostrap) {
		this.bootstrap = boostrap;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		BootstrapConnectRequest request = null;
		try {
			request = BootstrapConnectRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		
		// Preserve port and create KademliaNode
		int port = request.getPort();
		KademliaNode receiver =
				KademliaNode.newBuilder()
					.setAddress(ip)
					.setId(bootstrap.getNextId())
					.setPort(port)
					.build();
		
		// Get K nodes
		List<KademliaNode> others = bootstrap.getKRandomNodes();
		
		BootstrapConnectResponse bootstrapConnectResponse = BootstrapConnectResponse.newBuilder()
				.setYou(receiver)
				.addAllOthers(others)
				.build();
		
		// Create message
		MessageContainer response = MessageContainer.newBuilder()
			.setType(MessageType.BOOTSTRAP_CONNECT_RESPONSE.getValue())
			.setData(bootstrapConnectResponse.toByteString())
			.build();
		
		bootstrap.sendResponse(receiver, response);
	}
}
