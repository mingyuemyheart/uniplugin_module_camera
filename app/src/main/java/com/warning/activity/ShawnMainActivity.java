package com.warning.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.warning.R;
import com.warning.adapter.MyPagerAdapter;
import com.warning.adapter.ShawnLeftAdapter;
import com.warning.common.CONST;
import com.warning.common.PgyApplication;
import com.warning.dto.NewsDto;
import com.warning.dto.WarningDto;
import com.warning.fragment.Fragment1;
import com.warning.fragment.Fragment2;
import com.warning.fragment.Fragment3;
import com.warning.fragment.Fragment4;
import com.warning.fragment.Fragment5;
import com.warning.util.AutoUpdateUtil;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;
import com.warning.view.MainViewPager;
import com.yanzhenjie.sofia.Sofia;

import net.tsz.afinal.FinalBitmap;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 主页面
 */
public class ShawnMainActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private TextView tv1, tv2, tv3, tv4, tv5;
	private ImageView iv1, iv2, iv3, iv4, iv5;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private MyBroadCastReceiver mReceiver;
	private String locationId;
	private String BROADCAST_ACTION_NAME = "";//四个fragment广播名字
	private boolean isShowYiqing = false;
	private TextView tvTitle;
	private ImageView ivTitle,ivMenu,ivMy;
	
	//侧拉页面
	private DrawerLayout drawerlayout;
	private RelativeLayout reLeft;
	private TextView tvEdit;
	private ShawnLeftAdapter leftAdapter;
	private List<WarningDto> leftList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_main);
		mContext = this;
		if (!TextUtils.isEmpty(PgyApplication.getTop_img())) {
			Sofia.with(this)
					.invasionNavigationBar()
					.statusBarLightFont()//状态栏浅色字体
					.invasionStatusBar()//内容入侵状态栏
					.navigationBarBackground(ContextCompat.getColor(this, R.color.transparent))//导航栏背景色
					.statusBarBackground(ContextCompat.getColor(this, R.color.transparent));//状态
		}

		//是否显示隐私政策
		SharedPreferences sp1 = getSharedPreferences("SHOWPOLICY", Context.MODE_PRIVATE);
		boolean isShow = sp1.getBoolean("isShow", true);
		if (isShow) {
			promptDialog(sp1);
		}
		okHttpYiqingState();
	}

	/**
	 * 温馨提示对话框
	 */
	private void promptDialog(final SharedPreferences sp) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_prompt, null);
		TextView tvProtocal = view.findViewById(R.id.tvProtocal);
		TextView tvPolicy = view.findViewById(R.id.tvPolicy);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvProtocal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("isShow", false);
				editor.apply();
				Intent intent = new Intent(mContext, WebviewActivity.class);
				Bundle bundle = new Bundle();
				NewsDto data = new NewsDto();
				data.title = "用户协议";
				data.url = "http://12379.tianqi.cn/Public/12379_app_yhxy.html";
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		tvPolicy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("isShow", false);
				editor.apply();
				Intent intent = new Intent(mContext, WebviewActivity.class);
				Bundle bundle = new Bundle();
				NewsDto data = new NewsDto();
				data.title = "隐私政策";
				data.url = "http://12379.tianqi.cn/Public/12379_app_yszc.html";
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				finish();
			}
		});
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("isShow", false);
				editor.apply();
			}
		});
	}

	private void init() {
		initBroadcast();
		initWidget();
		initViewPager();
		initListView();
	}

	private void initBroadcast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.BROADCAST_DRAWER);
		intentFilter.addAction(CONST.REFRESH_ATTENTION_LIST);
		intentFilter.addAction(CONST.BROADCAST_FRAGMENT1TOFRAGMENT2);
		registerReceiver(mReceiver, intentFilter);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (TextUtils.equals(arg1.getAction(), CONST.BROADCAST_DRAWER)) {//关闭或打开侧拉页面
				if (drawerlayout.isDrawerOpen(reLeft)) {
					drawerlayout.closeDrawer(reLeft);
				}else {
					drawerlayout.openDrawer(reLeft);
					//统计点击次数
					StatisticUtil.submitClickCount("3", "城市订阅");
				}
			} else if (TextUtils.equals(arg1.getAction(), CONST.REFRESH_ATTENTION_LIST)) {
				locationId = arg1.getStringExtra("locationId");
				WarningDto data = arg1.getExtras().getParcelable("data");
				if (data != null) {
					boolean isRefresh = arg1.getBooleanExtra("isRefresh", true);
					if (isRefresh) {
						leftList.clear();
					}
					leftList.add(data);
				}
				if (leftAdapter != null) {
					leftAdapter.notifyDataSetChanged();
				}
				saveCitysToLocal();
				cancelDialog();
			} else if (TextUtils.equals(arg1.getAction(), CONST.BROADCAST_FRAGMENT1TOFRAGMENT2)) {//fragment1跳转到fragment2
				if (viewPager != null) {
					viewPager.setCurrentItem(1, true);
				}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}
	
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(this, mContext, "44", getString(R.string.app_name), true);

		ivMenu = findViewById(R.id.ivMenu);
		ivMenu.setOnClickListener(this);
		ivMy = findViewById(R.id.ivMy);
		ivTitle = findViewById(R.id.ivTitle);
		tvTitle = findViewById(R.id.tvTitle);
		RelativeLayout reTitle = findViewById(R.id.reTitle);
		ImageView ivBanner = findViewById(R.id.ivBanner);
		ivBanner.setOnClickListener(this);
		LinearLayout ll1 = findViewById(R.id.ll1);
		ll1.setOnClickListener(new MyOnClickListener(0));
		LinearLayout ll2 = findViewById(R.id.ll2);
		ll2.setOnClickListener(new MyOnClickListener(2));
		LinearLayout ll3 = findViewById(R.id.ll3);
		ll3.setOnClickListener(new MyOnClickListener(3));
		LinearLayout ll4 = findViewById(R.id.ll4);
		ll4.setOnClickListener(new MyOnClickListener(4));
		LinearLayout ll5 = findViewById(R.id.ll5);
		ll5.setOnClickListener(new MyOnClickListener(1));
		tv1 = findViewById(R.id.tv1);
		tv2 = findViewById(R.id.tv2);
		tv3 = findViewById(R.id.tv3);
		tv4 = findViewById(R.id.tv4);
		tv5 = findViewById(R.id.tv5);
		iv1 = findViewById(R.id.iv1);
		iv2 = findViewById(R.id.iv2);
		iv3 = findViewById(R.id.iv3);
		iv4 = findViewById(R.id.iv4);
		iv5 = findViewById(R.id.iv5);

		if (!TextUtils.isEmpty(PgyApplication.getTop_img())) {
			FinalBitmap finalBitmap = FinalBitmap.create(this);
			finalBitmap.display(ivBanner, PgyApplication.getTop_img(), null, 0);
			ivBanner.setVisibility(View.VISIBLE);
			reTitle.setBackgroundColor(Color.TRANSPARENT);
		} else {
			ivBanner.setVisibility(View.GONE);
			reTitle.setBackgroundColor(getResources().getColor(R.color.title_bg));
		}

		if (isShowYiqing) {
			ll5.setVisibility(View.VISIBLE);
		} else {
			ll5.setVisibility(View.GONE);
		}

		drawerlayout = findViewById(R.id.drawerlayout);
		drawerlayout.setVisibility(View.VISIBLE);
		tvEdit = findViewById(R.id.tvEdit);
		tvEdit.setOnClickListener(this);
		tvEdit.setText(getString(R.string.editor));
		ImageView ivAdd = findViewById(R.id.ivAdd);
		ivAdd.setOnClickListener(this);
		reLeft = findViewById(R.id.reLeft);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		LayoutParams params = reLeft.getLayoutParams();
		params.width = dm.widthPixels-(int)CommonUtil.dip2px(this, 50);
		reLeft.setLayoutParams(params);
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment1 = new Fragment1();
		fragments.add(fragment1);
		Fragment fragment5 = new Fragment5();
		fragments.add(fragment5);
		Fragment fragment2 = new Fragment2();
		fragments.add(fragment2);
		Fragment fragment3 = new Fragment3();
		fragments.add(fragment3);
		Fragment fragment4 = new Fragment4();
		fragments.add(fragment4);

		viewPager = findViewById(R.id.viewPager);
		MyPagerAdapter pagerAdapter = new MyPagerAdapter(this, fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 0) {
					if (!BROADCAST_ACTION_NAME.contains(Fragment1.class.getName())) {
						Intent intent = new Intent();
						intent.setAction(Fragment1.class.getName());
						sendBroadcast(intent);
						BROADCAST_ACTION_NAME += Fragment1.class.getName();
					}
					ivMenu.setVisibility(View.VISIBLE);
					ivTitle.setVisibility(View.VISIBLE);
					ivMy.setVisibility(View.VISIBLE);
					ivMy.setImageResource(R.drawable.iv_person);
					ivMy.setOnClickListener(v -> startActivity(new Intent(mContext, MyActivity.class)));
					tvTitle.setText("");
					drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
					iv1.setImageResource(R.drawable.iv_fragment1_press);
					iv2.setImageResource(R.drawable.iv_fragment2);
					iv3.setImageResource(R.drawable.iv_fragment3);
					iv4.setImageResource(R.drawable.iv_fragment4);
					iv5.setImageResource(R.drawable.iv_fragment5);
					tv1.setTextColor(getResources().getColor(R.color.white));
					tv2.setTextColor(getResources().getColor(R.color.text_color1));
					tv3.setTextColor(getResources().getColor(R.color.text_color1));
					tv4.setTextColor(getResources().getColor(R.color.text_color1));
					tv5.setTextColor(getResources().getColor(R.color.text_color1));
				}else if (arg0 == 1) {
					if (!BROADCAST_ACTION_NAME.contains(Fragment5.class.getName())) {
						Intent intent = new Intent();
						intent.setAction(Fragment5.class.getName());
						sendBroadcast(intent);
						BROADCAST_ACTION_NAME += Fragment5.class.getName();
					}
					ivMenu.setVisibility(View.GONE);
					ivTitle.setVisibility(View.GONE);
					tvTitle.setText(getString(R.string.fragment5));
					drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					iv1.setImageResource(R.drawable.iv_fragment1);
					iv2.setImageResource(R.drawable.iv_fragment2);
					iv3.setImageResource(R.drawable.iv_fragment3);
					iv4.setImageResource(R.drawable.iv_fragment4);
					iv5.setImageResource(R.drawable.iv_fragment5_press);
					tv1.setTextColor(getResources().getColor(R.color.text_color1));
					tv2.setTextColor(getResources().getColor(R.color.text_color1));
					tv3.setTextColor(getResources().getColor(R.color.text_color1));
					tv4.setTextColor(getResources().getColor(R.color.text_color1));
					tv5.setTextColor(getResources().getColor(R.color.white));
				}else if (arg0 == 2) {
					if (!BROADCAST_ACTION_NAME.contains(Fragment2.class.getName())) {
						Intent intent = new Intent();
						intent.setAction(Fragment2.class.getName());
						sendBroadcast(intent);
						BROADCAST_ACTION_NAME += Fragment2.class.getName();
					}
					ivMenu.setVisibility(View.GONE);
					ivTitle.setVisibility(View.GONE);
					ivMy.setVisibility(View.VISIBLE);
					ivMy.setImageResource(R.drawable.iv_refresh);
					ivMy.setOnClickListener(v -> {
						Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.round_animation);
						ivMy.startAnimation(animation);
						new Handler().postDelayed(() -> ivMy.clearAnimation(), 1000);
						((Fragment2) fragment2).refresh();
					});
					tvTitle.setText(getString(R.string.fragment2));
					drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					iv1.setImageResource(R.drawable.iv_fragment1);
					iv2.setImageResource(R.drawable.iv_fragment2_press);
					iv3.setImageResource(R.drawable.iv_fragment3);
					iv4.setImageResource(R.drawable.iv_fragment4);
					iv5.setImageResource(R.drawable.iv_fragment5);
					tv1.setTextColor(getResources().getColor(R.color.text_color1));
					tv2.setTextColor(getResources().getColor(R.color.white));
					tv3.setTextColor(getResources().getColor(R.color.text_color1));
					tv4.setTextColor(getResources().getColor(R.color.text_color1));
					tv5.setTextColor(getResources().getColor(R.color.text_color1));
				}else if (arg0 == 3) {
					if (!BROADCAST_ACTION_NAME.contains(Fragment3.class.getName())) {
						Intent intent = new Intent();
						intent.setAction(Fragment3.class.getName());
						sendBroadcast(intent);
						BROADCAST_ACTION_NAME += Fragment3.class.getName();
					}
					ivMenu.setVisibility(View.GONE);
					ivTitle.setVisibility(View.GONE);
					ivMy.setVisibility(View.GONE);
					tvTitle.setText(getString(R.string.fragment3));
					drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					iv1.setImageResource(R.drawable.iv_fragment1);
					iv2.setImageResource(R.drawable.iv_fragment2);
					iv3.setImageResource(R.drawable.iv_fragment3_press);
					iv4.setImageResource(R.drawable.iv_fragment4);
					iv5.setImageResource(R.drawable.iv_fragment5);
					tv1.setTextColor(getResources().getColor(R.color.text_color1));
					tv2.setTextColor(getResources().getColor(R.color.text_color1));
					tv3.setTextColor(getResources().getColor(R.color.white));
					tv4.setTextColor(getResources().getColor(R.color.text_color1));
					tv5.setTextColor(getResources().getColor(R.color.text_color1));
				}else if (arg0 == 4) {
					if (!BROADCAST_ACTION_NAME.contains(Fragment4.class.getName())) {
						Intent intent = new Intent();
						intent.setAction(Fragment4.class.getName());
						sendBroadcast(intent);
						BROADCAST_ACTION_NAME += Fragment4.class.getName();
					}
					ivMenu.setVisibility(View.GONE);
					ivTitle.setVisibility(View.GONE);
					ivMy.setVisibility(View.VISIBLE);
					ivMy.setImageResource(R.drawable.iv_camera);
					ivMy.setOnClickListener(v -> ((Fragment4) fragment4).checkAuthority());
					tvTitle.setText(getString(R.string.fragment4));
					drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					iv1.setImageResource(R.drawable.iv_fragment1);
					iv2.setImageResource(R.drawable.iv_fragment2);
					iv3.setImageResource(R.drawable.iv_fragment3);
					iv4.setImageResource(R.drawable.iv_fragment4_press);
					iv5.setImageResource(R.drawable.iv_fragment5);
					tv1.setTextColor(getResources().getColor(R.color.text_color1));
					tv2.setTextColor(getResources().getColor(R.color.text_color1));
					tv3.setTextColor(getResources().getColor(R.color.text_color1));
					tv4.setTextColor(getResources().getColor(R.color.white));
					tv5.setTextColor(getResources().getColor(R.color.text_color1));
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	/**
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {

		private int index;

		private MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
		}
	}
	
	/**
	 * 初始化leftListView
	 */
	private void initListView() {
		ListView cityListView = findViewById(R.id.cityListView);
		leftAdapter = new ShawnLeftAdapter(mContext, leftList);
		cityListView.setAdapter(leftAdapter);
	}
	
	/**
	 * 保存关注的城市列表数据到本地
	 */
	private void saveCitysToLocal() {
		String cityInfo = "";
		for (int i = 1; i < leftList.size(); i++) {//从1开始是为了过滤掉定位城市
			cityInfo += (leftList.get(i).cityId+","+leftList.get(i).cityName+","+leftList.get(i).warningId+";");
		}
		
		//保存所有的城市id
		SharedPreferences sp = getSharedPreferences("CITYIDS", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("cityInfo", cityInfo);
		editor.apply();
		Log.e("cityInfo", cityInfo);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawerlayout != null) {
				if (drawerlayout.isDrawerOpen(reLeft)) {
					drawerlayout.closeDrawer(reLeft);
				}else {
					if ((System.currentTimeMillis() - mExitTime) > 2000) {
						Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
						mExitTime = System.currentTimeMillis();
					} else {
						finish();
					}
				}
			}else {
				finish();
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBanner:
			if (TextUtils.isEmpty(PgyApplication.getTop_img_title()) || TextUtils.isEmpty(PgyApplication.getTop_img_url())) {
				return;
			}
			NewsDto dto = new NewsDto();
			dto.title = PgyApplication.getTop_img_title();
			dto.url = PgyApplication.getTop_img_url();
			Intent intent = new Intent(this, WebviewActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", dto);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivMenu:
			if (drawerlayout.isDrawerOpen(reLeft)) {
				drawerlayout.closeDrawer(reLeft);
			}else {
				drawerlayout.openDrawer(reLeft);
				//统计点击次数
				StatisticUtil.submitClickCount("3", "城市订阅");
			}
			break;
			
			//侧拉页面
		case R.id.tvEdit:
			if (TextUtils.equals(tvEdit.getText().toString(), getString(R.string.editor))) {
				tvEdit.setText(getString(R.string.complete));
				if (leftAdapter != null) {
					leftAdapter.isDelete = true;
					leftAdapter.notifyDataSetChanged();
				}
			}else if (TextUtils.equals(tvEdit.getText().toString(), getString(R.string.complete))) {
				saveCitysToLocal();
				
				tvEdit.setText(getString(R.string.editor));
				if (leftAdapter != null) {
					leftAdapter.isDelete = false;
					leftAdapter.notifyDataSetChanged();
				}
			}
			break;
		case R.id.ivAdd:
			if (leftList.size() >= 10) {
				Toast.makeText(mContext, getString(R.string.most_add_city), Toast.LENGTH_SHORT).show();
				return;
			}else {
				intent = new Intent(mContext, CityActivity.class);
				intent.putExtra("cityId", locationId);
				startActivityForResult(intent, 1001);
			}
			break;

		default:
			break;
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1001:
				Bundle bundle = data.getExtras();
				String cityId = bundle.getString("cityId");
				String cityName = bundle.getString("cityName");
				String warningId = bundle.getString("warningId");
				
				Intent intent = new Intent();
				intent.setAction(CONST.BROADCAST_ADD);
				intent.putExtra("cityId", cityId);
				intent.putExtra("cityName", cityName);
				intent.putExtra("warningId", warningId);
				sendBroadcast(intent);

				showDialog();
				break;

			default:
				break;
			}
		}
	}

	private void okHttpYiqingState() {
		final String url = "http://new.12379.tianqi.cn/Extra2019/get_world_yqstatus?uid="+UID;
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
						init();
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("code")) {
											if (TextUtils.equals(obj.getString("code"), "1")) {
												isShowYiqing = true;
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
									init();
								}
							});
						}
					}
				});
			}
		}).start();
	}

}
