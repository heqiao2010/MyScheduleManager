package com.hq.schedule.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.hq.schedule.activitys.MainActivity;

public class SharedPreferenceHelper {

	public static String[] getUserInfo(Context context) {
		String[] userInfo = new String[2];
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		userInfo[0] = prefs.getString("username", "");
		userInfo[1] = prefs.getString("password", "");
		return userInfo;
	}

	public static void saveUserInfo(String username, String password,
			Context context) {
		// 将用户名和密码保存在SharedPreferences中
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString("username", username);
		ed.putString("password", password);
		ed.apply();
	}

	public static int getAnimationType(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getInt("AnimationType", 0);
	}

	public static void saveAnimationType(int type, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("AnimationType", type);
		ed.apply();
	}

	public static int getTextSize(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getInt("textSize", 20);
	}

	public static void saveTextSize(int textSize, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("textSize", textSize);
		ed.apply();
	}

	public static int getTextLength(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getInt("textLenght", 140);
	}

	public static void saveTextLength(int textLength, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("textLenght", textLength);
		ed.apply();
	}

	/**
	 * 获取文字图片颜色类型 0:白底黑字， 1:白字黑底，2：红字白底，3：红字黑底
	 * 
	 * @param context
	 * @return
	 */
	public static int getTextPictColorType(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getInt("textPictColor", 0);
	}

	/**
	 * 保存文字图片颜色类型 0:白底黑字， 1:白字黑底，2：红字白底，3：红字黑底
	 * 
	 * @param colorType
	 * @param context
	 */
	public static void saveTextPictColorType(int colorType, Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("textPictColor", colorType);
		ed.apply();
	}

	/**
	 * 获取服务器IP地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getServerIp(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getString("ServerIP", UrlInfo.serverIP);
	}

	/**
	 * 保存服务器IP地址
	 * 
	 * @param context
	 * @param serverIP
	 */
	public static void saveServerIp(Context context, String serverIP) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString("ServerIP", serverIP);
		ed.apply();
	}

	/**
	 * 获取服务器端口号
	 * 
	 * @param context
	 * @return
	 */
	public static String getServerPort(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		return prefs.getString("ServerPort", UrlInfo.port);
	}

	public static void saveServerPort(Context context, String serverPort) {
		SharedPreferences prefs = context.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putString("ServerPort", serverPort);
		ed.apply();
	}
	// /**
	// * 获取客户端的RSA 私钥
	// * @param context
	// * @return CD， CN
	// */
	// public static String[] getClientPR(Context context){
	// SharedPreferences prefs = context.getSharedPreferences(
	// MainActivity.PREFERENCES, 0);
	// String[] ret = new String[2];
	// ret[0] = prefs.getString("ClinetD", "");
	// ret[1] = prefs.getString("ClinetN", "");
	// return ret;
	// }
	//
	// /**
	// * 保存客户端的RSA 私钥
	// * @param clientN
	// * @param clientD
	// * @param context
	// */
	// public static void saveClientPR(String clientN, String clientD, Context
	// context){
	// SharedPreferences prefs = context.getSharedPreferences(
	// MainActivity.PREFERENCES, 0);
	// SharedPreferences.Editor ed = prefs.edit();
	// ed.putString("ClinetD", clientD);
	// ed.putString("ClinetN", clientN);
	// ed.apply();
	// }
}
