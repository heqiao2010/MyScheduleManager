package com.hq.schedule.activitys;

import com.hq.schedule.R;
import com.hq.schedule.utility.SharedPreferenceHelper;
import com.hq.schedule.utility.UrlInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AccountManage extends Activity {
	private Button account_manage_login = null;
	private Button account_manage_register = null;
	private Button account_manage_logout = null;
	private Button account_manage_return = null;
	private EditText ip_edit = null;
	private EditText port_edit = null;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.account_manage);
		initUI();
		setEventListener();
	}

	private void initUI() {
		account_manage_login = (Button) findViewById(R.id.account_manage_login);
		account_manage_register = (Button) findViewById(R.id.account_manage_register);
		account_manage_logout = (Button) findViewById(R.id.account_manage_logout);
		account_manage_return = (Button) findViewById(R.id.account_manage_return);
	}

	private void setEventListener() {
		String userInfo[] = SharedPreferenceHelper.getUserInfo(this);
		if (null == userInfo || "".equals(userInfo[0])
				|| "".equals(userInfo[1])) {
			account_manage_logout.setEnabled(false);
		} else {
			account_manage_login.setText("重新登录");
		}
		// account_manage_delete_all_info.setEnabled(false);
		account_manage_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(AccountManage.this, LoginActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});
		account_manage_register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(AccountManage.this, RegisterActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});
		account_manage_logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferenceHelper.saveUserInfo("", "", AccountManage.this);
				Toast.makeText(AccountManage.this, "注销成功.", Toast.LENGTH_LONG)
						.show();
			}
		});
		account_manage_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exit();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.account_manage, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.account_manage_sever_setting:
			showServerInofEditDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
							Toast.makeText(AccountManage.this, "无效的IP或者端口号.",
									Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	public void saveServerInfo(String ipStr, String portStr) {
		SharedPreferenceHelper.saveServerIp(this, ipStr);
		SharedPreferenceHelper.saveServerPort(this, portStr);
	}

	private void exit() {
		finish();
		overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
	}

	@Override
	public void onBackPressed() {
		exit();
	}
}
