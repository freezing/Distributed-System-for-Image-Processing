package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class KademliaMessageManager extends MessageManager {
	
	private static InetAddress localhost;
	
	static {
		try {
			localhost = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
		}
	}

	public KademliaMessageManager(int port) throws SocketException {
		super(port);
	}
	
	public void sendMessage(int id, MessageType type, byte[] message) throws IOException {
		sendMessage(localhost, id, type, message);
	}
	
	public void registerListener(MessageType type, final KademliaMessageListener listener) {
		registerListener(type, new MessageListener() {
			public void messageReceived(InetAddress sourceIP, int sourcePort,
					byte[] message) {
				listener.messageReceived(sourcePort, message);				
			}
		});
	}

}
