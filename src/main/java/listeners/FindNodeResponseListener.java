package listeners;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.google.protobuf.InvalidProtocolBufferException;

import kademlia.KademliaNodeWorker;
import network.MessageListener;
import protos.KademliaProtos.FindNodeResponse;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;

public class FindNodeResponseListener implements MessageListener {

	private ConcurrentHashMap<KademliaId, CountDownLatch> latchMap;
	
	private KademliaNodeWorker worker;
	
	public FindNodeResponseListener(KademliaNodeWorker worker) {
		this.worker = worker;
		this.latchMap = new ConcurrentHashMap<KademliaId, CountDownLatch>();
	}

	public void messageReceived(String ip, KademliaNode sender, byte[] message) {
		FindNodeResponse response = parseResponse(message);
		CountDownLatch latch = latchMap.get(response.getSearchId());
		worker.addAllToKBuckets(response.getResultsList());
		worker.addToKBuckets(sender);
		latch.countDown();
	}

	private FindNodeResponse parseResponse(byte[] message) {
		try {
			return FindNodeResponse.parseFrom(message);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	public void put(KademliaId key, CountDownLatch value) {
		latchMap.put(key, value);
	}
}
