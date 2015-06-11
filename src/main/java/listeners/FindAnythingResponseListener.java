package listeners;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import network.MessageListener;
import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;

public abstract class FindAnythingResponseListener implements MessageListener {
	
	private ConcurrentHashMap<KademliaId, CountDownLatch> latchMap;
	
	public FindAnythingResponseListener() {
		this.latchMap = new ConcurrentHashMap<KademliaId, CountDownLatch>();
	}

	public abstract void messageReceived(String ip, KademliaNode sender, byte[] message);
	
	public abstract boolean hasValue(KademliaId id);

	public void put(KademliaId key, CountDownLatch value) {
		latchMap.put(key, value);
	}
	
	public void latchCountDown(KademliaId key) {
		CountDownLatch latch = latchMap.get(key);
		latch.countDown();
	}
}
