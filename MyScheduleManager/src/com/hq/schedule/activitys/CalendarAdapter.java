package com.hq.schedule.activitys;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.calendartools.*;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalendarAdapter extends BaseAdapter {

	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 本月的天数
	private int daysOfWeek = 0; // 本月第一天是星期几
	private int daysOfLastMonth = 0; // 上一个月的总天数
	private Context context;
	private String[] dayNumber = new String[42]; // 一个gridview中的日期存入此数组中
	private LunarCalendar lc = null;
	private String currentYear = "";
	private String currentMonth = "";
	private SimpleDateFormat sdf = null;
	private int currentFlag = -1; // 用于标记当天
	private String showYear = ""; // 用于在头部显示的年份
	private String showMonth = ""; // 用于在头部显示的月份
	private String animalsYear = "";
	private String leapMonth = ""; // 闰哪一个月
	private String cyclical = ""; // 天干地支
	// 系统当前时间
	private String sysDate = "";
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";

	public CalendarAdapter() {
		Date date = new Date();
		sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
		sysDate = sdf.format(date); // 当期日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
	}

	public CalendarAdapter(Context context, Resources rs, int jumpMonth,
			int jumpYear, int year_c, int month_c, int day_c) {
		this();
		this.context = context;
		new SpecialCalendar();
		lc = new LunarCalendar();
		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = year_c + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = year_c + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = year_c - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
		}
		currentYear = String.valueOf(stepYear); // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月
		String.valueOf(day_c);
		getCalendarAdapter(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth));
	}

	public CalendarAdapter(Context context, Resources rs, int year, int month,
			int day) {
		this();
		this.context = context;
		new SpecialCalendar();
		lc = new LunarCalendar();
		currentYear = String.valueOf(year);
		// 得到跳转到的年份
		currentMonth = String.valueOf(month); // 得到跳转到的月份
		String.valueOf(day);
		getCalendarAdapter(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth));
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendarAdapter(int year, int month) {
		isLeapyear = SpecialCalendar.isLeapYear(year); // 是否为闰年
		daysOfMonth = SpecialCalendar.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		daysOfWeek = SpecialCalendar.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		daysOfLastMonth = SpecialCalendar.getDaysOfMonth(isLeapyear,
				0 == month - 1 ? 12 : month - 1); // 上一个月的总天数
		Log.d("main", isLeapyear + " ======  " + daysOfMonth
				+ "  ============  " + daysOfWeek + "  =========   "
				+ daysOfLastMonth);
		getDayTitles(year, month);
	}

	// 将一个月中的每一天的值添加入数组dayNuber中
	private void getDayTitles(int year, int month) {
		String lunarDay = "";
		for (int i = 0; i < dayNumber.length; i++) {
			if (i < daysOfWeek) { // 前一个月
				int temp = daysOfLastMonth - daysOfWeek + 1;
				lunarDay = lc.getLunarDate(year, month - 1, temp + i, false);
				dayNumber[i] = (temp + i) + "." + lunarDay;
			} else if (i < daysOfMonth + daysOfWeek) { // 本月
				String day = String.valueOf(i - daysOfWeek + 1); // 得到的日期
				lunarDay = lc.getLunarDate(year, month, i - daysOfWeek + 1,
						false);
				dayNumber[i] = i - daysOfWeek + 1 + "." + lunarDay;
				// 对于当前月才去标记当前日期
				if (sys_year.equals(String.valueOf(year))
						&& sys_month.equals(String.valueOf(month))
						&& sys_day.equals(day)) {
					// 笔记当前日期
					currentFlag = i;
				}
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
				setAnimalsYear(lc.animalsYear(year));
				setLeapMonth(lc.leapMonth == 0 ? "" : String
						.valueOf(lc.leapMonth));
				setCyclical(lc.cyclical(year));
			} else { // 下一个月
				int iday = i - daysOfMonth - daysOfWeek + 1;
				lunarDay = lc.getLunarDate(year, month + 1, iday, false);
				dayNumber[i] = iday + "." + lunarDay;
			}
		}

	}

	/**
	 * 点击每一个item时返回item中的日期
	 * 
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position) {
		return dayNumber[position];
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return daysOfWeek;
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return (daysOfWeek + daysOfMonth) - 1;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

	public String getAnimalsYear() {
		return animalsYear;
	}

	public void setAnimalsYear(String animalsYear) {
		this.animalsYear = animalsYear;
	}

	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}

	public String getCyclical() {
		return cyclical;
	}

	public void setCyclical(String cyclical) {
		this.cyclical = cyclical;
	}

	public int getDaysOfMonth() {
		return daysOfMonth;
	}

	public void setDaysOfMonth(int daysOfMonth) {
		this.daysOfMonth = daysOfMonth;
	}

	public int getDayOfWeek() {
		return daysOfWeek;
	}

	public void setDayOfWeek(int daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}

	@Override
	public int getCount() {
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// 通过postion：gridview中的位置，来确定这个位置的日期
	public Calendar getDateByPosition(int position) {
		// 获取日期
		int year = Integer.parseInt(this.showYear);
		int month = Integer.parseInt(this.showMonth);
		int day = Integer.parseInt(dayNumber[position].split("\\.")[0]);
		if (position < getStartPositon()) { // 上一个月
			month = (0 == month - 1) ? 12 : month - 1;
		} else if (position > getEndPosition()) { // 下一个月
			month = (13 == month + 1) ? 1 : month + 1;
		} else {
			// 什么都不做
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1); // Calendar month: 0-11
		c.set(Calendar.DAY_OF_MONTH, day);
		return c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.convert_view, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.tvtext);
		String d = dayNumber[position].split("\\.")[0];
		String dv = dayNumber[position].split("\\.")[1];
		SpannableString sp = new SpannableString(d + "\n" + dv);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
				d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f), 0, d.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (dv != null || dv != "") {
			sp.setSpan(new RelativeSizeSpan(0.75f), d.length() + 1,
					dayNumber[position].length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		textView.setText(sp);
		
		// 当前月信息显示
		if (position < daysOfMonth + daysOfWeek && position >= daysOfWeek) {
			textView.setTextColor(Color.BLACK);// 当月字体设黑
		} else {
			textView.setTextColor(Color.GRAY);
		}

		// 设置日程标记背景
		Cursor cursor = null;
		try{
			cursor = Alarms.getAlarmsCursorByDate(
					getDateByPosition(position), context.getContentResolver());
			if (0 != cursor.getCount()) {
				if( currentFlag == position ){
					textView.setBackgroundResource(R.drawable.current_day_bgc_mark);
					textView.setTextColor(Color.WHITE);
				} else {
					textView.setBackgroundResource(R.drawable.mark_bg);
				}
			} else {
				if( currentFlag == position ){
					textView.setBackgroundResource(R.drawable.current_day_bgc);
					textView.setTextColor(Color.WHITE);
				} else {
					textView.setBackgroundResource(R.drawable.cell_bg);
				}
			}
		} catch( Exception e){
			Log.e("main", "异常： " + e.toString());
		} finally {
			cursor.close();
		}
// 设置当天的背景
//		if (currentFlag == position) {
//			textView.setBackgroundResource(R.drawable.current_day_bgc);
//			textView.setTextColor(Color.WHITE);
//		}
		return convertView;
	}
}
