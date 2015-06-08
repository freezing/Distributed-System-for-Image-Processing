package factories;

import network.MessageType;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

public class MessageContainerFactory {
	public static MessageContainer make(KademliaNode sender, int type, byte[] message) {		
		return MessageContainer.newBuilder()
				.setType(type)
				.setSender(sender)
				.setData(ByteString.copyFrom(message))
				.build();
	}

	public static Message make(KademliaNode sender, Object obj) {
		MessageContainer.Builder builder = MessageContainer.newBuilder()
				.setSender(sender);
		
		if (obj instanceof FindNodeRequest) {
			FindNodeRequest request = (FindNodeRequest) obj;
			builder
				.setType(MessageType.NODE_FIND_NODE_REQUEST.getValue())
				.setData(request.toByteString());
		} else {
			throw new IllegalArgumentException(obj.toString());
		}
		
		return builder.build();
	}
}
