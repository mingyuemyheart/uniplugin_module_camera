package com.camera.common;

import android.os.Environment;

import com.camera.R;

public class CONST {

	//通用
	public static String SDCARD_PATH = Environment.getExternalStorageDirectory()+"/guansafety";
	public static String VIDEO_ADDR = SDCARD_PATH + "/video";//拍摄视频保存的路径
	public static String THUMBNAIL_ADDR = SDCARD_PATH + "/thumbnail";//缩略图保存的路径
	public static String PICTURE_ADDR = SDCARD_PATH + "/picture";//拍照保存的路径
	public static String VIDEOTYPE = ".mp4";//mp4格式播放视频要快，比.3gp速度快很多
	public static String PICTURETYPE = ".jpg";
	public static int TIME = 120;//视频录制时间限定为120秒

}
