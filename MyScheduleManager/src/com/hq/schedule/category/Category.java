package com.hq.schedule.category;

import android.database.Cursor;
import android.net.Uri;

//该类描述日程类别
public class Category {
	public int id; // id
	public String category_name; // 类别名，数据库限制长度10
	public int priority_level;
	/**
	 * The content:// 为这个表定义一个共享的Url
	 */
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.example.scheldulemanager.category/category");
	/**
	 * 类别的id
	 * <P>
	 * Type: INTERGER
	 * </P>
	 */
	public static final String ID = "_id";

	/**
	 * 类别的名称：固定长度10
	 * <P>
	 * Type: String
	 * </P>
	 */
	public static final String CATEGORY_NAME = "category_name";

	/**
	 * 类别的优先级
	 * <P>
	 * Type: String
	 * </P>
	 */
	public static final String PRIORITY_LEVEL = "priority_level";

	// 查询字串
	public static final String[] CATEGORY_QUERY_COLUMNS = { ID, CATEGORY_NAME,
			PRIORITY_LEVEL };

    /**
     * 默認排序
     */
    public static final String DEFAULT_SORT_ORDER =
    		PRIORITY_LEVEL + " ASC";
	/**
	 * 当调用cursor.getColumnIndexOrThrow()的时候， 他们的顺序必须与上面查询字符串的顺序一致
	 */
	public static final int CATEGORY_ID_INDEX = 0;
	public static final int CATEGORY_NAME_INDEX = 1;
	public static final int CATEGORY_PRIORITY_LEVEL_INDEX = 2;

	public Category(int id, String category_name, int priority_level) {
		super();
		this.id = id;
		this.category_name = category_name;
		this.priority_level = priority_level;
	}

	public Category(Cursor c) {
		super();
		this.id = c.getInt(CATEGORY_ID_INDEX);
		this.category_name = c.getString(CATEGORY_NAME_INDEX);
		this.priority_level = c.getInt(CATEGORY_PRIORITY_LEVEL_INDEX);
	}
	
}
