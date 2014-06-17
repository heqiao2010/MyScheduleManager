package com.hq.schedule.activitys;

import java.util.Calendar;
import com.hq.schedule.alarm.Alarms;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;

public class AlarmAscHelper implements Runnable {
	public Handler myHandler = null;
	private Cursor resultCursor = null;
	private Calendar c = null;
	private ContentResolver contentResolver = null;

	public Cursor getResultCursor() {
		return resultCursor;
	}

	public AlarmAscHelper(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public AlarmAscHelper(Calendar c, ContentResolver contentResolver) {
		this.c = c;
		this.contentResolver = contentResolver;
	}

	public AlarmAscHelper(int year, int month, int day,
			ContentResolver contentResolver) {
		c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1); // calendar month 0 - 11
		c.set(Calendar.DAY_OF_MONTH, day);
		this.contentResolver = contentResolver;
	}

	@Override
	public void run() {
		// 向数据库中查询
		if (null == contentResolver) {
			resultCursor = null;
		} else if (null == c) { // 没有传入日期，则查出所有的提醒信息
			resultCursor = Alarms.getNotTimeOutAlarmsCursor(contentResolver);
		} else {
			resultCursor = Alarms.getAlarmsCursorByDate(c, contentResolver);
		}
		// 查询完成触发handler
		if (null != this.myHandler) {
			this.myHandler.sendEmptyMessage(0);
		}
	}
}
