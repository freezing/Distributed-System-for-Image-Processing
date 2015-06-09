package listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import network.MessageListener;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import util.Constants;
import utils.KademliaUtils;
import bootstrap.BootstrapServer;
import buckets.SingleKBucket;

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
		
		bootstrap.removeNodeByAddressAndPort(receiver.getAddress(), receiver.getPort());
		
		// Get K nodes
		List<KademliaNode> others = bootstrap.getKRandomNodes();
		
		// Add receiver in the list
		bootstrap.addNode(receiver);
		
		BootstrapConnectResponse bootstrapConnectResponse = BootstrapConnectResponse.newBuilder()
				.setYou(receiver)
				.addAllOthers(others)
				.build();
		
		// Create message
		MessageContainer response = MessageContainer.newBuilder()
			.setType(MessageType.BOOTSTRAP_CONNECT_RESPONSE.getValue())
			.setData(bootstrapConnectResponse.toByteString())
			.build();
		
		
		/*List<KademliaNode> nodes = new ArrayList<KademliaNode>(bootstrap.getNodes());
		final KademliaId cmp = KademliaUtils.generateId(6534); //receiver.getId(); 
		Collections.sort(nodes, new Comparator<KademliaNode>() {

			public int compare(KademliaNode a, KademliaNode b) {
				KademliaId ax = KademliaUtils.XOR(a.getId(), cmp);
				KademliaId bx = KademliaUtils.XOR(b.getId(), cmp);
				return KademliaUtils.compare(ax, bx);
			}
			
		});
		for (int i = 0; i < 5; i++) {
			if (i < nodes.size())
			System.out.println(nodes.get(i));
		}*/
		
		bootstrap.sendResponse(receiver, response);
	}
}
