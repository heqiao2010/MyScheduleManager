package com.hq.schedule.category;

import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

public class CategoryPreference extends ListPreference {
	private ContentResolver contentResolver = null;
	private SparseArray<String> categorys = null;
	private String current_category_id = "";

	public CategoryPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		contentResolver = context.getContentResolver();
		categorys = getAllCategorys(contentResolver);
		setEntries(getCategorysName());
		setEntryValues(getCategorysID());
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String categoryName = categorys.get(Integer.parseInt(current_category_id));
			setSummary(categoryName);
			callChangeListener(categoryName);
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		final CharSequence[] entryValues = getEntryValues();
		int checkedItem = 0;
		for( int i=0; i<entryValues.length; i++){
			if(current_category_id.equals(entryValues[i])){
				checkedItem = i;
				break;
			}
		}
		builder.setSingleChoiceItems(getEntries(), checkedItem,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						current_category_id = (String) entryValues[which];
					}
				});
	}

	public void setCurrentCategoryID( int id ){
		current_category_id = String.valueOf(id);
		String categoryName = categorys.get(Integer.parseInt(current_category_id));
		setSummary(categoryName);
	}
	
	public int getCurrentCategoryID(){
		return Integer.parseInt(current_category_id);
	}
	
	private String[] getCategorysID(){
		String[] ids = new String[categorys.size()];
		for( int i=0; i<categorys.size(); i++ ){
			ids[i] = String.valueOf(categorys.keyAt(i));
		}
		return ids;
	}
	
	private String[] getCategorysName(){
		String[] names = new String[categorys.size()];
		for( int i=0; i<categorys.size(); i++ ){
			names[i] = categorys.valueAt(i);
		}
		return names;
	}
	
	//获取Category所有的信息
	private SparseArray<String> getAllCategorys(ContentResolver contentResolver) {
		Cursor cursor = Categorys.getCategorysCursor(contentResolver);
		SparseArray<String> categorys = new SparseArray<String>();
		Category category = null;
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				category = new Category(cursor);
				categorys.append(category.id, category.category_name);
			}
		} catch (Exception e) {
			Log.e("main", "in CategoryPreference, 异常:" + e.toString());
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return categorys;
	}
}
