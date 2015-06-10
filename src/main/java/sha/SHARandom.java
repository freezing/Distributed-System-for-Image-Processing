package sha;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.google.protobuf.ByteString;

public class SHARandom {
	
	private static SHARandom instance = null;
	
	public static SHARandom getInstance(){
		if(instance == null){
			instance = new SHARandom();
		}
		return instance;
	}
	
	private SecureRandom prng;
	
	private MessageDigest sha = null;
	
	private SHARandom(){
		try {
			prng = SecureRandom.getInstance("SHA1PRNG");
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ByteString getRandomDigest(){
		byte[] input = new byte[20];
		prng.nextBytes(input);
		
		ByteString bs = ByteString.copyFrom(sha.digest(input));
		return bs;
	}

}
