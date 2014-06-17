package com.hq.schedule.views;

import com.hq.schedule.R;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class OverLay {
	private static TextView overlay = null;
	private static Handler handler; //隐藏overlay的线程
	private static OverlayThread overlayThread;
	private static OverLay mOverLay = null;
	public static Handler outerHandler = null;

	/* 
	 * 弹出提示框,这是个单例
	 */
	private OverLay(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
		overlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
		handler = new Handler();
		overlayThread = new OverlayThread();
	}

	// 设置overlay不可见
	private class OverlayThread implements Runnable {
		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
			if( null!=outerHandler){
				outerHandler.sendEmptyMessage(0);
			}
		}
	}
	
	//获取OverLay单例
	public synchronized static OverLay getOverLay(Context context) {
		if (null == mOverLay) {
			mOverLay = new OverLay(context);
		}
		return mOverLay;
	}

	public void setOuterHandler(Handler outerHandler){
		OverLay.outerHandler = outerHandler;
	}
	
	public void showOverLay(String msg, int delayTime) {
		overlay.setText(msg);
		overlay.setVisibility(View.VISIBLE);
		handler.removeCallbacks(overlayThread);
		// 延迟delayTime后执行，让overlay为不可见
		handler.postDelayed(overlayThread, delayTime);
	}
	
	public void setOverLayTextSize(float textSize){
		overlay.setTextSize(textSize);
	}
}
