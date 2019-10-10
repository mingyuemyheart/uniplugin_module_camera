package com.warning.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.warning.R;
import com.warning.common.CONST;
import com.warning.common.PgyApplication;
import com.warning.util.AuthorityUtil;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录界面
 */
public class ShawnLoginActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private EditText etUserName,etPwd;
	private TextView tvSend;
	private int seconds = 60;
	private Timer timer;
	private double lat = 0, lng = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_login);
		mContext = this;
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		RelativeLayout reTitle = findViewById(R.id.reTitle);
		reTitle.setBackgroundColor(Color.TRANSPARENT);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("登录");
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		TextView tvLogin = findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvSend = findViewById(R.id.tvSend);
		tvSend.setOnClickListener(this);

		startLocation();
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
		mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
		mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
		mLocationClient.setLocationListener(new AMapLocationListener() {
			@Override
			public void onLocationChanged(AMapLocation aMapLocation) {
				if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
					lat = aMapLocation.getLatitude();
					lng = aMapLocation.getLongitude();
				}
			}
		});
		mLocationClient.startLocation();//启动定位
	}

	/**
	 * 获取验证码
	 */
	private void OkHttpCode() {
		final String url = "http://new.12379.tianqi.cn/Work/LoginSendcode";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("phonenumber", etUserName.getText().toString().trim());
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								resetTimer();
								Toast.makeText(mContext, "登录失败，重新登录试试", Toast.LENGTH_SHORT).show();
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("status")) {
											int status  = obj.getInt("status");
											if (status == 1) {//成功发送验证码
												//发送验证码成功
												etPwd.setFocusable(true);
												etPwd.setFocusableInTouchMode(true);
												etPwd.requestFocus();
											}else {//发送验证码失败
												if (!obj.isNull("msg")) {
													resetTimer();
													Toast.makeText(mContext, obj.getString("msg"), Toast.LENGTH_SHORT).show();
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
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 101:
				if (seconds <= 0) {
					resetTimer();
				}else {
					tvSend.setText(seconds--+"s");
				}
				break;

			default:
				break;
			}
		};
	};
	
	/**
	 * 验证登录信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入手机验证码", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 登录接口
	 */
	private void OkhttpLogin() {
		final String url = "http://new.12379.tianqi.cn/Work/Login";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("phonenumber", etUserName.getText().toString().trim());
		builder.add("vcode", etPwd.getText().toString().trim());
		builder.add("lat", lat+"");
		builder.add("lon", lng+"");
		builder.add("platform_type", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(mContext));
		builder.add("mobile_type", android.os.Build.MODEL);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, "登录失败", Toast.LENGTH_SHORT).show();
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
															checkAuthority();
														}
													}

													saveUserInfo();
													StatisticUtil.OkhttpPushToken(PgyApplication.pushToken);
													resetTimer();
													setResult(RESULT_OK);
													finish();
												}
											}else {
												//失败
												if (!object.isNull("msg")) {
													final String msg = object.getString("msg");
													if (msg != null) {
														runOnUiThread(new Runnable() {
															@Override
															public void run() {
																Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
															}
														});
													}
												}
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}
	
	/**
	 * 重置计时器
	 */
	private void resetTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		seconds = 60;
		tvSend.setText("获取验证码");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		resetTimer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvSend:
			if (timer == null) {
				if (TextUtils.isEmpty(etUserName.getText().toString())) {
					Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
					return;
				}
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						handler.sendEmptyMessage(101);
					}
				}, 0, 1000);
				OkHttpCode();
			}
			break;
		case R.id.tvLogin:
			if (checkInfo()) {
				showDialog();
				OkhttpLogin();
			}
			break;

		default:
			break;
		}
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};

	//拒绝的权限集合
	private List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			downloadPortrait();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				downloadPortrait();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				requestPermissions(permissions, AuthorityUtil.AUTHOR_LOCATION);
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
					for (int g : grantResults) {
						if (g != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						startLocation();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(this, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!this.shouldShowRequestPermissionRationale(permission)) {
							AuthorityUtil.intentAuthorSetting(this, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(PHOTO).build(), new Callback() {
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
	
}
