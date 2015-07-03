package listeners;

import java.util.List;

import network.MessageListener;
import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.BootstrapConnectResponse;
import protos.KademliaProtos.DeadNodeReportRequest;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import bootstrap.BootstrapServer;

import com.google.protobuf.InvalidProtocolBufferException;

public class BootstrapDeadNodesListener implements MessageListener {
	
	private BootstrapServer bootstrap;
	
	public BootstrapDeadNodesListener(BootstrapServer boostrap) {
		this.bootstrap = boostrap;
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		DeadNodeReportRequest request = null;
		try {
			request = DeadNodeReportRequest.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		
		for (KademliaNode deadNode: request.getDeadNodesList()) {
			bootstrap.removeNodeByAddressAndPort(deadNode.getAddress(), deadNode.getPort());
		}
	}
}
