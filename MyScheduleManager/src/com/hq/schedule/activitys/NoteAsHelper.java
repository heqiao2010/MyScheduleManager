package com.hq.schedule.activitys;

import com.hq.schedule.note.Notes;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;

public class NoteAsHelper implements Runnable {
	public Handler myHandler = null;
	private Cursor resultCursor = null;
	private ContentResolver contentResolver = null;

	public Cursor getResultCursor() {
		return resultCursor;
	}

	public NoteAsHelper(ContentResolver contentResolver) {
		super();
		this.contentResolver = contentResolver;
	}

	@Override
	public void run() {
		// 向数据库中查询
		if (null == contentResolver) {
			resultCursor = null;
		} else {
			resultCursor = Notes.getNotesCursor(contentResolver);
		}
		// 查询完成触发handler
		if (null != this.myHandler) {
			this.myHandler.sendEmptyMessage(0);
		}
	}
}
