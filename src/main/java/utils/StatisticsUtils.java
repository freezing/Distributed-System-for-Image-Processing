package utils;

import protos.KademliaProtos.HashTableValue;
import util.Constants;

public class StatisticsUtils {

	public static boolean isAllFinished(HashTableValue value) {
		if (value == null) {
			return false;
		}
		return value.getFinishedTasks() == value.getValidTasks();
	}

	public static boolean isLeafNode(HashTableValue value) {
		return value.hasUnitTask();
	}
	
	public static boolean isAllInProgress(HashTableValue value) {
		long currentTime = System.currentTimeMillis();
		long lastTimeTaken = value.getLastTimeTaken();
		return currentTime - lastTimeTaken < Constants.JOB_TIMEOUT_TIME_MS;
	}
	
	public static boolean isNonTakenTask(HashTableValue value) {
		if (!isLeafNode(value)) {
			return false;
		}
		
		if (isAllInProgress(value)) {
			return false;
		}
		
		if (isAllFinished(value)) {
			return false;
		}
		
		return true;
	}

	public static boolean hasNonFinishedTasksNotInProgress(HashTableValue value) {		
		if (isAllFinished(value) || isAllInProgress(value)) {
			return false;
		}		
		return true;
	}

	public static float calculatePercentage(HashTableValue rootValue) {
		if (rootValue == null) {
			return 0;
		}
		
		float percentage = (float)(rootValue.getFinishedTasks()) / (float)(rootValue.getValidTasks());
		return percentage * 100.0f;
	}

}
