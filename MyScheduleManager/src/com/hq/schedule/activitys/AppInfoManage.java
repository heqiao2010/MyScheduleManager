package com.hq.schedule.activitys;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.hq.schedule.R;
import com.hq.schedule.utility.BackupHelper;
import com.hq.schedule.utility.ClientEncryption;
import com.hq.schedule.utility.RecoverHelper;
import com.hq.schedule.utility.SharedPreferenceHelper;
import com.hq.schedule.utility.UrlInfo;

public class AppInfoManage extends Activity {
	private HttpManager myHttpManager = null;
	private static Handler myHandler = null;
	private ProgressDialog myProgressDialog = null;
	private ListView backup_listview = null;
	private SimpleAdapter list_adapter = null;
	private List<Map<String, ?>> listItems;
	private EditText ip_edit = null;
	private EditText port_edit = null;
	private Thread myThread = null;
	public static String DBERROR = "DB Error.";
	public static String UEREXISTS = "User Exists.";
	public static String SUCCEED = "Succeeded";
	public static String FAILED = "Failed";
	public static String DATAEMPTY = "Empty";
	public static String JSONERROR = "JSON Error";
	public static String DESKEYERROR = "DES Key Error";
	public static String ENCRYPTIONERROR = "encryption Error";
	public static String HTTPERROR = "HTTP Error";
	public static String EXCEPTION = "Exception";
	public static String CONECTIONEXCEPTION = "Conection Exception";
	public static ClientEncryption mClientEncryption = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.appinfo_manage);
		initUI();
		setEventListener();
		recover();
	}

	private void initUI() {
		backup_listview = (ListView) findViewById(R.id.backup_listview);
	}

	private void setEventListener() {
		backup_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Map<String, ?> selectedItem = (Map<String, ?>) listItems
						.get(position);
				showRecoverDialog((String) selectedItem.get("backup_time"));
			}
		});
	}

	/**
	 * 显示还原对话框 备份日期
	 * 
	 * @param backup_date
	 */
	private void showRecoverDialog(final String backup_date) {
		new AlertDialog.Builder(this)
				.setTitle("确认还原 " + backup_date + " 的备份信息?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						recoverData(backup_date);
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 显示一个按钮的对话框 对话框标题
	 * 
	 * @param title
	 */
	private void showOneBtntDialog(String title) {
		new AlertDialog.Builder(this).setTitle(title)
				.setPositiveButton("确定", null).show();
	}

	/**
	 * RecoverDataHandler 处理还原时，Http返回结果的回调事件（更新UI）
	 * 
	 * @author Joel
	 */
	static class RecoverDataHandler extends Handler {
		WeakReference<AppInfoManage> mActivity;

		RecoverDataHandler(AppInfoManage activity) {
			mActivity = new WeakReference<AppInfoManage>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AppInfoManage theActivity = mActivity.get();
			// 撤销进度框
			theActivity.dismissProgressDialog();
			String resultJsonStr = theActivity.myHttpManager.getresultString();
			Log.i("main", "服务端返回: " + resultJsonStr);
			try {
				JSONObject resultJson = new JSONObject(resultJsonStr);
				String returnInfo = resultJson.getString("return");
				if (null == returnInfo || "".equals(returnInfo)) {
					Toast.makeText(theActivity, "错误：返回数据为空。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.HTTPERROR.equals(returnInfo)) {
					Toast.makeText(theActivity, "HTTP 错误，请确保手机能上网。",
							Toast.LENGTH_LONG).show();
				} else if (AppInfoManage.CONECTIONEXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "服务端拒绝连接。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.EXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "客户端异常。", Toast.LENGTH_LONG)
							.show();
				} else {
					JSONObject returnJSON = new JSONObject(returnInfo);
					String resultStr = returnJSON.getString("get_backup_info")
							.trim();
					// 服务端返回数据校验
					if (DBERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端数据库异常。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.DESKEYERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "DES密钥错误。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.ENCRYPTIONERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端加密异常。",
								Toast.LENGTH_LONG).show();
					} else if (FAILED.equals(resultStr)) {
						Toast.makeText(theActivity, "验证失败，请重新登录。",
								Toast.LENGTH_LONG).show();
						// 清空用户数据
						SharedPreferenceHelper
								.saveUserInfo("", "", theActivity);
						mClientEncryption = null;
						theActivity.startLoginActivity();
					} else if (SUCCEED.equals(resultStr)) {
						// 客户端解密
						if (null == mClientEncryption) {
							Toast.makeText(theActivity, "无法获取本地密钥，请重新登陆。",
									Toast.LENGTH_LONG).show();
							// 清空用户数据
							SharedPreferenceHelper.saveUserInfo("", "",
									theActivity);
							theActivity.startLoginActivity();
						}
						String backup_info = returnJSON
								.getString("backup_info").trim();
						Log.i("main", "客户端获取服务端发送的密文:" + backup_info);
						String c_sdeskey = returnJSON.getString("c_sdeskey")
								.trim();
						Log.i("main", "客户端获取服务端发送的加密后的DES密钥:" + c_sdeskey);
						BigInteger cdeskey = new BigInteger(c_sdeskey);
						backup_info = mClientEncryption.decrypt(backup_info,
								cdeskey);
						Log.i("main", "客户端解密出的明文:" + backup_info);
						RecoverHelper mRecoverHelper = new RecoverHelper(
								theActivity);
						mRecoverHelper.paserBackupInfoJson(backup_info);
						theActivity.showOneBtntDialog("还原成功.");
					}
				}
			} catch (JSONException e) {
				theActivity.showOneBtntDialog("还原失败：解析错误。");
				Log.e("main", "exception:" + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				theActivity.showOneBtntDialog("还原失败,未知错误:" + e);
				Log.e("main", "exception:" + e.toString());
			}
		}
	}

	/**
	 * 还原应用数据 备份日期
	 * 
	 * @param backup_date
	 */
	private void recoverData(String backup_date) {
		String[] userInfo = SharedPreferenceHelper.getUserInfo(this);
		String username = userInfo[0];
		String password = userInfo[1];
		Log.i("main", "加密前的用户名和密码:" + username + "," + password);
		String c_deskeyStr = null;
		if ("".equals(username) || "".equals(password)) {
			startLoginActivity();
			return;
		}
		try {
			if (null == mClientEncryption) {
				mClientEncryption = new ClientEncryption(username, password);
			}
			BigInteger deskey = mClientEncryption.getRandomSeed();
			Log.i("main", "客户端生成DES密钥:" + deskey.toString());
			// 加密
			username = mClientEncryption.encrypt(username, deskey);
			password = mClientEncryption.encrypt(password, deskey);
			backup_date = mClientEncryption.encrypt(backup_date, deskey);
			c_deskeyStr = mClientEncryption.encryptDESKey(deskey).toString();
			Log.i("main", "加密后的用户名和密码:" + username + "," + password);
			Log.i("main", "客户端生成加密后的DES密钥:" + c_deskeyStr);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("main", "没有找到该算法:" + e.toString());
			return;
		} catch (UnsupportedEncodingException e) {
			Log.i("main", "不支持的编码:" + e.toString());
			e.printStackTrace();
			return;
		}
		myHttpManager = new HttpManager(UrlInfo.getUrl(this, "get_backup_info"));
		myHttpManager.addParams("c_deskeyStr", c_deskeyStr);
		myHttpManager.addParams("username", username);
		myHttpManager.addParams("password", password);
		myHttpManager.addParams("backup_time", backup_date);
		myHandler = new RecoverDataHandler(this);
		myHttpManager.setHandler(myHandler);
		myThread = new Thread(myHttpManager);
		showProgressDialog("正获取备份数据", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(this, "获取备份数据异常:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * BackupDataHandler 处理备份时，Http返回结果的回调事件（更新UI）
	 * 
	 * @author Joel
	 * 
	 */
	static class BackupDataHandler extends Handler {
		WeakReference<AppInfoManage> mActivity;

		BackupDataHandler(AppInfoManage activity) {
			mActivity = new WeakReference<AppInfoManage>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AppInfoManage theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			String returnJson = theActivity.myHttpManager.getresultString();
			Log.i("main", "服务端返回: " + returnJson);
			try {
				JSONObject resultJson = new JSONObject(returnJson);
				String returnInfo = resultJson.getString("return");
				if (null == returnInfo || "".equals(returnInfo)) {
					Toast.makeText(theActivity, "错误：返回数据为空。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.CONECTIONEXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "服务端拒绝连接。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.HTTPERROR.equals(returnInfo)) {
					Toast.makeText(theActivity, "HTTP 错误，请确保手机能上网。",
							Toast.LENGTH_LONG).show();
				} else if (AppInfoManage.EXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "客户端异常。", Toast.LENGTH_LONG)
							.show();
				} else {
					JSONObject returnJSON = new JSONObject(returnInfo);
					String resultStr = returnJSON.getString("backup_info")
							.trim();
					// 服务端返回数据校验
					if (DBERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端数据库异常。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.DESKEYERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "DES密钥错误。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.ENCRYPTIONERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端加密异常。",
								Toast.LENGTH_LONG).show();
					} else if (FAILED.equals(resultStr)) {
						Toast.makeText(theActivity, "验证失败，请重新登录。",
								Toast.LENGTH_LONG).show();
						// 清空用户数据
						SharedPreferenceHelper
								.saveUserInfo("", "", theActivity);
						mClientEncryption = null;
						theActivity.startLoginActivity();
					} else if (SUCCEED.equals(resultStr)) {
						int schedule_count = returnJSON
								.getInt("schedule_count");
						int cateogory_count = returnJSON
								.getInt("category_count");
						int note_count = returnJSON.getInt("note_count");
						String backup_time = returnJSON
								.getString("backup_time");
						theActivity.showOneBtntDialog("于" + backup_time
								+ " 成功备份");
						// +"了：" + schedule_count + "个日程,"
						// + cateogory_count + "个日程分类," + note_count
						// + "个笔记.");
					}
				}
			} catch (JSONException e) {
				theActivity.showOneBtntDialog("解析错误。");
				Log.e("main", "exception:" + e.toString());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 备份应用数据 含有备份数据的JSON字串
	 * 
	 * @param backup_info_str
	 */
	private void backupData(String backup_info_str) {
		Log.i("main", "backup_info_str: " + backup_info_str);
		String[] userInfo = SharedPreferenceHelper.getUserInfo(this);
		String username = userInfo[0];
		String password = userInfo[1];
		String c_deskeyStr = null;
		if ("".equals(username) || "".equals(password)) {
			startLoginActivity();
			return;
		}
		try {
			if (null == mClientEncryption) {
				mClientEncryption = new ClientEncryption(username, password);
			}
			BigInteger deskey = mClientEncryption.getRandomSeed();
			Log.i("main", "客户端生成DES密钥:" + deskey.toString());
			// 加密
			username = mClientEncryption.encrypt(username, deskey);
			password = mClientEncryption.encrypt(password, deskey);
			backup_info_str = mClientEncryption
					.encrypt(backup_info_str, deskey);
			c_deskeyStr = mClientEncryption.encryptDESKey(deskey).toString();
			Log.i("main", "客户端生成加密后的DES密钥:" + c_deskeyStr);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("main", "没有找到该算法:" + e.toString());
			return;
		} catch (UnsupportedEncodingException e) {
			Log.i("main", "不支持的编码:" + e.toString());
			e.printStackTrace();
			return;
		}
		myHttpManager = new HttpManager(UrlInfo.getUrl(this, "backup_info"));
		myHttpManager.addParams("c_deskeyStr", c_deskeyStr);
		myHttpManager.addParams("username", username);
		myHttpManager.addParams("password", password);
		myHttpManager.addParams("info", backup_info_str);
		Log.i("main", "cbackup_info_str: " + backup_info_str);
		myHandler = new BackupDataHandler(this);
		myHttpManager.setHandler(myHandler);
		myThread = new Thread(myHttpManager);
		showProgressDialog("正在发送备份数据", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(this, "备份数据异常:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * 显示进度框
	 * 
	 * @param title
	 * @param message
	 */
	public void showProgressDialog(String title, String message) {
//		if(null == myProgressDialog){
//			myProgressDialog = ProgressDialog.show(this, title, message);
//		} else {
//			myProgressDialog.show();
//		}
		if (null == myProgressDialog) {
			myProgressDialog = new ProgressDialog(this);
			myProgressDialog.setTitle(title);
			myProgressDialog.setMessage(message);
			myProgressDialog.setCancelable(true);
			myProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					Toast.makeText(AppInfoManage.this, "已取消操作.",
							Toast.LENGTH_SHORT).show();
					// 对话框取消时，所做的操作
					if (null != myProgressDialog
							&& myProgressDialog.isShowing()) {
						dismissProgressDialog();
						if (null != myThread) {
							myThread.interrupt();
						}
					}
				}
			});
		} else {
			myProgressDialog.setTitle(title);
			myProgressDialog.setMessage(message);
		}
		myProgressDialog.show();
	}

	/**
	 * 撤销进度框
	 */
	private void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	/**
	 * 执行备份动作
	 */
	private void backup() {
		showProgressDialog("正在备份数据", "请稍后..."); // 显示进度框
		String[] userInfo = SharedPreferenceHelper.getUserInfo(this);
		if ("".equals(userInfo[0]) || "".equals(userInfo[1])) {
			startLoginActivity();
			return;
		} else {
			BackupHelper mBackupHelper = new BackupHelper(this);
			try {
				JSONObject backJsonObj = mBackupHelper.getBackupJSON();
				backupData(backJsonObj.toString());
			} catch (JSONException e) {
				showOneBtntDialog("备份失败，构建错误。");
				Log.e("main", "exception:" + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				showOneBtntDialog("备份失败，未知错误:" + e);
				Log.e("main", "exception:" + e.toString());
			}
		}
	}

	/**
	 * 获取备份列表的应用信息
	 * 
	 * @param backup_list_json
	 * @return List<Map<String, ?>>
	 * @throws JSONException
	 */
	private List<Map<String, ?>> getBackupListItems(JSONArray backup_list_json)
			throws JSONException {
		listItems = new ArrayList<Map<String, ?>>();
		for (int i = 0; i < backup_list_json.length(); i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			JSONObject jo = (JSONObject) backup_list_json.opt(i);
			String backup_time = (String) jo.get("backup_time");
			if (backup_time.length() >= 19) {
				backup_time = backup_time.substring(0, 19);
			}
			item.put("backup_time", backup_time);
			item.put("category_count", jo.get("category_count") + "个日程分类");
			item.put("note_count", jo.get("note_count") + "个笔记");
			item.put("schedule_count", jo.get("schedule_count") + "个日程");
			listItems.add(item);
		}
		return listItems;
	}

	/**
	 * 转到登陆Activity
	 */
	private void startLoginActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		this.overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	/**
	 * RecoverHandler 处理还原之前获取备份列表，Http返回结果的回调事件（更新UI）
	 * 
	 * @author Joel
	 */
	static class RecoverHandler extends Handler {
		WeakReference<AppInfoManage> mActivity;

		RecoverHandler(AppInfoManage activity) {
			mActivity = new WeakReference<AppInfoManage>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AppInfoManage theActivity = mActivity.get();
			// 撤销进度框
			theActivity.dismissProgressDialog(); // 取消进度框
			String jsonStr = theActivity.myHttpManager.getresultString();
			Log.i("main", "服务端返回" + jsonStr);
			try {
				JSONObject resultJson = new JSONObject(jsonStr);
				String returnInfo = resultJson.getString("return");
				// 客户端异常检测
				if (null == returnInfo || "".equals(returnInfo)) {
					Toast.makeText(theActivity, "错误：返回数据为空。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.CONECTIONEXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "服务端拒绝连接。", Toast.LENGTH_LONG)
							.show();
				} else if (AppInfoManage.HTTPERROR.equals(returnInfo)) {
					Toast.makeText(theActivity, "HTTP 错误，请确保手机能上网。",
							Toast.LENGTH_LONG).show();
				} else if (AppInfoManage.EXCEPTION.equals(returnInfo)) {
					Toast.makeText(theActivity, "客户端异常。", Toast.LENGTH_LONG)
							.show();
				} else {
					JSONObject returnJSON = new JSONObject(returnInfo);
					String resultStr = returnJSON.getString("get_backup_list")
							.trim();
					// 服务端返回数据校验
					if (DBERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端数据库异常。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.DESKEYERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "DES密钥错误。",
								Toast.LENGTH_LONG).show();
					} else if (AppInfoManage.ENCRYPTIONERROR.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端加密异常。",
								Toast.LENGTH_LONG).show();
					} else if (FAILED.equals(resultStr)) {
						Toast.makeText(theActivity, "验证失败，请重新登录。",
								Toast.LENGTH_LONG).show();
						// 清空用户数据
						SharedPreferenceHelper
								.saveUserInfo("", "", theActivity);
						mClientEncryption = null;
						theActivity.startLoginActivity();
					} else if (DATAEMPTY.equals(resultStr)) {
						Toast.makeText(theActivity, "服务端，没有您的备份记录。",
								Toast.LENGTH_LONG).show();
					} else if (SUCCEED.equals(resultStr)) {
						String backup_listStr = returnJSON
								.getString("backup_list");
						if ("".equals(backup_listStr)) {
							Toast.makeText(theActivity, "无法获取服务端传来的备份信息。",
									Toast.LENGTH_LONG).show();
						} else {
							JSONArray backup_list_Json = new JSONArray(
									backup_listStr);
							theActivity.getBackupListItems(backup_list_Json);
							theActivity.setListAdapter();
						}
					} else {
						Toast.makeText(theActivity, "服务端返回错误的数据格式。",
								Toast.LENGTH_LONG).show();
						Log.i("main", "服务端返回错误的数据格式: " + resultStr);
					}
				}
			} catch (JSONException e) {
				Toast.makeText(theActivity, "解析失败。", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void setListAdapter() {
		list_adapter = new SimpleAdapter(this, listItems,
				R.layout.list_item_backup, new String[] { "backup_time",
						"schedule_count", "note_count", "category_count" },
				new int[] { R.id.backup_time_tv, R.id.schedule_count_tv,
						R.id.note_count_tv, R.id.category_count_tv });
		backup_listview.setAdapter(list_adapter);
	}

	/**
	 * 还原动作，需向服务端获取备份列表
	 */
	private void recover() {
		String[] userInfo = SharedPreferenceHelper.getUserInfo(this);
		String username = userInfo[0];
		String password = userInfo[1];
		Log.i("main", "加密前的用户名和密码:" + username + "," + password);
		String c_deskeyStr = null;
		if ("".equals(username) || "".equals(password)) {
			startLoginActivity();
			return;
		}
		try {
			if (null == mClientEncryption) {
				mClientEncryption = new ClientEncryption(username, password);
			}
			BigInteger deskey = mClientEncryption.getRandomSeed();
			Log.i("main", "客户端生成DES密钥:" + deskey.toString());
			// 加密
			username = mClientEncryption.encrypt(username, deskey);
			password = mClientEncryption.encrypt(password, deskey);
			c_deskeyStr = mClientEncryption.encryptDESKey(deskey).toString();
			Log.i("main", "客户端生成加密后的DES密钥:" + c_deskeyStr);
			Log.i("main", "加密后的用户名和密码:" + username + "," + password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("main", "没有找到该算法:" + e.toString());
			return;
		} catch (UnsupportedEncodingException e) {
			Log.i("main", "不支持的编码:" + e.toString());
			e.printStackTrace();
			return;
		}
		// recover
		myHttpManager = new HttpManager(UrlInfo.getUrl(this, "get_backup_list"));
		myHttpManager.addParams("c_deskeyStr", c_deskeyStr);
		myHttpManager.addParams("username", username);
		myHttpManager.addParams("password", password);
		myHandler = new RecoverHandler(this);
		myHttpManager.setHandler(myHandler);
		myThread = new Thread(myHttpManager);
		showProgressDialog("正获取备份列表", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(this, "获取备份列表异常:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.appinfo_manage, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void showServerInofEditDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.server_info_setting,
				(ViewGroup) findViewById(R.id.server_info_setting));
		ip_edit = (EditText) layout
				.findViewById(R.id.server_info_setting_ip_ed);
		port_edit = (EditText) layout
				.findViewById(R.id.server_info_setting_port_ed);
		ip_edit.setText(SharedPreferenceHelper.getServerIp(this));
		port_edit.setText(SharedPreferenceHelper.getServerPort(this));
		new AlertDialog.Builder(this).setTitle("编辑服务器信息").setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String portStr = port_edit.getText().toString();
						String ipStr = ip_edit.getText().toString();
						if (UrlInfo.isPositiveInteger(portStr)
								&& UrlInfo.isValideIpAddr(ipStr)) {
							saveServerInfo(ipStr, portStr);
						} else {
							Toast.makeText(AppInfoManage.this, "无效的IP或者端口号.",
									Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	public void saveServerInfo(String ipStr, String portStr) {
		SharedPreferenceHelper.saveServerIp(this, ipStr);
		SharedPreferenceHelper.saveServerPort(this, portStr);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.server_info_setting:
			showServerInofEditDialog();
			return true;
		case R.id.app_info_backup:
			backup();
			return true;
		case R.id.app_info_refresh:
			recover();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void exit() {
		finish();
		overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
	}

	@Override
	public void onBackPressed() {
		// if (null != myProgressDialog && myProgressDialog.isShowing()) {
		// dismissProgressDialog();
		// if (null != myThread) {
		// myThread.interrupt();
		// }
		// return;
		// }
		exit();
	}
}
