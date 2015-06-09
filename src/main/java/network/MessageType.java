package network;

public enum MessageType {
	NODE_PING_REQUEST(0),
	NODE_PING_RESPONSE(1),
	NODE_STORE_REQUEST(2),
	NODE_STORE_RESPONSE(3),
	NODE_FIND_NODE_REQUEST(4),
	NODE_FIND_NODE_RESPONSE(5),
	NODE_FIND_VALUE_REQUEST(6),
	NODE_FIND_VALUE_RESPONSE(7),
	BOOTSTRAP_CONNECT_REQUEST(8),
	BOOTSTRAP_CONNECT_RESPONSE(9),
	BLUR_IMAGE_REQUEST(10),
	BLUR_IMAGE_RESPONSE(11);

    private final int value;
    private MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
