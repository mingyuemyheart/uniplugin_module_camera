package com.warning.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.autonavi.ae.gmap.gloverlay.GLRctRouteOverlay;
import com.warning.R;
import com.warning.activity.WebviewActivity;
import com.warning.activity.YiqingListActivity;
import com.warning.activity.YiqingPercentActivity;
import com.warning.dto.NewsDto;
import com.warning.dto.WarningDto;
import com.warning.dto.YiqingDto;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.MainViewPager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 疫情
 * @author shawn_sun
 */
public class Fragment5 extends Fragment implements OnClickListener, OnMapClickListener, OnMarkerClickListener, InfoWindowAdapter{
	
	private TextView tvPrompt;//没有数据时提示
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private ImageView ivRefresh, ivYiqingLegend;
	private LinearLayout llPrompt = null;
	private Map<String, YiqingDto> yiqingList = new LinkedHashMap<>();
	private List<Marker> yiqingMarkerList = new ArrayList<>();
	private Marker sMarker = null;
	private DecimalFormat df = new DecimalFormat("###,###,###");
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	private long confirmCount, deathCount;
	private MyBroadCastReceiver mReceiver = null;
	private RelativeLayout reWarning;
	private Map<String, List<Polygon>> mapData = new HashMap<>();//mapid为key
	private String sMapId;

	private List<WarningDto> warningList = new ArrayList<>();
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ImageView[] ivTips;//装载点的数组
	private ViewGroup viewGroup;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment5, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initBroadCast();
		initAmap(view, savedInstanceState);
		initWidget(view);
	}

	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Fragment5.class.getName());
		getActivity().registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), Fragment5.class.getName())) {
				refresh();
			}
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		llPrompt = (LinearLayout) view.findViewById(R.id.llPrompt);
		llPrompt.setOnClickListener(this);
		ivYiqingLegend = view.findViewById(R.id.ivYiqingLegend);
		ImageView ivYiqingInfo = view.findViewById(R.id.ivYiqingInfo);
		ivYiqingInfo.setOnClickListener(this);
		ImageView ivYiqingPercent = view.findViewById(R.id.ivYiqingPercent);
		ivYiqingPercent.setOnClickListener(this);
		TextView tvList = view.findViewById(R.id.tvList);
		tvList.setOnClickListener(this);
		viewGroup = view.findViewById(R.id.viewGroup);
        viewPager = view.findViewById(R.id.viewPager);
		reWarning = view.findViewById(R.id.reWarning);

		OkhttpWarning();
    }
	
	private void refresh() {
		android.view.animation.Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.round_animation);
		ivRefresh.startAnimation(animation);
		OkhttpYiqing();
	}

	private void OkhttpYiqing() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://warn-wx.tianqi.cn/Test/getworldqydata";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										yiqingList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											YiqingDto dto = new YiqingDto();
											JSONObject itemObj = array.getJSONObject(i);

											if (!itemObj.isNull("mapid")) {
												dto.mapid = itemObj.getString("mapid");
											}

											dto.count = "0";
											if (!itemObj.isNull("count")) {
												String count = itemObj.getString("count");
												if (TextUtils.equals(count, "None")) {
													dto.count = "0";
												} else {
													dto.count = count;
												}
											}
											confirmCount += Long.valueOf(dto.count);

											dto.death_count = "0";
											if (!itemObj.isNull("death_count")) {
												String death_count = itemObj.getString("death_count");
												if (TextUtils.equals(death_count, "None")) {
													dto.death_count = "0";
												} else {
													dto.death_count = death_count;
												}
											}
											deathCount += Long.valueOf(dto.death_count);

											if (!itemObj.isNull("nameEn")) {
												dto.nameEn = itemObj.getString("nameEn");
											}
											if (!itemObj.isNull("nameZn")) {
												dto.nameZn = itemObj.getString("nameZn");
											}
											if (!itemObj.isNull("latlon")) {
												String latlon = itemObj.getString("latlon");
												if (!TextUtils.isEmpty(latlon) && latlon.contains(",")) {
													String[] location = latlon.split(",");
													dto.lat = Double.parseDouble(location[0]);
													dto.lng = Double.parseDouble(location[1]);
													yiqingList.put(dto.mapid, dto);
												}
											}
										}

										ivRefresh.clearAnimation();
										if (yiqingList.size() > 0) {
											llPrompt.setVisibility(View.VISIBLE);

											String str1 = sdf1.format(new Date())+"，全球新冠肺炎确诊病例";
											String str2 = df.format(confirmCount);
											String str3 = "例，死亡";
											String str4 = df.format(deathCount);
											String str5 = "例。";
											String warningInfo = str1+str2+str3+str4+str5;
											SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
											ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
											ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
											ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
											ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.red));
											ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
											builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan2, str1.length(), str1.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan3, str1.length()+str2.length(), str1.length()+str2.length()+str3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan4, str1.length()+str2.length()+str3.length(), str1.length()+str2.length()+str3.length()+str4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan5, str1.length()+str2.length()+str3.length()+str4.length(), str1.length()+str2.length()+str3.length()+str4.length()+str5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											tvPrompt.setText(builder);

											ivYiqingLegend.setVisibility(View.VISIBLE);
