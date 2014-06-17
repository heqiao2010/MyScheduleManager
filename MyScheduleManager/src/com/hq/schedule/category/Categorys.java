package com.hq.schedule.category;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Categorys {

	private static ContentValues createContentValues(Category category) {
		ContentValues values = new ContentValues(2);
		values.put(Category.PRIORITY_LEVEL, category.priority_level);
		values.put(Category.CATEGORY_NAME, category.category_name);
		// 返回ContentValues
		return values;
	}

	/**
	 * 增加一个日程分类
	 * 
	 * @return 返回增加的類別ID
	 */
	public static int addCategory(Context context, Category category) {
		ContentValues values = createContentValues(category);
		Uri uri = context.getContentResolver().insert(Category.CONTENT_URI,
				values);
		category.id = (int) ContentUris.parseId(uri);
		// 返回类别id
		return category.id;
	}

	/**
	 * 删除一个日程分类，要注意在删除类别的时候，是否需要检查当前日程中是否有引用这个日程类别
	 * 如果有，则不应该删除。这个限制在数据库表中没有做外键关联，但在程序逻辑上要做限制。
	 */
	public static void deleteCategory(Context context, int categoryId) {
		if (categoryId == -1) {
			return;
		}
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(Category.CONTENT_URI, categoryId);
		contentResolver.delete(uri, "", null);
	}

	/**
	 * 更改一個日程類別
	 * 
	 * @return 更改的記錄數目
	 */
	public static int setCategory(Context context, Category category) {
		ContentValues values = createContentValues(category);
		ContentResolver resolver = context.getContentResolver();
		int count = resolver.update(
				ContentUris.withAppendedId(Category.CONTENT_URI, category.id),
				values, null, null);

		return count;
	}

	/**
	 * 通過Category ID查詢，返回一個Category對象 如果無查詢結果，則返回空
	 */
	public static Category getCategory(ContentResolver contentResolver,
			int category_id) {
		Cursor cursor = contentResolver.query(
				ContentUris.withAppendedId(Category.CONTENT_URI, category_id),
				Category.CATEGORY_QUERY_COLUMNS, null, null,
				Category.DEFAULT_SORT_ORDER);
		Category category = null;
		try {
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					category = new Category(cursor);
				}
			}
		} catch (Exception e) {
			Log.e("main", "In getNote,异常: " + e.toString());
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return category;
	}

	/**
	 * 查詢所有的Category
	 * 
	 * @return cursor over all Categorys
	 */
	public static Cursor getCategorysCursor(ContentResolver contentResolver) {
		return contentResolver.query(Category.CONTENT_URI,
				Category.CATEGORY_QUERY_COLUMNS, null, null,
				Category.DEFAULT_SORT_ORDER);
	}

	/**
	 * 查詢指定名字的的Category
	 * 
	 * @return cursor over 指定名字的 Categorys
	 */
	public static Cursor getCategorysCursorByCategoryName(String CategoryName,
			ContentResolver contentResolver) {
		return contentResolver.query(Category.CONTENT_URI,
				Category.CATEGORY_QUERY_COLUMNS, Category.CATEGORY_NAME + " = "
						+ CategoryName, // where字句
				null, Category.DEFAULT_SORT_ORDER);
	}

	/**
	 * 查詢指定優先級的Category
	 * 
	 * @return cursor over 指定優先級的 Categorys
	 */
	public static Cursor getCategorysCursorByCategoryName(
			int category_priority, ContentResolver contentResolver) {
		return contentResolver.query(Category.CONTENT_URI,
				Category.CATEGORY_QUERY_COLUMNS, Category.PRIORITY_LEVEL
						+ " = " + category_priority, // where字句
				null, Category.DEFAULT_SORT_ORDER);
	}

}
