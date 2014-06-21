package encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerEncryption {
	private RSA sRSA = null;
	
	public ServerEncryption(){
		BigInteger p = new BigInteger(ServerEncryptionInfo.p);
		BigInteger q = new BigInteger(ServerEncryptionInfo.q);
		sRSA = new RSA(p, q);
	}
	
	/**
	 * 获取字符MD5值，
	 * @param str
	 * @return BigInteger
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public BigInteger getMD5BigIntOfString(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest mMD5 = MessageDigest.getInstance("MD5");
		byte[] md5byte = mMD5.digest(str.getBytes("UTF-8"));
		BigInteger bigInt = new BigInteger(1, md5byte);
		bigInt = bigInt.pow(3);
		System.out.println("---------------" +bigInt.toString().length());
		return bigInt;
	}
	
	/**
	 * 获取字符串的MD5值
	 * 
	 * @param str
	 * @return String形式十进制数(返回长度小于150)
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public String getMD5StrOfString(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest mMD5 = MessageDigest.getInstance("MD5");
		byte[] md5byte = mMD5.digest(str.getBytes("UTF-8"));
		BigInteger bigInt = new BigInteger(1, md5byte);
		bigInt = bigInt.pow(3);
		String retStr = bigInt.toString(10);
		return retStr;
	}

	public BigInteger getRandomSeed(){
		long rand = (long) (Math.random() * 10E10);
		BigInteger seed = BigInteger.valueOf(rand).pow(5);
		return seed;
	}
	
	public RSA getSRSA(){
		return sRSA;
	}
	
	public RSA getCRSA(String username, String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		BigInteger cp = getMD5BigIntOfString(username);
		BigInteger cq = getMD5BigIntOfString( password);
		cp = cp.nextProbablePrime();
		cq = cq.nextProbablePrime();
		if (cp.compareTo(cq) == 0) {
			cp = cp.divide(BigInteger.valueOf(2)).nextProbablePrime();
		}
		return new RSA(cp, cq);
	}
	
	public String encrypt(String text, BigInteger deskey)
			throws UnsupportedEncodingException {
		DES mDES = new DES(deskey.toString());
		return mDES.encrypt(text);
	}
	
	public String decrypt(String ctext, BigInteger cdeskey){
		BigInteger deskey = sRSA.decode(cdeskey);
		System.out.println("服务端解密出DES密钥: " + deskey);
		DES mDES = new DES(deskey.toString());
		return mDES.decrypt(ctext);
	}
	
	public BigInteger encryptBigInteger(BigInteger m){
		return getSRSA().encode(m);
	}
	
	public BigInteger decryptBigInteger(BigInteger c){
		return getSRSA().decode(c);
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		// MessageDigest mMD5 = MessageDigest.getInstance("MD5");
		// String testStr = "0";
		// byte[] md5byte = mMD5.digest(testStr.getBytes("UTF-8"));
		// BigInteger bigInt = new BigInteger(1, md5byte);
		// String retStr = bigInt.toString(10);
		
//		String testStr = null;
//		Scanner in = new Scanner(System.in);
//		while (!"-1".equals(testStr)) {
//			testStr = in.nextLine();
//			testStr = getMD5ValueOfStr(testStr);
//			System.out.println(testStr);
//		}
//		in.close();
//		long rand = (long) (Math.random()* 10E19);
//		long rand = (long) (new Random().nextDouble() * 10E19);
//		System.out.println(getRandomSeed());
		
//		double rand = (Math.random());
//		System.out.println(rand);
	}
}
