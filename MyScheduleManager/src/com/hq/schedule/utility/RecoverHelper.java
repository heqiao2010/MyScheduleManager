package com.hq.schedule.utility;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.hq.schedule.activitys.AppInfoManage;
import com.hq.schedule.alarm.AlarmProvider;
import com.hq.schedule.category.CategoryProvider;
import com.hq.schedule.note.NoteProvider;

public class RecoverHelper {
	private SQLiteOpenHelper mAlarmDBOpenHelper = null;
	private SQLiteOpenHelper mCategoryDBOpenHelper = null;
	private SQLiteOpenHelper mNoteDBOpenHelper = null;

	public RecoverHelper(Context context) {
		mAlarmDBOpenHelper = new AlarmProvider.DatabaseHelper(context);
		mCategoryDBOpenHelper = new CategoryProvider.DatabaseHelper(context);
		mNoteDBOpenHelper = new NoteProvider.DatabaseHelper(context);
	}

	public void paserBackupInfoJson(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		// 日程
		String scheduleStr = jsonObj.getString("schedule_info").trim();
		JSONArray alarmJsonArray = null;
		if (null == scheduleStr || "".equals(scheduleStr)
				|| AppInfoManage.DATAEMPTY.equals(scheduleStr)) {
			Log.i("main", "In paserBackupInfoJson: scheduleStr is empty.");
			alarmJsonArray = new JSONArray();
		} else if ('{' == scheduleStr.charAt(0)) { // 只有一条数据
			alarmJsonArray = new JSONArray('[' + scheduleStr + ']');
		} else {
			alarmJsonArray = new JSONArray(scheduleStr);
		}
		// 日程分类
		String categoryStr = jsonObj.getString("category_info").trim();
		JSONArray categoryJsonArray = null;
		if (null == categoryStr || "".equals(categoryStr)
				|| AppInfoManage.DATAEMPTY.equals(categoryStr)) {
			Log.i("main", "In paserBackupInfoJson: categoryStr is empty.");
			categoryJsonArray = new JSONArray();
		} else if ('{' == categoryStr.charAt(0)) { // 只有一条数据
			categoryJsonArray = new JSONArray('[' + categoryStr + ']');
		} else {
			categoryJsonArray = new JSONArray(categoryStr);
		}
		// 笔记
		String noteStr = jsonObj.getString("note_info").trim();
		JSONArray noteJsonArray = null;
		if (null == noteStr || "".equals(noteStr)
				|| AppInfoManage.DATAEMPTY.equals(noteStr)) {
			Log.i("main", "In paserBackupInfoJson: noteStr is empty.");
			noteJsonArray = new JSONArray();
		} else if ('{' == noteStr.charAt(0)) { // 只有一条数据
			noteJsonArray = new JSONArray('[' + noteStr + ']');
		} else {
			noteJsonArray = new JSONArray(noteStr);
		}
		recoverAlarms(alarmJsonArray); // recover alarms
		recoverCategory(categoryJsonArray); // recover category
		recoverNote(noteJsonArray); // recover note
	}

	private List<ContentValues> getAlarmValues(JSONArray alarmJsonArray)
			throws JSONException {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0; i < alarmJsonArray.length(); i++) {
			ContentValues values = new ContentValues();
			JSONObject jo = (JSONObject) alarmJsonArray.opt(i);
			values.put("_id", jo.getInt("_id"));
			values.put("year", jo.getInt("year"));
			values.put("month", jo.getInt("month"));
			values.put("day", jo.getInt("day"));
			values.put("hour", jo.getInt("hour"));
			values.put("minutes", jo.getInt("minutes"));
			values.put("daysofweek", jo.getInt("daysofweek"));
			values.put("alarmtime", jo.getLong("alarmtime"));
			values.put("enabled", jo.getInt("enabled"));
			values.put("vibrate", jo.getInt("vibrate"));
			values.put("message", jo.getString("message"));
			values.put("alert", jo.getString("alert"));
			values.put("category", jo.getInt("category"));
			values.put("sort_key", jo.getString("sort_key"));
			list.add(values);
		}
		return list;
	}

	private List<ContentValues> getCategoryValues(JSONArray categoryArrays)
			throws JSONException {
		List<ContentValues> list = new ArrayList<ContentValues>();
		if (0 == categoryArrays.length()) {
			ContentValues values = new ContentValues();
			values.put("_id", 0);
			values.put("category_name", "默认分类");
			values.put("priority_level", 0);
			list.add(values);
		} else {
			for (int i = 0; i < categoryArrays.length(); i++) {
				ContentValues values = new ContentValues();
				JSONObject jo = (JSONObject) categoryArrays.opt(i);
				values.put("_id", jo.getInt("_id"));
				values.put("category_name", jo.getString("category_name"));
				values.put("priority_level", jo.getInt("priority_level"));
				list.add(values);
			}
		}
		return list;
	}

	private List<ContentValues> getNoteValues(JSONArray noteArray)
			throws JSONException {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0; i < noteArray.length(); i++) {
			ContentValues values = new ContentValues();
			JSONObject jo = (JSONObject) noteArray.opt(i);
			values.put("_id", jo.getInt("_id"));
			values.put("note_text", jo.getString("note_text"));
			values.put("create_time", jo.getString("create_time"));
			list.add(values);
		}
		return list;
	}

	private void recoverAlarms(JSONArray alarmArray) throws JSONException {
		List<ContentValues> alarmsList = getAlarmValues(alarmArray);
		Log.i("main", alarmsList.size() + "alarms from server.");
		// 获得可写的SQLiteDatabase对象
		SQLiteDatabase sqliteDatabase = mAlarmDBOpenHelper
				.getWritableDatabase();
		try {
			sqliteDatabase.delete("alarms", null, null); // delete all alrams
			for (int i = 0; i < alarmsList.size(); i++) {
				sqliteDatabase.insert("alarms", null, alarmsList.get(i));
			}
		} finally {
			sqliteDatabase.close();
		}
	}

	private void recoverCategory(JSONArray categoryArray) throws JSONException {
		List<ContentValues> categoryList = getCategoryValues(categoryArray);
		Log.i("main", categoryList.size() + "categorys from server.");
		// 获得可写的SQLiteDatabase对象
		SQLiteDatabase sqliteDatabase = mCategoryDBOpenHelper
				.getWritableDatabase();
		try {
			sqliteDatabase.delete("categorys", null, null); // delete all
															// category
			for (int i = 0; i < categoryList.size(); i++) {
				sqliteDatabase.insert("categorys", null, categoryList.get(i)); // insert
																				// category
																				// from
																				// json
			}
		} finally {
			sqliteDatabase.close();
		}
	}

	private void recoverNote(JSONArray noteArray) throws JSONException {
		List<ContentValues> noteList = getNoteValues(noteArray);
		Log.i("main", noteList.size() + "notes from server.");
		// 获得可写的SQLiteDatabase对象
		SQLiteDatabase sqliteDatabase = mNoteDBOpenHelper.getWritableDatabase();
		try {
			sqliteDatabase.delete("notes", null, null); // delete all notes
			for (int i = 0; i < noteList.size(); i++) {
				sqliteDatabase.insert("notes", null, noteList.get(i));
			}
		} finally {
			sqliteDatabase.close();
		}
	}
}
