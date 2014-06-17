package com.hq.schedule.activitys;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import com.hq.schedule.R;
import com.hq.schedule.utility.ClientEncryption;
import com.hq.schedule.utility.SharedPreferenceHelper;
import com.hq.schedule.utility.UrlInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class RegisterActivity extends Activity {
	private TextView register_error_tv = null;
	private EditText register_username_ed = null;
	private EditText register_name_ed = null;
	private EditText register_age_ed = null;
	private RadioGroup ganderGroup = null;
	private RadioButton maleRadioButton = null;
	private RadioButton femalRadioButton = null;
	private EditText register_email_ed = null;
	private EditText register_password_ed = null;
	private EditText register_repassword_ed = null;
	private Button register_ok_btn = null;
	private Button register_return_btn = null;
	private String genderStr = LoginActivity.MALE;
	private HttpManager myHttpManager = null;
	private ProgressDialog myProgressDialog = null;
	private static Handler registerHandler = null;
	private Thread myThread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		initUI();
		setEventListener();
	}

	private void initUI() {
		register_error_tv = (TextView) findViewById(R.id.register_error_tv);
		register_username_ed = (EditText) findViewById(R.id.register_username_ed);
		register_name_ed = (EditText) findViewById(R.id.register_name_ed);
		register_age_ed = (EditText) findViewById(R.id.register_age_ed);
		ganderGroup = (RadioGroup) findViewById(R.id.register_gander_radiogroup);
		maleRadioButton = (RadioButton) findViewById(R.id.register_male_radioButton);
		femalRadioButton = (RadioButton) findViewById(R.id.register_female_radioButton);
		register_email_ed = (EditText) findViewById(R.id.register_email_ed);
		register_password_ed = (EditText) findViewById(R.id.register_password_ed);
		register_repassword_ed = (EditText) findViewById(R.id.register_repassword_ed);
		register_ok_btn = (Button) findViewById(R.id.register_ok_btn);
		register_return_btn = (Button) findViewById(R.id.register_return_btn);
	}

	private void setEventListener() {
		ganderGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == maleRadioButton.getId()) {
					genderStr = LoginActivity.MALE;
				} else if (checkedId == femalRadioButton.getId()) {
					genderStr = LoginActivity.FEMALE;
				}
			}
		});
		register_username_ed
				.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_name_ed
				.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_age_ed.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_email_ed
				.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_password_ed
				.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_repassword_ed
				.setOnFocusChangeListener(new MyOnFocusChangeListener());
		register_return_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finishActivity();
			}
		});
		register_ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String usernameStr = register_username_ed.getText().toString()
						.trim();
				String nameStr = register_name_ed.getText().toString().trim();
				String ageStr = register_age_ed.getText().toString().trim();
				String emailStr = register_email_ed.getText().toString().trim();
				String passwordStr = register_password_ed.getText().toString()
						.trim();
				String repasswordStr = register_repassword_ed.getText()
						.toString().trim();
				Pattern email_pattern = Pattern
						.compile("//w+@(//w+//.)+[a-z]{2,3}");
				Matcher m = email_pattern.matcher(emailStr);
				if ("".equals(usernameStr) || "".equals(nameStr)
						|| "".equals(ageStr) || "".equals(emailStr)
						|| "".equals(passwordStr) || "".equals(repasswordStr)) {
					Toast.makeText(RegisterActivity.this, "注册信息填写不完整",
							Toast.LENGTH_LONG).show();
					register_error_tv.setVisibility(View.VISIBLE);
					register_error_tv.setText("请填写注册信息");
					return;
				} else if (!passwordStr.equals(repasswordStr)) {
					Toast.makeText(RegisterActivity.this, "两次填写密码不一致",
							Toast.LENGTH_LONG).show();
					register_error_tv.setVisibility(View.VISIBLE);
					register_error_tv.setText("重新填写密码");
					register_password_ed.setText("");
					register_repassword_ed.setText("");
					return;
				} else if (m.matches()) {
					register_error_tv.setVisibility(View.VISIBLE);
					register_error_tv.setText("错误的邮件地址格式");
					register_email_ed.setText("");
				} else {
					register(usernameStr, nameStr, genderStr, ageStr, emailStr,
							passwordStr);
				}
			}
		});
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
					Toast.makeText(RegisterActivity.this, "已取消操作.",
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

	// 撤销进度框
	private void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	// exit Activity
	private void finishActivity() {
		this.finish();
	}

	static class RegisterHandler extends Handler {
		WeakReference<RegisterActivity> mActivity;
		String username = "";
		String password = "";

		RegisterHandler(RegisterActivity activity, String username,
				String password) {
			mActivity = new WeakReference<RegisterActivity>(activity);
			this.username = username;
			this.password = password;
		}

		@Override
		public void handleMessage(Message msg) {
			RegisterActivity theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			String jsonStr = theActivity.myHttpManager.getresultString();
			Log.i("main", "服务端返回" + jsonStr);
			try {
				JSONObject resultJson = new JSONObject(jsonStr);
				String returnInfo = resultJson.getString("return");
				// 客户端异常检测
				if (null == returnInfo || "".equals(returnInfo)) {
					theActivity.register_error_tv.setVisibility(View.VISIBLE);
					theActivity.register_error_tv.setText("错误：返回数据为空");
				} else if (AppInfoManage.CONECTIONEXCEPTION.equals(returnInfo)) {
					theActivity.register_error_tv.setVisibility(View.VISIBLE);
					theActivity.register_error_tv.setText("错误：服务端拒绝连接");
				} else if (AppInfoManage.HTTPERROR.equals(returnInfo)) {
					theActivity.register_error_tv.setVisibility(View.VISIBLE);
					theActivity.register_error_tv.setText("HTTP 错误，请确保手机能上网");
				} else if (AppInfoManage.EXCEPTION.equals(returnInfo)) {
					theActivity.register_error_tv.setVisibility(View.VISIBLE);
					theActivity.register_error_tv.setText("客户端异常");
				} else {
					String resultStr = (new JSONObject(returnInfo)).getString(
							"register").trim();
					// 服务端返回数据校验
					if (AppInfoManage.DBERROR.equals(resultStr)) {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("服务端数据库异常");
					} else if (AppInfoManage.FAILED.equals(resultStr)) {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("注册失败");
					} else if (AppInfoManage.UEREXISTS.equals(resultStr)) {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("该用户已存在，请更换用户名");
					} else if (AppInfoManage.DESKEYERROR.equals(resultStr)) {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("DES密钥错误");
					} else if (AppInfoManage.ENCRYPTIONERROR.equals(resultStr)) {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("服务端加密异常");
					} else if (AppInfoManage.SUCCEED.equals(resultStr)) {
						Toast.makeText(theActivity, "注册成功", Toast.LENGTH_LONG)
								.show();
						SharedPreferenceHelper.saveUserInfo(username, password,
								theActivity);
						theActivity.finishActivity();
					} else {
						theActivity.register_error_tv
								.setVisibility(View.VISIBLE);
						theActivity.register_error_tv.setText("服务端返回未知数据:"
								+ resultStr);
					}
				}
			} catch (JSONException e) {
				theActivity.register_error_tv.setVisibility(View.VISIBLE);
				theActivity.register_error_tv.setText("解析失败");
			}

		}
	}

	private void register(String username, String name, String gender,
			String age, String email, String password) {
		Log.i("main", "注册前的信息:" + username + "," + name + "," + gender + ","
				+ age + "," + email + "," + password);
		String c_deskeyStr = null;
		String c_username = null;
		String c_name = null;
		String c_gender = null;
		String c_age = null;
		String c_email = null;
		String c_password = null;
		try {
			ClientEncryption mClientEncryption = new ClientEncryption(username,
					password);
			BigInteger deskey = mClientEncryption.getRandomSeed();
			Log.i("main", "客户端生成DES密钥:" + deskey.toString());
			// 加密
			c_username = mClientEncryption.encrypt(username, deskey);
			c_name = mClientEncryption.encrypt(name, deskey);
			c_gender = mClientEncryption.encrypt(gender, deskey);
			c_age = mClientEncryption.encrypt(age, deskey);
			c_email = mClientEncryption.encrypt(email, deskey);
			c_password = mClientEncryption.encrypt(password, deskey);
			c_deskeyStr = mClientEncryption.encryptDESKey(deskey).toString();
			Log.i("main", "加密的注册的信息:" + c_username + "," + c_name + ","
					+ c_gender + "," + c_age + "," + c_email + "," + c_password);
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
		myHttpManager = new HttpManager(UrlInfo.getUrl(this, "register"));
		myHttpManager.addParams("c_deskeyStr", c_deskeyStr);
		myHttpManager.addParams("username", c_username);
		myHttpManager.addParams("name", c_name);
		myHttpManager.addParams("gender", c_gender);
		myHttpManager.addParams("age", c_age);
		myHttpManager.addParams("email", c_email);
		myHttpManager.addParams("password", c_password);
		registerHandler = new RegisterHandler(this, username, password);
		myHttpManager.setHandler(registerHandler);
		myThread = new Thread(myHttpManager);
		showProgressDialog("注册中", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			register_error_tv.setVisibility(View.VISIBLE);
			register_error_tv.setText("注册异常");
			Toast.makeText(this, "exception:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private class MyOnFocusChangeListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getId() == R.id.register_username_ed
					|| v.getId() == R.id.register_name_ed
					|| v.getId() == R.id.register_age_ed
					|| v.getId() == R.id.register_email_ed
					|| v.getId() == R.id.register_password_ed
					|| v.getId() == R.id.register_repassword_ed) {
				register_error_tv.setVisibility(View.INVISIBLE);
			}
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
		// if(myProgressDialog.isShowing()){
		// dismissProgressDialog();
		// return;
		// }
		exit();
	}
}
