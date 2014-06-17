package com.hq.schedule.activitys;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hq.schedule.R;
import com.hq.schedule.activitys.CategoryManage.GetDataHandler;
import com.hq.schedule.note.Note;
import com.hq.schedule.note.Notes;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class NoteBookActivity extends Activity {
	private ListView noteBookList = null;
	private List<Map<String, ?>> listdata = null;
	SimpleAdapter adapter = null;
	private ProgressDialog myProgressDialog = null;
	private NoteAsHelper myNoteAsHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_book);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		noteBookList = (ListView) findViewById(R.id.notebook_list);
		noteBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Map<String, ?> item = listdata.get(position);
				int note_id = (Integer) item.get(Note.ID);
				Intent i = new Intent();
				i.setClass(NoteBookActivity.this, NoteEdit.class);
				i.putExtra(Note.ID, note_id);
				startActivity(i);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});
		noteBookList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				Map<String, ?> item = listdata.get(position);
				int note_id = (Integer) item.get(Note.ID);
				String note_text = (String) item.get(Note.NOTE_TEXT);
				note_text = note_text.length() > 5 ? note_text.substring(0, 5)
						: note_text;
				showDeleteDialog(note_id, note_text);
				return true;
			}
		});
		getDataFromDB();
	}

	private void showDeleteDialog(final int note_id, final String note_text) {
		new AlertDialog.Builder(this).setTitle("删除" + note_text + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Notes.deleteNote(NoteBookActivity.this, note_id);
						Toast.makeText(NoteBookActivity.this,
								note_text + "已成功删除", Toast.LENGTH_SHORT).show();
						refresh();
					}
				}).setNegativeButton("取消", null).show();
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

	private List<Map<String, ?>> getListData(Cursor cursor) {
		listdata = new ArrayList<Map<String, ?>>();
		if (null == cursor) {
			return listdata;
		}
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				Map<String, Object> item = new HashMap<String, Object>();
				Note note = new Note(cursor);
				item.put(Note.ID, note.id);
				item.put(Note.NOTE_TEXT, note.note_text);
				if (null != note.create_time && note.create_time.length() >= 19) {
					note.create_time = note.create_time.substring(0, 19);
				}
				item.put(Note.NOTE_CREATE_TIME, note.create_time);
				listdata.add(item);
			}
		} catch (Exception e) {
			Log.e("main", "异常： " + e.toString());
		} finally {
			cursor.close();
		}
		return listdata;
	}

	private void refresh() {
		getDataFromDB();
	}

	private void setAdapter(List<Map<String, ?>> listdata) {
		adapter = new SimpleAdapter(this, listdata, R.layout.nb_list_item,
				new String[] { Note.NOTE_TEXT, Note.NOTE_CREATE_TIME },
				new int[] { R.id.nb_item_text, R.id.nb_item_note_time });
		noteBookList.setAdapter(adapter);
	}

	static class GetDataHandler extends Handler {
		WeakReference<NoteBookActivity> mActivity;

		GetDataHandler(NoteBookActivity activity) {
			mActivity = new WeakReference<NoteBookActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			NoteBookActivity theActivity = mActivity.get();
			theActivity.dismissProgressDialog(); // 取消进度框
			Cursor cursor = theActivity.myNoteAsHelper.getResultCursor();
			if (null == cursor) {
				Toast.makeText(theActivity, "查询出错!", Toast.LENGTH_SHORT).show();
			} else {
				theActivity.setAdapter(theActivity.getListData(cursor));
			}
		}
	}

	private void getDataFromDB() {
		ContentResolver contentResolver = getContentResolver();
		myNoteAsHelper = new NoteAsHelper(contentResolver);
		myNoteAsHelper.myHandler = new GetDataHandler(this);
		Thread myThread = new Thread(myNoteAsHelper);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 刷新列表
		refresh();
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notebook, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.refresh_note:
			refresh();
			return true;
		case R.id.add_note:
			Intent i = new Intent();
			i.setClass(this, NoteEdit.class);
			startActivity(i);
			overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
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
