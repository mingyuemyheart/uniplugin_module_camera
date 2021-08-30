package com.warning.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.common.PgyApplication;

/**
 * 预警推送策略
 */

public class PushActivity extends BaseActivity implements OnClickListener{
	
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private boolean state1 = true, state2 = true, state3 = true, state4 = true, state5 = true, state6 = true, state7 = true, state8 = true,
			state9 = true, state10 = false, state11 = false;
	private Editor editor = null;
	private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7, checkBox8;
	private LinearLayout ll1, ll2, ll3;
	private ImageView iv1, iv2, iv3;
	private String tag1, tag2, tag3, tag4, tag5, tag6, tag7, tag8;
	private String tags = "";
	private TextView tvPrompt;
	private TextView tvTags;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_msg);
		initWidget();
	}
	
	private void initWidget() {
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		tvPrompt.setOnLongClickListener(v -> {
			if (tvTags.getVisibility() == View.GONE) {
				tvTags.setVisibility(View.VISIBLE);
			}else {
				tvTags.setVisibility(View.GONE);
			}
			return false;
		});
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.setting_push));
		checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
		checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
		checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
		checkBox4 = (CheckBox) findViewById(R.id.checkBox4);
		checkBox5 = (CheckBox) findViewById(R.id.checkBox5);
		checkBox6 = (CheckBox) findViewById(R.id.checkBox6);
		checkBox7 = (CheckBox) findViewById(R.id.checkBox7);
		checkBox8 = (CheckBox) findViewById(R.id.checkBox8);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll1.setOnClickListener(this);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		ll2.setOnClickListener(this);
		ll3 = (LinearLayout) findViewById(R.id.ll3);
		ll3.setOnClickListener(this);
		iv1 = (ImageView) findViewById(R.id.iv1);
		iv2 = (ImageView) findViewById(R.id.iv2);
		iv3 = (ImageView) findViewById(R.id.iv3);
		tvTags = (TextView) findViewById(R.id.tvTags);
		tvTags.setText(PgyApplication.TAGS);

		SharedPreferences sp = getSharedPreferences("CHECKBOX", Context.MODE_PRIVATE);
		state1 = sp.getBoolean("state1", true);
		state2 = sp.getBoolean("state2", true);
		state3 = sp.getBoolean("state3", true);
		state4 = sp.getBoolean("state4", true);
		state5 = sp.getBoolean("state5", true);
		state6 = sp.getBoolean("state6", true);
		state7 = sp.getBoolean("state7", true);
		state8 = sp.getBoolean("state8", true);
		state9 = sp.getBoolean("state9", true);
		state10 = sp.getBoolean("state10", false);
		state11 = sp.getBoolean("state11", false);
		editor = sp.edit();
		
		if (state1) {
			checkBox1.setChecked(true);
			tag1 = PgyApplication.nationTag;
		}else {
			checkBox1.setChecked(false);
			tag1 = "";
		}
		if (state2) {
			checkBox2.setChecked(true);
			tag2 = PgyApplication.proTag;
		}else {
			checkBox2.setChecked(false);
			tag2 = "";
		}
		if (state3) {
			checkBox3.setChecked(true);
			tag3 = PgyApplication.cityTag;
		}else {
			checkBox3.setChecked(false);
			tag3 = "";
		}
		if (state4) {
			checkBox4.setChecked(true);
			tag4 = PgyApplication.disTag;
		}else {
			checkBox4.setChecked(false);
			tag4 = "";
		}
		if (state5) {
			checkBox5.setChecked(true);
			tag5 = PgyApplication.redTag;
		}else {
			checkBox5.setChecked(false);
			tag5 = "";
		}
		if (state6) {
			checkBox6.setChecked(true);
			tag6 = PgyApplication.orangeTag;
		}else {
			checkBox6.setChecked(false);
			tag6 = "";
		}
		if (state7) {
			checkBox7.setChecked(true);
			tag7 = PgyApplication.yellowTag;
		}else {
			checkBox7.setChecked(false);
			tag7 = "";
		}
		if (state8) {
			checkBox8.setChecked(true);
			tag8 = PgyApplication.blueTag;
		}else {
			checkBox8.setChecked(false);
			tag8 = "";
		}
		if (state9) {
			iv1.setImageResource(R.drawable.iv_checkbox_selected);
			iv2.setImageResource(R.drawable.iv_checkbox);
			iv3.setImageResource(R.drawable.iv_checkbox);
			tvPrompt.setText("全时段接收预警推送");
		}
		if (state10) {
			iv1.setImageResource(R.drawable.iv_checkbox);
			iv2.setImageResource(R.drawable.iv_checkbox_selected);
			iv3.setImageResource(R.drawable.iv_checkbox);
			tvPrompt.setText("夜间（晚22:00至早8:00期间）不接收预警推送");
		}
		if (state11) {
			iv1.setImageResource(R.drawable.iv_checkbox);
			iv2.setImageResource(R.drawable.iv_checkbox);
			iv3.setImageResource(R.drawable.iv_checkbox_selected);
			tvPrompt.setText("全时段关闭预警推送");
		}
		tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
		
		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					tag1 = PgyApplication.nationTag;
				}else {
					tag1 = "";
				}
				tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
				editor.putBoolean("state1", arg1);
				editor.apply();
			}
		});
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					tag2 = PgyApplication.proTag;
				}else {
					tag2 = "";
				}
				tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
				editor.putBoolean("state2", arg1);
				editor.apply();
			}
		});
		checkBox3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					tag3 = PgyApplication.cityTag;
				}else {
					tag3 = "";
				}
				tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
				editor.putBoolean("state3", arg1);
				editor.apply();
			}
		});
		checkBox4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				checkBox4.setChecked(true);
			}
		});
		checkBox5.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				checkBox5.setChecked(true);
			}
		});
		checkBox6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				checkBox6.setChecked(true);
			}
		});
		checkBox7.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					tag7 = PgyApplication.yellowTag;
				}else {
					tag7 = "";
				}
				tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
				editor.putBoolean("state7", arg1);
				editor.apply();
			}
		});
		checkBox8.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					tag8 = PgyApplication.blueTag;
				}else {
					tag8 = "";
				}
				tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
				editor.putBoolean("state8", arg1);
				editor.apply();
			}
		});
	} 

	/**
	 * 设置umeng推送的tags
	 */
	private void setPushTags() {
		if (!TextUtils.isEmpty(tags)) {
			PgyApplication.addPushTags(tags);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setPushTags();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setPushTags();
			finish();
			break;
		case R.id.ll1:
			iv1.setImageResource(R.drawable.iv_checkbox_selected);
			iv2.setImageResource(R.drawable.iv_checkbox);
			iv3.setImageResource(R.drawable.iv_checkbox);
			tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
			tvPrompt.setText("全时段接收预警推送");
			editor.putBoolean("state9", true);
			editor.putBoolean("state10", false);
			editor.putBoolean("state11", false);
			editor.apply();
			
			PgyApplication.enablePush();
			break;
		case R.id.ll2:
			iv1.setImageResource(R.drawable.iv_checkbox);
			iv2.setImageResource(R.drawable.iv_checkbox_selected);
			iv3.setImageResource(R.drawable.iv_checkbox);
			tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
			tvPrompt.setText("夜间（晚22:00至早8:00期间）不接收预警推送");
			editor.putBoolean("state9", false);
			editor.putBoolean("state10", true);
			editor.putBoolean("state11", false);
			editor.apply();
			
			PgyApplication.setNoDisturbMode();
			break;
		case R.id.ll3:
			iv1.setImageResource(R.drawable.iv_checkbox);
			iv2.setImageResource(R.drawable.iv_checkbox);
			iv3.setImageResource(R.drawable.iv_checkbox_selected);
			tags = tag1+","+tag2+","+tag3+","+tag4+","+tag5+","+tag6+","+tag7+","+tag8;
			tvPrompt.setText("全时段关闭预警推送");
			editor.putBoolean("state9", false);
			editor.putBoolean("state10", false);
			editor.putBoolean("state11", true);
			editor.apply();
			
			PgyApplication.disablePush();
			break;

		default:
			break;
		}
	}
	
}
