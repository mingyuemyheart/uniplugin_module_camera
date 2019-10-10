package com.warning.activity;

/**
 * 我的积分
 * @author shawn_sun
 *
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.adapter.MyScoreAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.util.OkHttpUtil;
import com.warning.view.CircleImageView;
import com.warning.view.ScrollviewListview;

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

public class MyScoreActivity extends BaseActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private CircleImageView ivPortrait = null;
	private TextView tvUserName = null;
	private TextView tvScore = null;
	private ScrollviewListview listView = null;
	private MyScoreAdapter mAdapter = null;
	private List<PhotoDto> scoreList = new ArrayList<>();
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private LinearLayout llContent = null;
	private String totalScore = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_score);
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
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.post(new Runnable() {
			@Override
			public void run() {
				refreshLayout.setRefreshing(true);
			}
		});
	}

	@Override
	public void onRefresh() {
		refresh();
	}

	private void refresh() {
		refreshUserInfo();
		OkhttpScore("http://new.12379.tianqi.cn/Work/getpointmes");
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("我的积分");
		tvTitle.setFocusable(true);
		tvTitle.setFocusableInTouchMode(true);
		tvTitle.requestFocus();
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvScore = (TextView) findViewById(R.id.tvScore);
		llContent = (LinearLayout) findViewById(R.id.llContent);

		refresh();
	}
	
	/**
	 * 显示用户信息
	 */
	private void refreshUserInfo() {
		if (!TextUtils.isEmpty(TOKEN)) {//已登录状态
			//刷新用户名
			if (!TextUtils.isEmpty(NICKNAME)) {
				tvUserName.setText(NICKNAME);
			}else if (!TextUtils.isEmpty(PHONENUMBER)) {
				if (PHONENUMBER.length() >= 7) {
					tvUserName.setText(PHONENUMBER.replace(PHONENUMBER.substring(3, 7), "****"));
				}else {
					tvUserName.setText(PHONENUMBER);
				}
			}
			
			//刷新头像
			Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
			if (bitmap != null) {
				ivPortrait.setImageBitmap(bitmap);
			}else {
				ivPortrait.setImageResource(R.drawable.iv_portrait);
			}
		}else {//未登录状态
			tvUserName.setText("点击登录");
			ivPortrait.setImageResource(R.drawable.iv_portrait);
		}
	}
	
	private void initListView() {
		listView = (ScrollviewListview) findViewById(R.id.listView);
		mAdapter = new MyScoreAdapter(mContext, scoreList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = scoreList.get(arg2);
				if (TextUtils.equals(dto.scoreType, "2")) {//登录
					return;
				}else {
					Intent intent = new Intent(mContext, ScoreDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}
	
	/**
	 * 获取积分信息
	 */
	private void OkhttpScore(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("token", TOKEN);
				builder.add("uid", UID);
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
							}
						});
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
												int status = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("sum")) {
														totalScore = object.getString("sum");
													}
													if (!object.isNull("info")) {
														JSONArray array = object.getJSONArray("info");
														scoreList.clear();
														for (int i = 0; i < array.length(); i++) {
															PhotoDto dto = new PhotoDto();
															JSONObject itemObj = array.getJSONObject(i);
															if (!itemObj.isNull("name")) {
																dto.scoreName = itemObj.getString("name");
															}
															if (!itemObj.isNull("method")) {
																dto.scoreType = itemObj.getString("method");
															}
															if (!itemObj.isNull("sum")) {
																dto.score = itemObj.getInt("sum");
															}
															scoreList.add(dto);
														}

														if (scoreList.size() > 0 && mAdapter != null) {
															tvScore.setText(totalScore);
															mAdapter.notifyDataSetChanged();
														}
														refreshLayout.setRefreshing(false);
														llContent.setVisibility(View.VISIBLE);
													}
												}else {//失败
													if (!object.isNull("msg")) {
														try {
															Toast.makeText(mContext, object.getString("msg"), Toast.LENGTH_SHORT).show();
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
