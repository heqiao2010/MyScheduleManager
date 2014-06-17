package com.hq.schedule.utility;

import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.Alarms;
import com.hq.schedule.category.Category;
import com.hq.schedule.category.Categorys;
import com.hq.schedule.note.Note;
import com.hq.schedule.note.Notes;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class BackupHelper {
	private ContentResolver contentResolver = null;
	private String backup_time = null;

	public BackupHelper(Context context) {
		contentResolver = context.getContentResolver();
		Calendar mCalendar = Calendar.getInstance();
		backup_time = mCalendar.get(Calendar.YEAR) + "-"
				+ (mCalendar.get(Calendar.MONTH) + 1) + "-"
				+ mCalendar.get(Calendar.DAY_OF_MONTH) + " "
				+ mCalendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ mCalendar.get(Calendar.MINUTE) + ":"
				+ mCalendar.get(Calendar.SECOND);
	}

	public JSONObject getBackupJSON() throws JSONException {
		JSONObject backupJsonObj = new JSONObject();
		JSONArray alarmJsonArray = backupAlarms(contentResolver);
		JSONArray categoryJsonArray = backupCategory(contentResolver);
		JSONArray noteJsonArray = backupNote(contentResolver);
		if (null == alarmJsonArray || null == categoryJsonArray
				|| null == noteJsonArray) {
			return null;
		} else {
			backupJsonObj.put("schedule", alarmJsonArray);
			backupJsonObj.put("category", categoryJsonArray);
			backupJsonObj.put("note", noteJsonArray);
			backupJsonObj.put("backup_time", backup_time);
		}
		return backupJsonObj;
	}

	private JSONArray backupAlarms(ContentResolver contentResolver)
			throws JSONException {
		JSONArray alarmJsonArray = new JSONArray();
		Cursor cursor = Alarms.getAlarmsCursor(contentResolver);
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				JSONObject jo = new JSONObject();
				jo.put("_id", cursor.getInt(Alarm.Columns.ALARM_ID_INDEX));
				jo.put("year", cursor.getInt(Alarm.Columns.ALARM_YEAR_INDEX));
				jo.put("month", cursor.getInt(Alarm.Columns.ALARM_MONTH_INDEX));
				jo.put("day", cursor.getInt(Alarm.Columns.ALARM_DAY_INDEX));
				jo.put("hour", cursor.getInt(Alarm.Columns.ALARM_HOUR_INDEX));
				jo.put("minutes",
						cursor.getInt(Alarm.Columns.ALARM_MINUTES_INDEX));
				jo.put("daysofweek",
						cursor.getInt(Alarm.Columns.ALARM_DAYS_OF_WEEK_INDEX));
				jo.put("alarmtime",
						cursor.getLong(Alarm.Columns.ALARM_TIME_INDEX));
				jo.put("enabled",
						cursor.getInt(Alarm.Columns.ALARM_ENABLED_INDEX));
				jo.put("vibrate",
						cursor.getInt(Alarm.Columns.ALARM_VIBRATE_INDEX));
				jo.put("message",
						cursor.getString(Alarm.Columns.ALARM_MESSAGE_INDEX));
				jo.put("alert",
						cursor.getString(Alarm.Columns.ALARM_ALERT_INDEX));
				jo.put("category",
						cursor.getInt(Alarm.Columns.ALARM_CATEGORY_INDEX));
				jo.put("sort_key",
						cursor.getString(Alarm.Columns.ALARM_SORT_KEY_INDEX));
				jo.put("backup_time", backup_time);
				alarmJsonArray.put(jo);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
			return null;
		} finally {
			cursor.close();
		}
		return alarmJsonArray;
	}

	private JSONArray backupCategory(ContentResolver contentResolver)
			throws JSONException {
		JSONArray categoryJsonArray = new JSONArray();
		Cursor cursor = Categorys.getCategorysCursor(contentResolver);
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				JSONObject jo = new JSONObject();
				jo.put("_id", cursor.getInt(Category.CATEGORY_ID_INDEX));
				jo.put("category_name",
						cursor.getString(Category.CATEGORY_NAME_INDEX));
				jo.put("priority_level",
						cursor.getInt(Category.CATEGORY_PRIORITY_LEVEL_INDEX));
				jo.put("backup_time", backup_time);
				categoryJsonArray.put(jo);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
			return null;
		} finally {
			cursor.close();
		}
		return categoryJsonArray;
	}

	private JSONArray backupNote(ContentResolver contentResolver)
			throws JSONException {
		JSONArray noteJsonArray = new JSONArray();
		Cursor cursor = Notes.getNotesCursor(contentResolver);
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				JSONObject jo = new JSONObject();
				jo.put("_id", cursor.getInt(Note.NOTE_ID_INDEX));
				jo.put("note_text", cursor.getString(Note.NOTE_TEXT_INDEX));
				jo.put("create_time",
						cursor.getString(Note.NOTE_CREATE_TIME_INDEX));
				jo.put("backup_time", backup_time);
				noteJsonArray.put(jo);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
			return null;
		} finally {
			cursor.close();
		}
		return noteJsonArray;
	}

}
