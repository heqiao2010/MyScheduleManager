package com.hq.schedule.activitys;

import com.hq.schedule.category.Categorys;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;

public class CategoryAsHelper implements Runnable {
	public Handler myHandler = null;
	private Cursor resultCursor = null;
	private ContentResolver contentResolver = null;

	public Cursor getResultCursor() {
		return resultCursor;
	}

	public CategoryAsHelper(ContentResolver contentResolver) {
		super();
		this.contentResolver = contentResolver;
	}

	@Override
	public void run() {
		// 向数据库中查询
		if (null == contentResolver) {
			resultCursor = null;
		} else {
			resultCursor = Categorys.getCategorysCursor(contentResolver);
		}
		// 查询完成触发handler
		if (null != this.myHandler) {
			this.myHandler.sendEmptyMessage(0);
		}
	}
}
