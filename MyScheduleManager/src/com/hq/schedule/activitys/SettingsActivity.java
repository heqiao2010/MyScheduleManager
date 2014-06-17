package com.hq.schedule.activitys;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.MenuItem;

import com.hq.schedule.R;
import com.hq.schedule.alarm.Alarms;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {

	private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;
	private static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";
	public static final String AUTO_DELETE_TIME_OUT_ALARM = "auto_delete_time_out_alarm"; // 是否自动删除过期日程
	public static final String KEY_ALARM_SNOOZE = "snooze_duration";
	public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// 静音模式下，闹钟是否响
		if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
			CheckBoxPreference pref = (CheckBoxPreference) preference;
			int ringerModeStreamTypes = Settings.System.getInt(
					getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

			if (pref.isChecked()) {
				ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
			} else {
				ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
			}

			Settings.System.putInt(getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED,
					ringerModeStreamTypes);

			return true;
		}
		// 是否自动删除过期日程
		if (AUTO_DELETE_TIME_OUT_ALARM.equals(preference.getKey())) {
			CheckBoxPreference pref = (CheckBoxPreference) preference;
			SharedPreferences prefs = SettingsActivity.this
					.getSharedPreferences(MainActivity.PREFERENCES, 0);
			SharedPreferences.Editor ed = prefs.edit();
			Boolean isAutoDelete = prefs.getBoolean(AUTO_DELETE_TIME_OUT_ALARM,
					false);
			if (pref.isChecked()) {
				if (!isAutoDelete) {
					Alarms.deleteAllTimeOutAlarms(SettingsActivity.this);
					ed.putBoolean(AUTO_DELETE_TIME_OUT_ALARM, true);
					ed.apply();
				}
			} else {
				ed.putBoolean(AUTO_DELETE_TIME_OUT_ALARM, false);
				ed.apply();
			}
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		final ListPreference listPref = (ListPreference) pref;
		final int idx = listPref.findIndexOfValue((String) newValue);
		listPref.setSummary(listPref.getEntries()[idx]);
		return true;
	}

	private void refresh() {
		@SuppressWarnings("deprecation")
		final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
		final int silentModeStreams = Settings.System.getInt(
				getContentResolver(),
				Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
		alarmInSilentModePref
				.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

		@SuppressWarnings("deprecation")
		final ListPreference snooze = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
		snooze.setSummary(snooze.getEntry());
		snooze.setOnPreferenceChangeListener(this);

		@SuppressWarnings("deprecation")
		final CheckBoxPreference autoDeleteTimeAutoPref = (CheckBoxPreference) findPreference(AUTO_DELETE_TIME_OUT_ALARM);
		SharedPreferences prefs = SettingsActivity.this.getSharedPreferences(
				MainActivity.PREFERENCES, 0);
		Boolean isAutoDelete = prefs.getBoolean(AUTO_DELETE_TIME_OUT_ALARM,
				false);
		autoDeleteTimeAutoPref.setChecked(isAutoDelete);
	}

	/**
	 * 菜单按钮点击事件，通过点击ActionBar的Home图标按钮来退出
	 */
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
