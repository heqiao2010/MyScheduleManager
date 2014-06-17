package com.hq.schedule.activitys;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.MonthYearDateSlider;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.calendartools.SpecialCalendar;
import com.hq.schedule.utility.SharedPreferenceHelper;
import com.hq.schedule.views.OverLay;
import com.hq.schedule.views.TimePicker;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class CalendarView extends Fragment implements OnGestureListener {
	public static final int CALENDARVIEW_REQUEST_CODE = 111; // 向子Activity发送的请求值
	public static final int RESULT_CANCELL = 222; // 表示子Activity取消
	public static final int RESULT_REMINDER_ALTER = 333; // 子Activity改变了闹钟
	private static final int SWIPE_MIN_DISTANCE = 60;
	private static final int SWIPE_THRESHOLD_VELOCITY = 180;
	private static final int OVERLAY_SHOW_TIME = 1000;
	private static final int OVERLAY_TEXT_SIZE = 40;
	public static ViewFlipper viewFlipper = null;
	private DateSlider.OnDateSetListener mDateSetListener = null; // 日期改变的监听者
	private static int jumpMonth = 0;
	private static int jumpYear = 0;
	private static Handler mHandler;
	private static Boolean bShowNext = false;
	private int currentYear = 0;
	private int currentMonth = 0;
	private int currentDay = 0;
	private String currentDate = "";
	private int windowWidth = 480; // 屏幕宽度
	private int windowHeight = 800; // 屏幕高度
	private TimePicker mTimePicker; // 获取时间的组件
	private Button pointer_btn; // 触发显示可拖动组件的按钮
	private TextView pointer_bg; // 可拖动组件中显示大头针图标的TextView
	private TextView pointer_info; // 可拖动组件中显示提示信息的TextView
	private LinearLayout floating_widget_layout; // 可拖动的组件
	private LinearLayout calendar_view_layout; // calendar view layout
	private RelativeLayout calendar_view_main_container_layout; //
	private EditText edtx;
	private GridView currentGridView = null; // 记录当前GridView
	private CalendarAdapter currentAdapter = null;
	private Animation pre_HideAnimation = null;
	private Animation pre_ShowAnimation = null;
	private Animation nex_HideAnimation = null;
	private Animation nex_ShowAnimation = null;
	private GestureDetector gestureDetector = null;
	private int maxJumpMonth = 0;
	private int minJumpMonth = 0;
	private Button show_list_btn = null;
	private Button show_setting_btn = null;
	private TextView title_tv = null;
	// 选中信息（Y，M，D，H，m，index），index表示选中的是grid中的第几个子View
	private int[] select_info = new int[7];

	public CalendarView() {
		super();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d",
				Locale.getDefault());
		currentDate = sdf.format(date);
		currentYear = Integer.parseInt(currentDate.split("-")[0]);
		currentMonth = Integer.parseInt(currentDate.split("-")[1]);
		currentDay = Integer.parseInt(currentDate.split("-")[2]);
	}

	private void getMaxAndMinJumpMonth() {
		maxJumpMonth = getJumpMonthByDate(SpecialCalendar.getMaxDate());
		minJumpMonth = getJumpMonthByDate(SpecialCalendar.getMinDate());
	}

	public void getWindowSize(int windowWidth, int windowHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}

	class TimePickerOnChangeListener implements TimePicker.OnChangeListener {
		@Override
		public void onChange(int hour, int minute) {
			Log.e("main", "TimePicker:" + hour + ":" + minute);
			select_info[3] = hour;
			select_info[4] = minute;
		}
	}

	private int getJumpMonthByDate(Calendar c) {
		int xYear = (c.get(Calendar.YEAR) - currentYear);
		int xMonth = (c.get(Calendar.MONTH) - currentMonth + 1);
		return xYear * 12 + xMonth;
	}

	/**
	 * 比较两个日期的大小，如果1>2,返回1,如果1<2返回-1,相等返回0
	 * 
	 * @param year1
	 * @param month1
	 * @param year2
	 * @param month2
	 * @return INT 0：equal 1:bigger -1:smaller
	 */
	private int dateComp(int year1, int month1, int year2, int month2) {
		if (year1 == year2) {
			if (month1 == month2) {
				return 0;
			} else if (month1 > month2) {
				return 1;
			} else {
				return -1;
			}
		} else if (year1 > year2) {
			return 1;
		} else {
			return -1;
		}
	}

	// 设置按钮的监听事件
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getMaxAndMinJumpMonth(); // 获得JumpMonth的最值
		mDateSetListener = new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
				// update the dateText view with the corresponding date
				int compCode = dateComp(selectedDate.get(Calendar.YEAR),
						selectedDate.get(Calendar.MONTH) + 1,
						Integer.parseInt(currentAdapter.getShowYear()),
						Integer.parseInt(currentAdapter.getShowMonth()));
				jumpMonth = getJumpMonthByDate(selectedDate);
				if (0 == compCode) {
					return; // 如果选择日期，就是当前月，则不做任何事
				} else if (1 == compCode) {
					showPreviousMonths();
				} else { // -1
					showNextMonths();
				}
			}
		};
		DisplayMetrics dm = getResources().getDisplayMetrics();
		getWindowSize(dm.widthPixels, dm.heightPixels);
		if (null == pointer_btn) {
			Log.e("main", "In onActivityCreated, pointer button is null...");
			return;
		}
		pointer_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String editStr = edtx.getText().toString();
				if (null == editStr || "".equals(editStr)) {
					Toast.makeText(getActivity(), "请输入日程信息.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				pointer_info.setText(editStr); // 获取输入框中的文本信息
				pointer_btn.setVisibility(View.INVISIBLE);
				edtx.setVisibility(View.INVISIBLE);
				floating_widget_layout.setVisibility(View.VISIBLE);
				calendar_view_main_container_layout
						.bringChildToFront(floating_widget_layout);
				floating_widget_layout
						.setOnTouchListener(new MyOnTouchListener(windowWidth,
								windowHeight));
				// pointer_info.setVisibility(View.VISIBLE);
				// pointer_bg.setVisibility(View.VISIBLE);
				// floating_widget_layout.layout(pointer_btn.getLeft(),
				// pointer_btn.getTop(),
				// edtx.getRight(), pointer_btn.getBottom()); // (40X160)
			}
		});

		edtx.setOnLongClickListener(new OnLongClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean onLongClick(View v) {
				ViewGroup.LayoutParams lp = edtx.getLayoutParams();
				if (pointer_btn.getVisibility() == View.VISIBLE
						&& mTimePicker.getVisibility() == View.VISIBLE) {
					pointer_btn.setVisibility(View.GONE);
					mTimePicker.setVisibility(View.GONE);
					lp.width = ViewGroup.LayoutParams.FILL_PARENT;
					edtx.setLayoutParams(lp);
					Log.e("main", edtx.getWidth() + "");
				} else {
					pointer_btn.setVisibility(View.VISIBLE);
					mTimePicker.setVisibility(View.VISIBLE);
					lp.width = 180;
					edtx.setLayoutParams(lp);
				}
				return false;
			}
		});
		show_list_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SlidingFragmentActivity mActivity = (SlidingFragmentActivity) getActivity();
				mActivity.toggle();
			}
		});
		show_setting_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SlidingFragmentActivity mActivity = (SlidingFragmentActivity) getActivity();
				mActivity.showSecondaryMenu();
			}
		});
		title_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Calendar c = Calendar.getInstance();
				new MonthYearDateSlider(getActivity(), mDateSetListener, c,
						SpecialCalendar.getMinDate(), SpecialCalendar
								.getMaxDate()).show();
			}
		});
		setAnimation(SharedPreferenceHelper.getAnimationType(getActivity()));
		mHandler = new ShowGridViewHandler(this);
	}

	static class ShowGridViewHandler extends Handler {
		WeakReference<CalendarView> mActivity;

		ShowGridViewHandler(CalendarView activity) {
			mActivity = new WeakReference<CalendarView>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CalendarView theActivity = mActivity.get();
			// 显示计算GridView，显示动画
			if (CalendarView.bShowNext) {
				theActivity.showNextMonths();
			} else {
				theActivity.showPreviousMonths();
			}
		}
	}

	/**
	 * 设置动画效果 type
	 * 
	 * @param type
	 *            0-9
	 */
	public void setAnimation(int type) {
		// 6,5,3,1
		switch (type) {
		case 1:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.fade);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.hold);
			break;
		case 2:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_scale_action);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_alpha_action);
			break;
		case 3:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.scale_rotate);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_alpha_action);
			break;
		case 4:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.scale_translate_rotate);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_alpha_action);
			break;
		case 5:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.scale_translate);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_alpha_action);
			break;
		case 6:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.hyperspace_in);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.hyperspace_out);
			break;
		case 7:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.wave_scale);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.my_alpha_action);
			break;
		case 8:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.zoom_enter);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.zoom_exit);
			break;
		case 9:
			nex_HideAnimation = pre_HideAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.slide_up_in);
			nex_ShowAnimation = pre_ShowAnimation = AnimationUtils
					.loadAnimation(getActivity(), R.anim.slide_down_out);
			break;
		default:
			nex_HideAnimation = AnimationUtils.loadAnimation(getActivity(),
					R.anim.push_down_out);
			nex_ShowAnimation = AnimationUtils.loadAnimation(getActivity(),
					R.anim.push_down_in);
			pre_HideAnimation = AnimationUtils.loadAnimation(getActivity(),
					R.anim.push_up_out);
			pre_ShowAnimation = AnimationUtils.loadAnimation(getActivity(),
					R.anim.push_up_in);
		}
	}

	// 可拖动控件的Touch的监听者
	class MyOnTouchListener implements OnTouchListener {
		int screenWidth = 480, screenHeight = 800;
		int lastX, lastY;

		public MyOnTouchListener(int screenWidth, int screenHeight) {
			this.screenHeight = screenHeight;
			this.screenWidth = screenWidth;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int ea = event.getActionMasked();
			switch (ea) {
			case MotionEvent.ACTION_DOWN:
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			/**
			 * layout(l,t,r,b) l Left position, relative to parent t Top
			 * position, relative to parent r Right position, relative to parent
			 * b Bottom position, relative to parent
			 * */
			case MotionEvent.ACTION_MOVE:
				int dx = (int) event.getRawX() - lastX;
				int dy = (int) event.getRawY() - lastY;
				int left = v.getLeft() + dx;
				int top = v.getTop() + dy;
				int right = v.getRight() + dx;
				int bottom = v.getBottom() + dy;
				if (left < 0) {
					left = 0;
					right = left + v.getWidth();
				}
				if (right > screenWidth + 230) {
					right = screenWidth + 230;
					left = right - v.getWidth();
				}
				if (top < 0) {
					top = 0;
					bottom = top + v.getHeight();
				}
				if (bottom > screenHeight - 125) { //screenHeight - 125计算日历高度
					bottom = screenHeight - 125;
					top = bottom - v.getHeight();
				}
				v.layout(left, top, right, bottom);
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:
				// 获得这个View的中心点
				int l = v.getLeft();
				int t = v.getTop();
				int b = v.getBottom();
				getSelectDate(l, t, b);
				pointer_btn.setVisibility(View.VISIBLE);
				edtx.setVisibility(View.VISIBLE);
				floating_widget_layout.setVisibility(View.GONE);
				break;
			}
			return true; // consume the event
		}
	}

	private void getSelectDate(int l, int t, int b) {
		// 获取viewPager的top值
		// int fl = this.viewFlipper.getLeft();
		int ft = viewFlipper.getTop();
		// int fr = this.viewFlipper.getRight();
		// int fb = this.viewFlipper.getBottom();
		if (t + b / 2 < ft) { // 说明没有拖拽至日历框内部
			return;
		}
		// 将传入的控件位置转化到gridView的同一个坐标系下
		t -= ft;
		b -= ft;
		// 算出中心点坐标
		int x = l + pointer_bg.getWidth() / 2;
		int y = (t + b) / 2;

		// 获取gridView的四个角的坐标
		int gl = CalendarView.viewFlipper.getLeft();
		int gt = CalendarView.viewFlipper.getTop();
		int gr = CalendarView.viewFlipper.getRight();
		int gb = CalendarView.viewFlipper.getBottom();

		// Toast.makeText(getActivity(), fl+","+ft+","+fr+","+fb,
		// Toast.LENGTH_SHORT).show();
		// 计算gridView中每个方块的宽度(gridView 6X7)
		int wm = (gr - gl) / 7;
		// 计算gridView中每个方块的高度(gridView 6X7)
		int hm = (gb - gt) / 6;
		int index = 0; // 日期位置
		index = (y / hm) * 7 + (x / wm);
		if (null == currentAdapter) {
			Log.e("main", "Can't get Adapter of GridView");
			return;
		}
		int imonth = Integer.parseInt(currentAdapter.getShowMonth());
		int iyear = Integer.parseInt(currentAdapter.getShowYear());
		if (0 <= index && index < 42) {
			if (index < currentAdapter.getStartPositon()) {
				imonth--;
				if (imonth <= 0) {
					imonth += 12;
					iyear--;
				}
			} else if (index > currentAdapter.getEndPosition()) {
				imonth++;
				if (imonth > 12) {
					imonth -= 12;
					iyear++;
				}
			}
			String tmpStr[] = currentAdapter.getDateByClickItem(index).split(
					"\\.");
			// 选中的日期
			select_info[0] = iyear;
			select_info[1] = imonth - 1; // 在公历Calendar中，月份从0开始
			select_info[2] = Integer.parseInt(tmpStr[0]);
			select_info[6] = index;
		}
		new AlertDialog.Builder(getActivity())
				.setTitle(
						"定时到" + select_info[0] + "年" + (select_info[1] + 1)
								+ "月" + select_info[2] + "日" + select_info[3]
								+ ":" + select_info[4] + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Calendar c = Calendar.getInstance();
						c.set(Calendar.YEAR, select_info[0]);
						c.set(Calendar.MONTH, select_info[1]);
						c.set(Calendar.DAY_OF_MONTH, select_info[2]);
						c.set(Calendar.HOUR_OF_DAY, select_info[3]);
						c.set(Calendar.MINUTE, select_info[4]);
						setAlarmClock(c);
						edtx.setText("");
						// 更新GridView
						updateView(select_info[6]);
					}
				}).setNegativeButton("取消", null).show();

	}

	// set a alarm
	private void setAlarmClock(Calendar c) {
		Alarm alarm = new Alarm();
		alarm.year = c.get(Calendar.YEAR);
		alarm.month = c.get(Calendar.MONTH) + 1; // calendar month： 0-11
		alarm.day = c.get(Calendar.DAY_OF_MONTH);
		alarm.hour = c.get(Calendar.HOUR_OF_DAY);
		alarm.minutes = c.get(Calendar.MINUTE);
		alarm.time = c.getTimeInMillis();
		alarm.vibrate = true;
		alarm.enabled = true;
		String reminderStr = edtx.getText().toString();
		alarm.label = "".equals(reminderStr) ? getString(R.string.empty_reminder_inf)
				: reminderStr;
		Alarms.addAlarm(getActivity(), alarm);
		Log.e("main", "d:" + c.toString());
		Log.e("main", "d.getTime():" + c.getTimeInMillis()
				+ "System.currentTimeMillis():" + System.currentTimeMillis());
		Toast.makeText(
				getActivity(),
				"已经定时到：" + c.get(Calendar.YEAR) + "年"
						+ (c.get(Calendar.MONTH) + 1) + "月"
						+ c.get(Calendar.DAY_OF_MONTH) + "日"
						+ c.get(Calendar.HOUR_OF_DAY) + "时"
						+ c.get(Calendar.MINUTE) + "分", Toast.LENGTH_SHORT)
				.show();
	}

	public void updateView(int select_index) {
		if (null == currentAdapter) {
			Log.e("main", "When Update View, Can't get Adapter of GridView");
			return;
		} else {
			currentAdapter.notifyDataSetChanged(); // 更新当前GridView
		}
	}

	public void setTitle(String title) {
		title_tv.setText(title);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		gestureDetector = new GestureDetector(getActivity(), this);
		calendar_view_layout = (LinearLayout) inflater.inflate(
				R.layout.calendar_view, container, false);
		mTimePicker = (TimePicker) calendar_view_layout
				.findViewById(R.id.time_picker);
		TimePickerOnChangeListener timePickerOnChangeListener = new TimePickerOnChangeListener();
		Log.e("main", "CalendarView:onCreateView, OnChangeListene="
				+ timePickerOnChangeListener.toString());
		mTimePicker.setOnChangeListener(timePickerOnChangeListener);
		calendar_view_main_container_layout = (RelativeLayout) calendar_view_layout
				.findViewById(R.id.calendar_view_main_container);
		floating_widget_layout = (LinearLayout) calendar_view_layout
				.findViewById(R.id.floating_widget);
		pointer_btn = (Button) calendar_view_layout
				.findViewById(R.id.pointer_btn);
		viewFlipper = (ViewFlipper) calendar_view_layout
				.findViewById(R.id.viewFlipper);
		viewFlipper.removeAllViews();
		pointer_info = (TextView) calendar_view_layout
				.findViewById(R.id.pointer_info);
		pointer_bg = (TextView) calendar_view_layout
				.findViewById(R.id.pointer_bg);
		edtx = (EditText) calendar_view_layout.findViewById(R.id.event_edit);
		show_list_btn = (Button) calendar_view_layout
				.findViewById(R.id.show_list_btn);
		show_setting_btn = (Button) calendar_view_layout
				.findViewById(R.id.show_setting_btn);
		title_tv = (TextView) calendar_view_layout.findViewById(R.id.title_tv);
		currentAdapter = new CalendarAdapter(getActivity(), getResources(),
				jumpMonth, jumpYear, currentYear, currentMonth, currentDay);
		currentGridView = getGridView(currentAdapter);
		viewFlipper.addView(currentGridView, 0);
		setTitle(getStringOfDate());
		return calendar_view_layout;
	}

	// 获取一个gridview
	private GridView getGridView(final CalendarAdapter cadp) {
		@SuppressWarnings("deprecation")
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		GridView gridView = new GridView(getActivity());
		gridView.setNumColumns(7);
		gridView.setColumnWidth(46);
		if (this.windowWidth == 480 && this.windowHeight == 800) {
			gridView.setColumnWidth(69);
		}
		// gridView.setPadding(0, 5, 0, 0);
		gridView.setGravity(Gravity.CENTER_VERTICAL);
		gridView.setVerticalSpacing(1);
		gridView.setHorizontalSpacing(1);
		gridView.setBackgroundResource(R.drawable.gridview_bk);
		gridView.setOnTouchListener(new OnTouchListener() {
			// 将gridview中的触摸事件回传给gestureDetector
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return CalendarView.this.gestureDetector.onTouchEvent(event);
			}
		});
		gridView.requestDisallowInterceptTouchEvent(false);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			// 设置gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> adv, View v, int position,
					long arg3) {
				// 记录选中位置
				select_info[6] = position;
				// 获取日期
				int year = Integer.parseInt(cadp.getShowYear());
				int month = Integer.parseInt(cadp.getShowMonth());
				int day = Integer.parseInt(cadp.getDateByClickItem(position)
						.split("\\.")[0]);
				if (position < cadp.getStartPositon()) { // 上一个月
					month--;
				} else if (position > cadp.getEndPosition()) { // 下一个月
					month++;
				} else {
					// 什么都不做
				}
				// 跳转到显示日程信息界面
				Intent intent = new Intent();
				intent.setClass(getActivity(), ReminderList.class);
				intent.putExtra(Alarms.AlARM_TIME_YEAR, year);
				intent.putExtra(Alarms.AlARM_TIME_MONTH, month);
				intent.putExtra(Alarms.AlARM_TIME_DAY, day);
				// 启动下一个Activity,设置一个请求值111
				startActivityForResult(intent, CALENDARVIEW_REQUEST_CODE);
				getActivity().overridePendingTransition(R.anim.zoom_enter,
						R.anim.zoom_exit);
			}
		});
		gridView.requestDisallowInterceptTouchEvent(true);
		gridView.setLayoutParams(params);
		gridView.setAdapter(cadp);
		return gridView;
	}

	// 获取字符串形式的日期
	public String getStringOfDate() {
		if (null == currentAdapter) {
			Log.e("main",
					"getStringOfDate is called, currentAdapter is null...");
			return "";
		}
		StringBuffer textDate = new StringBuffer();
		textDate.append(currentAdapter.getShowYear()).append("年")
				.append(currentAdapter.getShowMonth()).append("月");
		if (!currentAdapter.getLeapMonth().equals("")
				&& currentAdapter.getLeapMonth() != null) {
			textDate.append("闰").append(currentAdapter.getLeapMonth())
					.append("月");
		}
		textDate.append(currentAdapter.getAnimalsYear()).append("年")
				.append("(").append(currentAdapter.getCyclical()).append("年)");
		return textDate.toString();
	}

	// 获取字符串形式的日期
	public String getStringOfDateByJumpMonth(int jumpMonth) {
		int year = currentYear, month = currentMonth;
		year += (jumpMonth / 12);
		month += (jumpMonth % 12);
		if (month > 12) {
			year++;
			month -= 12;
		} else if (month <= 0) {
			month += 12;
			year--;
		} else {
			// do nothing
		}
		return String.valueOf(year) + "年" + String.valueOf(month) + "月";
	}

	// private int getMonthByJumpMonth(int jumpMonth) {
	// int month = currentMonth;
	// month += (jumpMonth % 12);
	// if (month > 12) {
	// month -= 12;
	// } else if (month <= 0) {
	// month += 12;
	// }
	// return month;
	// }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CALENDARVIEW_REQUEST_CODE) // 如果一个返回的Activity的请求值为111，
		{
			// 说明是本Activity是调用者
			switch (resultCode) {
			case RESULT_REMINDER_ALTER: // 设置提醒改变了内容
				// 刷新页面
				updateView(select_info[6]);
				break;
			case RESULT_CANCELL: // 设置提醒为改变内容
				// 什么都不做
				break;
			default:
			}
		}
	}

	@Override
	public boolean onDown(MotionEvent mevent) {
		return false;
	}

	// 上翻
	private void showNextMonths() {
		currentAdapter = new CalendarAdapter(getActivity(), getResources(),
				jumpMonth, jumpYear, currentYear, currentMonth, currentDay);
		currentGridView = getGridView(currentAdapter); // 获取新的GridView对象
		viewFlipper.addView(currentGridView, 1); // 将新的GridView加入到FlipperPager中第二个位置
		viewFlipper.setInAnimation(nex_ShowAnimation);// 设置新GridView进入动画
		viewFlipper.setOutAnimation(nex_HideAnimation);// 设置原GridView退出动画
		viewFlipper.showNext();// 播放跳转动画
		viewFlipper.removeViewAt(0);// 删除FlipperPager中第一个位置的GridView(原GridView)
		setTitle(getStringOfDate()); // 更改标题为日期
	}

	// 下翻
	private void showPreviousMonths() {
		currentAdapter = new CalendarAdapter(getActivity(), getResources(),
				jumpMonth, jumpYear, currentYear, currentMonth, currentDay);
		currentGridView = getGridView(currentAdapter);
		viewFlipper.addView(currentGridView, 1);
		viewFlipper.setInAnimation(pre_ShowAnimation);
		viewFlipper.setOutAnimation(pre_HideAnimation);
		viewFlipper.showPrevious();
		viewFlipper.removeViewAt(0);
		setTitle(getStringOfDate()); // 更改标题为日期
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			// 像下滑动
			if (jumpMonth == maxJumpMonth) {
				Toast.makeText(getActivity(), "最大日期只支持到2049-12",
						Toast.LENGTH_LONG).show();
				return true;
			} else {
				bShowNext = false;
				jumpMonth++; // 下一个月
				OverLay.getOverLay(getActivity()).setOuterHandler(mHandler);
				OverLay.getOverLay(getActivity()).setOverLayTextSize(
						OVERLAY_TEXT_SIZE);
				OverLay.getOverLay(getActivity()).showOverLay(
						getStringOfDateByJumpMonth(jumpMonth),
						OVERLAY_SHOW_TIME);
				return true;
			}
		} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			// 向上滑动
			if (jumpMonth == minJumpMonth) {
				Toast.makeText(getActivity(), "最小日期只支持到1900-03",
						Toast.LENGTH_LONG).show();
				return true;
			} else {
				bShowNext = true;
				jumpMonth--; // 上一个月
				OverLay.getOverLay(getActivity()).setOuterHandler(mHandler);
				OverLay.getOverLay(getActivity()).setOverLayTextSize(
						OVERLAY_TEXT_SIZE);
				OverLay.getOverLay(getActivity()).showOverLay(
						getStringOfDateByJumpMonth(jumpMonth),
						OVERLAY_SHOW_TIME);
				return true;
			}
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public DateSlider.OnDateSetListener getmDateSetListener() {
		return mDateSetListener;
	}
}
