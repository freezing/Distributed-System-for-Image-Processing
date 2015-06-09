package factories;

import network.MessageType;
import protos.KademliaProtos.BootstrapConnectRequest;
import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.FindNodeResponse;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

import com.google.protobuf.ByteString;

public class MessageContainerFactory {
	public static MessageContainer make(KademliaNode sender, int type, byte[] message) {		
		return MessageContainer.newBuilder()
				.setType(type)
				.setSender(sender)
				.setData(ByteString.copyFrom(message))
				.build();
	}

	public static MessageContainer make(KademliaNode sender, Object obj) {
		MessageContainer.Builder builder = MessageContainer.newBuilder();
		
		if (sender != null) {
			builder.setSender(sender);
		}
		
		if (obj instanceof FindNodeRequest) {
			FindNodeRequest request = (FindNodeRequest) obj;
			builder
				.setType(MessageType.NODE_FIND_NODE_REQUEST.getValue())
				.setData(request.toByteString());
		} else if (obj instanceof FindNodeResponse) {
			FindNodeResponse response = (FindNodeResponse) obj;
			builder.setType(MessageType.NODE_FIND_NODE_RESPONSE.getValue())
				.setData(response.toByteString());
		}
		else if (obj instanceof BootstrapConnectRequest) {
			BootstrapConnectRequest request = (BootstrapConnectRequest) obj;
			builder.setType(MessageType.BOOTSTRAP_CONNECT_REQUEST.getValue())
				.setData(request.toByteString());
		} else {
			throw new IllegalArgumentException(obj.toString());
		}
		
		return builder.build();
	}
}