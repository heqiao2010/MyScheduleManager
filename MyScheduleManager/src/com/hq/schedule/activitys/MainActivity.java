package com.hq.schedule.activitys;

import com.hq.schedule.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class MainActivity extends SlidingFragmentActivity {

	public static final String PREFERENCES = "MySchedulePreferences";
	public static CalendarView mainFragment = null;
	public static ReminderListView leftFragment = null;
	public static RightSetting rightFragment = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置标题
//		setTitle("日程管理1.1");
		// 初始化滑动菜单
		initSlidingMenu(savedInstanceState);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu(Bundle savedInstanceState) {
		// 设置滑动菜单的属性值
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadow);
		getSlidingMenu().setBehindOffsetRes(R.dimen.slidingmenu_offset);
		getSlidingMenu().setFadeDegree(0.35f);

		// 设置主界面的视图
		setContentView(R.layout.main_frame);
		mainFragment = new CalendarView();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_frame, mainFragment).commit();

		// 设置滑动菜单的右视图界面
		setBehindContentView(R.layout.right_frame);
		rightFragment = new RightSetting();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.left_frame, rightFragment).commit();

		// 设置滑动菜单的左视图界面
		getSlidingMenu().setSecondaryMenu(R.layout.left_frame);
		leftFragment = new ReminderListView();
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.right_frame, leftFragment).commit();

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.main, menu);
//		return super.onCreateOptionsMenu(menu);
//	}

	/**
	 * 菜单按钮点击事件，通过点击ActionBar的Home图标按钮来打开滑动菜单
	 */
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.select_date: // 点击选择日期按钮
//			Calendar c = Calendar.getInstance();
//			new MonthYearDateSlider(this, mainFragment.getmDateSetListener(),
//					c, SpecialCalendar.getMinDate(), SpecialCalendar.getMaxDate()).show();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void finish() {
		showExitDialog();
	}

	public void exit() {
		super.finish();
	}

	private void showExitDialog() {
		new AlertDialog.Builder(this).setTitle("您真的要退出吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						exit();
					}
				}).setNegativeButton("取消", null).show();
	}
}
