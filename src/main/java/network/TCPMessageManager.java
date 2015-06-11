package network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;
import utils.KademliaUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import factories.MessageContainerFactory;

public class TCPMessageManager {
	private Thread listenerThread;
	private HashMap<Integer, ArrayList<MessageListener>> listeners;
	private ServerSocket serverSocket;
	
	public TCPMessageManager(int port) {
		listeners = new HashMap<Integer, ArrayList<MessageListener>>(
				MessageType.values().length);

		for (MessageType msgtype : MessageType.values()) {
			listeners.put(msgtype.getValue(), new ArrayList<MessageListener>());
		}

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e1) {
			throw new RuntimeException("Socket exception: " + e1);
		}

		listenerThread = new Thread() {
			public void run() {
				while (true) {
					try {
						Socket receiveSocket = serverSocket.accept();
					    BufferedInputStream inFromClient = new BufferedInputStream(receiveSocket.getInputStream());
					    ByteBuffer bf = ByteBuffer.allocate(4);
					    bf.order(ByteOrder.LITTLE_ENDIAN);
					    for (int i=0; i<4; i++) {
					    	int b = inFromClient.read();
					        if (b == -1) {
					            System.out.println("greska sa TCP-om");
					        }
					        bf.put( (byte) b);
					    }
					    bf.rewind();
					    int length = bf.getInt();
					    byte[] receiveData = new byte[length];
					    inFromClient.read(receiveData);
					    InetAddress address = receiveSocket.getInetAddress();
					    receiveSocket.close();
					    
					    MessageContainer message = null;
						try {
							message = MessageContainer.parseFrom(receiveData);
						} catch (InvalidProtocolBufferException e) {
							throw new RuntimeException(e);
						}
						
						ArrayList<MessageListener> listenerList = listeners
								.get(message.getType());
						if (listenerList != null) {
							for (MessageListener listener : listenerList) {
								KademliaNode sender = message.hasSender() ? message.getSender() : null;
								listener.messageReceived(address.getHostAddress(), sender,
										message.getData().toByteArray());
							}
						}
					} catch (IOException e) {
						throw new RuntimeException("Socket receive exception: "+e);
					}
				}
			}
		};
	}
	
	public void sendMessage(KademliaNode receiver, MessageContainer message) {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(receiver.getAddress());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		try {
			Socket sendSocket = new Socket(inetAddress, receiver.getPort());
			byte[] data = new byte[message.toByteArray().length+4];
			ByteBuffer bf = ByteBuffer.wrap(data);
			bf.order(ByteOrder.LITTLE_ENDIAN);
			bf.putInt(message.toByteArray().length);
			bf.put(message.toByteArray());
			sendSocket.getOutputStream().write(data);
			sendSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void registerListener(MessageType type, MessageListener listener) {
		ArrayList<MessageListener> listenerList = listeners
				.get(type.getValue());
		if (listenerList != null) {
			listenerList.add(listener);
		} else {
			System.out.println("not registered");
		}
	}

	public void startListening() {
		listenerThread.start();
	}
	
	/*public static void main(String[] args) {
		TCPMessageManager manager = new TCPMessageManager(12345);
		TCPMessageManager manager2 = new TCPMessageManager(12346);
		KademliaNode sender = KademliaNode.newBuilder().setAddress("127.0.0.1").setPort(12345).setId(KademliaUtils.randomId()).build();
		KademliaNode receiver = KademliaNode.newBuilder().setAddress("127.0.0.1").setPort(12346).setId(KademliaUtils.randomId()).build();
		byte[] bytes = new byte[3];
		bytes[0] = 24;
		bytes[1] =23;
		bytes[2] = 2;
		MessageContainer msg = MessageContainer.newBuilder().setType(1).setData(ByteString.copyFrom(bytes)).build();
		manager2.registerListener(MessageType.NODE_PING_RESPONSE, new MessageListener() {
			public void messageReceived(String ip, KademliaNode sender,
					byte[] message) {
				System.out.println(message[2]);
			}
		});
		manager2.startListening();
		manager.sendMessage(receiver, msg);
	}*/
}
