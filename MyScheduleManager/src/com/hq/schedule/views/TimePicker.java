package com.hq.schedule.views;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hq.schedule.activitys.MainActivity;
import com.hq.schedule.views.wheel.NumericWheelAdapter;
import com.hq.schedule.views.wheel.OnWheelChangedListener;
import com.hq.schedule.views.wheel.WheelView;


public class TimePicker extends LinearLayout {

	private Calendar calendar = Calendar.getInstance(); // 日历类
	private boolean isHourOfDay = true; // 24小时制
	private WheelView hours, mins; // Wheel picker
	private static OnChangeListener onChangeListener; // onChangeListener,注意设置为static

	// Constructors
	public TimePicker(Context context) {
		super(context);
		init(context);
	}

	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 初始化组件
	 * 
	 * @param context
	 */
	private void init(Context context) {
		this.setOrientation(LinearLayout.HORIZONTAL);
		//hours
		hours = new WheelView(context);
		LayoutParams lparams_hours = new LayoutParams(80,
				LayoutParams.WRAP_CONTENT);
		lparams_hours.setMargins(0, 10, 5, 0);
		hours.setLayoutParams(lparams_hours);
		hours.setAdapter(new NumericWheelAdapter(0, 23));
		hours.setVisibleItems(3);
		hours.setCyclic(true);	//循环显示
		hours.addChangingListener(onHoursChangedListener);
		addView(hours);
		//TextView
//		TextView tv = new TextView(context);
//		tv.setText(":");
//		LayoutParams lparams_textview= new LayoutParams(5,
//				LayoutParams.WRAP_CONTENT); 
//		tv.setLayoutParams(lparams_textview);
//		addView(tv);
		//mins
		mins = new WheelView(context);
		LayoutParams lparams_minuts= new LayoutParams(80,
				LayoutParams.WRAP_CONTENT);
		lparams_minuts.setMargins(5, 10, 0, 0);
		mins.setLayoutParams(lparams_minuts); /*new LayoutParams(80, LayoutParams.WRAP_CONTENT*/
		mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		mins.setVisibleItems(3);
		mins.setCyclic(true);	//循环显示
		mins.addChangingListener(onMinsChangedListener);
		addView(mins);
	}

	// listeners
	private OnWheelChangedListener onHoursChangedListener = new OnWheelChangedListener() {
		@Override
		public void onChanged(WheelView hours, int oldValue, int newValue) {
			calendar.set(Calendar.HOUR_OF_DAY, newValue);
			if (null == onChangeListener) {
				Log.e("main",
						"TimePicker.OnWheelChangedListener:onChanged"
				+" onChangeListener is null...");
			} else {
				onChangeListener.onChange(getHourOfDay(), getMinute());
				Log.e("main",
						"TimePicker.OnWheelChangedListener:onChanged onChangeListener:"
								+ onChangeListener.toString());
			}
		}
	};
	private OnWheelChangedListener onMinsChangedListener = new OnWheelChangedListener() {
		@Override
		public void onChanged(WheelView mins, int oldValue, int newValue) {
			calendar.set(Calendar.MINUTE, newValue);
			if (null == onChangeListener) {
				Log.e("main",
						"TimePicker.OnWheelChangedListener:onChanged onChangeListener is null...");
			} else {
				onChangeListener.onChange(getHourOfDay(), getMinute());
				Log.e("main",
						"TimePicker.OnWheelChangedListener:onChanged onChangeListener:"
								+ onChangeListener.toString());
			}
		}
	};

	/**
	 * 定义了监听时间改变的监听器借口
	 * 
	 * @author Wang_Yuliang
	 * 
	 */
	public interface OnChangeListener {
		void onChange(int hour, int munite);
	}

	/**
	 * 设置监听器的方法
	 * 
	 * @param onChangeListener
	 */
	public void setOnChangeListener(OnChangeListener mOnChangeListener) {
		if (null == mOnChangeListener) {
			Log.e("main",
					"TimePicker:setOnChangeListener mOnChangeListener: null");
		} else {
			Log.e("main",
					"TimePicker:setOnChangeListener mOnChangeListener:"
							+ mOnChangeListener.toString());
		}

		onChangeListener = mOnChangeListener;
		Log.e("main",
				"TimePicker:after setOnChangeListener OnChangeListener:"
						+ onChangeListener.toString());
	}

	/**
	 * 设置小时
	 * 
	 * @param hour
	 */
	public void setHourOfDay(int hour) {
		hours.setCurrentItem(hour);
	}

	/**
	 * 获得24小时制小时
	 * 
	 * @return
	 */
	public int getHourOfDay() {
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 设置分钟
	 */
	public void setMinute(int minute) {
		mins.setCurrentItem(minute);
	}

	/**
	 * 获得分钟
	 * 
	 * @return
	 */
	public int getMinute() {
		return calendar.get(Calendar.MINUTE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 默认设置为系统时间
		setHourOfDay(getHourOfDay());
		setMinute(getMinute());
	}
}
