package com.warning.activity;

/**
 * 城市选择
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warning.R;
import com.warning.adapter.CityAdapter;
import com.warning.adapter.CityFragmentAdapter;
import com.warning.dto.CityDto;
import com.warning.manager.DBManager;

public class CityActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private EditText etSearch = null;
	private TextView tvNational = null;//全国热门
	private LinearLayout llHot = null;
	
	//搜索城市后的结果列表
	private ListView mListView = null;
	private CityAdapter cityAdapter = null;
	private List<CityDto> cityList = new ArrayList<>();

	//全国热门
	private GridView nGridView = null;
	private CityFragmentAdapter nAdapter = null;
	private List<CityDto> nList = new ArrayList<>();
	
	private String locationId = null;//定位城市的id
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		mContext = this;
		initWidget();
		initListView();
		initNGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		tvNational = (TextView) findViewById(R.id.tvNational);
		tvNational.setOnClickListener(this);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.add_city));
		llHot = (LinearLayout) findViewById(R.id.llHot);
		
		locationId = getIntent().getStringExtra("cityId");
	}
	
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (arg0.toString() == null) {
				return;
			}

			cityList.clear();
			if (arg0.toString().trim().equals("")) {
				mListView.setVisibility(View.GONE);
				nGridView.setVisibility(View.VISIBLE);
				llHot.setVisibility(View.VISIBLE);
			}else {
				mListView.setVisibility(View.VISIBLE);
				nGridView.setVisibility(View.GONE);
				llHot.setVisibility(View.GONE);
				searchCityByKeyword(arg0.toString().trim());
			}

		}
	};
	
	/**
	 * 初始化数据库
	 */
	private void searchCityByKeyword(String keyword) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where pro like " + "\"%" + keyword + "%\"" + " or city like " + "\"%" + keyword + "%\"" + " or dis like " + "\"%" + keyword + "%\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			CityDto dto = new CityDto();
			dto.proName = cursor.getString(cursor.getColumnIndex("pro"));
			dto.cityName = cursor.getString(cursor.getColumnIndex("city"));
			dto.disName = cursor.getString(cursor.getColumnIndex("dis"));
			dto.cityId = cursor.getString(cursor.getColumnIndex("cid"));
			dto.warningId = cursor.getString(cursor.getColumnIndex("wid"));
			cityList.add(dto);
		}
		
		if (cityAdapter != null) {
			cityAdapter.notifyDataSetChanged();
		}
		
		getLocationCity(locationId, cityList);
		getSelectedCitys(cityList);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		cityAdapter = new CityAdapter(mContext, cityList);
		mListView.setAdapter(cityAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				CityDto dto = cityList.get(arg2);
				if (dto.isLocation || dto.isSelected) {
					finish();
				}else {
					intentAttention(dto.disName, dto.cityId, dto.warningId);
				}
			}
		});
	}
	
	/**
	 * 获取全国热门城市
	 * @return
	 */
	private void getNationHotCity() {
		nList.clear();
		String[] array = getResources().getStringArray(R.array.hotCity);
		for (int i = 0; i < array.length; i++) {
			String[] itemArray = array[i].split(",");
			CityDto dto = new CityDto();
			dto.cityId = itemArray[0];
			dto.cityName = itemArray[1];
			dto.warningId = itemArray[2];
			nList.add(dto);
		}
		
		getLocationCity(locationId, nList);
		getSelectedCitys(nList);
	}
	
	/**
	 * 初始化全国热门
	 */
	private void initNGridView() {
		getNationHotCity();
		nGridView = (GridView) findViewById(R.id.nGridView);
		nAdapter = new CityFragmentAdapter(mContext, nList);
		nGridView.setAdapter(nAdapter);
		nGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				CityDto dto = nList.get(arg2);
				if (dto.isLocation || dto.isSelected) {
					finish();
				}else {
					intentAttention(dto.cityName, dto.cityId, dto.warningId);
				}
			}
		});
	}
	
	/**
	 * 获取定位城市id
	 * @param locationId
	 * @param list
	 */
	private void getLocationCity(String locationId, List<CityDto> list) {
		if (TextUtils.isEmpty(locationId)) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			if (TextUtils.equals(locationId, list.get(i).cityId)) {
				list.get(i).isLocation = true;
			}
		}
	}
	
	/**
	 * //获取所有关注城市的id
	 * @param list
	 */
	private void getSelectedCitys(List<CityDto> list) {
		SharedPreferences sharedPreferences = getSharedPreferences("CITYIDS", Context.MODE_PRIVATE);
		String cityInfo = sharedPreferences.getString("cityInfo", null);
		if (!TextUtils.isEmpty(cityInfo)) {
			String[] info = cityInfo.split(";");
			for (int m = 0; m < info.length; m++) {
				String[] ids = info[m].split(",");
				for (int j = 0; j < list.size(); j++) {
					if (TextUtils.equals(ids[0], list.get(j).cityId)) {
						list.get(j).isSelected = true;
					}
				}
			}
		}
	}
	
	/**
	 * 迁移到关注列表页面
	 */
	private void intentAttention(String cityName, String cityId, String warningId) {
		Intent intent = new Intent();
		intent.putExtra("cityName", cityName);
		intent.putExtra("cityId", cityId);
		intent.putExtra("warningId", warningId);
		setResult(RESULT_OK, intent);
		finish();
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
