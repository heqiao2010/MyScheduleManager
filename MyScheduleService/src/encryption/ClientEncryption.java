package encryption;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientEncryption {

	private RSA cRSA;

	public ClientEncryption(String username, String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		cRSA = getCRSA(username, password);
	}

	/**
	 * 获取字符MD5值，
	 * 
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
		System.out.println("---------------" +bigInt.toString());
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

	/**
	 * 随机生成DES密钥
	 * @return 10E50以内整数
	 */
	public BigInteger getRandomSeed() {
		long rand = (long) (Math.random() * 10E10);
		BigInteger seed = BigInteger.valueOf(rand).pow(5);
		return seed;
	}

	/**
	 * 通过用户名和密码生成RSA密钥
	 * @param username
	 * @param password
	 * @return RSA对象
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public RSA getCRSA(String username, String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		BigInteger cp = getMD5BigIntOfString(username);
		BigInteger cq = getMD5BigIntOfString(password);
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

	public String decrypt(String ctext, BigInteger cdeskey) {
		BigInteger deskey = cRSA.decode(cdeskey);
		System.out.println("cdeskey: " + deskey);
		DES mDES = new DES(deskey.toString());
		return mDES.decrypt(ctext);
	}

	public RSA getCRSA() {
		return cRSA;
	}

	public static void main(String[] args) {
		String username = "qqwerqwerqwe";
		String password = "1rqwerqwerqwer";
		try {
			ClientEncryption mClientEncryption = new ClientEncryption(username,
					password);
			ServerEncryption mServerEncryption = new ServerEncryption();
			String message = "backup_info_str: {\"backup_time\":\"2014-5-23 23:0:13\",\"festi"
					+ "val\":[],\"category\":[{\"backup_time\":\"2014-5-23 23:0:13\",\"category"
					+ "__id\":0,\"priority_level\":2},{\"backup_time\":\"201"
					+ "4-5-23 23:0:13\",\"category_name\":\"第二个分类\",\"_id\":1,\"priori"
					+ "ty_level\":3}],\"schedule\":[{\"enabled\":0,\"minutes\":22,\"alert"
					+ "m_alert\",\"daysofweek\":0,\"hour\":9,\"backup_time}]}"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
					+ "端向服务端发送消息端向服务端发送消息端向服务端发送消息" + "客户端向服务端发送消息客户端向服务端发送消息.";
			// 客户端向服务端发送消息
			BigInteger clientDeskey = mClientEncryption.getRandomSeed();
			System.out.println("cdeskey: " + clientDeskey);
			String cMessage = mClientEncryption.encrypt(message, clientDeskey);
			System.out.println("C: " + cMessage);
			BigInteger c_clientDeskey = mServerEncryption.getSRSA().encode(
					clientDeskey);
			// 密文cMessage， 加密后的DES密钥c_clientDeskey

			// 服务端解密
			String sMessage = mServerEncryption.decrypt(cMessage,
					c_clientDeskey);
			System.out.println(sMessage);

			// 服务端向客户端发送消息
			BigInteger serverDeskey = mServerEncryption.getRandomSeed();
			System.out.println("sdeskey: " + serverDeskey);
			String c_message = mServerEncryption.encrypt(message, serverDeskey);
			BigInteger c_serverDeskey = mServerEncryption.getCRSA(username,
					password).encode(serverDeskey);

			// 客户端解密
			System.out.println("C:" + c_message);
			String cMessage1 = mClientEncryption.decrypt(c_message,
					c_serverDeskey);
			System.out.println(cMessage1);

		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
