package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import protos.KademliaProtos.KademliaNode;
import protos.KademliaProtos.MessageContainer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class MessageManager {
	private Thread listenerThread;
	private HashMap<Integer, ArrayList<MessageListener>> listeners;
	private DatagramSocket serverSocket;

	public MessageManager(int port) {
		listeners = new HashMap<Integer, ArrayList<MessageListener>>(
				MessageType.values().length);

		for (MessageType msgtype : MessageType.values()) {
			listeners.put(msgtype.getValue(), new ArrayList<MessageListener>());
		}

		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e1) {
			throw new RuntimeException("Socket exception: " + e1);
		}

		listenerThread = new Thread() {
			public void run() {
				byte[] receiveData = new byte[100000];
				while (true) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						serverSocket.receive(receivePacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					byte[] receiveDataTrimmed = Arrays.copyOf(receiveData, receivePacket.getLength());

					// Create MessageContainer
					MessageContainer message = null;
					try {
						message = MessageContainer.parseFrom(receiveDataTrimmed);
					} catch (InvalidProtocolBufferException e) {
						throw new RuntimeException(e);
					}
					
					ArrayList<MessageListener> listenerList = listeners
							.get(message.getType());
					if (listenerList != null) {
						for (MessageListener listener : listenerList) {
							KademliaNode sender = message.hasSender() ? message.getSender() : null;
							
							listener.messageReceived(receivePacket.getAddress()
									.getHostAddress(), sender,
									message.getData().toByteArray());
						}
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
		DatagramPacket sendPacket = new DatagramPacket(message.toByteArray(), message.toByteArray().length, inetAddress, receiver.getPort());
		
		try {
			serverSocket.send(sendPacket);
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
}
