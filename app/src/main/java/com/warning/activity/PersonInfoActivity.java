package com.warning.activity;

/**
 * 个人信息
 * @author shawn_sun
 *
 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.common.CONST;
import com.warning.util.AuthorityUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvPhone = null;
	private TextView tvScore = null;
	private LinearLayout llPortrait = null;
	private CircleImageView ivPortrait = null;
	private LinearLayout llNickName = null;
	private TextView tvNickName = null;
	private LinearLayout llMail = null;
	private TextView tvMail = null;
	private LinearLayout llUnit = null;
	private TextView tvUnit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_info);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("个人信息");
		tvScore = (TextView) findViewById(R.id.tvScore);
		tvPhone = (TextView) findViewById(R.id.tvPhone);
		llPortrait = (LinearLayout) findViewById(R.id.llPortrait);
		llPortrait.setOnClickListener(this);
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		llNickName = (LinearLayout) findViewById(R.id.llNickName);
		llNickName.setOnClickListener(this);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		llMail = (LinearLayout) findViewById(R.id.llMail);
		llMail.setOnClickListener(this);
		tvMail = (TextView) findViewById(R.id.tvMail);
		llUnit = (LinearLayout) findViewById(R.id.llUnit);
		llUnit.setOnClickListener(this);
		tvUnit = (TextView) findViewById(R.id.tvUnit);
		
		getPortrait();
		if (!TextUtils.isEmpty(NICKNAME)) {
			tvNickName.setText(NICKNAME);
		}
		if (!TextUtils.isEmpty(PHONENUMBER)) {
			tvPhone.setText(PHONENUMBER);
		}
		if (!TextUtils.isEmpty(MAIL)) {
			tvMail.setText(MAIL);
		}
		if (!TextUtils.isEmpty(POINTS)) {
			tvScore.setText(POINTS);
		}
		if (!TextUtils.isEmpty(UNIT)) {
			tvUnit.setText(UNIT);
		}
	}
	
	/**
	 * 获取相册
	 */
	private void getAlbum() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra("crop", "false");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
        startActivityForResult(intent, 0);
	}
	
	/**
	 * 上传图片
	 * @param url 接口地址
	 */
	private void OkhttpPostPotrait(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				MultipartBody.Builder builder = new MultipartBody.Builder();
				builder.setType(MultipartBody.FORM);
				builder.addFormDataPart("token", TOKEN);
				builder.addFormDataPart("uid", UID);
				builder.addFormDataPart("picture", "portait.jpg", RequestBody.create(MediaType.parse("image/*"), new File(CONST.PORTRAIT_ADDR)));
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
												int status = object.getInt("status");
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
														}

														saveUserInfo();

														getPortrait();

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
	
	/**
	 * 获取头像
	 */
	private void getPortrait() {
		Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
		if (bitmap != null) {
			ivPortrait.setImageBitmap(bitmap);
		}else {
			ivPortrait.setImageResource(R.drawable.iv_portrait);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 申请存储权限
	 */
	private void checkStorageAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			getAlbum();
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(PersonInfoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AuthorityUtil.AUTHOR_STORAGE);
			}else {
				getAlbum();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_STORAGE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					getAlbum();
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(PersonInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
					}
				}
				break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.llPortrait:
			checkStorageAuthority();
			break;
		case R.id.llNickName:
			Intent intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "昵称");
			intent.putExtra("content", NICKNAME);
			startActivityForResult(intent, 1);
			break;
		case R.id.llMail:
			intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "邮箱");
			intent.putExtra("content", MAIL);
			startActivityForResult(intent, 2);
			break;
		case R.id.llUnit:
			intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "单位名称");
			intent.putExtra("content", UNIT);
			startActivityForResult(intent, 3);
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
			case 0:
				if (data == null) {
					return;
				}

				Bitmap bitmap = null;
				Uri uri = data.getData();
				FileOutputStream fos = null;
				if (uri == null) {
					bitmap = data.getParcelableExtra("data");
				}else {
					try {
						ContentResolver resolver = getContentResolver();
						bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
				}
				
				try {
					File files = new File(CONST.SDCARD_PATH);
					if (!files.exists()) {
						files.mkdirs();
					}
					
					fos = new FileOutputStream(CONST.PORTRAIT_ADDR);
					if (bitmap != null && fos != null) {
						bitmap.compress(CompressFormat.PNG, 100, fos);
						
						if (bitmap != null && !bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
						}

						OkhttpPostPotrait("http://new.12379.tianqi.cn/Work/edituserinfo");
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				break;
			case 1:
				if (!TextUtils.isEmpty(NICKNAME)) {
					tvNickName.setText(NICKNAME);
				}
				break;
			case 2:
				if (!TextUtils.isEmpty(MAIL)) {
					tvMail.setText(MAIL);
				}
				break;
			case 3:
				if (!TextUtils.isEmpty(UNIT)) {
					tvUnit.setText(UNIT);
				}
				break;

			default:
				break;
			}
		}
	}
	
}
