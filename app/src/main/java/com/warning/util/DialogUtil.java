package com.warning.util;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;

import static com.warning.activity.BaseActivity.REALNAME;

public class DialogUtil {

	/**
	 * 欢迎对话框
	 */
	public static void welcomeDialog(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("SHOWWELCOME", Context.MODE_PRIVATE);
		String version = sharedPreferences.getString("version", "");
		if (TextUtils.equals(version, CommonUtil.getVersion(context))) {
			return;
		}else {
			Editor editor = sharedPreferences.edit();
			editor.putString("version", CommonUtil.getVersion(context));
			editor.apply();
		}
		
		if (TextUtils.isEmpty(REALNAME)) {
			return;
		}
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_welcome, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText("气象信息员"+REALNAME+"，欢迎您！");
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
}