//											removeMarkers(yiqingMarkerList);
//											addYiqingMarkers();
											drawWorld();
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

	private void drawWorld() {
		if (aMap == null) {
			return;
		}
		mapData.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Iterator<Map.Entry<String, YiqingDto>> entries = yiqingList.entrySet().iterator();
				while(entries.hasNext()){
					Map.Entry<String, YiqingDto> entry = entries.next();
					YiqingDto dto = entry.getValue();
					long count = Long.valueOf(dto.count);
					int fillColor = 0;
					if (count >= 2000) {
						fillColor = 0xff912a2d;
					} else if (count >= 1000) {
						fillColor = 0xffe23e49;
					} else if (count>= 100) {
						fillColor = 0xffeb6830;
					} else if (count >= 10) {
						fillColor = 0xffef8a46;
					} else if (count >= 1) {
						fillColor = 0xfffdbe31;
					} else {
						fillColor = 0xffdcdcdc;
					}
					String result = CommonUtil.getJson(getActivity(), "world_geo/"+dto.mapid+".json");
					if (!TextUtils.isEmpty(result)) {
						try {
							JSONObject obj = new JSONObject(result);
							if (!obj.isNull("geometry")) {
								JSONObject geometry = obj.getJSONObject("geometry");
								List<Polygon> polygons = new ArrayList<>();
								if (!geometry.isNull("coordinates")) {
									JSONArray coordinates = geometry.getJSONArray("coordinates");
									for (int i = 0; i < coordinates.length(); i++) {
										JSONArray array1 = coordinates.getJSONArray(i);
										for (int j = 0; j < array1.length(); j++) {
											JSONArray array2 = array1.getJSONArray(j);
											PolygonOptions polylineOption = new PolygonOptions();
											polylineOption.fillColor(fillColor);
											polylineOption.strokeColor(0xcc000000).strokeWidth(3);
											for (int k = 0; k < array2.length(); k++) {
												JSONArray array3 = array2.getJSONArray(k);
												double lat = array3.getDouble(1);
												double lng = array3.getDouble(0);
												if (lng < 179) {
													polylineOption.add(new LatLng(lat, lng));
												}
											}
											Polygon polygon = aMap.addPolygon(polylineOption);
											polygons.add(polygon);
										}
									}
								}
								mapData.put(dto.mapid, polygons);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}


				//适配geo数据层级不一致问题
				entries = yiqingList.entrySet().iterator();
				while(entries.hasNext()){
					Map.Entry<String, YiqingDto> entry = entries.next();
					YiqingDto dto = entry.getValue();
					long count = Long.valueOf(dto.count);
					int fillColor = 0;
					if (count >= 2000) {
						fillColor = 0xff912a2d;
					} else if (count >= 1000) {
						fillColor = 0xffe23e49;
					} else if (count>= 100) {
						fillColor = 0xffeb6830;
					} else if (count >= 10) {
						fillColor = 0xffef8a46;
					} else if (count >= 1) {
						fillColor = 0xfffdbe31;
					} else {
						fillColor = 0xffdcdcdc;
					}
					String result = CommonUtil.getJson(getActivity(), "world_geo/"+dto.mapid+".json");
					if (!TextUtils.isEmpty(result)) {
						try {
							JSONObject obj = new JSONObject(result);
							if (!obj.isNull("geometry")) {
								JSONObject geometry = obj.getJSONObject("geometry");
								List<Polygon> polygons = new ArrayList<>();
								if (!geometry.isNull("coordinates")) {
									JSONArray coordinates = geometry.getJSONArray("coordinates");
									for (int i = 0; i < coordinates.length(); i++) {
										JSONArray array1 = coordinates.getJSONArray(i);
										PolygonOptions polylineOption = new PolygonOptions();
										polylineOption.fillColor(fillColor);
										polylineOption.strokeColor(0xcc000000).strokeWidth(3);
										for (int j = 0; j < array1.length(); j++) {
											JSONArray array2 = array1.getJSONArray(j);
											double lat = array2.getDouble(1);
											double lng = array2.getDouble(0);
											if (lng < 179) {
												polylineOption.add(new LatLng(lat, lng));
											}
										}
										Polygon polygon = aMap.addPolygon(polylineOption);
										polygons.add(polygon);
									}
								}
								mapData.put(dto.mapid, polygons);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 添加疫情markers
	 */
	private void addYiqingMarkers() {
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < yiqingList.size(); i++) {
			YiqingDto dto = yiqingList.get(i);
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title(dto.nameZn);
			optionsTemp.snippet("确诊"+df.format(Float.valueOf(dto.count))+"例，死亡"+df.format(Float.valueOf(dto.death_count))+"例");
			optionsTemp.anchor(0.5f, 0.5f);
			optionsTemp.position(new LatLng(dto.lat, dto.lng));
			View mView = inflater.inflate(R.layout.layout_yiqing_marker, null);
			ImageView ivMarker = (ImageView) mView.findViewById(R.id.ivMarker);
			long count = Long.valueOf(dto.count);
			if (count >= 2000) {
				ivMarker.setImageResource(R.drawable.yiqing_circle6);
			} else if (count >= 1000) {
				ivMarker.setImageResource(R.drawable.yiqing_circle5);
			} else if (count>= 100) {
				ivMarker.setImageResource(R.drawable.yiqing_circle4);
			} else if (count >= 10) {
				ivMarker.setImageResource(R.drawable.yiqing_circle3);
			} else if (count >= 1) {
				ivMarker.setImageResource(R.drawable.yiqing_circle2);
			} else {
				ivMarker.setImageResource(R.drawable.yiqing_circle1);
			}
			optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
			final Marker marker = aMap.addMarker(optionsTemp);
			yiqingMarkerList.add(marker);
			Animation animation = new ScaleAnimation(0,1,0,1);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
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
		aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.0f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);

		TextView tvMapNumber = (TextView) view.findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (sMarker != null) {
			if (sMarker.isInfoWindowShown()) {
				sMarker.hideInfoWindow();
			}
		}
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Iterator<Map.Entry<String, List<Polygon>>> entries = mapData.entrySet().iterator();
		while(entries.hasNext()) {
			Map.Entry<String, List<Polygon>> entry = entries.next();
			List<Polygon> polygons = entry.getValue();
			for (Polygon polygon : polygons) {
				if (polygon != null && polygon.contains(arg0)) {
					if (TextUtils.equals(sMapId, entry.getKey())) {
						return;
					}
					sMapId = entry.getKey();
					YiqingDto dto = yiqingList.get(sMapId);
					MarkerOptions optionsTemp = new MarkerOptions();
					optionsTemp.anchor(0.5f, 1.0f);
					optionsTemp.position(new LatLng(dto.lat, dto.lng));
					View mView = inflater.inflate(R.layout.yiqing_marker_info, null);
					TextView tvNameZn = mView.findViewById(R.id.tvNameZn);
					TextView tvCount = mView.findViewById(R.id.tvCount);
					tvNameZn.setText(dto.nameZn);
					tvCount.setText("确诊"+df.format(Float.valueOf(dto.count))+"例，死亡"+df.format(Float.valueOf(dto.death_count))+"例");
					optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
					if (sMarker != null) {
						sMarker.remove();
					}
					sMarker = aMap.addMarker(optionsTemp);
					aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(dto.lat, dto.lng)));
					break;
				}
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			sMarker = marker;
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.yiqing_marker_info, null);
		TextView tvNameZn = mView.findViewById(R.id.tvNameZn);
		TextView tvCount = mView.findViewById(R.id.tvCount);
		tvNameZn.setText(marker.getTitle());
		tvCount.setText(marker.getSnippet());
		return mView;
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
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
			case R.id.ivRefresh:
				refresh();
				break;
			case R.id.tvList:
				startActivity(new Intent(getActivity(), YiqingListActivity.class));
				break;
			case R.id.ivYiqingPercent:
				startActivity(new Intent(getActivity(), YiqingPercentActivity.class));
				break;
			case R.id.ivYiqingInfo:
				Intent intent = new Intent(getActivity(), WebviewActivity.class);
				Bundle bundle = new Bundle();
				NewsDto data = new NewsDto();
				data.title = "数据免责声明";
				data.url = "http://12379.tianqi.cn/Public/12379_note.html";
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
				break;

		default:
			break;
		}
	}

	/**
	 * 获取预警
	 */
	private void OkhttpWarning() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?areaid=000000";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								reWarning.setVisibility(View.GONE);
							}
						});
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

												if (dto.name.contains("外交部") || dto.name.contains("文化和旅游部")) {
													warningList.add(dto);
												}
											}

											reWarning.setVisibility(View.VISIBLE);
											initViewPager();
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
	 * 初始化viewPager
	 */
	private void initViewPager() {
		mHandler.removeMessages(AUTO_PLUS);
		ivTips = new ImageView[warningList.size()];
		viewGroup.removeAllViews();
		fragments.clear();
		for (int i = 0; i < warningList.size(); i++) {
			WarningDto data = warningList.get(i);
			Fragment fragment = new WarningFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", data);
			fragment.setArguments(bundle);
			fragments.add(fragment);

			ImageView imageView = new ImageView(getActivity());
			imageView.setLayoutParams(new ViewGroup.LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_white);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);
		}

		if (warningList.size() == 0) {
			viewPager.setVisibility(View.GONE);
			viewGroup.setVisibility(View.GONE);
		}
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < warningList.size(); i++) {
					if(i == position){
						ivTips[i].setBackgroundResource(R.drawable.point_white);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		viewPager.setAdapter(new MyPagerAdapter());

		if (fragments.size() > 1) {
			mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME);
		}
	}

	private final int AUTO_PLUS = 1001;
	private static final int PHOTO_CHANGE_TIME = 3000;//定时变量
	private int index_plus = 0;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case AUTO_PLUS:
					viewPager.setCurrentItem(index_plus++);//收到消息后设置当前要显示的图片
					mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME);
					if (index_plus >= fragments.size()) {
						index_plus = 0;
					}
					break;
				default:
					break;
			}
		};
	};

	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}

}
