package com.hq.schedule.activitys;

import java.util.Calendar;
import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarm;
import com.hq.schedule.alarm.SetAlarm;
import com.hq.schedule.note.Note;
import com.hq.schedule.utility.GLFont;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.hq.schedule.note.Notes;

public class NoteEdit extends Activity {
	private EditText beditTx = null;
	private Note note = null;
	private ContentResolver contentResolver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		beditTx = (EditText) findViewById(R.id.reminder_edit_tx);
		beditTx.setHint("请输入笔记内容");
		note = new Note(); // 得到一个默认的Note对象 id：-1
		contentResolver = getContentResolver();
		getNoteID();
	}

	private void getNoteID() {
		Intent i = getIntent();
		note.id = i.getIntExtra(Note.ID, -1);
		if (-1 != note.id) { // 不是一个新笔记
			note = Notes.getNote(contentResolver, note.id);
			beditTx.setText(note.note_text);
		} else {
			beditTx.setHint("请输入笔记内容");
		}
	}

	private void saveNote() {
		note.note_text = beditTx.getText().toString();
		Calendar calendar = Calendar.getInstance();
		note.create_time = calendar.get(Calendar.YEAR) + "-"
				+ (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE);
		String toast_text = "保存成功";
		if (-1 == note.id) {
			note.id = Notes.addNote(NoteEdit.this, note);
		} else {
			int count = Notes.setNote(NoteEdit.this, note);
			if (1 != count) {
				toast_text = "保存异常";
				Log.e("main", "更新note, id：" + note.id + " note text: "
						+ note.note_text + " 异常，实际更改Note数: " + count);
			}
		}
		Toast.makeText(NoteEdit.this, toast_text, Toast.LENGTH_SHORT).show();
	}

	private void shareNote() {
		String shareStr = beditTx.getText().toString();
		if (null == shareStr || "".equals(shareStr)) {
			Toast.makeText(NoteEdit.this, "请先输入分享信息。", Toast.LENGTH_SHORT)
					.show();
		} else {
			GLFont.shareText(shareStr, this);
		}
	}

	private void setReminder() {
		Intent i = new Intent();
		i.putExtra(Alarm.Columns.MESSAGE, note.note_text);
		i.setClass(NoteEdit.this, SetAlarm.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.note_edit_save:
			saveNote();
			return true;
		case R.id.note_edit_share:
			shareNote();
			return true;
		case R.id.note_edit_setreminder:
			setReminder();
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
