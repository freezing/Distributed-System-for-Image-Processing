package sha;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha {
	
	private static Sha instance = null;
	
	public static Sha getInstance(){
		if(instance == null){
			instance = new Sha();
		}
		return instance;
	}
	
	private MessageDigest sha = null;
	
	private Sha(){
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] digest(int id) {
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.putInt(id);
		sha.reset();
		return sha.digest(buff.array());
	}
}