package factories;

import protos.KademliaProtos.FindValueRequest;
import protos.KademliaProtos.KademliaId;

public class FindValueRequestFactory {
	public static FindValueRequest make(KademliaId searchId) {
		return FindValueRequest.newBuilder()
				.setSearchId(searchId)
				.build();
	}
}
