package encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ComputeKey {

	private static final String HMAC_SHA512 = "HmacSHA512";

	private static String toHexString(byte[] bytes) {
		Formatter formatter = new Formatter();
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}
		String value = formatter.toString();
		formatter.close();
		return value;
	}

	public static String calculateHMAC(String data, String secret) {

		SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), HMAC_SHA512);
		Mac mac;
		try {
			mac = Mac.getInstance(HMAC_SHA512);
			mac.init(secretKeySpec);
			return toHexString(mac.doFinal(data.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static void main(String[] args) throws Exception {
		String secret = "0bb609ca8a8d4ca195dec3f3ed53b3fa";
		String txt = "https://bittrex.com/api/v1.1/account/getbalances?apikey=3bb70a1c954f4ce0ba1216c754839259&nonce=1515953162";
		String hmac = calculateHMAC(txt , secret);
		System.out.println(hmac);
	}

}
