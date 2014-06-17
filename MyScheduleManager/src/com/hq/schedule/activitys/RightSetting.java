package com.hq.schedule.activitys;

import com.hq.schedule.R;
import com.hq.schedule.utility.SharedPreferenceHelper;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RightSetting extends ListFragment {
	private SampleAdapter animation_dialog_adapter = null;
	private int animation_type = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.right_setting, null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// 获取点击事件
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		switch (position) {
		case 0:
			// 闹钟设置
			intent.setClass(getActivity(), SettingsActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 1:
			// 搜索
			intent.setClass(getActivity(), ReminderSearch.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 2:
			// 日程分组管理
			intent.setClass(getActivity(), CategoryManage.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		// note book
		case 3:
			intent.setClass(getActivity(), NoteBookActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		// manage app info
		case 4:
			intent.setClass(getActivity(), AppInfoManage.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		case 5:
			showSetAnimationTypeDialog();
			break;
		case 6:
			intent.setClass(getActivity(), Text2PictSetting.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		// 账户管理
		case 7:
			intent.setClass(getActivity(), AccountManage.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.push_left_in,
					R.anim.push_left_out);
			break;
		// exit
		case 8:
			getActivity().finish();
			break;
		default:
			Log.e("main", "Wrong position: " + position);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		adapter.add(new SampleItem("闹钟设置", R.drawable.setting));
		adapter.add(new SampleItem("日程搜索", R.drawable.search));
		adapter.add(new SampleItem("日程类别管理", R.drawable.groupmg));
		adapter.add(new SampleItem("记事本", R.drawable.notes));
		adapter.add(new SampleItem("应用信息管理", R.drawable.safe));
		adapter.add(new SampleItem("日历切换动画设置", R.drawable.swap));
		adapter.add(new SampleItem("文字图片设置", R.drawable.timage));
		adapter.add(new SampleItem("账户管理", R.drawable.user));
		adapter.add(new SampleItem("退出", R.drawable.power));
		setListAdapter(adapter);
	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	private void showSetAnimationTypeDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.animation_dialog,
				(ViewGroup) getActivity().findViewById(R.id.animation_dialog));
		ListView animation_dialog_list = (ListView) layout
				.findViewById(R.id.animation_dialog_list);
		animation_dialog_adapter = new SampleAdapter(getActivity());
		animation_dialog_adapter.add(new SampleItem("推动效果（默认）",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("淡入淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("放大淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("转动淡入效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("转动淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("左上角展开淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("压缩变小淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("波浪放大淡出效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("缩小效果",
				R.drawable.heart_empty));
		animation_dialog_adapter.add(new SampleItem("上下交错效果",
				R.drawable.heart_empty));

		animation_type = SharedPreferenceHelper.getAnimationType(getActivity());
		animation_dialog_adapter.getItem(animation_type).iconRes = R.drawable.heart_full;

		animation_dialog_list.setAdapter(animation_dialog_adapter);
		animation_dialog_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				animation_dialog_adapter.getItem(animation_type).iconRes = R.drawable.heart_empty;
				animation_type = position;
				animation_dialog_adapter.getItem(animation_type).iconRes = R.drawable.heart_full;
				animation_dialog_adapter.notifyDataSetChanged();
			}
		});
		new AlertDialog.Builder(getActivity()).setTitle("设置日历跳转动画")
				.setView(layout).setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MainActivity.mainFragment.setAnimation(animation_type);
						SharedPreferenceHelper.saveAnimationType(
								animation_type, getActivity());
					}
				}).show();
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.right_setting_row, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);
			return convertView;
		}
	}
}
