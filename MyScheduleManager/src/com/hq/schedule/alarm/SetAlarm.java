/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hq.schedule.alarm;

import java.io.IOException;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
import com.hq.schedule.R;
import com.hq.schedule.activitys.NoteEdit;
import com.hq.schedule.activitys.ReminderList;
import com.hq.schedule.activitys.ShowPicActivity;
import com.hq.schedule.calendartools.SpecialCalendar;
import com.hq.schedule.category.CategoryPreference;
import com.hq.schedule.utility.GLFont;
import com.hq.schedule.utility.ToastMaster;

/**
 * 管理每一个闹钟 每一个闹钟对应的信息都绑定在Preference中了
 */
public class SetAlarm extends PreferenceActivity implements
		DateSlider.OnDateSetListener, Preference.OnPreferenceChangeListener {

	private EditTextPreference mLabel;
	private CheckBoxPreference mEnabledPref;
	private Preference mDatePref;
	private CategoryPreference mCategoryPref;
	private AlarmPreference mAlarmPref;
	private CheckBoxPreference mVibratePref;
	private RepeatPreference mRepeatPref;
	private int mId;
	private int mYear = 2014;
	private int mMonth = 4;
	private int mDay = 1;
	private int mHour;
	private int mMinutes;
	private Alarm mOriginalAlarm;
	private Boolean enable_revert = false;

	/**
	 * Set an alarm. Requires an Alarms.ALARM_ID to be passed in as an extra.
	 * FIXME: Pass an Alarm object like every other Activity.
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Override the default content view.
		setContentView(R.layout.set_alarm);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.alarm_prefs);
		// Get each preference so we can retrieve the value later.
		mLabel = (EditTextPreference) findPreference("label");
		mLabel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference p, Object newValue) {
				String val = (String) newValue;
				// Set the summary based on the new label.
				p.setSummary(val);
				if (val != null && !val.equals(mLabel.getText())) {
					// Call through to the generic listener.
					return SetAlarm.this.onPreferenceChange(p, newValue);
				}
				return true;
			}
		});
		mEnabledPref = (CheckBoxPreference) findPreference("enabled");
		mEnabledPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference p,
							Object newValue) {
						// Pop a toast when enabling alarms.
						if (!mEnabledPref.isChecked()) {
							popAlarmSetToast(SetAlarm.this, mHour, mMinutes,
									mRepeatPref.getDaysOfWeek());
						}
						return SetAlarm.this.onPreferenceChange(p, newValue);
					}
				});
		mDatePref = findPreference("date");
		mAlarmPref = (AlarmPreference) findPreference("alarm");
		mAlarmPref.setOnPreferenceChangeListener(this);
		mVibratePref = (CheckBoxPreference) findPreference("vibrate");
		mVibratePref.setOnPreferenceChangeListener(this);
		mRepeatPref = (RepeatPreference) findPreference("setRepeat");
		mRepeatPref.setOnPreferenceChangeListener(this);
		mCategoryPref = (CategoryPreference) findPreference("category");
		mCategoryPref.setOnPreferenceChangeListener(this);

		Intent i = getIntent();
		mId = i.getIntExtra(Alarms.ALARM_ID, -1);
		if (true) {
			Log.v("wangxianming", "In SetAlarm, alarm id = " + mId);
		}

		Alarm alarm = null;
		if (mId == -1) {
			// No alarm id means create a new alarm.
			alarm = new Alarm();
			// 获取年月日
			mYear = i.getIntExtra(Alarms.AlARM_TIME_YEAR, 2014);
			mMonth = i.getIntExtra(Alarms.AlARM_TIME_MONTH, 4);
			mDay = i.getIntExtra(Alarms.AlARM_TIME_DAY, 1);
			String label = i.getStringExtra(Alarm.Columns.MESSAGE);
			if (null != label) {
				alarm.label = label;
			}
			alarm.year = mYear;
			alarm.month = mMonth;
			alarm.day = mDay;
		} else {
			/* load alarm details from database */
			alarm = Alarms.getAlarm(getContentResolver(), mId);
			// Bad alarm, bail to avoid a NPE.
			if (alarm == null) {
				Intent intent = new Intent();
				setResult(ReminderList.RESULT_CANCELL, intent); // 点击取消，不返回数据
				exit();
				return;
			}
		}
		mOriginalAlarm = alarm;
		updatePrefs(mOriginalAlarm);

		// We have to do this to get the save/cancel buttons to highlight on
		// their own.
		getListView().setItemsCanFocus(true);

		// Attach actions to each button.
		// Button b = (Button) findViewById(R.id.alarm_save);
		// b.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// saveAlarm();
		// Intent intent = new Intent();
		// // 将日期和Alarm ID传给父Activity
		// intent.putExtra(Alarms.AlARM_TIME_YEAR, mYear);
		// intent.putExtra(Alarms.AlARM_TIME_MONTH, mMonth);
		// intent.putExtra(Alarms.AlARM_TIME_DAY, mDay);
		// intent.putExtra(Alarms.ALARM_ID, mId);
		// setResult(ReminderList.RESULT_ALARM_SAVE, intent);
		// finish();
		// }
		// });
		// final Button revert = (Button) findViewById(R.id.alarm_revert);
		// revert.setEnabled(false);
		// revert.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// int newId = mId;
		// updatePrefs(mOriginalAlarm);
		// // "Revert" on a newly created alarm should delete it.
		// if (mOriginalAlarm.id == -1) {
		// Alarms.deleteAlarm(SetAlarm.this, newId);
		// } else {
		// saveAlarm();
		// }
		// revert.setEnabled(false);
		// }
		// });
		// b = (Button) findViewById(R.id.alarm_delete);
		// if (mId == -1) {
		// b.setEnabled(false);
		// } else {
		// b.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// deleteAlarm();
		// }
		// });
		// }

		// The last thing we do is pop the time picker if this is a new alarm.
		if (mId == -1) {
			showDatePicker();
		}
	}

	// Used to post runnables asynchronously.
	private static final Handler sHandler = new Handler();

	@Override
	public boolean onPreferenceChange(final Preference p, Object newValue) {
		// Asynchronously save the alarm since this method is called _before_
		// the value of the preference has changed.
		sHandler.post(new Runnable() {
			public void run() {
				// Editing any preference (except enable) enables the alarm.
				if (p != mEnabledPref) {
					mEnabledPref.setChecked(true);
				}
				saveAlarmAndEnableRevert();
			}
		});
		return true;
	}

	private void updatePrefs(Alarm alarm) {
		mId = alarm.id;
		mEnabledPref.setChecked(alarm.enabled);
		mLabel.setText(alarm.label);
		mLabel.setSummary(alarm.label);
		// 获取年月日
		mYear = alarm.year;
		mMonth = alarm.month;
		mDay = alarm.day;
		mHour = alarm.hour;
		mMinutes = alarm.minutes;
		mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
		mCategoryPref.setCurrentCategoryID(alarm.category);
		mVibratePref.setChecked(alarm.vibrate);
		// 保存alarm uri 到 preferrence中
		mAlarmPref.setAlert(alarm.alert);
		updateDate();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mDatePref) {
			showDatePicker();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onBackPressed() {
		// In the usual case of viewing an alarm, mTimePickerCancelled is
		// initialized to false. When creating a new alarm, this value is
		// assumed true until the user changes the time.
		// if (!mTimePickerCancelled) {
		// saveAlarm();
		// }
		Intent intent = new Intent();
		setResult(ReminderList.RESULT_CANCELL, intent); // 点击取消，不返回数据
		exit();
	}

	private void showDatePicker() {
		// DateTimeSlider中Month： 0-11
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, mYear);
		c.set(Calendar.MONTH, mMonth - 1);
		c.set(Calendar.DAY_OF_MONTH, mDay);
		c.set(Calendar.HOUR_OF_DAY, mHour);
		c.set(Calendar.MINUTE, mMinutes);
		new DateTimeSlider(this, this, c, SpecialCalendar.getMinDate(),
				SpecialCalendar.getMaxDate()).show();
	}

	private void updateDate() {
		Log.v("main", "updateDate " + mId);
		mDatePref.setSummary(mYear + "年" + mMonth + "月" + mDay + "日 " + mHour
				+ ":" + mMinutes);
	}

	private long saveAlarmAndEnableRevert() {
		// Enable "Revert" to go back to the original Alarm.
		enable_revert = true;
		return saveAlarm();
	}

	private long saveAlarm() {
		Alarm alarm = new Alarm();
		alarm.id = mId;
		alarm.enabled = mEnabledPref.isChecked();
		alarm.year = mYear;
		alarm.month = mMonth;
		alarm.day = mDay;
		alarm.hour = mHour;
		alarm.minutes = mMinutes;
		alarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
		alarm.vibrate = mVibratePref.isChecked();
		alarm.label = mLabel.getText();
		alarm.alert = mAlarmPref.getAlert();
		alarm.category = mCategoryPref.getCurrentCategoryID();
		// set time
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, mYear);
		c.set(Calendar.MONTH, mMonth - 1); // Calendar的Month 0-11
		c.set(Calendar.DAY_OF_MONTH, mDay);
		c.set(Calendar.HOUR_OF_DAY, mHour);
		c.set(Calendar.MINUTE, mMinutes);
		alarm.time = c.getTimeInMillis();

		if (alarm.id == -1) {
			Alarms.addAlarm(this, alarm);
			// addAlarm populates the alarm with the new id. Update mId so that
			// changes to other preferences update the new alarm.
			mId = alarm.id;
		} else {
			Alarms.setAlarm(this, alarm);
		}
		return alarm.time;
	}

	private void deleteAlarm() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_alarm))
				.setMessage(getString(R.string.delete_alarm_confirm))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int w) {
								Alarms.deleteAlarm(SetAlarm.this, mId);
								Intent intent = new Intent();
								// 将日期和Alarm ID传给父Activity
								intent.putExtra(Alarms.AlARM_TIME_YEAR, mYear);
								intent.putExtra(Alarms.AlARM_TIME_MONTH, mMonth);
								intent.putExtra(Alarms.AlARM_TIME_DAY, mDay);
								intent.putExtra(Alarms.ALARM_ID, mId);
								setResult(ReminderList.RESULT_ALARM_DELLET,
										intent); // 点击取消，不返回数据
								exit();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	/**
	 * Display a toast that tells the user how long until the alarm goes off.
	 * This helps prevent "am/pm" mistakes.
	 */
	static void popAlarmSetToast(Context context, int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {
		popAlarmSetToast(context,
				Alarms.calculateAlarm(hour, minute, daysOfWeek)
						.getTimeInMillis());
	}

	static void popAlarmSetToast(Context context, long timeInMillis) {
		String toastText = formatToast(context, timeInMillis);
		Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		ToastMaster.setToast(toast);
		toast.show();
	}

	/**
	 * format "Alarm set for 2 days 7 hours and 53 minutes from now"
	 */
	static String formatToast(Context context, long timeInMillis) {
		long delta = timeInMillis - System.currentTimeMillis();
		long hours = delta / (1000 * 60 * 60);
		long minutes = delta / (1000 * 60) % 60;
		long days = hours / 24;
		hours = hours % 24;

		String daySeq = (days == 0) ? "" : (days == 1) ? context
				.getString(R.string.day) : context.getString(R.string.days,
				Long.toString(days));

		String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context
				.getString(R.string.minute) : context.getString(
				R.string.minutes, Long.toString(minutes));

		String hourSeq = (hours == 0) ? "" : (hours == 1) ? context
				.getString(R.string.hour) : context.getString(R.string.hours,
				Long.toString(hours));

		boolean dispDays = days > 0;
		boolean dispHour = hours > 0;
		boolean dispMinute = minutes > 0;

		int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0)
				| (dispMinute ? 4 : 0);

		String[] formats = context.getResources().getStringArray(
				R.array.alarm_set);
		return String.format(formats[index], daySeq, hourSeq, minSeq);
	}

	@Override
	public void onDateSet(DateSlider view, Calendar selectedDate) {
		// TODO Auto-generated method stub
		mYear = selectedDate.get(Calendar.YEAR);
		mMonth = selectedDate.get(Calendar.MONTH) + 1;
		mDay = selectedDate.get(Calendar.DAY_OF_MONTH);
		mHour = selectedDate.get(Calendar.HOUR_OF_DAY);
		mMinutes = selectedDate.get(Calendar.MINUTE);
		mEnabledPref.setChecked(true);
		updateDate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.set_alarm, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void exit() {
		finish();
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			exit();
			break;
		case R.id.set_alarm_save:
			saveAlarm();
			Intent intent = new Intent();
			// 将日期和Alarm ID传给父Activity
			intent.putExtra(Alarms.AlARM_TIME_YEAR, mYear);
			intent.putExtra(Alarms.AlARM_TIME_MONTH, mMonth);
			intent.putExtra(Alarms.AlARM_TIME_DAY, mDay);
			intent.putExtra(Alarms.ALARM_ID, mId);
			setResult(ReminderList.RESULT_ALARM_SAVE, intent);
			exit();
			break;
		case R.id.set_alarm_revert:
			if (enable_revert) {
				int newId = mId;
				updatePrefs(mOriginalAlarm);
				// "Revert" on a newly created alarm should delete it.
				if (mOriginalAlarm.id == -1) {
					Alarms.deleteAlarm(SetAlarm.this, newId);
				} else {
					saveAlarm();
				}
			}
			break;
		case R.id.set_alarm_delete:
			if (-1 == mId) {
				Toast.makeText(SetAlarm.this, "该日程未保存。", Toast.LENGTH_SHORT)
						.show();
			} else {
				deleteAlarm();
			}
			break;
		case R.id.set_alarm_share:
			String shareStr = mLabel.getText().toString();
			if (null == shareStr || "".equals(shareStr)) {
				Toast.makeText(SetAlarm.this, "请先输入分享信息。", Toast.LENGTH_SHORT)
						.show();
			} else {
				GLFont.shareText(shareStr, this);
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
