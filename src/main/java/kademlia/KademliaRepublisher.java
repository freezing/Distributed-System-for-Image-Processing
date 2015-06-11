package kademlia;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;
import util.Constants;

public class KademliaRepublisher implements Runnable {
	
	private static final int maxStartWait = 60000;
	private static final Random rnd = new Random();
	
	private KademliaNodeWorker worker;
	
	public KademliaRepublisher(KademliaNodeWorker worker) {
		this.worker = worker;
	}
	
	private void sendAllValuesToNode(KademliaNode target, long newerThan) {
		for (Entry<KademliaId, HashTableValueWrapper> tableEntry: worker.getAllLocalHashMapItems()) {
			//if (!tableEntry.getValue().isFresh())
			if (tableEntry.getValue().getLastUpdated() > newerThan)
				worker.sendStoreRequest(target, tableEntry.getKey(), tableEntry.getValue().getValue(), true);
		}
	}
	
	private void republishAllValues() {
		System.out.println("republish");
		ArrayList<Entry<KademliaId, HashTableValueWrapper>> rottenValues = new ArrayList<Entry<KademliaId, HashTableValueWrapper>>(); 
		for (Entry<KademliaId, HashTableValueWrapper> tableEntry: worker.getAllLocalHashMapItems()) {
			if (!tableEntry.getValue().isFresh()) {
				rottenValues.add(tableEntry);
			}
		}
		
		for (Entry<KademliaId, HashTableValueWrapper> tableEntry: rottenValues) {
			worker.store(tableEntry.getKey(), tableEntry.getValue().getValue(), true);
		}
	}
	
	private void refreshBuckets() {
		/*for (KademliaId idToRefresh: kbuckets.getRottenBucketMembers()) {
			worker.findNode(idToRefresh);
		}*/
	}

	public void run() {
		/*try {
			Thread.sleep(rnd.nextInt(maxStartWait));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
		while (true) {
			try {
				Thread.sleep(Constants.REPUBLISH_PERIOD_S*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			republishAllValues();
			break;
		}
	}

}
