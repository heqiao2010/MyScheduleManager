package com.hq.schedule.activitys;

import com.hq.schedule.R;
import com.hq.schedule.utility.SharedPreferenceHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Text2PictSetting extends Activity {
	private ImageView text2pict_length_add_iv = null;
	private EditText text2pict_length_ed = null;
	private ImageView text2pict_length_sub_iv = null;
	private ImageView text2pict_size_add_iv = null;
	private EditText text2pict_size_ed = null;
	private ImageView text2pict_size_sub_iv = null;
	private CheckBox[] mTextPictColorType_cb = null;
	private Button ok_btn = null;
	private int textLength = 140;
	private int textSize = 20;
	private int colorType = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text2pict_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		initUI();
		setEventListener();
	}

	private void initUI() {
		text2pict_length_add_iv = (ImageView) findViewById(R.id.text2pict_length_add_iv);
		text2pict_length_ed = (EditText) findViewById(R.id.text2pict_length_ed);
		text2pict_length_sub_iv = (ImageView) findViewById(R.id.text2pict_length_sub_iv);
		text2pict_size_add_iv = (ImageView) findViewById(R.id.text2pict_size_add_iv);
		text2pict_size_ed = (EditText) findViewById(R.id.text2pict_size_ed);
		text2pict_size_sub_iv = (ImageView) findViewById(R.id.text2pict_size_sub_iv);
		mTextPictColorType_cb = new CheckBox[4];
		mTextPictColorType_cb[0] = (CheckBox) findViewById(R.id.text_pict_color_type0_cb);
		mTextPictColorType_cb[1] = (CheckBox) findViewById(R.id.text_pict_color_type1_cb);
		mTextPictColorType_cb[2] = (CheckBox) findViewById(R.id.text_pict_color_type2_cb);
		mTextPictColorType_cb[3] = (CheckBox) findViewById(R.id.text_pict_color_type3_cb);
		getPereferenceData();
		setCheckBoxesChecked(colorType);
		text2pict_length_ed.setText(String.valueOf(textLength));
		text2pict_size_ed.setText(String.valueOf(textSize));
		ok_btn = (Button) findViewById(R.id.text2pic_ok_btn);
	}

	private void getPereferenceData() {
		textLength = SharedPreferenceHelper.getTextLength(this);
		textSize = SharedPreferenceHelper.getTextSize(this);
		colorType = SharedPreferenceHelper.getTextPictColorType(this);
	}

	private void savaPereferenceData() {
		SharedPreferenceHelper.saveTextLength(textLength, this);
		SharedPreferenceHelper.saveTextSize(textSize, this);
		SharedPreferenceHelper.saveTextPictColorType(colorType, this);
	}

	private void setCheckBoxesChecked(int position) {
		if (position >= 0 && position < 4) {
			for (int i = 0; i < mTextPictColorType_cb.length; i++) {
				if (position == i) {
					mTextPictColorType_cb[i].setChecked(true);
				} else {
					mTextPictColorType_cb[i].setChecked(false);
				}
			}
		}
	}

	private void setEventListener() {
		text2pict_length_add_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String lenStr = text2pict_length_ed.getText().toString();
				try {
					int lenght = Integer.parseInt(lenStr);
					lenght++;
					text2pict_length_ed.setText(String.valueOf(lenght));
				} catch (NumberFormatException e) {
					Toast.makeText(Text2PictSetting.this, "请输入整数。",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
		text2pict_size_add_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String sizeStr = text2pict_size_ed.getText().toString();
				try {
					int size = Integer.parseInt(sizeStr);
					size++;
					if (size > 45) {
						Toast.makeText(Text2PictSetting.this, "文字大小不能大于45.",
								Toast.LENGTH_SHORT).show();
					} else {
						text2pict_size_ed.setText(String.valueOf(size));
					}
				} catch (NumberFormatException e) {
					Toast.makeText(Text2PictSetting.this, "请输入整数。",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});

		text2pict_length_sub_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String lenStr = text2pict_length_ed.getText().toString();
				try {
					int lenght = Integer.parseInt(lenStr);
					if (lenght <= 0) {
						Toast.makeText(Text2PictSetting.this, "文字大小不能小于零。",
								Toast.LENGTH_SHORT).show();
					} else {
						lenght--;
						text2pict_length_ed.setText(String.valueOf(lenght));
					}
				} catch (NumberFormatException e) {
					Toast.makeText(Text2PictSetting.this, "请输入整数。",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});

		text2pict_size_sub_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String sizeStr = text2pict_size_ed.getText().toString();
				try {
					int size = Integer.parseInt(sizeStr);
					if (size <= 0) {
						Toast.makeText(Text2PictSetting.this, "文字大小不能小于零。",
								Toast.LENGTH_SHORT).show();
					} else {
						size--;
						text2pict_size_ed.setText(String.valueOf(size));
					}
				} catch (NumberFormatException e) {
					Toast.makeText(Text2PictSetting.this, "请输入整数。",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});

		mTextPictColorType_cb[0].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCheckBoxesChecked(0);
			}
		});
		mTextPictColorType_cb[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCheckBoxesChecked(1);
			}
		});
		mTextPictColorType_cb[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCheckBoxesChecked(2);
			}
		});
		mTextPictColorType_cb[3].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCheckBoxesChecked(3);
			}
		});
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean data_ok = false;
				String lenStr = text2pict_length_ed.getText().toString();
				String sizeStr = text2pict_size_ed.getText().toString();
				try {
					textLength = Integer.parseInt(lenStr);
					textSize = Integer.parseInt(sizeStr);
					if (textLength <= 0 || textSize <= 0) {
						Toast.makeText(Text2PictSetting.this, "文字长度或者大小不能小于零。",
								Toast.LENGTH_SHORT).show();
						data_ok = false;
					} else if (textSize > 45) {
						Toast.makeText(Text2PictSetting.this, "文字大小不能大于45.",
								Toast.LENGTH_SHORT).show();
						data_ok = false;
					} else {
						data_ok = true;
					}
				} catch (NumberFormatException e) {
					data_ok = false;
					Toast.makeText(Text2PictSetting.this, "请输入整数。",
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				for (int i = 0; i < mTextPictColorType_cb.length; i++) {
					if (mTextPictColorType_cb[i].isChecked()) {
						colorType = i;
						break;
					}
				}
				if (data_ok) {
					savaPereferenceData();
					exit();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
