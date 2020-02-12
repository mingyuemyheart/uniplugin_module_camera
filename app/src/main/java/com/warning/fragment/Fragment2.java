package com.warning.fragment;

/**
 * 预警
 * @author shawn_sun
 *
 */

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.warning.R;
import com.warning.activity.OnlinePictureActivity;
import com.warning.activity.OnlineVideoActivity;
import com.warning.activity.WarningDetailActivity;
import com.warning.activity.ShawnWarningListActivity;
import com.warning.activity.ZixunDetailActivity;
import com.warning.adapter.NewsAdapter;
import com.warning.adapter.VideoWallAdapter;
import com.warning.adapter.ShawnWarningAdapter;
import com.warning.adapter.ShawnWarningStatisticAdapter;
import com.warning.common.CONST;
import com.warning.dto.NewsDto;
import com.warning.dto.PhotoDto;
import com.warning.dto.WarningDto;
import com.warning.util.AuthorityUtil;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;
import com.warning.view.ArcMenu;
import com.warning.view.ArcMenu.OnMenuItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Fragment2 extends Fragment implements OnClickListener, AMapLocationListener, OnMapClickListener,
OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener{
	
	private TextView tvPrompt;//没有数据时提示
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private float zoom = 3.5f;
	private ArcMenu arcMenu = null;
	private boolean blue = true, yellow = true, orange = true, red = true;
	private List<WarningDto> warningList = new ArrayList<>();
	private List<WarningDto> nationList = new ArrayList<>();
	private List<WarningDto> proList = new ArrayList<>();
	private List<WarningDto> cityList = new ArrayList<>();
	private List<WarningDto> disList = new ArrayList<>();
	private List<WarningDto> blueList = new ArrayList<>();
	private List<WarningDto> yellowList = new ArrayList<>();
	private List<WarningDto> orangeList = new ArrayList<>();
	private List<WarningDto> redList = new ArrayList<>();
	private List<Marker> blueMarkers = new ArrayList<>();
	private List<Marker> yellowMarkers = new ArrayList<>();
	private List<Marker> orangeMarkers = new ArrayList<>();
	private List<Marker> redMarkers = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private boolean isExpandMap = false;//是否放大地图
	private ImageView ivLocation = null;
	private ImageView ivRefresh = null;
	private TextView tvList = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double locationLat = 0, locationLng = 0;
	private int size1 = 30;
	private int size2 = 15;
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	
	private LinearLayout llPrompt = null;
	private ImageView ivArrow = null;
	private boolean isShowPrompt = false;
	
	//预警统计列表
	private ListView listView1 = null;
	private ShawnWarningStatisticAdapter adapter1 = null;
	private List<WarningDto> list1 = new ArrayList<>();
	
	//点击marker的预警信息
	private ListView listView2 = null;
	private ShawnWarningAdapter adapter2 = null;
	private List<WarningDto> list2 = new ArrayList<>();
	
	//咨询列表
	private ListView listView3 = null;
	private NewsAdapter adapter3 = null;
	private List<NewsDto> list3 = new ArrayList<>();
	
	//直报列表
	private ListView listView4 = null;
	private VideoWallAdapter adapter4 = null;
	private List<PhotoDto> list4 = new ArrayList<>();
	private int page = 1;
	private int pageSize = 100;
	
	private ImageView ivWarning = null;
	private ImageView ivZhibao = null;
	private ImageView ivZixun = null;
	private boolean isShowWarning = true;
	private boolean isShowZhibao = true;
	private boolean isShowZixun = true;
	private List<NewsDto> zixunList = new ArrayList<>();
	private List<Marker> zixunMarkerList = new ArrayList<>();
	private List<PhotoDto> zhibaoList = new ArrayList<>();
	private List<Marker> zhibaoMarkerList = new ArrayList<>();
	private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private long dateTime = 60*60*24*2;//咨询、直报获取数据天数
	
	private final String MARKER_WARNING = "101";//预警marker
	private final String MARKER_ZHIBAO = "102";//直报marker
	private final String MARKER_ZIXUN = "103";//资讯marker

	private MyBroadCastReceiver mReceiver = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment2, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initBroadCast();
		initAmap(view, savedInstanceState);
		initWidget(view);
		initListView1(view);
		initListView2(view);
		initListView3(view);
		initListView4(view);
	}

	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Fragment2.class.getName());
		getActivity().registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), Fragment2.class.getName())) {
				refresh();
			}
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		arcMenu = (ArcMenu) view.findViewById(R.id.arcMenu);
		arcMenu.setOnMenuItemClickListener(arcMenuListener);
		ivLocation = (ImageView) view.findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		tvList = (TextView) view.findViewById(R.id.tvList);
		tvList.setOnClickListener(this);
		llPrompt = (LinearLayout) view.findViewById(R.id.llPrompt);
		llPrompt.setOnClickListener(this);
		ivArrow = (ImageView) view.findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		ivWarning = (ImageView) view.findViewById(R.id.ivWarning);
		ivWarning.setOnClickListener(this);
		ivZhibao = (ImageView) view.findViewById(R.id.ivZhibao);
		ivZhibao.setOnClickListener(this);
		ivZixun = (ImageView) view.findViewById(R.id.ivZixun);
		ivZixun.setOnClickListener(this);
		
		if (isShowZhibao) {
			ivZhibao.setImageResource(R.drawable.iv_zhibao_selected);
		}else {
			ivZhibao.setImageResource(R.drawable.iv_zhibao_unselected);
		}
		if (isShowZixun) {
			ivZixun.setImageResource(R.drawable.iv_zixun_selected);
		}else {
			ivZixun.setImageResource(R.drawable.iv_zixun_unselected);
		}
		
