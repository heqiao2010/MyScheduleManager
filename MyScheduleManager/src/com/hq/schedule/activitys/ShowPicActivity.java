package com.hq.schedule.activitys;

import java.io.File;
import com.hq.schedule.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowPicActivity extends Activity {
	private ImageView imgview = null;
	private String filePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_pic);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		imgview = (ImageView) findViewById(R.id.image_view);
		filePath = getIntent().getStringExtra("filePath");
		if (null == filePath || "".equals(filePath)) {
			Toast.makeText(this, "无法获取图片路径", Toast.LENGTH_SHORT).show();
		} else {
			Bitmap mBitmap = BitmapFactory.decodeFile(filePath, null);
			imgview.setImageBitmap(mBitmap);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_pict, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			return true;
		case R.id.show_pict_share:
			if (null == filePath || "".equals(filePath)) {
				Toast.makeText(this, "无法获取生成图片路径，请重试。", Toast.LENGTH_LONG)
				.show();
				return true;
			} else {
				shareMsg(ShowPicActivity.this,
						(String) ShowPicActivity.this.getTitle(), "分享图片",
						"分享图片", filePath);
			}
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

	/**
	 * 分享功能
	 * 
	 * @param context
	 *            上下文
	 * @param activityTitle
	 *            Activity的名字
	 * @param msgTitle
	 *            消息标题
	 * @param msgText
	 *            消息内容
	 * @param imgPath
	 *            图片路径，不分享图片则传null
	 */
	public static void shareMsg(Context context, String activityTitle,
			String msgTitle, String msgText, String imgPath) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/png");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}
		intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(intent, activityTitle));
	}
}
