package com.warning.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.adapter.ScoreDetailAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
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

/**
 * 积分详情
 */

public class ScoreDetailActivity extends BaseActivity implements OnClickListener, OnRefreshListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private ScoreDetailAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 20;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private String baseUrl = "http://new.12379.tianqi.cn/Work/getpointinfo";//获取积分详情
	private PhotoDto data = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_detail);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
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
		mList.clear();
		page = 1;
		OkhttpList(baseUrl);
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		data = getIntent().getExtras().getParcelable("data");
		if (!TextUtils.isEmpty(data.scoreName)) {
			tvTitle.setText(data.scoreName);
		}
		
		refresh();
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new ScoreDetailAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(mContext, OnlinePictureActivity.class);
				}else {
					intent.setClass(mContext, OnlineVideoActivity.class);
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
					OkhttpList(baseUrl);
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}
	
	/**
	 * 异步请求
	 */
	private void OkhttpList(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("p", page+"");
				builder.add("size", pageSize+"");
				builder.add("token", TOKEN);
				builder.add("uid", UID);
				builder.add("method", data.scoreType);
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										final JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														JSONArray array = new JSONArray(object.getString("info"));
														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = (JSONObject) array.opt(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("points")) {
																dto.score = obj.getInt("points");
															}
															if (!obj.isNull("method")) {
																dto.scoreType = obj.getString("method");
															}
															if (!obj.isNull("why")) {
																JSONObject why = new JSONObject(obj.getString("why"));
																if (!why.isNull("type")) {
																	dto.scoreName = why.getString("type");
																}
															}

															if (!obj.isNull("work") && !TextUtils.isEmpty(obj.getString("work"))) {
																JSONObject itemObj = new JSONObject(obj.getString("work"));
																if (!itemObj.isNull("id")) {
																	dto.setVideoId(itemObj.getString("id"));
																}
																if (!itemObj.isNull("uid")) {
																	dto.uid = itemObj.getString("uid");
																}
																if (!itemObj.isNull("title")) {
																	dto.setTitle(itemObj.getString("title"));
																}
																if (!itemObj.isNull("status")) {
																	dto.status = itemObj.getString("status");
																}
																if (!itemObj.isNull("create_time")) {
																	dto.setCreateTime(itemObj.getString("create_time"));
																}
																if (!itemObj.isNull("latlon")) {
																	String latlon = itemObj.getString("latlon");
																	if (!TextUtils.isEmpty(latlon) && !TextUtils.equals(latlon, ",")) {
																		String[] latLngArray = latlon.split(",");
																		dto.lat = latLngArray[0];
																		dto.lng = latLngArray[1];
																	}
																}
																if (!itemObj.isNull("location")) {
																	dto.setLocation(itemObj.getString("location"));
																}
																if (!itemObj.isNull("nickname")) {
																	dto.nickName = itemObj.getString("nickname");
																}
																if (!itemObj.isNull("picture")) {
																	dto.portraitUrl = itemObj.getString("picture");
																}
																if (!itemObj.isNull("username")) {
																	dto.setUserName(itemObj.getString("username"));
																}
																if (!itemObj.isNull("phonenumber")) {
																	dto.phoneNumber = itemObj.getString("phonenumber");
																}
																if (!itemObj.isNull("praise")) {
																	dto.setPraiseCount(itemObj.getString("praise"));
																}
																if (!itemObj.isNull("comments")) {
																	dto.setCommentCount(itemObj.getString("comments"));
																}
																if (!itemObj.isNull("work_time")) {
																	dto.setWorkTime(itemObj.getString("work_time"));
																}
																if (!itemObj.isNull("workstype")) {
																	dto.setWorkstype(itemObj.getString("workstype"));
																}
																if (!itemObj.isNull("videoshowtime")) {
																	dto.showTime = itemObj.getString("videoshowtime");
																}
																if (!itemObj.isNull("worksinfo")) {
																	JSONObject workObj = new JSONObject(itemObj.getString("worksinfo"));

																	if (!workObj.isNull("thumbnail")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
																		if (!imgObj.isNull("url")) {
																			dto.setUrl(imgObj.getString("url"));
																		}
																	}

																	if (!workObj.isNull("video")) {
																		JSONObject imgObj = new JSONObject(workObj.getString("video"));
																		if (!imgObj.isNull("url")) {
																			dto.setVideoUrl(imgObj.getString("url"));
																		}
																	}

																	List<String> urlList = new ArrayList<String>();
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

															if (!TextUtils.isEmpty(dto.getWorkTime())) {
																mList.add(dto);
															}
														}

														refreshLayout.setRefreshing(false);
														if (mAdapter != null) {
															mAdapter.notifyDataSetChanged();
														}

													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														try {
															String msg = object.getString("msg");
															if (msg != null) {
																Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
															}
														} catch (JSONException e) {
															e.printStackTrace();
														}
													}

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