//		refresh();
    }
	
	private void refresh() {
		android.view.animation.Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.round_animation);
		ivRefresh.startAnimation(animation);
		checkAuthority();
		OkhttpWarning();
		OkhttpZixun("http://new.12379.tianqi.cn/Extra/get_tfsj");
//		OkhttpZhibao("http://new.12379.tianqi.cn/Work/getlist?flag=1");
		OkhttpZhibao("http://new.12379.tianqi.cn/Work/getlist");
		StatisticUtil.submitClickCount("2", "预警地图");
	}

	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			startLocation();
		}else {
			deniedList.clear();
			for (int i = 0; i < allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(getActivity(), allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i]);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				startLocation();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				Fragment2.this.requestPermissions(permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						startLocation();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限，是否前往设置？");
					}
				}else {
					for (int i = 0; i < permissions.length; i++) {
						if (!Fragment2.this.shouldShowRequestPermissionRationale(permissions[i])) {
							AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(View view, Bundle bundle) {
		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.5f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);

		TextView tvMapNumber = (TextView) view.findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(getActivity());//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			locationLat = amapLocation.getLatitude();
			locationLng = amapLocation.getLongitude();
			ivLocation.setVisibility(View.VISIBLE);
			isExpandMap = true;
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									warningList.clear();
									nationList.clear();
									proList.clear();
									cityList.clear();
									disList.clear();
									try {
										final JSONObject object = new JSONObject(result);
										if (object != null) {
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
													}

													if (!TextUtils.isEmpty(item0)) {
														if (!dto.name.contains("解除")) {
															if (TextUtils.equals(item0, "000000")) {
																nationList.add(dto);
															}else if (TextUtils.equals(item0.substring(item0.length()-4, item0.length()), "0000")) {
																proList.add(dto);
															}else if (TextUtils.equals(item0.substring(item0.length()-2, item0.length()), "00")) {
																cityList.add(dto);
															}else {
																disList.add(dto);
															}
														}
													}
												}

												try {
													String count = warningList.size()+"";
													if (TextUtils.equals(count, "0")) {
														String time = "";
														if (!object.isNull("time")) {
															long t = object.getLong("time");
															time = sdf.format(new Date(t*1000));
														}
														tvPrompt.setText(time+", "+"当前生效预警"+count+"条");
														tvList.setVisibility(View.GONE);
														arcMenu.setVisibility(View.GONE);
														ivWarning.setVisibility(View.GONE);
														ivRefresh.clearAnimation();
														llPrompt.setVisibility(View.VISIBLE);
														return;
													}

													String time = "";
													if (!object.isNull("time")) {
														long t = object.getLong("time");
														time = sdf.format(new Date(t*1000));
													}
													String str1 = time+", "+"当前生效预警";
													String str2 = "条";
													String warningInfo = str1+count+str2;
													SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
													ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
													ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
													ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
													builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													builder.setSpan(builderSpan2, str1.length(), str1.length()+count.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													builder.setSpan(builderSpan3, str1.length()+count.length(), str1.length()+count.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													tvPrompt.setText(builder);
													tvList.setVisibility(View.VISIBLE);
													arcMenu.setVisibility(View.VISIBLE);
													ivWarning.setVisibility(View.VISIBLE);
													ivRefresh.clearAnimation();
													llPrompt.setVisibility(View.VISIBLE);

													unselectedWarning();
													selectedWarning();

													//计算统计列表信息
													int rnation = 0;int rpro = 0;int rcity = 0;int rdis = 0;
													int onation = 0;int opro = 0;int ocity = 0;int odis = 0;
													int ynation = 0;int ypro = 0;int ycity = 0;int ydis = 0;
													int bnation = 0;int bpro = 0;int bcity = 0;int bdis = 0;
													int wnation = 0;int wpro = 0;int wcity = 0;int wdis = 0;
													for (int i = 0; i < warningList.size(); i++) {
														WarningDto dto = warningList.get(i);
														if (TextUtils.equals(dto.color, "04")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																rnation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																rpro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																rcity += 1;
															}else {
																rdis += 1;
															}
														}else if (TextUtils.equals(dto.color, "03")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																onation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																opro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																ocity += 1;
															}else {
																odis += 1;
															}
														}else if (TextUtils.equals(dto.color, "02")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																ynation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																ypro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																ycity += 1;
															}else {
																ydis += 1;
															}
														}else if (TextUtils.equals(dto.color, "01")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																bnation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																bpro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																bcity += 1;
															}else {
																bdis += 1;
															}
														}else if (TextUtils.equals(dto.color, "05")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																wnation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																wpro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																wcity += 1;
															}else {
																wdis += 1;
															}
														}
													}

													list1.clear();
													WarningDto wDto = new WarningDto();
													wDto.colorName = "预警"+warningList.size();
													wDto.nationCount = "国家级"+(rnation+onation+ynation+bnation);
													wDto.proCount = "省级"+(rpro+opro+ypro+bpro);
													wDto.cityCount = "市级"+(rcity+ocity+ycity+bcity);
													wDto.disCount = "县级"+(rdis+odis+ydis+bdis);
													list1.add(wDto);

													wDto = new WarningDto();
													wDto.colorName = "红"+(rnation+rpro+rcity+rdis);
													wDto.nationCount = rnation+"";
													wDto.proCount = rpro+"";
													wDto.cityCount = rcity+"";
													wDto.disCount = rdis+"";
													if (rnation+rpro+rcity+rdis > 0) {
														list1.add(wDto);
													}

													wDto = new WarningDto();
													wDto.colorName = "橙"+(onation+opro+ocity+odis);
													wDto.nationCount = onation+"";
													wDto.proCount = opro+"";
													wDto.cityCount = ocity+"";
													wDto.disCount = odis+"";
													if (onation+opro+ocity+odis > 0) {
														list1.add(wDto);
													}

													wDto = new WarningDto();
													wDto.colorName = "黄"+(ynation+ypro+ycity+ydis);
													wDto.nationCount = ynation+"";
													wDto.proCount = ypro+"";
													wDto.cityCount = ycity+"";
													wDto.disCount = ydis+"";
													if (ynation+ypro+ycity+ydis > 0) {
														list1.add(wDto);
													}

													wDto = new WarningDto();
													wDto.colorName = "蓝"+(bnation+bpro+bcity+bdis);
													wDto.nationCount = bnation+"";
													wDto.proCount = bpro+"";
													wDto.cityCount = bcity+"";
													wDto.disCount = bdis+"";
													if (bnation+bpro+bcity+bdis > 0) {
														list1.add(wDto);
													}

													wDto = new WarningDto();
													wDto.colorName = "未知"+(wnation+wpro+wcity+wdis);
													wDto.nationCount = wnation+"";
													wDto.proCount = wpro+"";
													wDto.cityCount = wcity+"";
													wDto.disCount = wdis+"";
													if (wnation+wpro+wcity+wdis > 0) {
														list1.add(wDto);
													}

													if (adapter1 != null) {
														adapter1.nation = rnation+onation+ynation+bnation+wnation;
														adapter1.pro = rpro+opro+ypro+bpro+wpro;
														adapter1.city = rcity+ocity+ycity+bcity+wnation;
														adapter1.dis = rdis+odis+ydis+bdis+wdis;
														adapter1.notifyDataSetChanged();
													}
												}catch (JSONException e) {
													e.printStackTrace();
												}
											}
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

	/**
	 * 在地图上添加marker
	 */
	private void addWarningMarkers(List<WarningDto> list, List<Marker> markerList) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
		    if (!TextUtils.equals(dto.item0, "000000")) {
		    	double lat = Double.valueOf(dto.lat);
				double lng = Double.valueOf(dto.lng);
				MarkerOptions optionsTemp = new MarkerOptions();
				optionsTemp.title(dto.lat+","+dto.lng);
				optionsTemp.snippet(MARKER_WARNING);
				optionsTemp.anchor(0.5f, 0.5f);
				optionsTemp.position(new LatLng(lat, lng));
				View mView = inflater.inflate(R.layout.warning_marker_view, null);
				ImageView ivMarker = (ImageView) mView.findViewById(R.id.ivMarker);
				LayoutParams params = ivMarker.getLayoutParams();
				if (zoom < 4.0) {
					params.width = (int) CommonUtil.dip2px(getActivity(), size2);
					params.height = (int) CommonUtil.dip2px(getActivity(), size2);
				}else {
					params.width = (int) CommonUtil.dip2px(getActivity(), size1);
					params.height = (int) CommonUtil.dip2px(getActivity(), size1);
				}
				ivMarker.setLayoutParams(params);
				
				Bitmap bitmap = null;
				if (dto.color.equals(CONST.blue[0])) {
					bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
					}
				}else if (dto.color.equals(CONST.yellow[0])) {
					bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
					}
				}else if (dto.color.equals(CONST.orange[0])) {
					bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
					}
				}else if (dto.color.equals(CONST.red[0])) {
					bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
					}
				}else if (dto.color.equals(CONST.unknown[0])) {
					bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/default"+CONST.imageSuffix);
				}
				ivMarker.setImageBitmap(bitmap);
				optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
				
				if (leftlatlng == null || rightLatlng == null) {
					Marker marker = aMap.addMarker(optionsTemp);
					if (isShowWarning) {
						marker.setVisible(true);
					}else {
						marker.setVisible(false);
					}
					markerList.add(marker);
					Animation animation = new ScaleAnimation(0,1,0,1);
					animation.setInterpolator(new LinearInterpolator());
					animation.setDuration(300);
					marker.setAnimation(animation);
					marker.startAnimation();
				}else {
					if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
						Marker marker = aMap.addMarker(optionsTemp);
						if (isShowWarning) {
							marker.setVisible(true);
						}else {
							marker.setVisible(false);
						}
						markerList.add(marker);
						Animation animation = new ScaleAnimation(0,1,0,1);
						animation.setInterpolator(new LinearInterpolator());
						animation.setDuration(300);
						marker.setAnimation(animation);
						marker.startAnimation();
					}
				}
			}
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (listView2.getVisibility() == View.VISIBLE) {
			hideAnimation(listView2);
			listView2.setVisibility(View.GONE);
		}
		if (listView3.getVisibility() == View.VISIBLE) {
			hideAnimation(listView3);
			listView3.setVisibility(View.GONE);
		}
		if (listView4.getVisibility() == View.VISIBLE) {
			hideAnimation(listView4);
			listView4.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (listView2.getVisibility() == View.VISIBLE) {
			hideAnimation(listView2);
			listView2.setVisibility(View.GONE);
		}
		if (listView3.getVisibility() == View.VISIBLE) {
			hideAnimation(listView3);
			listView3.setVisibility(View.GONE);
		}
		if (listView4.getVisibility() == View.VISIBLE) {
			hideAnimation(listView4);
			listView4.setVisibility(View.GONE);
		}
		
		if (TextUtils.equals(marker.getSnippet(), MARKER_WARNING)) {//预警类型marker
			list2.clear();
			if (zoom <= 6.0f) {
				addInfoList(proList, marker, list2);
			}else if (zoom > 6.0f && zoom <= 8.0f) {
				addInfoList(proList, marker, list2);
				addInfoList(cityList, marker, list2);
			}else if (zoom > 8.0f) {
				addInfoList(proList, marker, list2);
				addInfoList(cityList, marker, list2);
				addInfoList(disList, marker, list2);
			}
			if (adapter2 != null) {
				adapter2.notifyDataSetChanged();
				setListViewHeight(listView2, list2.size(), 50, 100, 150);
			}
			
			if (listView2.getVisibility() == View.GONE) {
				showAnimation(listView2);
				listView2.setVisibility(View.VISIBLE);
			}
		}else if (TextUtils.equals(marker.getSnippet(), MARKER_ZHIBAO)) {//直报类型marker
			list4.clear();
			for (int i = 0; i < zhibaoList.size(); i++) {
				PhotoDto dto = zhibaoList.get(i);
				String[] latLng = marker.getTitle().split(",");
				if (TextUtils.equals(latLng[0], dto.lat) && TextUtils.equals(latLng[1], dto.lng)) {
					list4.add(dto);
				}
			}
			if (adapter4 != null) {
				adapter4.notifyDataSetChanged();
				setListViewHeight(listView4, list4.size(), 70, 140, 140);
			}
			
			if (listView4.getVisibility() == View.GONE) {
				showAnimation(listView4);
				listView4.setVisibility(View.VISIBLE);
			}
		}else if (TextUtils.equals(marker.getSnippet(), MARKER_ZIXUN)) {//资讯类型marker
			list3.clear();
			for (int i = 0; i < zixunList.size(); i++) {
				NewsDto dto = zixunList.get(i);
				String[] latLng = marker.getTitle().split(",");
				if (TextUtils.equals(latLng[0], dto.lat) && TextUtils.equals(latLng[1], dto.lng)) {
					list3.add(dto);
				}
			}
			if (adapter3 != null) {
				adapter3.notifyDataSetChanged();
				setListViewHeight(listView3, list3.size(), 70, 140, 140);
			}
			
			if (listView3.getVisibility() == View.GONE) {
				showAnimation(listView3);
				listView3.setVisibility(View.VISIBLE);
			}
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
//		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View mView = inflater.inflate(R.layout.warning_marker_info, null);
//		ListView mListView = null;
//		ShawnWarningAdapter mAdapter = null;
//		final List<WarningDto> infoList = new ArrayList<WarningDto>();
//		
//		infoList.clear();
//		if (zoom <= 6.0f) {
//			addInfoList(proList, marker, infoList);
//		}else if (zoom > 6.0f && zoom <= 8.0f) {
//			addInfoList(proList, marker, infoList);
//			addInfoList(cityList, marker, infoList);
//		}else if (zoom > 8.0f) {
//			addInfoList(proList, marker, infoList);
//			addInfoList(cityList, marker, infoList);
//			addInfoList(disList, marker, infoList);
//		}
//		
//		mListView = (ListView) mView.findViewById(R.id.listView);
//		mAdapter = new ShawnWarningAdapter(getActivity(), infoList, true);
//		mListView.setAdapter(mAdapter);
//		LayoutParams params = mListView.getLayoutParams();
//		if (infoList.size() == 1) {
//			params.height = (int) CommonUtil.dip2px(getActivity(), 50);
//		}else if (infoList.size() == 2) {
//			params.height = (int) CommonUtil.dip2px(getActivity(), 100);
//		}else if (infoList.size() > 2){
//			params.height = (int) CommonUtil.dip2px(getActivity(), 150);
//		}
//		mListView.setLayoutParams(params);
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				intentDetail(infoList.get(arg2));
//			}
//		});
//		return mView;
		return null;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intent = new Intent(getActivity(), WarningDetailActivity.class);
		intent.putExtra("url", data.html);
		startActivity(intent);
	}
	
	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			String[] latLng = marker.getTitle().split(",");
			if (TextUtils.equals(latLng[0], dto.lat) && TextUtils.equals(latLng[1], dto.lng)) {
				infoList.add(dto);
			}
		}
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 移除地图上指定marker
	 * @param markers
	 */
	private void removeMarkers(List<Marker> markers) {
		for (int i = 0; i < markers.size(); i++) {
			final Marker marker = markers.get(i);
			Animation animation = new ScaleAnimation(1,0,1,0);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.remove();
				}
			});
		}
		markers.clear();
	}
	
	private OnMenuItemClickListener arcMenuListener = new OnMenuItemClickListener() {
		@Override
		public void onClick(View view, int pos) {
			if (pos == 0) {
				if (blue) {
					blue = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_blue_press);
					removeMarkers(blueMarkers);
				}else {
					blue = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_blue);
					addWarningMarkers(blueList, blueMarkers);
				}
			}else if (pos == 1) {
				if (yellow) {
					yellow = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_yellow_press);
					removeMarkers(yellowMarkers);
				}else {
					yellow = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_yellow);
					addWarningMarkers(yellowList, yellowMarkers);
				}
			}else if (pos == 2) {
				if (orange) {
					orange = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_orange_press);
					removeMarkers(orangeMarkers);
				}else {
					orange = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_orange);
					addWarningMarkers(orangeList, orangeMarkers);
				}
			}else if (pos == 3) {
				if (red) {
					red = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_red_press);
					removeMarkers(redMarkers);
				}else {
					red = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_red);
					addWarningMarkers(redList, redMarkers);
				}
			}
		}
	};
	
	/**
	 * 不选中预警
	 */
	private void unselectedWarning() {
		removeMarkers(blueMarkers);
		removeMarkers(yellowMarkers);
		removeMarkers(orangeMarkers);
		removeMarkers(redMarkers);
		blueList.clear();
		yellowList.clear();
		orangeList.clear();
		redList.clear();
	}
	
	/**
	 * 选中预警
	 */
	private void selectedWarning() {
		if (zoom <= 6.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}else if (zoom > 6.0f && zoom <= 8.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				WarningDto dto = cityList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}else if (zoom > 8.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				WarningDto dto = cityList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < disList.size(); i++) {
				WarningDto dto = disList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}
		
		if (blue) {
			addWarningMarkers(blueList, blueMarkers);
		}
		if (yellow) {
			addWarningMarkers(yellowList, yellowMarkers);
		}
		if (orange) {
			addWarningMarkers(orangeList, orangeMarkers);
		}
		if (red) {
			addWarningMarkers(redList, redMarkers);
		}
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		unselectedWarning();
		selectedWarning();
	}
	
	/**
	 * 初始化预警统计列表
	 * @param view
	 */
	private void initListView1(View view) {
		listView1 = (ListView) view.findViewById(R.id.listView1);
		adapter1 = new ShawnWarningStatisticAdapter(getActivity(), list1);
		listView1.setAdapter(adapter1);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				clickPromptWarning();
			}
		});
	}
	
	/**
	 * 初始化点击marker的预警列表
	 */
	private void initListView2(View view) {
		listView2 = (ListView) view.findViewById(R.id.listView2);
		adapter2 = new ShawnWarningAdapter(getActivity(), list2, false);
		listView2.setAdapter(adapter2);
		setListViewHeight(listView2, list2.size(), 50, 100, 150);
		listView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(list2.get(arg2));
			}
		});
	}
	
	private void initListView3(View view) {
		listView3 = (ListView) view.findViewById(R.id.listView3);
		adapter3 = new NewsAdapter(getActivity(), list3);
		listView3.setAdapter(adapter3);
		setListViewHeight(listView3, list3.size(), 70, 140, 140);
		listView3.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(getActivity(), ZixunDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", list3.get(arg2));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * 设置listview高度
	 * @param listView
	 * @param size
	 */
	private void setListViewHeight(ListView listView, int size, int height1, int height2, int height3) {
		LayoutParams params = listView.getLayoutParams();
		if (size == 1) {
			params.height = (int) CommonUtil.dip2px(getActivity(), height1);
		}else if (size == 2) {
			params.height = (int) CommonUtil.dip2px(getActivity(), height2);
		}else if (size > 2){
			params.height = (int) CommonUtil.dip2px(getActivity(), height3);
		}
		listView.setLayoutParams(params);
	}
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
	}
	
	/** 
     * 隐藏或显示ListView的动画 
     */  
    public void hideOrShowListViewAnimator(final View view, final int startValue,final int endValue){  
        //1.设置属性的初始值和结束值  
        ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);  
        //2.为目标对象的属性变化设置监听器  
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                int animatorValue = (Integer) animation.getAnimatedValue();  
                float fraction = animatorValue/100f;  
                IntEvaluator mEvaluator = new IntEvaluator();  
                //3.使用IntEvaluator计算属性值并赋值给ListView的高  
                view.getLayoutParams().height = mEvaluator.evaluate(fraction, startValue, endValue);  
                view.requestLayout();  
            }  
        });  
        //4.为ValueAnimator设置LinearInterpolator  
        mAnimator.setInterpolator(new LinearInterpolator());  
        //5.设置动画的持续时间  
        mAnimator.setDuration(200);  
        //6.为ValueAnimator设置目标对象并开始执行动画  
        mAnimator.setTarget(view);  
        mAnimator.start();  
    } 
    
    private void clickPromptWarning() {
    	int height = CommonUtil.getListViewHeightBasedOnChildren(listView1);
		if (isShowPrompt == false) {
			isShowPrompt = true;
			ivArrow.setImageResource(R.drawable.iv_arrow_black_up);
			hideOrShowListViewAnimator(listView1, 0, height);
		}else {
			isShowPrompt = false;
			ivArrow.setImageResource(R.drawable.iv_arrow_black_down);
			hideOrShowListViewAnimator(listView1, height, 0);
		}
    }
    
    /**
	 * 获取天气咨询
	 */
	private void OkhttpZixun(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										zixunList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											NewsDto newsDto = new NewsDto();
											if (!obj.isNull("title")) {
												newsDto.title = obj.getString("title");
											}
											if (!obj.isNull("time")) {
												newsDto.time = obj.getString("time");
											}
											if (!obj.isNull("content")) {
												newsDto.content = obj.getString("content");
											}
											if (!obj.isNull("url")) {
												newsDto.url = obj.getString("url");
											}
											if (!obj.isNull("img")) {
												newsDto.imgUrl = obj.getString("img");
											}
											if (!obj.isNull("latlon")) {
												String latlon = obj.getString("latlon");
												if (!TextUtils.isEmpty(latlon) && !TextUtils.equals(latlon, ",")) {
													String[] latLngArray = latlon.split(",");
													newsDto.lat = latLngArray[0];
													newsDto.lng = latLngArray[1];
												}
											}

											try {
												if (!TextUtils.isEmpty(newsDto.time)) {
													long currentDate = System.currentTimeMillis()/1000;
													long beforeDate = currentDate-dateTime;
													long workDate = sdf5.parse(newsDto.time).getTime()/1000;
													if (workDate >= beforeDate && workDate <= currentDate) {
														if (!TextUtils.isEmpty(newsDto.lat) && !TextUtils.isEmpty(newsDto.lng)) {
															zixunList.add(newsDto);
														}
													}
												}
											} catch (ParseException e) {
												e.printStackTrace();
											}
										}

										ivRefresh.clearAnimation();
										if (zixunList.size() > 0) {
											ivZixun.setVisibility(View.VISIBLE);
											removeMarkers(zixunMarkerList);
											addZixunMarkers();
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
	
	/**
	 * 添加咨询markers
	 */
	private void addZixunMarkers() {
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < zixunList.size(); i++) {
			final NewsDto dto = zixunList.get(i);
			ImageLoader.getInstance().loadImage(dto.imgUrl, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				}
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap) {
					double lat = Double.valueOf(dto.lat);
					double lng = Double.valueOf(dto.lng);
					MarkerOptions optionsTemp = new MarkerOptions();
					optionsTemp.title(dto.lat+","+dto.lng);
					optionsTemp.snippet(MARKER_ZIXUN);
					optionsTemp.anchor(0.5f, 0.5f);
					optionsTemp.position(new LatLng(lat, lng));
					View mView = inflater.inflate(R.layout.layout_zixun_marker, null);
					ImageView ivMarker = (ImageView) mView.findViewById(R.id.ivMarker);
					if (bitmap != null) {
						Bitmap b = CommonUtil.getRoundedCornerBitmap(bitmap, 20);
						if (b != null) {
							ivMarker.setImageBitmap(b);
							LayoutParams params = ivMarker.getLayoutParams();
							params.width = (int) CommonUtil.dip2px(getActivity(), 40);
							params.height = (int) CommonUtil.dip2px(getActivity(), 30);
							ivMarker.setLayoutParams(params);
						}
					}
					optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
//					if (leftlatlng == null || rightLatlng == null) {
						final Marker marker = aMap.addMarker(optionsTemp);
						if (isShowZixun) {
							marker.setVisible(true);
						}else {
							marker.setVisible(false);
						}
						zixunMarkerList.add(marker);
						Animation animation = new ScaleAnimation(0,1,0,1);
						animation.setInterpolator(new LinearInterpolator());
						animation.setDuration(300);
						marker.setAnimation(animation);
						marker.startAnimation();
//					}else {
//						if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
//							Marker marker = aMap.addMarker(optionsTemp);
//							if (isShowZixun) {
//								marker.setVisible(true);
//							}else {
//								marker.setVisible(false);
//							}
//							zixunMarkerList.add(marker);
//							Animation animation = new ScaleAnimation(0,1,0,1);
//							animation.setInterpolator(new LinearInterpolator());
//							animation.setDuration(300);
//							marker.setAnimation(animation);
//							marker.startAnimation();
//						}
//					}
				}
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
				}
			});
		}
	}
	
	/**
	 * 显示或隐藏资讯marker，这里不清除list
	 */
	private void switchZixunMarkers(final boolean flag) {
		for (int i = 0; i < zixunMarkerList.size(); i++) {
			final Marker marker = zixunMarkerList.get(i);
			Animation animation = null;
			if (flag) {//显示marker
				animation = new ScaleAnimation(0,1,0,1);
				marker.setVisible(flag);
				marker.setToTop();
			}else {//隐藏marker
				animation = new ScaleAnimation(1,0,1,0);
			}
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.setVisible(flag);
				}
			});
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView4(View view) {
		listView4 = (ListView) view.findViewById(R.id.listView4);
		adapter4 = new VideoWallAdapter(getActivity(), list4);
		listView4.setAdapter(adapter4);
		setListViewHeight(listView4, list4.size(), 70, 140, 140);
		listView4.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = list4.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(getActivity(), OnlinePictureActivity.class);
				}else {
					intent.setClass(getActivity(), OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * 获取直报
	 */
	private void OkhttpZhibao(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("p", String.valueOf(page));
				builder.add("size", String.valueOf(pageSize));
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														zhibaoList.clear();
														JSONArray array = new JSONArray(object.getString("info"));
														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = (JSONObject) array.opt(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("id")) {
																dto.setVideoId(obj.getString("id"));
															}
															if (!obj.isNull("uid")) {
																dto.uid = obj.getString("uid");
															}
															if (!obj.isNull("title")) {
																dto.setTitle(obj.getString("title"));
															}
															if (!obj.isNull("create_time")) {
																dto.setCreateTime(obj.getString("create_time"));
															}
															if (!obj.isNull("latlon")) {
																String latlon = obj.getString("latlon");
																if (!TextUtils.isEmpty(latlon) && !TextUtils.equals(latlon, ",")) {
																	String[] latLngArray = latlon.split(",");
																	dto.lat = latLngArray[0];
																	dto.lng = latLngArray[1];
																}
															}
															if (!obj.isNull("location")) {
																dto.setLocation(obj.getString("location"));
															}
															if (!obj.isNull("nickname")) {
																dto.nickName = obj.getString("nickname");
															}
															if (!obj.isNull("username")) {
																dto.setUserName(obj.getString("username"));
															}
															if (!obj.isNull("picture")) {
																dto.portraitUrl = obj.getString("picture");
															}
															if (!obj.isNull("phonenumber")) {
																dto.phoneNumber = obj.getString("phonenumber");
															}
															if (!obj.isNull("praise")) {
																dto.setPraiseCount(obj.getString("praise"));
															}
															if (!obj.isNull("comments")) {
																dto.setCommentCount(obj.getString("comments"));
															}
															if (!obj.isNull("work_time")) {
																dto.setWorkTime(obj.getString("work_time"));
															}
															if (!obj.isNull("workstype")) {
																dto.setWorkstype(obj.getString("workstype"));
															}
															if (!obj.isNull("videoshowtime")) {
																dto.showTime = obj.getString("videoshowtime");
															}
															if (!obj.isNull("worksinfo")) {
																String worksinfo = obj.getString("worksinfo");
																if (!TextUtils.isEmpty(worksinfo)) {
																	JSONObject workObj = new JSONObject(worksinfo);
																	//视频
																	if (!workObj.isNull("video")) {
																		JSONObject video = workObj.getJSONObject("video");
																		if (!video.isNull("ORG")) {//腾讯云结构解析
																			JSONObject ORG = video.getJSONObject("ORG");
																			if (!ORG.isNull("url")) {
																				dto.videoUrl = ORG.getString("url");
																			}
																			if (!video.isNull("SD")) {
																				JSONObject SD = video.getJSONObject("SD");
																				if (!SD.isNull("url")) {
																					dto.sd = SD.getString("url");
																				}
																			}
																			if (!video.isNull("HD")) {
																				JSONObject HD = video.getJSONObject("HD");
																				if (!HD.isNull("url")) {
																					dto.hd = HD.getString("url");
																					dto.videoUrl = HD.getString("url");
																				}
																			}
																			if (!video.isNull("FHD")) {
																				JSONObject FHD = video.getJSONObject("FHD");
																				if (!FHD.isNull("url")) {
																					dto.fhd = FHD.getString("url");
																				}
																			}
																		}else {
																			dto.videoUrl = video.getString("url");
																		}
																	}
																	if (!workObj.isNull("thumbnail")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
																		if (!imgObj.isNull("url")) {
																			dto.setUrl(imgObj.getString("url"));
																		}
																	}

																	//图片
																	List<String> urlList = new ArrayList<>();
																	if (!workObj.isNull("imgs1")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																			dto.setUrl(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs2")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs3")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs4")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs5")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs6")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs7")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs8")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	if (!workObj.isNull("imgs9")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
																		if (!imgObj.isNull("url")) {
																			urlList.add(imgObj.getString("url"));
																		}
																	}
																	dto.setUrlList(urlList);
																}
															}

//											zhibaoList.add(dto);
															try {
																if (!TextUtils.isEmpty(dto.workTime)) {
																	long currentDate = System.currentTimeMillis()/1000;
																	long beforeDate = currentDate-dateTime;
																	long workDate = sdf5.parse(dto.workTime).getTime()/1000;
																	if (workDate >= beforeDate && workDate <= currentDate) {
																		if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
																			zhibaoList.add(dto);
																		}
																	}
																}
															} catch (ParseException e) {
																e.printStackTrace();
															}

														}
													}

													ivRefresh.clearAnimation();
													if (zhibaoList.size() > 0) {
														ivZhibao.setVisibility(View.VISIBLE);
														removeMarkers(zhibaoMarkerList);
														addZhibaoMarkers();
													}

												}else {
													//失败

												}
											}
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
	
	/**
	 * 添加直报markers
	 */
	private void addZhibaoMarkers() {
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < zhibaoList.size(); i++) {
			final PhotoDto dto = zhibaoList.get(i);
			ImageLoader.getInstance().loadImage(dto.url, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				}
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap) {
					double lat = Double.valueOf(dto.lat);
					double lng = Double.valueOf(dto.lng);
					MarkerOptions optionsTemp = new MarkerOptions();
					optionsTemp.title(dto.lat+","+dto.lng);
					optionsTemp.snippet(MARKER_ZHIBAO);
					optionsTemp.anchor(0.5f, 0.5f);
					optionsTemp.position(new LatLng(lat, lng));
					View mView = inflater.inflate(R.layout.layout_zhibao_marker, null);
					ImageView ivMarker = (ImageView) mView.findViewById(R.id.ivMarker);
					ImageView ivVideo = (ImageView) mView.findViewById(R.id.ivVideo);
					if (bitmap != null) {
						Bitmap b = CommonUtil.getRoundedCornerBitmap(bitmap, 20);
						if (b != null) {
							ivMarker.setImageBitmap(b);
							LayoutParams params = ivMarker.getLayoutParams();
							params.width = (int) CommonUtil.dip2px(getActivity(), 40);
							params.height = (int) CommonUtil.dip2px(getActivity(), 30);
							ivMarker.setLayoutParams(params);
						}
						if (TextUtils.equals(dto.workstype, "video")) {
							ivVideo.setVisibility(View.VISIBLE);
						}else {
							ivVideo.setVisibility(View.INVISIBLE);
						}
					}
					optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
//					if (leftlatlng == null || rightLatlng == null) {
						final Marker marker = aMap.addMarker(optionsTemp);
						if (isShowZhibao) {
							marker.setVisible(true);
						}else {
							marker.setVisible(false);
						}
						zhibaoMarkerList.add(marker);
						Animation animation = new ScaleAnimation(0,1,0,1);
						animation.setInterpolator(new LinearInterpolator());
						animation.setDuration(300);
						marker.setAnimation(animation);
						marker.startAnimation();
//					}else {
//						if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
//							Marker marker = aMap.addMarker(optionsTemp);
//							if (isShowZhibao) {
//								marker.setVisible(true);
//							}else {
//								marker.setVisible(false);
//							}
//							zhibaoMarkerList.add(marker);
//							Animation animation = new ScaleAnimation(0,1,0,1);
//							animation.setInterpolator(new LinearInterpolator());
//							animation.setDuration(300);
//							marker.setAnimation(animation);
//							marker.startAnimation();
//						}
//					}
				}
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
				}
			});
		}
	}
	
	/**
	 * 显示或隐藏直报marker，这里不清除list
	 */
	private void switchZhibaoMarkers(final boolean flag) {
		for (int i = 0; i < zhibaoMarkerList.size(); i++) {
			final Marker marker = zhibaoMarkerList.get(i);
			Animation animation = null;
			if (flag) {//显示marker
				animation = new ScaleAnimation(0,1,0,1);
				marker.setVisible(flag);
				marker.setToTop();
			}else {//隐藏marker
				animation = new ScaleAnimation(1,0,1,0);
			}
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.setVisible(flag);
				}
			});
		}
	}
	
    /**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		if (mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llPrompt:
		case R.id.ivArrow:
			clickPromptWarning();
			break;
		case R.id.ivWarning:
			if (isShowWarning) {
				isShowWarning = false;
				ivWarning.setImageResource(R.drawable.iv_warning_unselected);
				unselectedWarning();
				tvList.setVisibility(View.GONE);
				arcMenu.setVisibility(View.GONE);
			}else {
				isShowWarning = true;
				ivWarning.setImageResource(R.drawable.iv_warning_selected);
				selectedWarning();
				tvList.setVisibility(View.VISIBLE);
				arcMenu.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivZhibao:
			if (isShowZhibao) {
				isShowZhibao = false;
				ivZhibao.setImageResource(R.drawable.iv_zhibao_unselected);
				switchZhibaoMarkers(false);
			}else {
				isShowZhibao = true;
				ivZhibao.setImageResource(R.drawable.iv_zhibao_selected);
				switchZhibaoMarkers(true);
			}
			break;
		case R.id.ivZixun:
			if (isShowZixun) {
				isShowZixun = false;
				ivZixun.setImageResource(R.drawable.iv_zixun_unselected);
				switchZixunMarkers(false);
			}else {
				isShowZixun = true;
				ivZixun.setImageResource(R.drawable.iv_zixun_selected);
				switchZixunMarkers(true);
			}
			break;
		case R.id.ivLocation:
			if (isExpandMap) {
				isExpandMap = false;
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 12.0f));
			}else {
				isExpandMap = true;
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.5f));
			}
			break;
		case R.id.ivRefresh:
			refresh();
			break;
		case R.id.tvList:
			Intent intent = new Intent(getActivity(), ShawnWarningListActivity.class);
			intent.putExtra("isVisible", true);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
