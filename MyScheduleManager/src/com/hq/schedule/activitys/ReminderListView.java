package com.hq.schedule.activitys;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.alarm.SetAlarm;
import com.hq.schedule.views.OverLay;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReminderListView extends Fragment {
	private BaseAdapter adapter;
	private MyLetterListView letterListView;
	private ListView reminderListview = null;
	private HashMap<String, Integer> alphaIndexer;// 存放存在的汉语拼音首字母和与之对应的列表位置
	private List<ContentValues> list = null;
	private String[] sections;// 存放存在的汉语拼音首字母
	private static final String ALARM_ID = "alarm_id";
	private static final String ALARM_TIME = "alarm_time";
	private static final String ALARM_DATE = "alarm_date";
	private static final String ALARM_LABEL = "alarm_label";
	private static final String SORT_KEY = "sort_key";
	private static final String ALARM_FLAG = "flag";
	private ProgressDialog myProgressDialog = null;
	private Button deleteButton = null;
	private Button refreshButton = null;
	private AlarmAscHelper myAlarmAscHelper = null;
	private FragmentActivity myActivity = null;
	private Button bt_selectall;
	private Button bt_cancel;
	private Button bt_deselectall;
	private Button bt_confirmdelete;
	private int checkNum; // 记录选中的条目数量
	private TextView tv_show;// 用于显示选中的条目数量
	private RelativeLayout delete_chooser_bar = null;
	private Boolean bShowCheckBox = false; // 是否显示CheckBox

	public ReminderListView() {
		super();
	}

	private FragmentActivity getMyActivity() {
		if (null == myActivity) {
			myActivity = getActivity();
		}
		return myActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.comming_reminder_list,
				container, false);
		reminderListview = (ListView) rootView
				.findViewById(R.id.reminder_listview);
		deleteButton = (Button) rootView.findViewById(R.id.delete_reminder_btn);
		refreshButton = (Button) rootView.findViewById(R.id.refresh_btn);
		letterListView = (MyLetterListView) rootView
				.findViewById(R.id.letterListView);
		bt_selectall = (Button) rootView.findViewById(R.id.bt_selectall);
		bt_cancel = (Button) rootView.findViewById(R.id.bt_cancelselectall);
		bt_deselectall = (Button) rootView.findViewById(R.id.bt_deselectall);
		bt_confirmdelete = (Button) rootView
				.findViewById(R.id.bt_confirmdelete);
		tv_show = (TextView) rootView.findViewById(R.id.show_delete_count_tv);
		delete_chooser_bar = (RelativeLayout) rootView
				.findViewById(R.id.reminder_delete_choose_bar);
		alphaIndexer = new HashMap<String, Integer>();
		getDataFromDB();
		letterListView
				.setOnTouchingLetterChangedListener(new LetterListViewListener());
		return rootView;
	}

	// 获得汉语拼音首字母
	private String getAlpha(String str) {
		if (null == str || 0 == str.trim().length()) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		// 正则表达式，判断首字母是否是英文字母
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase(Locale.ENGLISH);
		} else {
			return "#";
		}
	}

	private class LetterListViewListener implements
			MyLetterListView.OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(final String s) {
			OverLay.getOverLay(getActivity()).setOuterHandler(null);
			OverLay.getOverLay(getActivity()).setOverLayTextSize(70);
			OverLay.getOverLay(getActivity()).showOverLay(s, 1500);
			if (alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				reminderListview.setSelection(position);
			}
		}

	}

	class MyOnItemLongClickListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int postion, long id) {
			ContentValues item = list.get(postion);
			int alarm_id = (Integer) item.get(ALARM_ID);
			String label = item.getAsString(ALARM_LABEL);
			if (4 < label.length()) {
				label = label.substring(0, 5);
				label += "...";
			}
			showDeleteAlarm(alarm_id, label);
			return false;
		}
	}

	private void showDeleteAlarm(final int alarm_id, final String label) {
		new AlertDialog.Builder(myActivity).setTitle("删除" + label + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Alarms.deleteAlarm(myActivity, alarm_id);
						Toast.makeText(myActivity, label + "成功删除",
								Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", null).show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 删除提醒
				deleteReminder();
			}
		});
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				refresh();
			}
		});
		reminderListview.setOnItemClickListener(new AdapterOnClickListener());
		reminderListview
				.setOnItemLongClickListener(new MyOnItemLongClickListener());
		// 全选
		bt_selectall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int listSize = list.size();
				for (int i = 0; i < listSize; i++) {
					list.get(i).put(ALARM_FLAG, "true");
				}
				checkNum = listSize;
				dataChanged();
			}
		});
		// 取消选择
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				for (int i = 0; i < list.size(); i++) {
					list.get(i).put(ALARM_FLAG, "false");
				}
				checkNum = 0;
				dataChanged();
			}
		});
		// 反选
		bt_deselectall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				checkNum = 0;
				for (int i = 0; i < list.size(); i++) {
					if ("true".equals(list.get(i).get(ALARM_FLAG))) {
						list.get(i).put(ALARM_FLAG, "false");
					} else {
						list.get(i).put(ALARM_FLAG, "true");
						checkNum++;
					}
				}
				dataChanged();
			}
		});
		// 确认删除
		bt_confirmdelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (0 == checkNum) {
					Toast.makeText(getActivity(), "请选中要删除的项目.",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					new AlertDialog.Builder(getActivity())
							.setTitle("确认删除所选?")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											deleteChoosenReminder();
										}
									}).setNegativeButton("取消", null).show();
				}
			}
		});
	}

	private void deleteChoosenReminder() {
		Iterator<ContentValues> iterator = list.iterator();
		while (iterator.hasNext()) {
			ContentValues temp = iterator.next();
			if (temp.get(ALARM_FLAG).equals("true")) {
				int alarm_id = Integer.parseInt(temp.get(ALARM_ID).toString());
				Alarms.deleteAlarm(myActivity, alarm_id);
				iterator.remove();
			}
		}
		Toast.makeText(myActivity, "已删除" + checkNum + "项", Toast.LENGTH_SHORT)
				.show();
		checkNum = 0;
		dataChanged();
	}

	private void dataChanged() {
		// 通知listView刷新
		setAdapter(list);
		// TextView显示最新的选中数目
		tv_show.setText("已选中" + checkNum + "项.");
	}

	public void refresh() {
		getDataFromDB();
		checkNum = 0;
		tv_show.setText("已选中" + checkNum + "项.");
	}

	private void deleteReminder() {
		if (delete_chooser_bar.getVisibility() == View.VISIBLE) {
			deleteButton.setBackgroundResource(R.drawable.list_uncheck);
			// letterListView.setVisibility(View.VISIBLE);
			delete_chooser_bar.setVisibility(View.GONE);
			bShowCheckBox = false;
			dataChanged();
		} else {
			deleteButton.setBackgroundResource(R.drawable.list_check);
			// letterListView.setVisibility(View.GONE);
			delete_chooser_bar.setVisibility(View.VISIBLE);
			bShowCheckBox = true;
			dataChanged();
		}
	}

	private String formatDate(int year, int month, int day) {
		return year + getString(R.string.the_year) + month
				+ getString(R.string.the_month) + day
				+ getString(R.string.the_day);
	}

	private void fillIndexValues() {
		alphaIndexer = new HashMap<String, Integer>();
		sections = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			// 当前汉语拼音首字母
			String currentStr = getAlpha(list.get(i).getAsString(SORT_KEY));
			// 上一个汉语拼音首字母，如果不存在为" "
			String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1)
					.getAsString(SORT_KEY)) : " ";
			if (!previewStr.equals(currentStr)) {
				String name = getAlpha(list.get(i).getAsString(SORT_KEY));
				alphaIndexer.put(name, i);
				sections[i] = name;
			}
		}
	}

	private List<ContentValues> getItems(Cursor cursor) {
		list = new ArrayList<ContentValues>();
		if (null == cursor) {
			return list;
		}
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				ContentValues item = new ContentValues();
				Alarm alarm = new Alarm(cursor);
				item.put(ALARM_ID, alarm.id);
				item.put(ALARM_DATE,
						formatDate(alarm.year, alarm.month, alarm.day));
				item.put(ALARM_TIME, alarm.hour + ":" + alarm.minutes);
				item.put(ALARM_LABEL, alarm.label);
				String sort_key = cursor
						.getString(Alarm.Columns.ALARM_SORT_KEY_INDEX);
				Log.i("main", "从数据库中获取sort_key为：" + sort_key);
				item.put(SORT_KEY, sort_key);
				item.put(ALARM_FLAG, "false");
				list.add(item);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
		} finally {
			cursor.close();
		}
		return list;
	}

	// 撤销进度框
	private void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	// 显示进度框
	public void showProgressDialog(String title, String message) {
		if (null == myProgressDialog) {
			myProgressDialog = ProgressDialog.show(getMyActivity(), title,
					message);
		}
	}

	private class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<ContentValues> list;

		public ListAdapter(Context context, List<ContentValues> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
			fillIndexValues();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.list_item_with_date_label, null);
				holder = new ViewHolder();
				holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
				holder.time_tv = (TextView) convertView
						.findViewById(R.id.reminder_time_tv);
				holder.date_tv = (TextView) convertView
						.findViewById(R.id.reminder_date_tv);
				holder.label_tv = (TextView) convertView
						.findViewById(R.id.reminder_label_tv);
				holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
				if (bShowCheckBox) {
					holder.cb.setVisibility(View.VISIBLE);
				} else {
					holder.cb.setVisibility(View.GONE);
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ContentValues cv = list.get(position);
			holder.time_tv.setText(cv.getAsString(ALARM_TIME));
			holder.date_tv.setText(cv.getAsString(ALARM_DATE));
			holder.label_tv.setText(cv.getAsString(ALARM_LABEL));
			holder.cb.setChecked(list.get(position).get(ALARM_FLAG)
					.equals("true"));
			String currentStr = getAlpha(list.get(position).getAsString(
					SORT_KEY));
			String previewStr = (position - 1) >= 0 ? getAlpha(list.get(
					position - 1).getAsString(SORT_KEY)) : " ";
			if (!previewStr.equals(currentStr)) {
				holder.alpha.setVisibility(View.VISIBLE);
				holder.alpha.setText(currentStr);
			} else {
				holder.alpha.setVisibility(View.GONE);
			}
			return convertView;
		}

		private class ViewHolder {
			TextView alpha;
			TextView time_tv;
			TextView date_tv;
			TextView label_tv;
			CheckBox cb;
		}
	}

	private void setAdapter(List<ContentValues> list) {
		adapter = new ListAdapter(getMyActivity(), list);
		reminderListview.setAdapter(adapter);
	}

	static class GetDataHandler extends Handler {
		WeakReference<ReminderListView> mActivity;

		GetDataHandler(ReminderListView activity) {
			mActivity = new WeakReference<ReminderListView>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ReminderListView theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			Cursor cursor = theActivity.myAlarmAscHelper.getResultCursor();
			if (null == cursor) {
				Toast.makeText(theActivity.getActivity(), "查询出错!",
						Toast.LENGTH_SHORT).show();
			} else {
				theActivity.setAdapter(theActivity.getItems(cursor));
			}
		}
	}

	// 获取数据
	private void getDataFromDB() {
		ContentResolver contentResolver = getMyActivity().getContentResolver();
		// 获取全部提醒信息
		myAlarmAscHelper = new AlarmAscHelper(contentResolver);
		myAlarmAscHelper.myHandler = new GetDataHandler(this);
		Thread myThread = new Thread(myAlarmAscHelper);
		showProgressDialog("查询中", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(getMyActivity(), "exception:" + e.toString(),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 更新View
		// this.adapter.notifyDataSetChanged();
		getDataFromDB();
	}

	private class AdapterOnClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			if (bShowCheckBox) {
				// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
				ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) v
						.getTag();
				// 调整选定条目
				if (holder.cb.isChecked() == true) {
					list.get(position).put(ALARM_FLAG, "false");
					checkNum--;
				} else {
					list.get(position).put(ALARM_FLAG, "true");
					checkNum++;
				}
				// 改变CheckBox的状态
				holder.cb.toggle();
				// 用TextView显示
				tv_show.setText("已选中" + checkNum + "项");
			} else {
				ContentValues selectedItem = (ContentValues) (list
						.get(position));
				Intent intent = new Intent();
				intent.setClass(getMyActivity(), SetAlarm.class);
				intent.putExtra(Alarms.ALARM_ID,
						(Integer) selectedItem.get(ALARM_ID));
				// 启动下一个Activity
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.zoom_enter,
						R.anim.zoom_exit);
			}
		}
	}
}
