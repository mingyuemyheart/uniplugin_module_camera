package com.warning.fragment;

/**
 * 直报
 */

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.warning.R;
import com.warning.activity.CameraActivity;
import com.warning.activity.LoginActivity;
import com.warning.activity.OnlinePictureActivity;
import com.warning.activity.OnlineVideoActivity;
import com.warning.adapter.VideoWallAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.util.AuthorityUtil;
import com.warning.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.warning.activity.BaseActivity.TOKEN;

public class Fragment4 extends Fragment implements OnRefreshListener {
	
	private ListView mListView = null;
	private VideoWallAdapter mAdapter = null;
	private List<PhotoDto> zhibaoList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 20;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private String ZHIBAOURL = "http://new.12379.tianqi.cn/Work/getlist";
	private TextView tvPrompt = null;
	private MyBroadCastReceiver mReceiver = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment4, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initBroadCast();
		initRefreshLayout(view);
		initWidget(view);
		initListView(view);
	}

	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Fragment4.class.getName());
		getActivity().registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), Fragment4.class.getName())) {
				refresh();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.post(new Runnable() {
			@Override
			public void run() {
				refreshLayout.setRefreshing(true);
			}
		});
		refreshLayout.setOnRefreshListener(this);
	}
	
	@Override
	public void onRefresh() {
		refresh();
	}
	
	private void refresh() {
		zhibaoList.clear();
		page = 1;
		OkhttpZhibao(ZHIBAOURL);
	}
	
	private void initWidget(View view) {
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		
//		refresh();
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new VideoWallAdapter(getActivity(), zhibaoList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = zhibaoList.get(arg2);
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
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OkhttpZhibao(ZHIBAOURL);
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
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
				builder.add("p", page+"");
				builder.add("size", pageSize+"");
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
										if (!object.isNull("status")) {
											int status  = object.getInt("status");
											if (status == 1) {//成功
												if (!object.isNull("info")) {
													JSONArray array = new JSONArray(object.getString("info"));
													for (int i = 0; i < array.length(); i++) {
														JSONObject obj = (JSONObject) array.opt(i);
														PhotoDto dto = new PhotoDto();
														if (page == 1 && i == 0) {
															dto.isTop = true;
														}
														if (!obj.isNull("id")) {
															dto.setVideoId(obj.getString("id"));
														}
														if (!obj.isNull("uid")) {
															dto.uid = obj.getString("uid");
														}
														if (!obj.isNull("title")) {
															dto.title = obj.getString("title");
														}
														if (!obj.isNull("content")) {
															dto.content = obj.getString("content");
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

														if (!TextUtils.isEmpty(dto.workTime)) {
															zhibaoList.add(dto);
														}
													}

													refreshLayout.setRefreshing(false);
													if (zhibaoList.size() == 0) {
														tvPrompt.setText("暂无直报内容");
														tvPrompt.setVisibility(View.VISIBLE);
														return;
													}
													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}
												}else {
													tvPrompt.setText("暂无直报内容");
													tvPrompt.setVisibility(View.VISIBLE);
													refreshLayout.setRefreshing(false);
												}
											}else {
												//失败

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
	
	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请相机权限
	 */
	public void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			if (TextUtils.isEmpty(TOKEN)) {
				startActivityForResult(new Intent(getActivity(), LoginActivity.class), 1);
			}else {
				startActivity(new Intent(getActivity(), CameraActivity.class));
			}
		}else {
			deniedList.clear();
			for (int i = 0; i < allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(getActivity(), allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i]);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				if (TextUtils.isEmpty(TOKEN)) {
					startActivityForResult(new Intent(getActivity(), LoginActivity.class), 1);
				}else {
					startActivity(new Intent(getActivity(), CameraActivity.class));
				}
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				Fragment4.this.requestPermissions(permissions, AuthorityUtil.AUTHOR_CAMERA);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_CAMERA:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						if (TextUtils.isEmpty(TOKEN)) {
							startActivityForResult(new Intent(getActivity(), LoginActivity.class), 1);
						}else {
							startActivity(new Intent(getActivity(), CameraActivity.class));
						}
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、相机权限、麦克风、存储权限，是否前往设置？");
					}
				}else {
					for (int i = 0; i < permissions.length; i++) {
						if (!Fragment4.this.shouldShowRequestPermissionRationale(permissions[i])) {
							AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、相机权限、麦克风、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 1:
				startActivity(new Intent(getActivity(), CameraActivity.class));
				break;

			default:
				break;
			}
		}
	}

}
