package com.hq.schedule.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.hq.schedule.activitys.ShowPicActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

public class GLFont {

	public static void shareText(String text, Context context) {
		int textLength = SharedPreferenceHelper.getTextLength(context);
		if (text.length() < textLength) { // 分享文字 140
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
			intent.putExtra(Intent.EXTRA_TEXT, text);
			context.startActivity(Intent.createChooser(intent, "分享到:"));
		} else {
			// 获取文字大小
			int textSize = SharedPreferenceHelper.getTextSize(context);
			if (textSize < 0) { // 避免错误的textSize
				textSize = 20;
			}
			int colorType = SharedPreferenceHelper
					.getTextPictColorType(context);
			Bitmap mBitmap = GLFont.textAsBitmap(text, textSize, colorType);
			try {
				String filePath = GLFont.saveMyBitmap("test", mBitmap);
				Intent intent = new Intent();
				intent.setClass(context, ShowPicActivity.class);
				intent.putExtra("filePath", filePath);
				context.startActivity(intent);
			} catch (IOException e) {
				Toast.makeText(context, "生成图片失败。" + e.toString(),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
	}

	public static Bitmap textAsBitmap(String text, float textSize, int colorType) {
		TextPaint textPaint = new TextPaint();
		// textPaint.setARGB(0x31, 0x31, 0x31, 0);
		switch (colorType) {
		case 1:
			textPaint.setColor(Color.WHITE);
			break;
		case 2:
		case 3:
			textPaint.setColor(Color.RED);
			break;
		default:
			textPaint.setColor(Color.BLACK);
		}
		textPaint.setTextSize(textSize);
		StaticLayout layout = new StaticLayout(text, textPaint, 450,
				Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
		Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
				layout.getHeight() + 20, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.translate(10, 10);
		switch (colorType) {
		case 1:
		case 3:
			canvas.drawColor(Color.BLACK);
			break;
		case 2:
			canvas.drawColor(Color.WHITE);
			break;
		default:
			canvas.drawColor(Color.WHITE);
		}
		layout.draw(canvas);
		Log.d("main",
				String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
		return bitmap;
	}

	public static String saveMyBitmap(String bitName, Bitmap mBitmap)
			throws IOException {
		String filePath = Environment.getExternalStorageDirectory().getPath()
				.toString()
				+ "/" + bitName + ".png";
		File f = new File(filePath);
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filePath;
	}

	// /*
	// * 默认采用白色字体，宋体文字加粗
	// */
	// public static Bitmap getImage(int width, int height, String mString,
	// int size) {
	// return getImage(width, height, mString, size, Color.RED,
	// Typeface.create("宋体", Typeface.BOLD));
	// }
	//
	// public static Bitmap getImage(int width, int height, String mString,
	// int size, int color) {
	// return getImage(width, height, mString, size, color,
	// Typeface.create("宋体", Typeface.BOLD));
	// }
	//
	// public static Bitmap getImage(int width, int height, String mString,
	// int size, int color, String familyName) {
	// return getImage(width, height, mString, size, color,
	// Typeface.create(familyName, Typeface.BOLD));
	// }
	//
	// public static Bitmap getImage(int width, int height, String mString,
	// int size, int color, Typeface font) {
	// int x = width;
	// int y = height;
	//
	// Bitmap bmp = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
	// // 图象大小要根据文字大小算下,以和文本长度对应
	// Canvas canvasTemp = new Canvas(bmp);
	// canvasTemp.drawColor(Color.BLACK);
	// Paint p = new Paint();
	// p.setColor(color);
	// p.setTypeface(font);
	// p.setAntiAlias(true);// 去除锯齿
	// p.setFilterBitmap(true);// 对位图进行滤波处理
	// p.setTextSize(scalaFonts(size));
	// float tX = (x - getFontlength(p, mString)) / 2;
	// float tY = (y - getFontHeight(p)) / 2 + getFontLeading(p);
	// canvasTemp.drawText(mString, tX, tY, p);
	//
	// return bmp;
	// }
	//
	// /**
	// * 根据屏幕系数比例获取文字大小
	// *
	// * @return
	// */
	// private static float scalaFonts(int size) {
	// // 暂未实现
	// return size;
	// }
	//
	// /**
	// * @return 返回指定笔和指定字符串的长度
	// */
	// public static float getFontlength(Paint paint, String str) {
	// return paint.measureText(str);
	// }
	//
	// /**
	// * @return 返回指定笔的文字高度
	// */
	// public static float getFontHeight(Paint paint) {
	// FontMetrics fm = paint.getFontMetrics();
	// return fm.descent - fm.ascent;
	// }
	//
	// /**
	// * @return 返回指定笔离文字顶部的基准距离
	// */
	// public static float getFontLeading(Paint paint) {
	// FontMetrics fm = paint.getFontMetrics();
	// return fm.leading - fm.ascent;
	// }
}
