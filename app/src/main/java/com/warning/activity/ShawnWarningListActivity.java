package com.warning.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warning.R;
import com.warning.adapter.ShawnWarningAdapter;
import com.warning.adapter.WarningListAdapter1;
import com.warning.adapter.WarningListAdapter2;
import com.warning.adapter.WarningListAdapter3;
import com.warning.dto.WarningDto;
import com.warning.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警列表
 * @author shawn_sun
 */
public class ShawnWarningListActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private LinearLayout llSearch,llSelect;
	private ShawnWarningAdapter cityAdapter;
	private List<WarningDto> warningList = new ArrayList<>();//上个界面传过来的所有预警数据
	private List<WarningDto> showList = new ArrayList<>();//用于存放listview上展示的数据
	private List<WarningDto> searchList = new ArrayList<>();//用于存放搜索框搜索的数据
	private List<WarningDto> selecteList = new ArrayList<>();//用于存放三个sppiner删选的数据
	private TextView tv1, tv2, tv3;
	private ImageView iv1, iv2, iv3;
	private WarningListAdapter1 adapter1;
	private List<WarningDto> list1 = new ArrayList<>();
	private WarningListAdapter2 adapter2;
	private List<WarningDto> list2 = new ArrayList<>();
	private WarningListAdapter3 adapter3;
	private List<WarningDto> list3 = new ArrayList<>();
	private LinearLayout llContainer1, llContainer2, llContainer3;
	private String type = "999999";
	private String color = "999999";
	private String id = "999999";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_warning_list);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.warning_list));
		EditText etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		llSearch = findViewById(R.id.llSearch);
		llSelect = findViewById(R.id.llSelect);
		LinearLayout ll1 = findViewById(R.id.ll1);
		ll1.setOnClickListener(this);
		LinearLayout ll2 = findViewById(R.id.ll2);
		ll2.setOnClickListener(this);
		LinearLayout ll3 = findViewById(R.id.ll3);
		ll3.setOnClickListener(this);
		tv1 = findViewById(R.id.tv1);
		tv2 = findViewById(R.id.tv2);
		tv3 = findViewById(R.id.tv3);
		iv1 = findViewById(R.id.iv1);
		iv2 = findViewById(R.id.iv2);
		iv3 = findViewById(R.id.iv3);
		llContainer1 = findViewById(R.id.llContainer1);
		llContainer2 = findViewById(R.id.llContainer2);
		llContainer3 = findViewById(R.id.llContainer3);

		if (getIntent().hasExtra("warningList")) {
			boolean isVisible = getIntent().getBooleanExtra("isVisible", false);
			if (isVisible) {
				llSearch.setVisibility(View.VISIBLE);
				llSelect.setVisibility(View.VISIBLE);
			}else {
				llSearch.setVisibility(View.GONE);
				llSelect.setVisibility(View.GONE);
			}
			warningList.clear();
			showList.clear();
			warningList.addAll(getIntent().getExtras().<WarningDto>getParcelableArrayList("warningList"));
			showList.addAll(warningList);
			initListView();
			initGridView1();
			initGridView2();
			initGridView3();
		}else {
			OkhttpWarning();
		}
    }

    /**
     * 获取预警
     */
    private void OkhttpWarning() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//        final String url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=1";
				final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns";
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    boolean isVisible = getIntent().getBooleanExtra("isVisible", false);
                                    if (isVisible) {
                                        llSearch.setVisibility(View.VISIBLE);
                                        llSelect.setVisibility(View.VISIBLE);
                                    }else {
                                        llSearch.setVisibility(View.GONE);
                                        llSelect.setVisibility(View.GONE);
                                    }
                                    warningList.clear();
                                    showList.clear();
                                    try {
                                        final JSONObject object = new JSONObject(result);
										if (!object.isNull("data")) {
											JSONArray jsonArray = object.getJSONArray("data");
											for (int i = 0; i < jsonArray.length(); i++) {
												JSONArray tempArray = jsonArray.getJSONArray(i);
												WarningDto dto = new WarningDto();
												dto.html = tempArray.optString(1);
												String[] array = dto.html.split("-");
												String item0 = array[0];
												String item1 = array[1];
												String item2 = array[2];

												dto.item0 = item0;
												dto.provinceId = item0.substring(0, 2);
												dto.type = item2.substring(0, 5);
												dto.color = item2.substring(5, 7);
												dto.time = item1;
												dto.lng = tempArray.optString(2);
												dto.lat = tempArray.optString(3);
												dto.name = tempArray.optString(0);

												if (!dto.name.contains("解除")) {
													warningList.add(dto);
													showList.add(dto);
												}
											}

											initListView();
											initGridView1();
											initGridView2();
											initGridView3();

										}
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
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
			searchList.clear();
			if (!TextUtils.isEmpty(arg0.toString().trim())) {
				type = "999999";
				tv1.setText(getString(R.string.warning_class));
				for (int i = 0; i < list1.size(); i++) {
					if (i == 0) {
						adapter1.isSelected.put(i, true);
					}else {
						adapter1.isSelected.put(i, false);
					}
				}
				adapter1.notifyDataSetChanged();
				closeList(llContainer1, iv1);
				
				color = "999999";
				tv2.setText(getString(R.string.warning_level));
				for (int i = 0; i < list2.size(); i++) {
					if (i == 0) {
						adapter2.isSelected.put(i, true);
					}else {
						adapter2.isSelected.put(i, false);
					}
				}
				adapter2.notifyDataSetChanged();
				closeList(llContainer2, iv2);

				id = "999999";
				tv3.setText(getString(R.string.warning_district));
				for (int i = 0; i < list3.size(); i++) {
					if (i == 0) {
						adapter3.isSelected.put(i, true);
					}else {
						adapter3.isSelected.put(i, false);
					}
				}
				adapter3.notifyDataSetChanged();
				closeList(llContainer3, iv3);
				
				for (int i = 0; i < warningList.size(); i++) {
					WarningDto data = warningList.get(i);
					if (data.name.contains(arg0.toString().trim())) {
						searchList.add(data);
					}
				}
				showList.clear();
				showList.addAll(searchList);
				cityAdapter.notifyDataSetChanged();
			}else {
				showList.clear();
				showList.addAll(warningList);
				cityAdapter.notifyDataSetChanged();
			}
		}
	};
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		ListView cityListView = findViewById(R.id.cityListView);
		cityAdapter = new ShawnWarningAdapter(mContext, showList, false);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto data = showList.get(arg2);
				Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
				intentDetail.putExtra("url", data.html);
				startActivity(intentDetail);
			}
		});
	}
	
	private boolean isContainsType(String type, String selectType) {
		if (TextUtils.equals(selectType, "999999")) {
			return true;
		}
		if (type.contains(selectType)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isContainsColor(String color, String selectColor) {
		if (TextUtils.equals(selectColor, "999999")) {
			return true;
		}
		if (color.contains(selectColor)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isContainsId(String id, String selectId) {
		if (TextUtils.equals(selectId, "999999")) {
			return true;
		}
		if (id.contains(selectId)) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initGridView1() {
		list1.clear();
		String[] array1 = getResources().getStringArray(R.array.warningType);
		for (int i = 0; i < array1.length; i++) {
			HashMap<String, Integer> map = new HashMap<>();
			String[] value = array1[i].split(",");
			int count = 0;
			for (int j = 0; j < warningList.size(); j++) {
				WarningDto dto2 = warningList.get(j);
				String[] array = dto2.html.split("-");
				String type = array[2].substring(0, 5);
				if (TextUtils.equals(type, value[0])) {
					map.put(type, count++);
				}
			}

			WarningDto dto = new WarningDto();
			dto.name = value[1];
			dto.type = value[0];
			dto.count = count;
			if (i == 0 || count > 0) {
				list1.add(dto);
			}
		}

		GridView gridView1 = findViewById(R.id.gridView1);
		adapter1 = new WarningListAdapter1(mContext, list1);
		gridView1.setAdapter(adapter1);
		gridView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto dto = list1.get(arg2);
				if (TextUtils.equals(dto.name, getString(R.string.all))) {
					tv1.setText(getString(R.string.warning_class));
					type = dto.type;
				}else {
					tv1.setText(dto.name);
					type = dto.type;
				}
				for (int i = 0; i < list1.size(); i++) {
					if (i == arg2) {
						adapter1.isSelected.put(i, true);
					}else {
						adapter1.isSelected.put(i, false);
					}
				}
				adapter1.notifyDataSetChanged();
				closeList(llContainer1, iv1);
				
				selecteList.clear();
				for (int i = 0; i < warningList.size(); i++) {
					if (isContainsType(warningList.get(i).type, type)
							&& isContainsColor(warningList.get(i).color, color)
							&& isContainsId(warningList.get(i).provinceId, id)) {
						selecteList.add(warningList.get(i));
					}
				}
				showList.clear();
				showList.addAll(selecteList);
				cityAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initGridView2() {
		list2.clear();
		String[] array2 = getResources().getStringArray(R.array.warningColor);
		for (int i = 0; i < array2.length; i++) {
			HashMap<String, Integer> map = new HashMap<>();
			String[] value = array2[i].split(",");
			int count = 0;
			for (int j = 0; j < warningList.size(); j++) {
				WarningDto dto2 = warningList.get(j);
				String[] array = dto2.html.split("-");
				String color = array[2].substring(5, 7);
				if (TextUtils.equals(color, value[0])) {
					map.put(color, count++);
				}
			}

			WarningDto dto = new WarningDto();
			dto.name = value[1];
			dto.color = value[0];
			dto.count = count;
			if (i == 0 || count > 0) {
				list2.add(dto);
			}
		}

		GridView gridView2 = findViewById(R.id.gridView2);
		adapter2 = new WarningListAdapter2(mContext, list2);
		gridView2.setAdapter(adapter2);
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto dto = list2.get(arg2);
				if (TextUtils.equals(dto.name, getString(R.string.all))) {
					tv2.setText(getString(R.string.warning_level));
					color = dto.color;
				}else {
					tv2.setText(dto.name);
					color = dto.color;
				}
				for (int i = 0; i < list2.size(); i++) {
					if (i == arg2) {
						adapter2.isSelected.put(i, true);
					}else {
						adapter2.isSelected.put(i, false);
					}
				}
				adapter2.notifyDataSetChanged();
				closeList(llContainer2, iv2);
				
				selecteList.clear();
				for (int i = 0; i < warningList.size(); i++) {
						if (isContainsType(warningList.get(i).type, type)
								&& isContainsColor(warningList.get(i).color, color)
								&& isContainsId(warningList.get(i).provinceId, id)) {
							selecteList.add(warningList.get(i));
						}
				}
				showList.clear();
				showList.addAll(selecteList);
				cityAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initGridView3() {
		list3.clear();
		String[] array3 = getResources().getStringArray(R.array.warningDis);
		for (int i = 0; i < array3.length; i++) {
			HashMap<String, Integer> map = new HashMap<>();
			String[] value = array3[i].split(",");
			int count = 0;
			for (int j = 0; j < warningList.size(); j++) {
				WarningDto dto2 = warningList.get(j);
				String[] array = dto2.html.split("-");
				String provinceId = array[0].substring(0, 2);
				if (TextUtils.equals(provinceId, value[0])) {
					map.put(provinceId, count++);
				}
			}

			WarningDto dto = new WarningDto();
			dto.name = value[1];
			dto.provinceId = value[0];
			dto.count = count;
			if (i == 0 || count > 0) {
				list3.add(dto);
			}
		}

		GridView gridView3 = findViewById(R.id.gridView3);
		adapter3 = new WarningListAdapter3(mContext, list3);
		gridView3.setAdapter(adapter3);
		gridView3.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto dto = list3.get(arg2);
				if (TextUtils.equals(dto.name, getString(R.string.all))) {
					tv3.setText(getString(R.string.warning_district));
					id = dto.provinceId;
				}else {
					tv3.setText(dto.name);
					id = dto.provinceId;
				}
				for (int i = 0; i < list3.size(); i++) {
					if (i == arg2) {
						adapter3.isSelected.put(i, true);
					}else {
						adapter3.isSelected.put(i, false);
					}
				}
				adapter3.notifyDataSetChanged();
				closeList(llContainer3, iv3);
				
				selecteList.clear();
				for (int i = 0; i < warningList.size(); i++) {
						if (isContainsType(warningList.get(i).type, type)
								&& isContainsColor(warningList.get(i).color, color)
								&& isContainsId(warningList.get(i).provinceId, id)) {
							selecteList.add(warningList.get(i));
						}
				}
				showList.clear();
				showList.addAll(selecteList);
				cityAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final LinearLayout view) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (!flag) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		view.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				view.clearAnimation();
			}
		});
	}
	
	private void bootAnimation(LinearLayout view, ImageView imageView) {
		if (view.getVisibility() == View.GONE) {
			openList(view, imageView);
		}else {
			closeList(view, imageView);
		}
	}
	
	private void openList(LinearLayout view, ImageView imageView) {
		if (view.getVisibility() == View.GONE) {
			startAnimation(false, view);
			view.setVisibility(View.VISIBLE);
			imageView.setImageResource(R.drawable.iv_arrow_black_up);
		}
	}
	
	private void closeList(LinearLayout view, ImageView imageView) {
		if (view.getVisibility() == View.VISIBLE) {
			startAnimation(true, view);
			view.setVisibility(View.GONE);
			imageView.setImageResource(R.drawable.iv_arrow_black_down);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ll1:
			bootAnimation(llContainer1, iv1);
			closeList(llContainer2, iv2);
			closeList(llContainer3, iv3);
			break;
		case R.id.ll2:
			bootAnimation(llContainer2, iv2);
			closeList(llContainer1, iv1);
			closeList(llContainer3, iv3);
			break;
		case R.id.ll3:
			bootAnimation(llContainer3, iv3);
			closeList(llContainer1, iv1);
			closeList(llContainer2, iv2);
			break;

		default:
			break;
		}
	}
	
}
