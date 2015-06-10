package kademlia;

import java.util.ArrayList;
import java.util.Map.Entry;

import protos.KademliaProtos.KademliaId;
import protos.KademliaProtos.KademliaNode;

public class KademliaRepublisher implements Runnable {
	
	KademliaNodeWorker worker;
	
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
		ArrayList<Entry<KademliaId, HashTableValueWrapper>> rottenValues = new ArrayList<Entry<KademliaId, HashTableValueWrapper>>(); 
		for (Entry<KademliaId, HashTableValueWrapper> tableEntry: worker.getAllLocalHashMapItems()) {
			if (!tableEntry.getValue().isFresh()) {
				rottenValues.add(tableEntry);
			}
		}
		
		for (Entry<KademliaId, HashTableValueWrapper> tableEntry: rottenValues) {
			worker.store(tableEntry.getKey(), tableEntry.getValue().getValue());
		}
	}
	
	private void refreshBuckets() {
		/*for (KademliaId idToRefresh: kbuckets.getRottenBucketMembers()) {
			worker.findNode(idToRefresh);
		}*/
	}

	public void run() {
		// TODO Auto-generated method stub

	}

}
