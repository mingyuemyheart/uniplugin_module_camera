package com.warning.activity;

/**
 * 我的
 * @author shawn_sun
 *
 */

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.common.CONST;
import com.warning.dto.NewsDto;
import com.warning.manager.DataCleanManager;
import com.warning.util.AuthorityUtil;
import com.warning.util.AutoUpdateUtil;
import com.warning.util.CommonUtil;
import com.warning.util.DialogUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private CircleImageView ivPortrait = null;
	private TextView tvUserName = null;
	private TextView tvLogout = null;
	private LinearLayout llPush = null;
	private LinearLayout llClearCache = null;
	private TextView tvCache = null;
	private LinearLayout llVersion = null;
	private TextView tvVersion = null;
	private LinearLayout llIntro = null;
	private LinearLayout llHotline1 = null;
	private TextView tvHotline1 = null;
	private LinearLayout llHotline2 = null;
	private TextView tvHotline2 = null;
	private String dialNumber = null;

	private LinearLayout llRecommend;
	private LinearLayout llUpload = null;
	private LinearLayout llCheck = null;
	private RelativeLayout reCheck = null;
	private TextView tvCheck = null;
	private LinearLayout llScore = null;
	private LinearLayout llMsg = null;
	private RelativeLayout reMsg = null;
	private TextView tvMsg = null;
	private boolean portraitOrUpload = true;//获取权限默认为获取头像，区分获取头像、获取我的上传

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("我的");
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvUserName.setOnClickListener(this);
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		llPush = (LinearLayout) findViewById(R.id.llPush);
		llPush.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(CommonUtil.getVersion(mContext));
		llIntro = (LinearLayout) findViewById(R.id.llIntro);
		llIntro.setOnClickListener(this);
		llHotline1 = (LinearLayout) findViewById(R.id.llHotline1);
		llHotline1.setOnClickListener(this);
		tvHotline1 = (TextView) findViewById(R.id.tvHotline1);
		llHotline2 = (LinearLayout) findViewById(R.id.llHotline2);
		llHotline2.setOnClickListener(this);
		tvHotline2 = (TextView) findViewById(R.id.tvHotline2);
		llUpload = (LinearLayout) findViewById(R.id.llUpload);
		llUpload.setOnClickListener(this);
		llCheck = (LinearLayout) findViewById(R.id.llCheck);
		llCheck.setOnClickListener(this);
		reCheck = (RelativeLayout) findViewById(R.id.reCheck);
		tvCheck = (TextView) findViewById(R.id.tvCheck);
		llScore = (LinearLayout) findViewById(R.id.llScore);
		llScore.setOnClickListener(this);
		llMsg = (LinearLayout) findViewById(R.id.llMsg);
		llMsg.setOnClickListener(this);
		reMsg = (RelativeLayout) findViewById(R.id.reMsg);
		tvMsg = (TextView) findViewById(R.id.tvMsg);
		llRecommend = findViewById(R.id.llRecommend);
		llRecommend.setOnClickListener(this);
		
		refreshUserInfo();
		
		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 显示用户信息
	 */
	private void refreshUserInfo() {
		if (!TextUtils.isEmpty(TOKEN)) {//已登录状态
			llUpload.setVisibility(View.VISIBLE);
			tvLogout.setVisibility(View.VISIBLE);
			if (TextUtils.equals(ISCHEKER, "1")) {//审核员
				llCheck.setVisibility(View.VISIBLE);
				OkhttpList("http://new.12379.tianqi.cn/Work/examine");
			}else {
				llCheck.setVisibility(View.GONE);
			}
			llScore.setVisibility(View.VISIBLE);
			llMsg.setVisibility(View.VISIBLE);
			OkhttpMsg("http://new.12379.tianqi.cn/Work/getnewuserMes");
			
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

			portraitOrUpload = true;
			checkStorageAuthority();
		}else {//未登录状态
			llUpload.setVisibility(View.GONE);
			tvLogout.setVisibility(View.GONE);
			llCheck.setVisibility(View.GONE);
			llScore.setVisibility(View.GONE);
			llMsg.setVisibility(View.GONE);
			tvUserName.setText("点击登录");
			ivPortrait.setImageResource(R.drawable.iv_portrait);
		}

	}

	/**
	 * 刷新头像
	 */
	private void refreshPortrait() {
		Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
		if (bitmap != null) {
			ivPortrait.setImageBitmap(bitmap);
		}else {
			ivPortrait.setImageResource(R.drawable.iv_portrait);
		}
	}

	/**
	 * 申请存储权限
	 */
	private void checkStorageAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			if (portraitOrUpload) {
				refreshPortrait();
			}else {
				startActivity(new Intent(mContext, MyUploadActivity.class));
			}
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(MyActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AuthorityUtil.AUTHOR_STORAGE);
			}else {
				if (portraitOrUpload) {
					refreshPortrait();
				}else {
					startActivity(new Intent(mContext, MyUploadActivity.class));
				}
			}
		}
	}

	/**
	 * 申请电话权限
	 */
	private void checkPhoneAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			try {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(MyActivity.this, new String[]{Manifest.permission.CALL_PHONE}, AuthorityUtil.AUTHOR_PHONE);
			}else {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_STORAGE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					if (portraitOrUpload) {
						refreshPortrait();
					}else {
						startActivity(new Intent(mContext, MyUploadActivity.class));
					}
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(MyActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
					}
				}
				break;
			case AuthorityUtil.AUTHOR_PHONE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(MyActivity.this, Manifest.permission.CALL_PHONE)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限，是否前往设置？");
					}
				}
				break;
		}
	}

	/**
	 * 获取审核列表
	 */
	private void OkhttpList(final String url) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(TOKEN)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("p", "1");
				builder.add("size", "1");
				builder.add("uid", UID);
				builder.add("token", TOKEN);
				builder.add("areas", AREAS);
				builder.add("status", "1");
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
													if (!object.isNull("count")) {
														final String count = object.getString("count");
														if (!TextUtils.isEmpty(count)) {
															int c = Integer.valueOf(count);
															if (c > 0) {
																reCheck.setVisibility(View.VISIBLE);
																tvCheck.setText(count);
															}else {
																reCheck.setVisibility(View.GONE);
																tvCheck.setText("");
															}
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

	/**
	 * 获取我的消息信息
	 * @param url
	 */
	private void OkhttpMsg(final String url) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(TOKEN)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("uid", UID);
				builder.add("token", TOKEN);
				builder.add("p", "1");
				builder.add("size", "1");
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("unread")) {
											final String unread = obj.getString("unread");
											if (!TextUtils.isEmpty(unread)) {
												int c = Integer.valueOf(unread);
												if (c > 0) {
													reMsg.setVisibility(View.VISIBLE);
													tvMsg.setText(unread);
												}else {
													reMsg.setVisibility(View.GONE);
													tvMsg.setText("");
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
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(final boolean flag, String message, String content, final TextView textView) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (flag) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							textView.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					DataCleanManager.clearLocalSave(mContext);
					try {
						String data = DataCleanManager.getLocalSaveSize(mContext);
						if (data != null) {
							textView.setText(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}
	
	private void dialPhone(String message, final String content, String positive) {
		dialNumber = content;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		TextView tvPositive = (TextView) view.findViewById(R.id.tvPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvPositive.setText(positive);
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				checkPhoneAuthority();
			}
		});
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void logout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				clearUserInfo();
				File file = new File(CONST.PORTRAIT_ADDR);
				if (file.exists()) {
					file.delete();
				}
				refreshUserInfo();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivPortrait:
		case R.id.tvUserName:
			if (TextUtils.isEmpty(TOKEN)) {
				startActivityForResult(new Intent(mContext, LoginActivity.class), 1);
			}else {
				startActivityForResult(new Intent(mContext, PersonInfoActivity.class), 2);
			}
			break;
		case R.id.tvLogout:
			logout("退出登录", "确定要退出登录？");
			break;
		case R.id.llUpload:
			portraitOrUpload = false;
			checkStorageAuthority();
			break;
		case R.id.llCheck:
			startActivityForResult(new Intent(mContext, CheckWorksActivity.class), 3);
			break;
		case R.id.llScore:
			startActivity(new Intent(mContext, MyScoreActivity.class));
			break;
		case R.id.llMsg:
			startActivityForResult(new Intent(mContext, MyMsgActivity.class), 4);
			break;
		case R.id.llPush:
			startActivity(new Intent(mContext, PushActivity.class));
			break;
		case R.id.llClearCache:
			deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llVersion:
			AutoUpdateUtil.checkUpdate(MyActivity.this, mContext, "44", getString(R.string.app_name), false);
			break;
		case R.id.llIntro:
			startActivity(new Intent(mContext, IntroduceActivity.class));
			break;
		case R.id.llRecommend:
			Intent intent = new Intent(mContext, WebviewActivity.class);
			NewsDto dto = new NewsDto();
			dto.title = getString(R.string.setting_recmmend);
			dto.url = "http://decision-admin.tianqi.cn/Public/share/12379.html";
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", dto);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.llHotline1:
			dialPhone(getString(R.string.setting_hotline1), tvHotline1.getText().toString(), getString(R.string.dial));
			break;
		case R.id.llHotline2:
			dialPhone(getString(R.string.setting_hotline2), tvHotline2.getText().toString(), getString(R.string.dial));
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
			case 2:
				refreshUserInfo();
				DialogUtil.welcomeDialog(mContext);
				break;

			case 3:
				OkhttpList("http://new.12379.tianqi.cn/Work/examine");
				break;
			case 4:
				OkhttpMsg("http://new.12379.tianqi.cn/Work/getnewuserMes");
				break;

			default:
				break;
			}
		}
	}
	
}
