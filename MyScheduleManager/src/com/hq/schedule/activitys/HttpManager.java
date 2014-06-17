package com.hq.schedule.activitys;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.os.Handler;
import android.util.Log;

public class HttpManager implements Runnable {
	public static final String HTTPERROR = "{\"return\": \"HTTP Error\" }";
	public static final String EXCEPTION = "{\"return\": \"Exception\" }";
	public static final String CONECTIONEXCEPTION = "{\"return\": \"Conection Exception\" }";
	// public static final int TIME_OUT = 500;
	public static Handler myHandler = null;
	private String resultString = "";
	private HttpPost httpRequest = null;
	private List<NameValuePair> params = null;
	private HttpResponse httpResponse;

	public HttpManager(String action) {
		Log.i("main", "action:" + action);
		httpRequest = new HttpPost(action);
		params = new ArrayList<NameValuePair>();
	}

	public void addParams(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	public void setHandler(Handler handler) {
		myHandler = handler;
	}

	@Override
	public void run() {
		try {
			// HTTP request
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			// HTTP response
			httpResponse = new DefaultHttpClient().execute(httpRequest);
			// 200
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(httpResponse.getEntity());
			} else {
				// error
				resultString = HttpManager.HTTPERROR;
				Log.e("main", "Error Response in HttpManager.run "
						+ httpResponse.getStatusLine().toString());
			}
		}catch (HttpHostConnectException e) {
			Log.e("main", "exception in HttpManager.run:" + e.toString());
			resultString = HttpManager.CONECTIONEXCEPTION;
		} catch (Exception e) {
			Log.e("main", "exception in HttpManager.run:" + e.toString());
			resultString = HttpManager.EXCEPTION;
		}
		// handler
		if (null != myHandler) {
			myHandler.sendEmptyMessage(0);
		}
	}

	// get resultString
	public String getresultString() {
		resultString = resultString.trim();
		return resultString;
	}
}
