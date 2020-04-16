package com.warning.common;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.common.inter.ITagManager;
import com.umeng.message.entity.UMessage;
import com.umeng.message.tag.TagManager;
import com.umeng.socialize.PlatformConfig;
import com.warning.activity.CheckWorksActivity;
import com.warning.activity.WarningDetailActivity;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.mezu.MeizuRegister;
import org.android.agoo.xiaomi.MiPushRegistar;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PgyApplication extends Application{

	public static String appKey = "57eb69ffe0f55a52e9002222", msgSecret = "ced91ba345fef220d2901519a4df945c";
	private static PushAgent mPushAgent;
	public static String DEVICETOKEN = "";
	
    public static String nationTag = "000000";
    public static String proTag = "";
    public static String cityTag = "";
    public static String disTag = "";
    public static String redTag = "level-red";
    public static String orangeTag = "level-orange";
    public static String yellowTag = "level-yellow";
    public static String blueTag = "level-blue";
	public static String pushToken = "";

	private static String appTheme = "0";

	public static String getAppTheme() {
		return appTheme;
	}
	public static void setTheme(String theme) {
		appTheme = theme;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		//初始化Imageloader
		ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(getApplicationContext()); 
		ImageLoader.getInstance().init(configuration);

		initUmeng();

		closeAndroidPDialog();

	}

	/**
	 * 初始化umeng
	 */
	private void initUmeng() {
		//umeng分享
		UMConfigure.init(this, appKey, "umeng", UMConfigure.DEVICE_TYPE_PHONE, msgSecret);
		PlatformConfig.setWeixin("wxa719cea9cb32cd8e", "078ee3ffe9d11633869619f90654b863");
		PlatformConfig.setQQZone("1105801723", "ug9Dw1bhK5SySSAv");
		PlatformConfig.setSinaWeibo("2992665318", "e4c88a5c72b95abc3661b11c38ff64a0", "http://sns.whalecloud.com/sina2/callback");
		UMConfigure.setLogEnabled(true);

		registerUmengPush();

		//华为推送
		HuaWeiRegister.register(this);

		//小米推送
		MiPushRegistar.register(this, "2882303761517530819", "5981753028819");

		//魅族推送
		MeizuRegister.register(this, "112611", "4bdce467a15e4ba4a34dc0e6db7ce817");
	}

	/**
	 * 注册umeng推送
	 */
	private void registerUmengPush() {
		mPushAgent = PushAgent.getInstance(this);

		//参数number可以设置为0~10之间任意整数。当参数为0时，表示不合并通知
		mPushAgent.setDisplayNotificationNumber(0);

//        //sdk开启通知声音
//        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
//        // sdk关闭通知声音
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//        // 通知声音由服务端控制
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);
//		mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//		mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//
//        //此处是完全自定义处理设置
//        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);

		//注册推送服务 每次调用register都会回调该接口
		mPushAgent.register(new IUmengRegisterCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				Log.e("deviceToken", deviceToken);
				DEVICETOKEN = deviceToken;
			}

			@Override
			public void onFailure(String s, String s1) {
			}
		});

		/**
		 * 自定义行为的回调处理
		 * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
		 * */
		mPushAgent.setNotificationClickHandler(new UmengNotificationClickHandler() {
			@Override
			public void dealWithCustomAction(Context context, UMessage msg) {
				super.dealWithCustomAction(context, msg);
				if (msg.extra != null) {
					JSONObject obj = new JSONObject(msg.extra);
					try {
						Intent intent;
						if (!obj.isNull("otherInfo")) {
							intent = new Intent(getApplicationContext(), CheckWorksActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}else if (!obj.isNull("url")) {
							String url = obj.getString("url");
							if (!TextUtils.isEmpty(url)) {
								intent = new Intent(getApplicationContext(), WarningDetailActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.putExtra("url", url);
								startActivity(intent);
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
	 * 打开推送
	 */
	public static void enablePush() {
		if (mPushAgent != null) {
			mPushAgent.enable(new IUmengCallback() {
				@Override
				public void onSuccess() {

				}

				@Override
				public void onFailure(String s, String s1) {

				}
			});
		}
	}

	/**
	 * 关闭推送
	 */
	public static void disablePush() {
		if (mPushAgent != null) {
			mPushAgent.disable(new IUmengCallback() {
				@Override
				public void onSuccess() {

				}

				@Override
				public void onFailure(String s, String s1) {

				}
			});
		}
	}

	/**
	 * 设置推送静默
	 */
	public static void setNoDisturbMode() {
		if (mPushAgent != null) {
			mPushAgent.setNoDisturbMode(22, 0, 8, 0);
		}
	}

	/**
	 * 设置推送标签
	 */
	public static void addPushTags(String tags) {
		if (mPushAgent != null) {
			mPushAgent.getTagManager().addTags(new TagManager.TCallBack() {
				@Override
				public void onMessage(boolean b, ITagManager.Result result) {
					mPushAgent.getTagManager().getTags(new TagManager.TagListCallBack() {
						@Override
						public void onMessage(boolean b, List<String> list) {
							String tags = "";
							for (int i = 0; i < list.size(); i++) {
								tags += list.get(i)+",";
							}
							Log.e("tgas", tags);
						}
					});
				}
			}, tags);
		}
	}

	/**
	 *  Android P 后谷歌限制了开发者调用非官方公开API 方法或接口
	 */
	private void closeAndroidPDialog(){
		try {
			Class aClass = Class.forName("android.content.pm.PackageParser$Package");
			Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
			declaredConstructor.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Class cls = Class.forName("android.app.ActivityThread");
			Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
			declaredMethod.setAccessible(true);
			Object activityThread = declaredMethod.invoke(null);
			Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
			mHiddenApiWarningShown.setAccessible(true);
			mHiddenApiWarningShown.setBoolean(activityThread, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
