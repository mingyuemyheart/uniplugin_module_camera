package com.warning.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.warning.common.CONST;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.warning.activity.BaseActivity.TOKEN;
import static com.warning.activity.BaseActivity.UID;

/**
 * 数据统计
 * @author shawn_sun
 *
 */

public class StatisticUtil {

	/**
	 * 统计安装次数
	 */
	public static void asyncQueryInstall(Context context) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
		SharedPreferences sp = context.getSharedPreferences("VERSION", Context.MODE_PRIVATE);
		String version = sp.getString("version", "");
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("version", CommonUtil.getVersion(context));
		editor.apply();
		if (!TextUtils.equals(version, CommonUtil.getVersion(context))) {
			final String url = "http://new.12379.tianqi.cn/Api/installCount?addtime="+sdf1.format(new Date())+"&appid="+CONST.APPID +
					"&mobile_type="+android.os.Build.MODEL.replace(" ", "")+"&newver="+CommonUtil.getVersion(context)+"&oldver="+version+"&os_version="+android.os.Build.VERSION.RELEASE+"&platform_type=android";
			OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
				}
				@Override
				public void onResponse(Call call, Response response) throws IOException {
				}
			});
		}
	}

	/**
	 * 统计登陆次数
	 */
	public static void asyncQueryLogin(final String url, final Context context) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", CONST.USERNAME);
		builder.add("password", CONST.PASSWORD);
		builder.add("appid", CONST.APPID);
		builder.add("device_id", "");
		builder.add("platform", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(context));
		builder.add("mobile_type", android.os.Build.MODEL);
		builder.add("address", "");
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
			}
		});
	}

	/**
	 * 提交点击次数
	 */
	public static void submitClickCount(String columnId, String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
		String addtime = sdf.format(new Date());
		final String url = "http://new.12379.tianqi.cn/Api/clickCount?addtime="+addtime+"&appid="+CONST.APPID+
				"&eventid=menuClick_"+columnId+"&eventname="+name+"&userid="+CONST.APP_UID+"&username="+CONST.USERNAME;
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
			}
		});
	}

	/**
	 * 上传播放或浏览次数
	 */
	public static void asyncQueryCount(final String url, final String workId) {
		if (TextUtils.isEmpty(workId) || TextUtils.isEmpty(UID)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("uid", UID);
				builder.add("wid", workId);
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}

	/**
	 * 统计分享次数
	 */
	public static void OkhttpShare(final String url, final String videoId, final String userId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				if (TOKEN != null && UID != null) {
					builder.add("token", TOKEN);
					builder.add("uid", UID);
				}
				builder.add("wid", videoId);
				builder.add("author", userId);
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}

	/**
	 * 设置审核的pushToken
	 * @param pushToken
	 */
	public static void OkhttpPushToken(final String pushToken) {
		if (TextUtils.isEmpty(pushToken)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://new.12379.tianqi.cn/Extra/savePushToken";
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("uid", UID);
				builder.add("token", TOKEN);
				builder.add("pushtoken", pushToken);
				builder.add("platform", "Android");
				RequestBody body = builder.build();
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}
	
}
