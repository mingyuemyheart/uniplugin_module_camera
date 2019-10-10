package com.warning.activity;


/**
 * 预警详情
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.warning.R;
import com.warning.common.CONST;
import com.warning.manager.DBManager;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class WarningDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String html = null;
	private ScrollView scrollView = null;
	private ImageView ivShare = null;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning_detail);
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
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
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
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvGuide = (TextView) findViewById(R.id.tvGuide);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.GONE);

		if (getIntent().hasExtra("url")) {
			html = getIntent().getStringExtra("url");
			refresh();
		}
	}
	
	private void refresh() {
		if (!TextUtils.isEmpty(html)) {
			OkhttpWarningDetail("https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/"+html);
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private void queryWarningGuide() {
		if (TextUtils.isEmpty(html)) {
			return;
		}
		String[] array = html.split("-");
		String item2 = array[2];
		String type = item2.substring(0, 5);
		String color = item2.substring(5, 7);
		
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + type+color + "\"",null);
		String content = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"));
		}
		if (!TextUtils.isEmpty(content)) {
			tvGuide.setText(getString(R.string.warning_guide)+content);
			tvGuide.setVisibility(View.VISIBLE);
		}else {
			tvGuide.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获取预警详情
	 */
	private void OkhttpWarningDetail(final String url) {
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
											if (object.getString("severityCode").equals(CONST.blue[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.orange[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.red[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.unknown[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/default"+CONST.imageSuffix);
											}
											imageView.setImageBitmap(bitmap);

											if (!TextUtils.isEmpty(tvIntro.getText().toString()) && !tvIntro.getText().toString().contains("防御指南")) {
												queryWarningGuide();
											}

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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureScrollView(scrollView);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
			Bitmap bitmap = CommonUtil.mergeBitmap(WarningDetailActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(WarningDetailActivity.this, bitmap);
			break;

		default:
			break;
		}
	}
	
}
