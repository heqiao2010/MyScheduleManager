package com.hq.schedule.activitys;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.alarm.SetAlarm;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

//列出某一天所有的提醒信息
public class ReminderList extends Activity {
	public static final int REQUEST_CODE = 100; // 向子Activity发送的请求值
	public static final int RESULT_CANCELL = 200; // 表示子Activity取消
	public static final int RESULT_ALARM_SAVE = 300; // 子Activity保存闹钟
	public static final int RESULT_ALARM_DELLET = 400; // 子Activity删除闹钟
	private ListView reminderListview = null;
	private List<Map<String, ?>> listItems;
	private ProgressDialog myProgressDialog = null;
	private SimpleAdapter adapter = null;
	private TextView bg_day_tv = null;
	private TextView bg_month_tv = null;
	private int mYear = 2014;
	private int mMonth = 4;
	private int mDay = 1;
	private AlarmAscHelper myAlarmAscHelper = null;
	private Boolean noDateFound = false;
	private Boolean reminder_altered = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneday_reminder_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		reminderListview = (ListView) findViewById(R.id.oneday_reminder_listview);
		reminderListview.setOnItemClickListener(new AdapterOnClickListener());
		bg_day_tv = (TextView) findViewById(R.id.bg_day_tv);
		bg_month_tv = (TextView) findViewById(R.id.bg_month_tv);
		getDateFromIntent();
		bg_day_tv.setText(String.valueOf(mDay));
		bg_month_tv.setText(mMonth + "月");
		getDataFromDB();
	}

	private String formatDate(int year, int month, int day) {
		return year + getString(R.string.the_year) + month
				+ getString(R.string.the_month) + day
				+ getString(R.string.the_day);
	}

	private void getDateFromIntent() {
		Intent i = getIntent();
		mYear = i.getIntExtra(Alarms.AlARM_TIME_YEAR, -1);
		mMonth = i.getIntExtra(Alarms.AlARM_TIME_MONTH, -1);
		mDay = i.getIntExtra(Alarms.AlARM_TIME_DAY, -1);
	}

	// 添加提醒
	private void addReminder() {
		Intent intent = new Intent();
		intent.putExtra(Alarms.AlARM_TIME_YEAR, mYear);
		intent.putExtra(Alarms.AlARM_TIME_MONTH, mMonth);
		intent.putExtra(Alarms.AlARM_TIME_DAY, mDay);
		intent.setClass(ReminderList.this, SetAlarm.class);
		// 启动下一个Activity,设置一个请求值100
		startActivityForResult(intent, REQUEST_CODE);
		overridePendingTransition(R.anim.zoom_enter,
				R.anim.zoom_exit);
	}

	// 刷新
	private void refresh() {
		getDataFromDB();
	}

	private List<Map<String, ?>> getItems(Cursor cursor) {
		listItems = new ArrayList<Map<String, ?>>();
		if (null == cursor) {
			return listItems;
		}
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				Map<String, Object> item = new HashMap<String, Object>();
				Alarm alarm = new Alarm(cursor);
				item.put("alarm_id", alarm.id);
				if (this.noDateFound) { // 加入日期
					item.put("date",
							formatDate(alarm.year, alarm.month, alarm.day));
				}
				item.put("time", alarm.hour + ":" + alarm.minutes);
				item.put("label", alarm.label);
				listItems.add(item);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
		} finally {
			cursor.close();
		}
		return listItems;
	}

	// 撤销进度框
	public void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	// 显示进度框
	public void showProgressDialog(String title, String message) {
		if (null == myProgressDialog) {
			myProgressDialog = ProgressDialog.show(this, title, message);
		}
	}

	static class GetDataHandler extends Handler {
		WeakReference<ReminderList> mActivity;

		GetDataHandler(ReminderList activity) {
			mActivity = new WeakReference<ReminderList>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ReminderList theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			Cursor cursor = theActivity.myAlarmAscHelper.getResultCursor();
			if (null == cursor) {
				Toast.makeText(theActivity, "查询出错!", Toast.LENGTH_SHORT).show();
			} else {
				theActivity.getItems(cursor);
				if (theActivity.noDateFound) { // 获取全部提醒信息
					theActivity.adapter = new SimpleAdapter(theActivity,
							theActivity.listItems,
							R.layout.list_item_with_date_label, new String[] {
									"date", "time", "label" }, new int[] {
									R.id.date_tv, R.id.time_tv, R.id.label_tv });
				} else { // 不需要需要展示日期
					theActivity.adapter = new SimpleAdapter(theActivity,
							theActivity.listItems, R.layout.list_item,
							new String[] { "time", "label" }, new int[] {
									R.id.time_tv, R.id.label_tv });
				}
				theActivity.reminderListview.setAdapter(theActivity.adapter);
			}
		}
	}

	private void getDataFromDB() {
		ContentResolver contentResolver = getContentResolver();
		if (this.noDateFound) { // 获取全部提醒信息
			myAlarmAscHelper = new AlarmAscHelper(contentResolver);
		} else { // 获取指定日期提醒信息
			myAlarmAscHelper = new AlarmAscHelper(mYear, mMonth, mDay,
					contentResolver);
		}
		myAlarmAscHelper.myHandler = new GetDataHandler(this);
		Thread myThread = new Thread(myAlarmAscHelper);
		showProgressDialog("查询中", "请稍后，数据太多..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(ReminderList.this, "exception:" + e.toString(),
					Toast.LENGTH_LONG).show();
		}
	}

	public class AdapterOnClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Map<String, ?> selectedItem = (Map<String, ?>) listItems
					.get(position);
			Intent intent = new Intent();
			intent.setClass(ReminderList.this, SetAlarm.class);
			intent.putExtra(Alarms.ALARM_ID,
					(Integer) selectedItem.get("alarm_id"));
			// 启动下一个Activity,设置一个请求值100
			startActivityForResult(intent, REQUEST_CODE);
			overridePendingTransition(R.anim.zoom_enter,
					R.anim.zoom_exit);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) // 如果一个返回的Activity的请求值为100，
		{
			// 说明是本Activity是调用者
			switch (resultCode) {
			case RESULT_ALARM_DELLET: // 闹钟删除
			case RESULT_ALARM_SAVE: // 闹钟保存
				reminder_altered = true;
				refresh();
				break;
			case RESULT_CANCELL: // 闹钟取消
				// 什么都不做
				break;
			default:
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if (reminder_altered) {
			setResult(CalendarView.RESULT_REMINDER_ALTER, intent);
		} else {
			setResult(CalendarView.RESULT_CANCELL, intent);
		}
		super.onBackPressed();
		exit();
	}

	private void exit() {
		finish();
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reminder_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.add_reminder:
			// 添加提醒
			addReminder();
			reminder_altered = true;
			return true;
		case R.id.refresh:
			refresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
