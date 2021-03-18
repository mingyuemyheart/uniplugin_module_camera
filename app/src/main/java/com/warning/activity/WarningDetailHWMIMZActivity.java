package com.warning.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.umeng.message.UmengNotifyClickActivity;
import com.warning.R;
import com.warning.common.CONST;
import com.warning.manager.DBManager;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;

import net.tsz.afinal.FinalBitmap;

import org.android.agoo.common.AgooConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警信息详情
 */

public class WarningDetailHWMIMZActivity extends UmengNotifyClickActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle,tvName,tvTime,tvIntro,tvGuide;
	private ImageView ivShare, imageView,ivPicture;//预警图标
	private ScrollView scrollView = null;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private String dataUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning_detail_hw_mi_mz);
		mContext = this;
		initRefreshLayout();
		initWidget();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.warning_detail));
		imageView = (ImageView) findViewById(R.id.imageView);
		ivPicture = findViewById(R.id.ivPicture);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.GONE);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvGuide = (TextView) findViewById(R.id.tvGuide);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
	}

	private void refresh() {
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpWarningDetail(dataUrl);
		}else {
			refreshLayout.setRefreshing(false);
		}
	}

	@Override
	public void onMessage(final Intent intent) {
		super.onMessage(intent);
		final String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
		Log.e("body", body);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(body)) {
					try {
						JSONObject obj = new JSONObject(body);
						if (!obj.isNull("extra")) {
							JSONObject extra = obj.getJSONObject("extra");
							if (!extra.isNull("url")) {
								String url = extra.getString("url");
								dataUrl = "https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/"+url;
								refresh();
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 获取预警详情
	 */
	private void OkHttpWarningDetail(final String url) {
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("sendTime")) {
												tvTime.setText(object.getString("sendTime"));
											}

											if (!object.isNull("description")) {
												tvIntro.setText(object.getString("description"));
											}

											String name = object.getString("headline");
											if (!TextUtils.isEmpty(name)) {
												tvName.setText(name.replace(getString(R.string.publish), getString(R.string.publish)+"\n"));
											}

											Bitmap bitmap = null;
											String color = object.getString("severityCode");
											String type = object.getString("eventType");
											if (color.equals(CONST.blue[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+type+CONST.blue[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
												}
											}else if (color.equals(CONST.yellow[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+type+CONST.yellow[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
												}
											}else if (color.equals(CONST.orange[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+type+CONST.orange[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
												}
											}else if (color.equals(CONST.red[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+type+CONST.red[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
												}
											}
											imageView.setImageBitmap(bitmap);

											if (!object.isNull("identifier")) {
												String identifier = object.getString("identifier");
												if (!TextUtils.isEmpty(identifier)) {
													String imgUrl = String.format("http://12379.tianqi.cn/Public/gw_html_imgs/%s.png", identifier);
													FinalBitmap finalBitmap = FinalBitmap.create(mContext);
													finalBitmap.display(ivPicture, imgUrl, null, 0);
												}
											}

											initDBManager(color, type);
											scrollView.setVisibility(View.VISIBLE);
											ivShare.setVisibility(View.VISIBLE);
											refreshLayout.setRefreshing(false);
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
	 * 初始化数据库
	 */
	private void initDBManager(String color, String type) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + type+color + "\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			tvGuide.setText(getString(R.string.warning_guide)+cursor.getString(cursor.getColumnIndex("WarningGuide")));
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.ivShare:
				Bitmap bitmap1 = CommonUtil.captureScrollView(scrollView);
				Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
				Bitmap bitmap = CommonUtil.mergeBitmap(WarningDetailHWMIMZActivity.this, bitmap1, bitmap2, false);
				CommonUtil.clearBitmap(bitmap1);
				CommonUtil.clearBitmap(bitmap2);
				CommonUtil.share(WarningDetailHWMIMZActivity.this, bitmap);
				break;

		default:
			break;
		}
	}

}
