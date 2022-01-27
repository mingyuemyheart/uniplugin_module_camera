package com.camera.util;

import android.Manifest;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态获取权限
 */
public class AuthorityUtil {

    public static final int AUTHOR_MULTI = 1000;//多个权限一起申请
    public static final int AUTHOR_LOCATION = 1001;//定位权限
    public static final int AUTHOR_STORAGE = 1002;//存储权限
    public static final int AUTHOR_PHONE = 1003;//电话权限
    public static final int AUTHOR_CAMERA = 1004;//相机权限
//    public static final int AUTHOR_CONTACTS = 1005;//通讯录权限
//    public static final int AUTHOR_MICROPHONE = 1006;//麦克风权限
//    public static final int AUTHOR_SMS = 1007;//短信权限
//    public static final int AUTHOR_CALENDAR = 1008;//日历权限
//    public static final int AUTHOR_SENSORS = 1009;//传感器权限

    //需要申请的所有权限
    public static String[] allPermissions = new String[] {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //拒绝的权限集合
    public static List<String> deniedList = new ArrayList<>();

}
