package com.hq.schedule.utility;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

public class ClientEncryption {

	private RSA cRSA;
	private BigInteger se = null;
	private BigInteger sn = null;
	// 服务端的RSA公钥
	public static String SE = "3810394688309709187623837890641670486252095"
			+ "7183440024388062795305778158817275003810397219173906693796"
			+ "6773665675964063755525076094345374581313824159238302057455"
			+ "0373387736244471801745160482986587378579865870886107300391"
			+ "4228013694234872699704694402998590153602671086690548315802"
			+ "08295229351110730168779";
	public static String SN = "1524157875323883675049535156256668194500838"
			+ "287337600975522511812231126352691000152415"
			+ "888766956267751867094662703856255022100304"
			+ "377381498325255296636977899587622618490874"
			+ "744702298722755550997104087212193261932467"
			+ "611514371589683549641821566212467477746075"
			+ "2798870903811999573234411205608762245389811307";

	public ClientEncryption(String username, String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		cRSA = getCRSA(username, password);
		se = new BigInteger(SE);
		sn = new BigInteger(SN);
	}

	/**
	 * 用服务端公钥加密DES密钥
	 * 
	 * @param desKey
	 * @return 加密后的DES密钥
	 */
	public BigInteger encryptDESKey(BigInteger desKey) {
		BigInteger c_desKey = desKey.modPow(se, sn);
		return c_desKey;
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

	public BigInteger getRandomSeed() {
		long rand = (long) (Math.random() * 10E10);
		BigInteger seed = BigInteger.valueOf(rand).pow(5);
		return seed;
	}

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
		Log.i("main", "客户端解密后的DES密钥:" + deskey);
		DES mDES = new DES(deskey.toString());
		return mDES.decrypt(ctext);
	}

	public RSA getCRSA() {
		return cRSA;
	}

	// public static void main(String[] args) {
	// String username = "q";
	// String password = "1";
	// try {
	// ClientEncryption mClientEncryption = new ClientEncryption(username,
	// password);
	// ServerEncryption mServerEncryption = new ServerEncryption();
	// String message = "就快毕业了客户端向服务端发送消息客户"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息"
	// + "端向服务端发送消息端向服务端发送消息端向服务端发送消息" + "客户端向服务端发送消息客户端向服务端发送消息.";
	// // 客户端向服务端发送消息
	// BigInteger clientDeskey = mClientEncryption.getRandomSeed();
	// System.out.println("cdeskey: " + clientDeskey);
	// String cMessage = mClientEncryption.encrypt(message, clientDeskey);
	// System.out.println("C: " + cMessage);
	// BigInteger c_clientDeskey = mServerEncryption.getSRSA().encode(
	// clientDeskey);
	// // 密文cMessage， 加密后的DES密钥c_clientDeskey
	//
	// // 服务端解密
	// String sMessage = mServerEncryption.decrypt(cMessage,
	// c_clientDeskey);
	// System.out.println(sMessage);
	//
	// // 服务端向客户端发送消息
	// BigInteger serverDeskey = mServerEncryption.getRandomSeed();
	// System.out.println("sdeskey: " + serverDeskey);
	// String c_message = mServerEncryption.encrypt(message,
	// serverDeskey);
	// BigInteger c_serverDeskey = mServerEncryption.getCRSA(username,
	// password).encode(serverDeskey);
	//
	// // 客户端解密
	// System.out.println("C:" + c_message);
	// String cMessage1 = mClientEncryption.decrypt(c_message,
	// c_serverDeskey);
	// System.out.println(cMessage1);
	//
	// } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	// e.printStackTrace();
	// }
	// }
}
