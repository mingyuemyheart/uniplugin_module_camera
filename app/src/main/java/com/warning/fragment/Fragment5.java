package com.warning.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.warning.R;
import com.warning.activity.WebviewActivity;
import com.warning.activity.YiqingListActivity;
import com.warning.dto.NewsDto;
import com.warning.dto.YiqingDto;
import com.warning.util.OkHttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	private List<YiqingDto> yiqingList = new ArrayList<>();
	private List<Marker> yiqingMarkerList = new ArrayList<>();
	private Marker sMarker = null;
	private DecimalFormat df = new DecimalFormat("###,###,###");
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	private long confirmCount, deathCount;
	private MyBroadCastReceiver mReceiver = null;
	
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
		TextView tvList = view.findViewById(R.id.tvList);
		tvList.setOnClickListener(this);
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
													yiqingList.add(dto);
												}
											}
										}

										ivRefresh.clearAnimation();
										if (yiqingList.size() > 0) {
											llPrompt.setVisibility(View.VISIBLE);
											tvPrompt.setText(sdf1.format(new Date())+"，全球新冠肺炎确诊病例"+df.format(confirmCount)+"例，死亡"+df.format(deathCount)+"例。");
											ivYiqingLegend.setVisibility(View.VISIBLE);
											removeMarkers(yiqingMarkerList);
											addYiqingMarkers();
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
			if (count >= 50000) {
				ivMarker.setImageResource(R.drawable.yiqing_circle6);
			} else if (count >= 5000) {
				ivMarker.setImageResource(R.drawable.yiqing_circle5);
			} else if (count>= 500) {
				ivMarker.setImageResource(R.drawable.yiqing_circle4);
			} else if (count >= 50) {
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
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			sMarker = marker;
		}
		return false;
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
				Intent intent = new Intent(getActivity(), YiqingListActivity.class);
				intent.putParcelableArrayListExtra("dataList", (ArrayList<? extends Parcelable>) yiqingList);
				startActivity(intent);
				break;
			case R.id.ivYiqingInfo:
				intent = new Intent(getActivity(), WebviewActivity.class);
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

}
