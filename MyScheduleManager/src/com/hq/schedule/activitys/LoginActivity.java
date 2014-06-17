package com.hq.schedule.activitys;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;
import com.hq.schedule.R;
import com.hq.schedule.utility.ClientEncryption;
import com.hq.schedule.utility.SharedPreferenceHelper;
import com.hq.schedule.utility.UrlInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public static final String MALE = "male";
	public static final String FEMALE = "female";
	private TextView login_error_tv = null;
	private EditText login_username_edit = null;
	private EditText login_password_edit = null;
	private Button login_btn = null;
	private Button register_btn = null;
	private HttpManager myHttpManager = null;
	private ProgressDialog myProgressDialog = null;
	private Thread myThread = null;
	private static Handler loginHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.login);
		initUI();
		setEventListener();
	}

	private void initUI() {
		login_error_tv = (TextView) findViewById(R.id.login_error_tv);
		login_username_edit = (EditText) findViewById(R.id.login_username_edit);
		login_password_edit = (EditText) findViewById(R.id.login_password_edit);
		login_btn = (Button) findViewById(R.id.login_btn);
		register_btn = (Button) findViewById(R.id.register_btn);
	}

	private void setEventListener() {
		login_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				login();
			}
		});
		register_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startRegisterActivity();
				// buildRegisterDialog();
			}
		});
	}

	private void startRegisterActivity() {
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
		this.overridePendingTransition(R.anim.push_left_in,
				R.anim.push_left_out);
	}

	// 显示进度框
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
			myProgressDialog.setOnCancelListener( new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					// 对话框取消时，所做的操作
					Toast.makeText(LoginActivity.this, "已取消操作.",
							Toast.LENGTH_SHORT).show();
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

	// 撤销进度框
	private void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	static class LoginHandler extends Handler {
		WeakReference<LoginActivity> mActivity;
		String username = "";
		String password = "";

		LoginHandler(LoginActivity activity, String username, String password) {
			mActivity = new WeakReference<LoginActivity>(activity);
			this.username = username;
			this.password = password;
		}

		@Override
		public void handleMessage(Message msg) {
			LoginActivity theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			String jsonStr = theActivity.myHttpManager.getresultString();
			Log.i("main", "服务端返回" + jsonStr);
			try {
				JSONObject resultJson = new JSONObject(jsonStr);
				String returnInfo = resultJson.getString("return");
				// 客户端异常检测
				if (null == returnInfo || "".equals(returnInfo)) {
					theActivity.login_error_tv.setVisibility(View.VISIBLE);
					theActivity.login_error_tv.setText("错误：返回数据为空");
				} else if (AppInfoManage.CONECTIONEXCEPTION.equals(returnInfo)) {
					theActivity.login_error_tv.setVisibility(View.VISIBLE);
					theActivity.login_error_tv.setText("错误：服务端拒绝连接");
				} else if (AppInfoManage.HTTPERROR.equals(returnInfo)) {
					theActivity.login_error_tv.setVisibility(View.VISIBLE);
					theActivity.login_error_tv.setText("HTTP 错误，请确保手机能上网");
				} else if (AppInfoManage.EXCEPTION.equals(returnInfo)) {
					theActivity.login_error_tv.setVisibility(View.VISIBLE);
					theActivity.login_error_tv.setText("客户端异常");
				} else {
					String resultStr = (new JSONObject(returnInfo)).getString(
							"validate_user").trim();
					// 服务端返回数据校验
					if (AppInfoManage.DBERROR.equals(resultStr)) {
						theActivity.login_error_tv.setVisibility(View.VISIBLE);
						theActivity.login_error_tv.setText("服务端数据库异常");
					} else if (AppInfoManage.FAILED.equals(resultStr)) {
						theActivity.login_error_tv.setVisibility(View.VISIBLE);
						theActivity.login_error_tv.setText("用户名或密码错误");
					} else if (AppInfoManage.DESKEYERROR.equals(resultStr)) {
						theActivity.login_error_tv.setVisibility(View.VISIBLE);
						theActivity.login_error_tv.setText("DES密钥错误");
					} else if (AppInfoManage.ENCRYPTIONERROR.equals(resultStr)) {
						theActivity.login_error_tv.setVisibility(View.VISIBLE);
						theActivity.login_error_tv.setText("服务端加密异常");
					} else if (AppInfoManage.SUCCEED.equals(resultStr)) {
						Toast.makeText(theActivity, "登录成功", Toast.LENGTH_LONG)
								.show();
						SharedPreferenceHelper.saveUserInfo(username, password,
								theActivity);
						theActivity.exit();
					} else {
						theActivity.login_error_tv.setVisibility(View.VISIBLE);
						theActivity.login_error_tv.setText("服务端返回未知数据:"
								+ resultStr);
					}
				}
			} catch (JSONException e) {
				theActivity.login_error_tv.setVisibility(View.VISIBLE);
				theActivity.login_error_tv.setText("解析失败");
			}
		}
	}

	private void login() {
		String username = login_username_edit.getText().toString().trim();
		String password = login_password_edit.getText().toString().trim();
		if ("".equals(username) || "".equals(password)) {
			login_error_tv.setVisibility(View.VISIBLE);
			login_error_tv.setText("请将登陆信息填写完整");
			return;
		}
		String c_deskeyStr = null;
		String c_username = null;
		String c_password = null;
		try {
			ClientEncryption mClientEncryption = new ClientEncryption(username,
					password);
			// 保存客户端RSA私钥
			// SharedPreferenceHelper.saveClientPR(mClientEncryption.getCRSA()
			// .getN().toString(), mClientEncryption.getCRSA().getD()
			// .toString(), this);

			BigInteger deskey = mClientEncryption.getRandomSeed();
			Log.i("main", "客户端生成DES密钥:" + deskey.toString());
			Log.i("main", "加密前的用户名和密码:" + username + "," + password);
			// 加密
			c_username = mClientEncryption.encrypt(username, deskey);
			c_password = mClientEncryption.encrypt(password, deskey);
			c_deskeyStr = mClientEncryption.encryptDESKey(deskey).toString();
			Log.i("main", "加密前的用户名和密码:" + c_username + "," + c_password);
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
		myHttpManager = new HttpManager(UrlInfo.getUrl(this, "validate_user"));
		myHttpManager.addParams("c_deskeyStr", c_deskeyStr);
		myHttpManager.addParams("username", c_username);
		myHttpManager.addParams("password", c_password);
		loginHandler = new LoginHandler(this, username, password);
		myHttpManager.setHandler(loginHandler);
		myThread = new Thread(myHttpManager);
		showProgressDialog("登录中", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			login_error_tv.setVisibility(View.VISIBLE);
			login_error_tv.setText("登录异常");
			Toast.makeText(this, "exception:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
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
		if (null != myProgressDialog && myProgressDialog.isShowing()) {
			dismissProgressDialog();
			if (null != myThread) {
				myThread.interrupt();
			}
			return;
		}
		exit();
	}
}
