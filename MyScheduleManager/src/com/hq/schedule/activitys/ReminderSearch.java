package com.hq.schedule.activitys;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.alarm.SetAlarm;
import com.hq.schedule.category.Category;
import com.hq.schedule.category.Categorys;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ReminderSearch extends Activity implements
		DateSlider.OnDateSetListener {
	private BaseAdapter adapter;
	private static final String ALARM_ID = "alarm_id";
	private static final String ALARM_TIME = "alarm_time";
	private static final String ALARM_DATE = "alarm_date";
	private static final String ALARM_LABEL = "alarm_label";
	private static final String ALARM_FLAG = "flag";
	private Button bt_selectall;
	private Button bt_cancel;
	private Button bt_deselectall;
	private Button bt_confirmdelete;
	private int checkNum; // 记录选中的条目数量
	private TextView tv_show;// 用于显示选中的条目数量
	private Boolean bShowCheckBox = false; // 是否显示CheckBox,是否进入删除模式
	private RelativeLayout delete_chooser_bar = null; // 删除按钮组
	private RelativeLayout reminder_search_edit_bar = null; // 搜索框组件组
	private RelativeLayout customize_search_bar = null; // 自定义搜索组件组
	private ImageView show_custimize_searcher_iv = null; // 显示高级搜索的imageview
	private Boolean bShowCumstomizePanel = false; // 是否显示自定义搜索栏
	private Spinner category_spinner = null; // 选择分类
	private CheckBox enable_checkbox = null; // 是否启用
	private EditText search_edtx = null; // 搜索框
	private ImageView text_deleter = null; // 搜索框中的删除图标
	private TextView bgtime_chooser = null; // 开始事件选择标签
	private TextView endtime_chooser = null; // 结束事件选择标签
	private int time_chooser = 1; // 1:begin, 2: end
	private ListView result_listView = null; // 查询结果列表
	private long bgtime = 0; // 开始时间，秒数
	private long endtime = 0; // 结束时间，秒数
	private int category_id = -1; // 选中的日程类型ID, -1表示这个条件不启用
	private Boolean bEnable = true; // 日程是否启用， 0：不启用， 1：启用，-1：该条件不启用
	private List<ContentValues> list = null;
	private DateTimeSlider mDateTimeSlider = null;
	private Calendar temC = null;
	private List<ContentValues> category_data = null;
	private ArrayAdapter<String> spinnerAdapter = null;
	private final static String START_TIME_TAG = "起始时间";
	private final static String END_TIME_TAG = "结束时间";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminder_search);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		temC = Calendar.getInstance();
		initView();
		setListener();
		initCategorySpinner();
		searchFromDB();
	}

	private void initView() {
		category_spinner = (Spinner) findViewById(R.id.category_spinner);
		enable_checkbox = (CheckBox) findViewById(R.id.reminder_enabled_checkbox);
		search_edtx = (EditText) findViewById(R.id.reminder_search_edtx);
		search_edtx.setText("");
		search_edtx.setHint("请输入查询字符");
		text_deleter = (ImageView) findViewById(R.id.text_deleter);
		text_deleter.setVisibility(View.INVISIBLE);
		bgtime_chooser = (TextView) findViewById(R.id.begin_time_chooser);
		bgtime_chooser.setText(START_TIME_TAG);
		endtime_chooser = (TextView) findViewById(R.id.end_time_chooser);
		endtime_chooser.setText(END_TIME_TAG);
		result_listView = (ListView) findViewById(R.id.search_result_listview);
		mDateTimeSlider = new DateTimeSlider(this, this, temC);
		// 删除组件
		bt_selectall = (Button) findViewById(R.id.reminder_search_bt_selectall);
		bt_cancel = (Button) findViewById(R.id.reminder_search_bt_cancelselectall);
		bt_deselectall = (Button) findViewById(R.id.reminder_search_bt_deselectall);
		bt_confirmdelete = (Button) findViewById(R.id.reminder_search_bt_confirmdelete);
		// 显示选中信息
		tv_show = (TextView) findViewById(R.id.reminder_search_show_delete_count_tv);
		tv_show.setText("已选中" + checkNum + "项.");
		// 删除按钮组
		delete_chooser_bar = (RelativeLayout) findViewById(R.id.reminder_search_delete_choose_bar);
		// 搜索框组件组
		reminder_search_edit_bar = (RelativeLayout) findViewById(R.id.reminder_search_edit_bar);
		// 自定义搜索组件组
		customize_search_bar = (RelativeLayout) findViewById(R.id.customize_search_chooser_bar);
		// 显示高级搜索的imageview
		show_custimize_searcher_iv = (ImageView) findViewById(R.id.show_custimize_searcher_iv);
	}

	private List<ContentValues> getCategoryData() {
		if (null == category_data) {
			category_data = new ArrayList<ContentValues>();
			Cursor cursor = Categorys.getCategorysCursor(getContentResolver());
			try {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					ContentValues item = new ContentValues();
					Category category = new Category(cursor);
					item.put(Category.ID, category.id);
					item.put(Category.CATEGORY_NAME, category.category_name);
					item.put(Category.PRIORITY_LEVEL, category.priority_level);
					category_data.add(item);
				}
			} catch (Exception e) {
				Log.e("main", "In getCategoryDate,异常：" + e.toString());
			} finally {
				if (null != cursor) {
					cursor.close();
				}
			}
		}
		return category_data;
	}

	/**
	 * 通过category_data获取所有的日程分类名称，注意： 在返回的分类名称数组中，第一个元素为"全部"，从第
	 * 1到n个才是真正的分类名，也就是说，返回的长度比 category_data数组长度大一。
	 * 
	 * @param category_data
	 * @return String[]
	 */
	private String[] getCategoryNames(List<ContentValues> category_data) {
		String[] category_names = new String[category_data.size() + 1];
		category_names[0] = "全部";
		for (int i = 0; i < category_data.size(); i++) {
			category_names[i + 1] = (String) category_data.get(i).get(
					Category.CATEGORY_NAME);
		}
		return category_names;
	}

	private void initCategorySpinner() {
		spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				getCategoryNames(getCategoryData()));
		category_spinner.setAdapter(spinnerAdapter);
		category_spinner.setSelection(0);
		category_spinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
						// 获取选中的Category ID
						if (0 == position) {
							category_id = -1;
						} else {
							category_id = (Integer) getCategoryData().get(
									position - 1).get(Category.ID);
						}
						searchFromDB();
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO 什么都没有选中的时候？

					}
				});
	}

	private void dataChanged() {
		// 通知listView刷新
		setAdapter(list);
		// TextView显示最新的选中数目
		tv_show.setText("已选中" + checkNum + "项.");
	}

	private class DeleteButtonsListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.reminder_search_bt_selectall: // 全选
				int listSize = list.size();
				for (int i = 0; i < listSize; i++) {
					list.get(i).put(ALARM_FLAG, "true");
				}
				checkNum = listSize;
				dataChanged();
				break;
			case R.id.reminder_search_bt_deselectall: // 反选
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
				break;
			case R.id.reminder_search_bt_cancelselectall: // 取消
				for (int i = 0; i < list.size(); i++) {
					list.get(i).put(ALARM_FLAG, "false");
				}
				checkNum = 0;
				dataChanged();
				break;
			case R.id.reminder_search_bt_confirmdelete: // 确定选择
				if (0 == checkNum) {
					Toast.makeText(ReminderSearch.this, "请选中要删除的项目.",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					new AlertDialog.Builder(ReminderSearch.this)
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
				break;
			}
		}
	}

	private void deleteChoosenReminder() {
		Iterator<ContentValues> iterator = list.iterator();
		while (iterator.hasNext()) {
			ContentValues temp = iterator.next();
			if (temp.get(ALARM_FLAG).equals("true")) {
				int alarm_id = Integer.parseInt(temp.get(ALARM_ID).toString());
				Alarms.deleteAlarm(this, alarm_id);
				iterator.remove();
			}
		}
		Toast.makeText(this, "已删除" + checkNum + "项", Toast.LENGTH_SHORT).show();
		checkNum = 0;
		dataChanged();
	}

	private void setListener() {
		result_listView.setOnItemClickListener(new AdapterOnClickListener());
		result_listView
				.setOnItemLongClickListener(new MyOnItemLongClickListener());
		DeleteButtonsListener mDeleteButtonsListener = new DeleteButtonsListener();
		bt_selectall.setOnClickListener(mDeleteButtonsListener);
		bt_cancel.setOnClickListener(mDeleteButtonsListener);
		bt_deselectall.setOnClickListener(mDeleteButtonsListener);
		bt_confirmdelete.setOnClickListener(mDeleteButtonsListener);
		enable_checkbox.setChecked(true);
		enable_checkbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundBtn,
							boolean b) {
						// 获取是否启用条件
						bEnable = b;
						searchFromDB();
					}
				});
		text_deleter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				search_edtx.setText("");
				text_deleter.setVisibility(View.INVISIBLE);
			}
		});
		search_edtx.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (!"".equals(search_edtx.getText().toString())) {
					text_deleter.setVisibility(View.VISIBLE);
				}
				searchFromDB();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
		});
		bgtime_chooser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDateTimeSlider.show();
				time_chooser = 1;
			}
		});
		bgtime_chooser.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				bgtime_chooser.setText(START_TIME_TAG);
				bgtime = 0;
				return true;
			}
		});
		endtime_chooser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDateTimeSlider.show();
				time_chooser = 2;
			}
		});
		endtime_chooser.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				endtime_chooser.setText(END_TIME_TAG);
				endtime = 0;
				searchFromDB();
				return true;
			}
		});
		show_custimize_searcher_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (customize_search_bar.getVisibility() == View.VISIBLE) {
					bShowCumstomizePanel = false;
					customize_search_bar.setVisibility(View.GONE);
					show_custimize_searcher_iv
							.setBackgroundResource(R.drawable.downward);
					searchFromDB();
				} else {
					bShowCumstomizePanel = true;
					customize_search_bar.setVisibility(View.VISIBLE);
					show_custimize_searcher_iv
							.setBackgroundResource(R.drawable.upward);
					searchFromDB();
				}
			}
		});
	}

	private String formatDate(int year, int month, int day) {
		return year + getString(R.string.the_year) + month
				+ getString(R.string.the_month) + day
				+ getString(R.string.the_day);
	}

	private void setAdapter(List<ContentValues> list) {
		adapter = new ListAdapter(this, list);
		result_listView.setAdapter(adapter);
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

	/**
	 * 获取查询的where条件
	 * 
	 * @return String
	 */
	private String getWhereStr() {
		String msgTag = search_edtx.getText().toString();
		String where = "";
		if (!"".equals(msgTag)) {
			where += "(" + Alarm.Columns.MESSAGE + " like '%" + msgTag
					+ "%' or " + Alarm.Columns.SORT_KEY + " like '"
					+ msgTag.trim() + "%' )";
		}
		if (bShowCumstomizePanel) {
			if (0 != bgtime) {
				where += "".equals(where) ? "(" + Alarm.Columns.ALARM_TIME
						+ " > " + bgtime + ") " : " and ("
						+ Alarm.Columns.ALARM_TIME + " > " + bgtime + ") ";
			}
			if (0 != endtime) {
				where += "".equals(where) ? "(" + Alarm.Columns.ALARM_TIME
						+ " < " + endtime + ") " : " and ("
						+ Alarm.Columns.ALARM_TIME + " < " + endtime + ") ";
			}
			if (-1 != category_id) {
				where += "".equals(where) ? "(" + Alarm.Columns.CATEGORY
						+ " = " + category_id + ") " : " and ("
						+ Alarm.Columns.CATEGORY + " = " + category_id + ") ";
			}
			where += "".equals(where) ? "(" + Alarm.Columns.ENABLED + " = "
					+ (bEnable ? 1 : 0) + ") " : " and ("
					+ Alarm.Columns.ENABLED + " = " + (bEnable ? 1 : 0) + ") ";
		}
		Log.i("main", "getWhereStr where: " + where);
		return where;
	}

	private void searchFromDB() {
		ContentResolver contentResolver = getContentResolver();
		String where = getWhereStr();
		if ("".equals(where)) {
			where = " 1==2 ";
		}
		Cursor cursor = Alarms.getAlarmsCursorByWhere(where, contentResolver);
		Log.i("main", "cusor count:" + cursor.getCount());
		getItems(cursor);
		Log.i("main", "Items count:" + list.size());
		setAdapter(list);
	}

	private class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<ContentValues> list;

		public ListAdapter(Context context, List<ContentValues> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
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
			holder.alpha.setVisibility(View.GONE);
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

	class MyOnItemLongClickListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			ContentValues item = list.get(position);
			int alarm_id = (Integer) item.get(ALARM_ID);
			String label = item.getAsString(ALARM_LABEL);
			if( 4 < label.length() ){
				label = label.substring(0, 5);
				label += "...";
			}
			showDeleteAlarm(alarm_id, label);
			return false;
		}
	}

	private void showDeleteAlarm(final int alarm_id, final String label) {
		new AlertDialog.Builder(this).setTitle("删除" + label + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Alarms.deleteAlarm(ReminderSearch.this, alarm_id);
						Toast.makeText(ReminderSearch.this, label + "成功删除",
								Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("取消", null).show();
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
				ContentValues selectedItem = list.get(position);
				Intent intent = new Intent();
				intent.setClass(ReminderSearch.this, SetAlarm.class);
				intent.putExtra(Alarms.ALARM_ID,
						(Integer) selectedItem.get("alarm_id"));
				startActivity(intent);
				overridePendingTransition(R.anim.zoom_enter,
						R.anim.zoom_exit);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDateSet(DateSlider view, Calendar selectedDate) {
		temC = selectedDate;
		String dateStr = String.format(Locale.CHINESE, " %te/%tm/%ty %tH:%02d",
				temC, temC, temC, temC, temC.get(Calendar.MINUTE));
		if (1 == time_chooser) {
			bgtime_chooser.setText(dateStr);
			bgtime = temC.getTimeInMillis();
		} else {
			endtime_chooser.setText(dateStr);
			endtime = temC.getTimeInMillis();
		}
		searchFromDB();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
			return true;
		case R.id.delete_reminder:
			if (delete_chooser_bar.getVisibility() == View.VISIBLE) { // 已显示删除框
				item.setIcon(this.getResources().getDrawable(R.drawable.list_uncheck));
				// 隐藏删除框，显示其他框
				delete_chooser_bar.setVisibility(View.GONE);
				reminder_search_edit_bar.setVisibility(View.VISIBLE);
				show_custimize_searcher_iv.setVisibility(View.VISIBLE);
				if (bShowCumstomizePanel) {
					customize_search_bar.setVisibility(View.VISIBLE);
				} else {
					customize_search_bar.setVisibility(View.GONE);
				}
				bShowCheckBox = false;
				this.setAdapter(list);
			} else { // 未显示删除框
				// 显示删除框，隐藏其他框
				item.setIcon(this.getResources().getDrawable(R.drawable.list_check));
				delete_chooser_bar.setVisibility(View.VISIBLE);
				reminder_search_edit_bar.setVisibility(View.GONE);
				show_custimize_searcher_iv.setVisibility(View.GONE);
				customize_search_bar.setVisibility(View.GONE);
				bShowCheckBox = true;
				this.setAdapter(list);
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
	}
}
