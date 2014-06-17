package com.hq.schedule.note;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Notes {

	private static ContentValues createContentValues(Note note) {
		ContentValues values = new ContentValues(2);
		values.put(Note.NOTE_TEXT, note.note_text);
		values.put(Note.NOTE_CREATE_TIME, note.create_time);
		// 返回ContentValues
		return values;
	}

	/**
	 * 增加一个筆記
	 * 
	 * @return 返回增加的筆記ID
	 */
	public static int addNote(Context context, Note note) {
		ContentValues values = createContentValues(note);
		Uri uri = context.getContentResolver().insert(Note.CONTENT_URI, values);
		note.id = (int) ContentUris.parseId(uri);
		// 返回类别id
		return note.id;
	}

	/**
	 * 刪除一个筆記 根據傳入的ID，刪除Note
	 */
	public static void deleteNote(Context context, int noteId) {
		if (noteId == -1) {
			return;
		}
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(Note.CONTENT_URI, noteId);
		contentResolver.delete(uri, "", null);
	}

	/**
	 * 查詢所有的Note
	 * 
	 * @return cursor over all Note
	 */
	public static Cursor getNotesCursor(ContentResolver contentResolver) {
		return contentResolver.query(Note.CONTENT_URI, Note.NOTE_QUERY_COLUMNS,
				null, null, Note.DEFAULT_SORT_ORDER);
	}

	/**
	 * 通过ID获取Note对象
	 * 
	 * @return Note
	 */
	public static Note getNote(ContentResolver contentResolver, int note_id) {
		Cursor cursor = contentResolver.query(
				ContentUris.withAppendedId(Note.CONTENT_URI, note_id),
				Note.NOTE_QUERY_COLUMNS, null, null, Note.DEFAULT_SORT_ORDER);
		Note note = null;
		try {
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					note = new Note(cursor);
				}
			}
		} catch (Exception e) {
			Log.e("main", "In getNote,异常: " + e.toString());
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return note;
	}

	/**
	 * 更新笔记
	 * @return 更改的note个数，1为正常
	 */
	public static int setNote(Context context, Note note) {
		ContentValues values = createContentValues(note);
		ContentResolver resolver = context.getContentResolver();
		int count = resolver.update(
				ContentUris.withAppendedId(Note.CONTENT_URI, note.id), values,
				null, null);
		return count;
	}
}
