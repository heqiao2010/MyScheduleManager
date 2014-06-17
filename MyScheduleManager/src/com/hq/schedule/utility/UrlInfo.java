package com.hq.schedule.utility;

import android.content.Context;

public class UrlInfo {
	public static String serverIP = "172.32.203.203";
	public static String port = "8080";

	public static String getUrl(Context context, String requeUrl) {
		String serverIpStr = SharedPreferenceHelper.getServerIp(context);
		String serverPortStr = SharedPreferenceHelper.getServerPort(context);
		String urlBase = "http://" + serverIpStr + ":" + serverPortStr;
		return urlBase + "/axis2/services/MyScheduleService/" + requeUrl
				+ "?response=application/json";
	}
	
	/**
	 * 检查一个字串是否为正整数
	 * @param numStr
	 * @return
	 */
	public static boolean isPositiveInteger(String numStr){
		try {
			int t = Integer.parseInt(numStr);
			if( t < 0){
				return false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 简单检查一个IP地址是否是IPv4地址
	 * @param ipStr
	 * @return
	 */
	public static boolean isValideIpAddr(String ipStr){
		try {
			String[] ipNums = ipStr.split(".");
			for(String ipNum : ipNums){
				if(!isPositiveInteger(ipNum)){
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
