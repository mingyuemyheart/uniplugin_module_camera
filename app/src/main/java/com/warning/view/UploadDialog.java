package com.warning.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.warning.R;

public class UploadDialog extends Dialog {

	private Context mContext;
	private String message = null;
	private TextView tvPercent = null;

	public UploadDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	public UploadDialog(Context context, String msg) {
		super(context);
		mContext = context;
		message = msg;
	}
	
	public void setStyle(int featureId) {
		requestWindowFeature(featureId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(R.color.transparent);
		setContentView(R.layout.dialog_upload);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		TextView tvContent = findViewById(R.id.content);
		tvPercent = findViewById(R.id.tvPercent);

		if (tvContent != null) {
			if (message == null) {
				tvContent.setText("正在加载...");
			} else {
				tvContent.setText(message);
			}
		}
		
	}
	
	public void setPercent(int percent) {
		if (tvPercent != null) {
			tvPercent.setText(percent + "%");
			Log.e("percent", percent + "%");
		}
	}
	
}
