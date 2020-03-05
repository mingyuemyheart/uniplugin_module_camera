package com.warning.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warning.R;
import com.warning.adapter.YiqingAdapter;
import com.warning.dto.YiqingDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 预警列表
 * @author shawn_sun
 */
public class YiqingListActivity extends BaseActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yiqing_list);
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("全球疫情列表");
    }

	/**
	 * 初始化listview
	 */
	private void initListView() {
		List<YiqingDto> dataList = new ArrayList<>();
		dataList.addAll(getIntent().getParcelableArrayListExtra("dataList"));

		ListView listView = findViewById(R.id.listView);
		YiqingAdapter mAdapter = new YiqingAdapter(this, dataList);
		listView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
