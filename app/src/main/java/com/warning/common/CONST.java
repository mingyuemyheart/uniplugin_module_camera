package com.warning.common;

import android.os.Environment;

import com.warning.R;

public class CONST {

	public static final String USERNAME = "12379";
	public static final String PASSWORD = "12379";
	public static final String APPID = "23";
	public static final String APP_UID = "2521";
	public static final String imageSuffix = ".png";//图标后缀名

	//intent传值的标示
	public static final String WEB_URL = "web_Url";//网页地址的标示
	public static final String COLUMN_ID = "column_id";//栏目id
	public static final String ACTIVITY_NAME = "activity_name";//界面名称
	public static final String IMG_URL = "img_url";//图片地址
	public static final String DATA_TIME = "data_time";//数据时间

	//预警颜色对应规则
	public static String[] blue = {"01", "_blue"};
	public static String[] yellow = {"02", "_yellow"};
	public static String[] orange = {"03", "_orange"};
	public static String[] red = {"04", "_red"};
	public static String[] unknown = {"05", ""};//未知预警
	
	//广播
	public static String REFRESH_ATTENTION_LIST = "refresh_attention_list";//刷新关注列表数据
	public static String BROADCAST_ADD = "broadcast_add";//添加关注城市的预警、天气信息
	public static String BROADCAST_REMOVE = "broadcast_remove";//清除已经添加的预警、天气信息
	public static String BROADCAST_DRAWER = "broadcast_drawer";//打开或关闭侧拉页面

	//下拉刷新progresBar四种颜色
	public static final int color1 = R.color.title_bg;
	public static final int color2 = R.color.title_bg;
	public static final int color3 = R.color.title_bg;
	public static final int color4 = R.color.title_bg;
	

	//通用
	public static String SDCARD_PATH = Environment.getExternalStorageDirectory()+"/12379";
	public static String PORTRAIT_ADDR = SDCARD_PATH + "/portrait.jpg";//头像保存的路径
	public static String VIDEO_ADDR = SDCARD_PATH + "/video";//拍摄视频保存的路径
	public static String OLD_VIDEO_ADDR = SDCARD_PATH + "/videofile";//拍摄视频保存的路径
	public static String THUMBNAIL_ADDR = SDCARD_PATH + "/thumbnail";//缩略图保存的路径
	public static String PICTURE_ADDR = SDCARD_PATH + "/picture";//拍照保存的路径
	public static String VIDEOTYPE = ".mp4";//mp4格式播放视频要快，比.3gp速度快很多
	public static String PICTURETYPE = ".jpg";
	public static int TIME = 120;//视频录制时间限定为120秒

}
