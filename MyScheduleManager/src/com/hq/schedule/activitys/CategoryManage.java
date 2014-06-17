package com.hq.schedule.activitys;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarms;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;
import com.hq.schedule.category.Category;
import com.hq.schedule.category.Categorys;

public class CategoryManage extends Activity {

	private ListView category_list = null;
	private List<Map<String, ?>> listItems;
	private SimpleAdapter adapter = null;
	private EditText new_category_name_ed = null;
	private RatingBar new_priority_level_rb = null;
	private ProgressDialog myProgressDialog = null;
	private CategoryAsHelper myCategoryAsHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_mg);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		category_list = (ListView) findViewById(R.id.category_list);
		getDataFromDB();
	}

	private void refresh() {
		getDataFromDB();
	}

	private void addCategory() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.new_category,
				(ViewGroup) findViewById(R.id.new_category));
		new_priority_level_rb = (RatingBar) layout
				.findViewById(R.id.priority_level_rb);
		new_category_name_ed = (EditText) layout
				.findViewById(R.id.new_category_name);
		new_priority_level_rb.setStepSize(1);
		new_priority_level_rb
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
						Toast.makeText(CategoryManage.this,
								"设置优先级为" + (int) rating, Toast.LENGTH_SHORT)
								.show();
					}
				});

		new AlertDialog.Builder(this).setTitle("创建日程分类").setView(layout)
				.setPositiveButton("创建", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String category_name = new_category_name_ed.getText()
								.toString();
						int rating = (int) new_priority_level_rb.getRating();
						if (0 == category_name.length()
								|| category_name.length() > 10) {
							Toast.makeText(CategoryManage.this,
									"创建失败，分类名称必须少于10个字且不为空.",
									Toast.LENGTH_SHORT).show();
						} else {
							Category category = new Category(-1, category_name,
									rating);
							category.id = Categorys.addCategory(
									CategoryManage.this, category);
							refresh(); // 刷新
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	private List<Map<String, ?>> getItems(Cursor cursor) {
		listItems = new ArrayList<Map<String, ?>>();
		if (null == cursor) {
			return listItems;
		}
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				Map<String, Object> item = new HashMap<String, Object>();
				Category category = new Category(cursor);
				item.put("category_id", category.id);
				item.put("category_name", category.category_name);
				item.put("priority_level", category.priority_level);
				listItems.add(item);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
		} finally {
			cursor.close();
		}
		return listItems;
	}

	static class GetDataHandler extends Handler {
		WeakReference<CategoryManage> mActivity;

		GetDataHandler(CategoryManage activity) {
			mActivity = new WeakReference<CategoryManage>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CategoryManage theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			Cursor cursor = theActivity.myCategoryAsHelper.getResultCursor();
			if (null == cursor) {
				Toast.makeText(theActivity, "查询出错!", Toast.LENGTH_SHORT).show();
			} else {
				theActivity.getItems(cursor);
				theActivity.adapter = new SimpleAdapter(theActivity,
						theActivity.listItems, R.layout.category_item,
						new String[] { "category_name", "priority_level" },
						new int[] { R.id.category_name_tv,
								R.id.priorityLevel_rb });
				theActivity.adapter.setViewBinder(new ViewBinder() {
					@Override
					public boolean setViewValue(View v, Object data,
							String textRepresentation) {
						if (v.getId() == R.id.priorityLevel_rb) {
							Integer intValue = (Integer) data;
							RatingBar ratingBar = (RatingBar) v;
							ratingBar.setRating(intValue);
							ratingBar.setEnabled(false);
							return true;
						}
						return false;
					}
				});
				theActivity.category_list.setAdapter(theActivity.adapter);
				theActivity.setListClickListener();
			}
		}
	}

	private void setListClickListener() {
		category_list.setOnItemClickListener(new AdapterOnClickListener());
		category_list
				.setOnItemLongClickListener(new AdapterOnLongClickListener());
	}

	// 撤销进度框
	private void dismissProgressDialog() {
		if (null != myProgressDialog) {
			myProgressDialog.dismiss();
			myProgressDialog = null;
		}
	}

	// 显示进度框
	public void showProgressDialog(String title, String message) {
		if (null == myProgressDialog) {
			myProgressDialog = ProgressDialog.show(this, title, message);
		}
	}

	private void getDataFromDB() {
		ContentResolver contentResolver = getContentResolver();
		myCategoryAsHelper = new CategoryAsHelper(contentResolver);
		myCategoryAsHelper.myHandler = new GetDataHandler(this);
		Thread myThread = new Thread(myCategoryAsHelper);
		showProgressDialog("查询中", "请稍后..."); // 显示进度框
		try {
			myThread.start();
		} catch (Exception e) {
			// 撤销进度框
			dismissProgressDialog();
			Toast.makeText(this, "exception:" + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private class AdapterOnClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Map<String, ?> selectedItem = (Map<String, ?>) listItems
					.get(position);
			int category_id = (Integer) selectedItem.get("category_id");
			String category_name = selectedItem.get("category_name").toString();
			int priority_level = (Integer) selectedItem.get("priority_level");
			showEditCategory(category_id, category_name, priority_level);
		}

	}

	private void showEditCategory(final int category_id, String category_name,
			int priority_level) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.new_category,
				(ViewGroup) findViewById(R.id.new_category));
		new_priority_level_rb = (RatingBar) layout
				.findViewById(R.id.priority_level_rb);
		new_category_name_ed = (EditText) layout
				.findViewById(R.id.new_category_name);
		new_priority_level_rb.setStepSize(1);
		new_priority_level_rb.setRating(priority_level);
		new_priority_level_rb
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
						Toast.makeText(CategoryManage.this,
								"设置优先级为" + (int) rating, Toast.LENGTH_SHORT)
								.show();
					}
				});
		new_category_name_ed.setText(category_name);
		new AlertDialog.Builder(this).setTitle("更改日程分类").setView(layout)
				.setPositiveButton("更改", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String category_name = new_category_name_ed.getText()
								.toString();
						int rating = (int) new_priority_level_rb.getRating();
						if (0 == category_name.length()
								|| category_name.length() > 10) {
							Toast.makeText(CategoryManage.this,
									"更改失败，分类名称必须少于10个字且不为空.",
									Toast.LENGTH_SHORT).show();
						} else {
							Category category = new Category(category_id,
									category_name, rating);
							category.id = Categorys.setCategory(
									CategoryManage.this, category);
							refresh(); // 刷新
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	private class AdapterOnLongClickListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			Map<String, ?> selectedItem = (Map<String, ?>) listItems
					.get(position);
			int category_id = (Integer) selectedItem.get("category_id");
			String category_name = selectedItem.get("category_name").toString();
			showDeleteCategory(category_id, category_name);
			return true;
		}
	}

	private void showDeleteCategory(final int category_id,
			final String category_name) {
		new AlertDialog.Builder(CategoryManage.this)
				.setTitle("删除" + category_name + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ContentResolver contentResolver = CategoryManage.this
								.getContentResolver();
						Cursor cursor = Alarms.getAlarmsCursorByCategoryId(
								category_id, contentResolver);
						try {
							if (0 == category_id) {
								Toast.makeText(CategoryManage.this,
										"该分类为默认分类，不能删除。", Toast.LENGTH_SHORT)
										.show();
							} else if (cursor.getCount() == 0) {
								Categorys.deleteCategory(CategoryManage.this,
										category_id);
								Toast.makeText(CategoryManage.this,
										"成功删除" + category_name,
										Toast.LENGTH_SHORT).show();
								refresh(); // 刷新
							} else {
								Toast.makeText(CategoryManage.this,
										"当前日程分类中有日程设置，请先删除该分类中的日程信息。",
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							Log.e("main", "异常:" + e.toString());
						} finally {
							if (null != cursor)
								cursor.close();
						}

					}
				}).setNegativeButton("取消", null).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.category, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.category_add: // 新增
			addCategory();
			return true;
		case R.id.category_refresh: // 刷新
			refresh();
			return true;
		case android.R.id.home:
			exit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void exit() {
		finish();
		overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
	}

	@Override
	public void onBackPressed() {
		exit();
	}

}
