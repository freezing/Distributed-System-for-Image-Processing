package factories;

import protos.KademliaProtos.FindNodeRequest;
import protos.KademliaProtos.KademliaId;

public class FindNodeRequestFactory {
	public static FindNodeRequest make(KademliaId searchId) {
		return FindNodeRequest.newBuilder()
				.setSearchId(searchId)
				.build();
	}
}
