package com.warning.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.common.CONST;
import com.warning.common.PgyApplication;
import com.warning.util.AuthorityUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 闪屏界面
 */
public class ShawnWelcomeActivity extends BaseActivity{
	
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_welcome);
		mContext = this;
		checkMultiAuthority();
	}

	/**
	 * 申请多个权限
	 */
	private void checkMultiAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
				OkHttpRefreshToken();
			}else {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(mContext, ShawnMainActivity.class));
						finish();
					}
				}, 1500);
			}
		}else {
			AuthorityUtil.deniedList.clear();
			for (int i = 0; i < AuthorityUtil.allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(mContext, AuthorityUtil.allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					AuthorityUtil.deniedList.add(AuthorityUtil.allPermissions[i]);
				}
			}
			if (AuthorityUtil.deniedList.isEmpty()) {//所有权限都授予
				if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
					OkHttpRefreshToken();
				}else {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							startActivity(new Intent(mContext, ShawnMainActivity.class));
							finish();
						}
					}, 1500);
				}
			}else {
				String[] permissions = AuthorityUtil.deniedList.toArray(new String[AuthorityUtil.deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_MULTI:
				if (!TextUtils.isEmpty(TOKEN) && !TextUtils.isEmpty(UID)) {
					OkHttpRefreshToken();
				}else {
					startActivity(new Intent(mContext, ShawnMainActivity.class));
					finish();
				}
				break;
		}
	}

	/**
	 * 刷新token
	 */
	private void OkHttpRefreshToken() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://new.12379.tianqi.cn/Work/Refreshtoken";
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
								promptDialog("获取用户信息失败", "登录失败");
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
										JSONObject object = new JSONObject(result);
										if (!object.isNull("status")) {
											int status  = object.getInt("status");
											if (status == 1) {//成功
												if (!object.isNull("token")) {
													TOKEN = object.getString("token");
												}

												if (!object.isNull("info")) {
													JSONObject obj = new JSONObject(object.getString("info"));
													if (!obj.isNull("uid")) {
														UID = obj.getString("uid");
													}
													if (!obj.isNull("phonenumber")) {
														PHONENUMBER = obj.getString("phonenumber");
													}
													if (!obj.isNull("isadmin")) {
														ISINFOER = obj.getString("isadmin");
													}
													if (!obj.isNull("status")) {
														ISCHEKER = obj.getString("status");
													}
													if (!obj.isNull("nickname")) {
														NICKNAME = obj.getString("nickname");
													}
													if (!obj.isNull("realname")) {
														REALNAME = obj.getString("realname");
													}
													if (!obj.isNull("work")) {
														UNIT = obj.getString("work");
													}
													if (!obj.isNull("email")) {
														MAIL = obj.getString("email");
													}
													if (!obj.isNull("points")) {
														POINTS = obj.getString("points");
													}
													if (!obj.isNull("areas")) {
														AREAS = obj.getString("areas");
													}
													if (!obj.isNull("picture")) {
														PHOTO = obj.getString("picture");
														if (!TextUtils.isEmpty(PHOTO)) {
															downloadPortrait(PHOTO);
														}
													}

													saveUserInfo();
													StatisticUtil.OkhttpPushToken(PgyApplication.pushToken);
													startActivity(new Intent(mContext, ShawnMainActivity.class));
													finish();
												}
											}else {//失败
												if (!object.isNull("msg")) {
													final String msg = object.getString("msg");
													promptDialog("获取用户信息失败", msg);
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
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(final String imgUrl) {
        new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(imgUrl).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final byte[] bytes = response.body().bytes();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								try {
									File files = new File(CONST.SDCARD_PATH);
									if (!files.exists()) {
										files.mkdirs();
									}

									FileOutputStream fos = new FileOutputStream(CONST.PORTRAIT_ADDR);
									if (bitmap != null) {
										bitmap.compress(CompressFormat.PNG, 100, fos);
										if (!bitmap.isRecycled()) {
											bitmap.recycle();
										}
									}
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	/**
	 * 刷新token失败后提示
	 * @param message 标题
	 * @param content 内容
	 */
	private void promptDialog(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_delete, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		TextView tvContent = view.findViewById(R.id.tvContent);
		LinearLayout llNegative = view.findViewById(R.id.llNegative);
		LinearLayout llPositive = view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				clearUserInfo();
				File file = new File(CONST.PORTRAIT_ADDR);
				if (file.exists()) {
					file.delete();
				}
				startActivity(new Intent(mContext, ShawnMainActivity.class));
				finish();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				startActivityForResult(new Intent(mContext, ShawnLoginActivity.class), 1001);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1001:
				startActivity(new Intent(this, ShawnMainActivity.class));
				finish();
				break;

			default:
				break;
			}
		}
	}
	
}
