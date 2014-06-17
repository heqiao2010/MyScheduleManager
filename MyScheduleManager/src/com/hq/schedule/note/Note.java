package com.hq.schedule.note;

import java.util.Calendar;

import android.database.Cursor;
import android.net.Uri;

//该类描述筆記
public class Note {
	public int id; // id
	public String note_text; // 筆記內容
	public String create_time;	//筆記創建時間

	/**
	 * The content:// 为这个表定义一个共享的Url
	 */
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.example.scheldulemanager.note/note");
	/**
	 * 筆記的id
	 * <P>
	 * Type: INTERGER
	 * </P>
	 */
	public static final String ID = "_id";

	/**
	 * 筆記的內容
	 * <P>
	 * Type: String
	 * </P>
	 */
	public static final String NOTE_TEXT = "note_text";
	
	/**
	 * 筆記的創建時間
	 * <P>
	 * Type: TIMESTAMP
	 * </P>
	 */
	public static final String NOTE_CREATE_TIME = "create_time";

	// 查询字串
	public static final String[] NOTE_QUERY_COLUMNS = { ID, NOTE_TEXT, NOTE_CREATE_TIME };
	/**
	 * 默認排序
	 */
	public static final String DEFAULT_SORT_ORDER = NOTE_CREATE_TIME + " ASC";

	/**
	 * 当调用cursor.getColumnIndexOrThrow()的时候， 他们的顺序必须与上面查询字符串的顺序一致
	 */
	public static final int NOTE_ID_INDEX = 0;
	public static final int NOTE_TEXT_INDEX = 1;
	public static final int NOTE_CREATE_TIME_INDEX = 2;

	public Note() {
		super();
		this.id = -1;
		this.note_text = "";
		Calendar calendar = Calendar.getInstance();
		this.create_time = calendar.get(Calendar.YEAR) + "-" 
				+ (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE);
	}
	
	public Note(int id, String note_text, String create_time) {
		super();
		this.id = id;
		this.note_text = note_text;
		this.create_time = create_time;
	}

	public Note(Cursor cursor) {
		super();
		this.id = cursor.getInt(Note.NOTE_ID_INDEX);
		this.note_text = cursor.getString(Note.NOTE_TEXT_INDEX);
		this.create_time = cursor.getString(Note.NOTE_CREATE_TIME_INDEX);
	}
}
