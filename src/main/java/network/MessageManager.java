package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageManager {
	private Thread listenerThread;
	private HashMap<Byte, ArrayList<MessageListener>> listeners;
	private DatagramSocket serverSocket;
	
	public MessageManager(int port) throws SocketException {
		listeners = new HashMap<Byte, ArrayList<MessageListener>>(MessageType.values().length);
		
		for (MessageType msgtype : MessageType.values()) {
			listeners.put(msgtype.getValue(), new ArrayList<MessageListener>());
		}

		serverSocket = new DatagramSocket(port);
		
		listenerThread = new Thread() {
			public void run() {
				byte[] receiveData = new byte[1024];
	            while (true) {
	            	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	            	
	            	try {
						serverSocket.receive(receivePacket);
					} catch (IOException e) {
						e.printStackTrace();
					}

	            	ArrayList<MessageListener> listenerList = listeners.get(receiveData[0]);
	            	if (listenerList != null) {
	            		for (MessageListener listener: listenerList) {
	            			listener.messageReceived(receivePacket.getAddress(), receivePacket.getPort(), receiveData);
	            		}
	            	}
	            }
			}
		};
	}
	
	public void sendMessage(InetAddress destIP, int destPort, MessageType type, byte[] message) throws IOException {
		byte[] data = new byte[message.length+1];
		data[0] = type.getValue();
		System.arraycopy(message, 0, data, 1, message.length);
		
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, destIP, destPort);
        serverSocket.send(sendPacket);
	}
	
	public void registerListener(MessageType type, MessageListener listener) {
		ArrayList<MessageListener> listenerList = listeners.get(type.getValue());
		if (listenerList != null) {
			listenerList.add(listener);
		} else {
			// throw
		}
	}
	
	public void startListening() {
		listenerThread.start();
	}
}
