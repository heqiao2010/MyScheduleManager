package com.hq.schedule.note;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NoteProvider extends ContentProvider {
	private SQLiteOpenHelper mOpenHelper;
	private static final int NOTES = 1;
	private static final int NOTES_ID = 2;
	private static final UriMatcher sURLMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURLMatcher.addURI("com.example.scheldulemanager.note", "note", NOTES);
		sURLMatcher.addURI("com.example.scheldulemanager.note", "note/#",
				NOTES_ID);
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "notes.db";
		private static final int DATABASE_VERSION = 5;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 创建日程类别
			db.execSQL("CREATE TABLE notes (" + "_id INTEGER PRIMARY KEY, "
					+ "note_text TEXT, " 
					+"create_time TIMESTAMP);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
				int currentVersion) {
			Log.v("main", "Upgrading notes database from version " + oldVersion
					+ " to " + currentVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	public NoteProvider() {
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sURLMatcher.match(url)) {
		case NOTES:
			count = db.delete("notes", where, whereArgs);
			break;
		case NOTES_ID:
			String segment = url.getPathSegments().get(1);
			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}
			count = db.delete("notes", where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public String getType(Uri url) {
		int match = sURLMatcher.match(url);
		switch (match) {
		case NOTES:
			return "vnd.android.cursor.dir/notes";
		case NOTES_ID:
			return "vnd.android.cursor.item/notes";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		if (sURLMatcher.match(url) != NOTES) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}
		ContentValues values = new ContentValues(initialValues);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert("notes", null, values); // 插入时，不允许列为空
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}
		Log.v("main", "Added note rowId = " + rowId);
		Uri newUrl = ContentUris.withAppendedId(Note.CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(newUrl, null);
		return newUrl;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		// Generate the body of the query
		int match = sURLMatcher.match(url);
		switch (match) {
		case NOTES:
			qb.setTables("notes");
			break;
		case NOTES_ID:
			qb.setTables("notes");
			qb.appendWhere("_id=");
			qb.appendWhere(url.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null,
				null, sort);
		if (ret == null) {
			if (true)
				Log.v("main", "Categorys.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}
		return ret;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		long rowId = 0;
		int match = sURLMatcher.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (match) {
		case NOTES_ID: {
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update("notes", values, "_id=" + rowId, null);
			break;
		}
		default: {
			throw new UnsupportedOperationException("Cannot update URL: " + url);
		}
		}
		Log.v("main", "*** notifyChange() rowId: " + rowId + " url " + url);
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

}
