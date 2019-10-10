package com.warning.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.warning.view.MyDialog2;

public class BaseActivity extends Activity {
	
	private Context mContext = null;
	private MyDialog2 mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		getUserInfo();
	}
	
	/**
	 * 初始化dialog
	 */
	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog2(mContext);
		}
		mDialog.show();
	}
	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static String PHONENUMBER = null;
	public static String TOKEN = null;//token
	public static String ISINFOER = "0";//是否信息员 1 是 0 不是
	public static String ISCHEKER = "0";//是否审核员 1是 0不是
	public static String UID = null;
	public static String POINTS = null;//积分
	public static String PHOTO = null;//头像地址
	public static String NICKNAME = null;//昵称
	public static String REALNAME = null;//真实姓名
	public static String AREAS = null;//审核权限范围
	public static String MAIL = null;//邮箱
	public static String UNIT = null;//单位
	public static class UserInfo {
		public static final String phonenumber = "phonenumber";
		public static final String token = "token";
		public static final String isinfoer = "isinfoer";
		public static final String ischeker = "ischeker";
		public static final String uid = "uid";
		public static final String points = "points";
		public static final String photo = "photo";
		public static final String nickName = "nickName";
		public static final String realName = "realName";
		public static final String areas = "areas";
		public static final String mail = "mail";
		public static final String unit = "unit";
	}

	/**
	 * 保存用户信息
	 */
	public void saveUserInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.token, TOKEN);
		editor.putString(UserInfo.uid, UID);
		editor.putString(UserInfo.phonenumber, PHONENUMBER);
		editor.putString(UserInfo.isinfoer, ISINFOER);
		editor.putString(UserInfo.ischeker, ISCHEKER);
		editor.putString(UserInfo.nickName, NICKNAME);
		editor.putString(UserInfo.realName, REALNAME);
		editor.putString(UserInfo.points, POINTS);
		editor.putString(UserInfo.photo, PHOTO);
		editor.putString(UserInfo.areas, AREAS);
		editor.putString(UserInfo.mail, MAIL);
		editor.putString(UserInfo.unit, UNIT);
		editor.apply();
	}

	/**
	 * 获取用户信息
	 */
	private void getUserInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		TOKEN = sharedPreferences.getString(UserInfo.token, null);
		UID = sharedPreferences.getString(UserInfo.uid, null);
		PHONENUMBER = sharedPreferences.getString(UserInfo.phonenumber, null);
		ISINFOER = sharedPreferences.getString(UserInfo.isinfoer, null);
		ISCHEKER = sharedPreferences.getString(UserInfo.ischeker, null);
		NICKNAME = sharedPreferences.getString(UserInfo.nickName, null);
		REALNAME = sharedPreferences.getString(UserInfo.realName, null);
		POINTS = sharedPreferences.getString(UserInfo.points, null);
		PHOTO = sharedPreferences.getString(UserInfo.photo, null);
		MAIL = sharedPreferences.getString(UserInfo.mail, null);
		UNIT = sharedPreferences.getString(UserInfo.unit, null);
	}

	/**
	 * 清除用户信息
	 */
	public void clearUserInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		TOKEN = null;
		UID = null;
		PHONENUMBER = null;
		ISINFOER = null;
		ISCHEKER = null;
		NICKNAME = null;
		REALNAME = null;
		POINTS = null;
		PHOTO = null;
		AREAS = null;
		MAIL = null;
		UNIT = null;
	}
	
}
