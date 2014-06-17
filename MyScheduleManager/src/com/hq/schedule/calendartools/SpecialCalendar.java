package com.hq.schedule.calendartools;

import java.util.Calendar;

public class SpecialCalendar {

	// 判断是否为闰年
	public static boolean isLeapYear(int year) {
		if (year % 100 == 0 && year % 400 == 0) {
			return true;
		} else if (year % 100 != 0 && year % 4 == 0) {
			return true;
		}
		return false;
	}

	//得到某月有多少天数
	public static int getDaysOfMonth(boolean isLeapyear, int month) {
		if( 1==month || 3==month || 5==month || 7==month || 8==month || 10==month || 12==month){
			return 31;
		} else if(4==month || 6==month || 9==month || 11==month ){
			return 30;
		} else {
			return isLeapyear ? 29 : 28;
		}
	}
	
	//指定某年中的某月的第一天是星期几
	public static int getWeekdayOfMonth(int year, int month){
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, 1);
		return cal.get(Calendar.DAY_OF_WEEK)-1;
	}
	
	public static Calendar getMinDate() {
		Calendar minDate = Calendar.getInstance();
		minDate.set(Calendar.YEAR, 1900);
		minDate.set(Calendar.MONTH, 2);
		minDate.set(Calendar.DAY_OF_MONTH, 1);
		return minDate;
	}

	public static Calendar getMaxDate() {
		Calendar maxDate = Calendar.getInstance();
		maxDate.set(Calendar.YEAR, 2049);
		maxDate.set(Calendar.MONTH, 11);
		maxDate.set(Calendar.DAY_OF_MONTH, 1);
		return maxDate;
	}
}
