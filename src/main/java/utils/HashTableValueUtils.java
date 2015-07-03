package utils;

import protos.KademliaProtos.HashTableValue;

public class HashTableValueUtils {

	public static HashTableValue updateParent(HashTableValue parentValue,
			HashTableValue leftChildValue, HashTableValue rightChildValue) {
		HashTableValue.Builder updated = HashTableValue.newBuilder(parentValue);
		
		long lastTimeTakenUpdate = Long.MAX_VALUE;
		if (StatisticsUtils.isAllFinished(leftChildValue)) {
			lastTimeTakenUpdate = Math.min(lastTimeTakenUpdate, leftChildValue.getLastTimeTaken());
		}
		
		if (StatisticsUtils.isAllFinished(rightChildValue)) {
			lastTimeTakenUpdate = Math.min(lastTimeTakenUpdate, rightChildValue.getLastTimeTaken());
		}
		
		updated
			.setFinishedTasks(leftChildValue.getFinishedTasks() + rightChildValue.getFinishedTasks())
			.setLastTimeTaken(lastTimeTakenUpdate);
		
		return updated.build();
	}

}
