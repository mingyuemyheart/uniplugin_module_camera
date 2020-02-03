package com.warning.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.warning.R;
import com.warning.activity.MyActivity;
import com.warning.activity.WarningDetailActivity;
import com.warning.activity.ShawnWarningListActivity;
import com.warning.activity.WebviewActivity;
import com.warning.activity.ZixunDetailActivity;
import com.warning.common.CONST;
import com.warning.common.PgyApplication;
import com.warning.dto.NewsDto;
import com.warning.dto.WarningDto;
import com.warning.dto.WeatherDto;
import com.warning.manager.DBManager;
import com.warning.util.AuthorityUtil;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;
import com.warning.util.WeatherUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 首页
 */
public class Fragment1 extends Fragment implements OnClickListener {

    private TextView tvNews1,tvPublishTime2,tvPosition,tvWarningName,tvDis,tvCity,tvPro,tvNation,tvWarningTime,tvWarningIntro,tvInfo;
    private TextSwitcher tvNews;
    private List<NewsDto> newsList = new ArrayList<>();
    private RollingThread rollingThread;
    private RelativeLayout reMain,reFact,reWarning;
    private ImageView ivWarning = null;
    private List<WarningDto> warningList = new ArrayList<>();
    private List<WarningDto> disList = new ArrayList<>();//定位城市预警信息列表
    private List<WarningDto> cityList = new ArrayList<>();//定位城市预警信息列表
    private List<WarningDto> proList = new ArrayList<>();//定位城市预警信息列表
    private List<WarningDto> nationList = new ArrayList<>();//定位城市预警信息列表
    private ProgressBar progressBar;
    private MyBroadCastReceiver mReceiver;
    private LinearLayout llContainer;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SwipeRefreshLayout refreshLayout;//下拉刷新布局
    private int height = 0;
    private ScrollView parentScrollView,childScrollView;
    private String locationId;//定位城市id

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shawn_fragment1, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initBroadcast();
        initRefreshLayout(view);
        initWidget(view);
    }

    private void initBroadcast() {
        mReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONST.BROADCAST_ADD);
        intentFilter.addAction(CONST.BROADCAST_REMOVE);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (TextUtils.equals(arg1.getAction(), CONST.BROADCAST_ADD)) {//添加关注城市预警信息
                String cityId = arg1.getStringExtra("cityId");//城市id
                String cityName = arg1.getStringExtra("cityName");//城市名称
                String warningId = arg1.getStringExtra("warningId");//预警id
                queryCityInfo(cityId, cityName, warningId);
            } else if (TextUtils.equals(arg1.getAction(), CONST.BROADCAST_REMOVE)) {//清除动态添加的预警、天气信息
                int index = arg1.getIntExtra("index", -1);
                for (int i = 0; i < llContainer.getChildCount(); i++) {
                    if (i == index) {
                        llContainer.removeViewAt(i);
                    }
                }
            }
        }
    }

    private void queryCityInfo(String cityId, String cityName, String warningId) {
        //获取预警信息
        if (TextUtils.isEmpty(warningId)) {
            warningId = queryWarningIdByCityId(cityId);
        }
        if (!TextUtils.isEmpty(warningId)) {
            getWeatherInfo(cityId, cityName, warningId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    /**
     * 初始化下拉刷新布局
     */
    private void initRefreshLayout(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
        refreshLayout.setProgressViewEndTarget(true, 400);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void initWidget(View view) {
        ImageView ivMenu = view.findViewById(R.id.ivMenu);
        ivMenu.setOnClickListener(this);
        ImageView ivMy = view.findViewById(R.id.ivMy);
        ivMy.setOnClickListener(this);
        tvNews1 = view.findViewById(R.id.tvNews1);
        tvNews = view.findViewById(R.id.tvNews);
        tvPublishTime2 = view.findViewById(R.id.tvPublishTime2);
        tvPosition = view.findViewById(R.id.tvPosition);
        tvDis = view.findViewById(R.id.tvDis);
        tvDis.setOnClickListener(this);
        tvCity = view.findViewById(R.id.tvCity);
        tvCity.setOnClickListener(this);
        tvPro = view.findViewById(R.id.tvPro);
        tvPro.setOnClickListener(this);
        tvNation = view.findViewById(R.id.tvNation);
        tvNation.setOnClickListener(this);
        tvWarningName = view.findViewById(R.id.tvWarningName);
        ivWarning = view.findViewById(R.id.ivWarning);
        tvWarningTime = view.findViewById(R.id.tvWarningTime);
        tvWarningIntro = view.findViewById(R.id.tvWarningIntro);
        reFact = view.findViewById(R.id.reFact);
        reWarning = view.findViewById(R.id.reWarning);
        progressBar = view.findViewById(R.id.progressBar);
        llContainer = view.findViewById(R.id.llContainer);
        reMain = view.findViewById(R.id.reMain);
        tvInfo = view.findViewById(R.id.tvInfo);
        tvInfo.setOnClickListener(this);
        parentScrollView = view.findViewById(R.id.parentScrollView);
        childScrollView = view.findViewById(R.id.childScrollView);
        parentScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                refreshLayout.setEnabled(true);
                return false;
            }
        });
        childScrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                childScrollView.requestDisallowInterceptTouchEvent(true);
                parentScrollView.getParent().requestDisallowInterceptTouchEvent(false);
                refreshLayout.setEnabled(false);
                return false;
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;

        refresh();
    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        llContainer.removeAllViews();

        removeThread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkhttpWarning();
                OkhttpZixun();
                checkAuthority();
            }
        }).start();
    }

    /**
     * 获取预警信息
     */
    private void OkhttpWarning() {
//        final String url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=1";
        final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns";
        OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                String result = response.body().string();
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject object = new JSONObject(result);
                        if (!object.isNull("data")) {
                            warningList.clear();
                            JSONArray jsonArray = object.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONArray tempArray = jsonArray.getJSONArray(i);
                                WarningDto dto = new WarningDto();
                                dto.html = tempArray.optString(1);
                                Log.e("html", dto.html);
                                String[] array = dto.html.split("-");
                                String item0 = array[0];
                                String item1 = array[1];
                                String item2 = array[2];
                                dto.item0 = item0;
                                dto.provinceId = item0.substring(0, 2);
                                dto.type = item2.substring(0, 5);
                                dto.color = item2.substring(5, 7);
                                dto.time = item1;
                                dto.lng = tempArray.optString(2);
                                dto.lat = tempArray.optString(3);
                                dto.name = tempArray.optString(0);
                                if (!dto.name.contains("解除")) {
                                    warningList.add(dto);
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

    /**
     * 获取天气咨询
     */
    private void OkhttpZixun() {
        final String url = "http://new.12379.tianqi.cn/Extra/get_tfsj_1";
        OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                final String result = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                newsList.clear();
                                JSONArray array = new JSONArray(result);
                                String time = null;
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    NewsDto newsDto = new NewsDto();
                                    if (!obj.isNull("title")) {
                                        newsDto.title = obj.getString("title");
                                    }
                                    if (!obj.isNull("time")) {
                                        newsDto.time = obj.getString("time");
                                    }
                                    if (!obj.isNull("content")) {
                                        newsDto.content = obj.getString("content");
                                    }
                                    if (!obj.isNull("url")) {
                                        newsDto.url = obj.getString("url");
                                    }
                                    if (!obj.isNull("img")) {
                                        newsDto.imgUrl = obj.getString("img");
                                    }
                                    if (!obj.isNull("show_type")) {
                                        newsDto.show_type = obj.getString("show_type");
                                    }

                                    try {
                                        if (i == 0) {
                                            time = sdf4.format(sdf5.parse(newsDto.time));
                                        }
                                        if (TextUtils.equals(time, sdf4.format(sdf5.parse(newsDto.time)))) {
                                            newsList.add(newsDto);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                tvNews.removeAllViews();
                                tvNews.setFactory(new ViewFactory() {
                                    @Override
                                    public View makeView() {
                                        TextView textView = new TextView(getActivity());
                                        textView.setSingleLine();
                                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                                        textView.setTextColor(Color.WHITE);
                                        textView.setEllipsize(TextUtils.TruncateAt.END);
                                        return textView;
                                    }
                                });
                                if (newsList.size() >= 2) {
                                    tvNews.setVisibility(View.VISIBLE);
                                    tvNews1.setVisibility(View.GONE);

                                    removeThread();
                                    rollingThread = new RollingThread();
                                    rollingThread.start();
                                } else if (newsList.size() == 1) {
                                    tvNews.setVisibility(View.GONE);
                                    tvNews1.setText(newsList.get(0).title);
                                    tvNews1.setVisibility(View.VISIBLE);
                                    tvNews1.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View arg0) {
                                            Intent intent;
                                            if (TextUtils.equals(newsList.get(0).show_type, "web")) {
                                                intent = new Intent(getActivity(), WebviewActivity.class);
                                            } else {
                                                intent = new Intent(getActivity(), ZixunDetailActivity.class);
                                            }
                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("data", newsList.get(0));
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        }
                                    });
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

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int index = msg.arg1;
            tvNews.setText(newsList.get(index).title);
            tvNews.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent;
                    if (TextUtils.equals(newsList.get(index).show_type, "web")) {
                        intent = new Intent(getActivity(), WebviewActivity.class);
                    } else {
                        intent = new Intent(getActivity(), ZixunDetailActivity.class);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", newsList.get(index));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    };

    private void removeThread() {
        if (rollingThread != null) {
            rollingThread.cancel();
            rollingThread = null;
        }
    }

    private class RollingThread extends Thread {
        static final int STATE_PLAYING = 1;
        static final int STATE_PAUSE = 2;
        static final int STATE_CANCEL = 3;
        private int state;
        private int index;
        private boolean isTracking = false;

        @Override
        public void run() {
            super.run();
            this.state = STATE_PLAYING;
            while (index < newsList.size()) {
                if (state == STATE_CANCEL) {
                    break;
                }
                if (state == STATE_PAUSE) {
                    continue;
                }
                if (isTracking) {
                    continue;
                }
                try {
                    Message msg = handler.obtainMessage();
                    msg.arg1 = index;
                    handler.sendMessage(msg);
                    sleep(4000);
                    index++;
                    if (index >= newsList.size()) {
                        index = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            this.state = STATE_CANCEL;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeThread();
    }

    /**
     * 获取疫情
     */
    private void okHttpInfo(final String pro, final String city) {
        final String url = String.format("http://warn-wx.tianqi.cn/Test/getwhqydata?pro=%s&city=%s", pro, city);
        OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                final String result = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                String proCount = "";
                                if (!obj.isNull("total_pro")) {
                                    JSONObject proObj = obj.getJSONObject("total_pro");
                                    if (!proObj.isNull("confirm")) {
                                        proCount = proObj.getString("confirm");
                                    }
                                }
                                String cityCount = "";
                                if (!obj.isNull("total")) {
                                    JSONObject cityObj = obj.getJSONObject("total");
                                    if (!cityObj.isNull("confirm")) {
                                        cityCount = cityObj.getString("confirm");
                                    }
                                }
                                tvInfo.setText(String.format("疫情信息提示：\n%s确诊%s例，\n%s确诊%s例。", city, cityCount, pro, proCount));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (CommonUtil.isLocationOpen(getActivity())) {
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
            AMapLocationClient mLocationClient = new AMapLocationClient(getActivity());//初始化定位
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
            mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
            mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
            mLocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                        tvPosition.setText(aMapLocation.getDistrict());
                        String pro = aMapLocation.getProvince();
                        if (pro.startsWith("北京") || pro.startsWith("天津") || pro.startsWith("上海") || pro.startsWith("重庆")) {
                            okHttpInfo(aMapLocation.getCity(), aMapLocation.getDistrict());
                        } else {
                            okHttpInfo(aMapLocation.getProvince(), aMapLocation.getCity());
                        }
                        getCityId(aMapLocation.getLongitude(), aMapLocation.getLatitude());
                    }
                }
            });
            mLocationClient.startLocation();//启动定位
        }else {
            locationDialog(getActivity());
        }
    }

    private void locationDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_dialog_location, null);
        LinearLayout llNegative = view.findViewById(R.id.llNegative);
        LinearLayout llPositive = view.findViewById(R.id.llPositive);

        final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

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
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1001);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_CANCELED) {
            switch (requestCode) {
                case 1001:
                    startLocation();
                    break;
            }
        }
    }

    /**
     * 获取cityId
     *
     * @param lng
     * @param lat
     */
    private void getCityId(final double lng, final double lat) {
        WeatherAPI.getGeo(getActivity(), String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler() {
            @Override
            public void onComplete(JSONObject content) {
                super.onComplete(content);
                if (!content.isNull("geo")) {
                    try {
                        JSONObject geoObj = content.getJSONObject("geo");
                        if (!geoObj.isNull("id")) {
                            String cityId = geoObj.getString("id");
                            if (!TextUtils.isEmpty(cityId)) {
                                locationId = cityId;
                                SharedPreferences sharedID = getActivity().getSharedPreferences("LOCATIONID", Context.MODE_PRIVATE);
                                Editor editor = sharedID.edit();
                                editor.putString("locationId", locationId);
                                editor.apply();

                                String warningId = queryWarningIdByCityId(cityId);
                                if (!TextUtils.isEmpty(warningId)) {
                                    setPushTags(warningId);
                                    //获取定位城市id和关注列表城市id，存放在cityidList里
                                    List<WarningDto> leftList = new ArrayList<>();
                                    WarningDto dto = new WarningDto();
                                    dto.cityId = cityId;
                                    dto.cityName = tvPosition.getText().toString();
                                    dto.warningId = warningId;
                                    leftList.add(dto);
                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CITYIDS", Context.MODE_PRIVATE);
                                    String cityInfo = sharedPreferences.getString("cityInfo", null);
                                    if (!TextUtils.isEmpty(cityInfo)) {//如果本次保存的关注列表不为空
                                        String[] info = cityInfo.split(";");
                                        for (int m = 0; m < info.length; m++) {
                                            String[] ids = info[m].split(",");
                                            dto = new WarningDto();
                                            dto.cityId = ids[0];
                                            dto.cityName = ids[1];
                                            dto.warningId = ids[2];
                                            leftList.add(dto);
                                        }
                                    }
                                    getWeathersInfo(leftList);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable error, String content) {
                super.onError(error, content);
                SharedPreferences sharedID = getActivity().getSharedPreferences("LOCATIONID", Context.MODE_PRIVATE);
                locationId = sharedID.getString("locationId", null);

                String warningId = queryWarningIdByCityId(locationId);
                if (!TextUtils.isEmpty(warningId)) {
                    setPushTags(warningId);
                    //获取定位城市id和关注列表城市id，存放在cityidList里
                    List<WarningDto> leftList = new ArrayList<>();
                    WarningDto dto = new WarningDto();
                    dto.cityId = locationId;
                    dto.cityName = tvPosition.getText().toString();
                    dto.warningId = warningId;
                    leftList.add(dto);
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CITYIDS", Context.MODE_PRIVATE);
                    String cityInfo = sharedPreferences.getString("cityInfo", null);
                    if (!TextUtils.isEmpty(cityInfo)) {//如果本次保存的关注列表不为空
                        String[] info = cityInfo.split(";");
                        for (int m = 0; m < info.length; m++) {
                            String[] ids = info[m].split(",");
                            dto = new WarningDto();
                            dto.cityId = ids[0];
                            dto.cityName = ids[1];
                            dto.warningId = ids[2];
                            leftList.add(dto);
                        }
                    }
                    getWeathersInfo(leftList);
                }
            }
        });
    }

    /**
     * 获取预警id
     */
    private String queryWarningIdByCityId(String cityId) {
        DBManager dbManager = new DBManager(getActivity());
        dbManager.openDateBase();
        dbManager.closeDatabase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"", null);
        String warningId = null;
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            warningId = cursor.getString(cursor.getColumnIndex("wid"));
        }
        return warningId;
    }

    /**
     * 设置umeng推送的tags
     *
     * @param warningId
     */
    private void setPushTags(String warningId) {
        PgyApplication.proTag = warningId.substring(0, 2) + "0000";
        PgyApplication.cityTag = warningId.substring(0, 4) + "00";
        PgyApplication.disTag = warningId;

        if (getActivity() == null) {
            return;
        }
        SharedPreferences checkSp = getActivity().getSharedPreferences("CHECKBOX", Context.MODE_PRIVATE);
        boolean state1 = checkSp.getBoolean("state1", true);
        boolean state2 = checkSp.getBoolean("state2", true);
        boolean state3 = checkSp.getBoolean("state3", true);
        boolean state4 = checkSp.getBoolean("state4", true);
        boolean state5 = checkSp.getBoolean("state5", true);
        boolean state6 = checkSp.getBoolean("state6", true);
        boolean state7 = checkSp.getBoolean("state7", true);
        boolean state8 = checkSp.getBoolean("state8", true);
        String tag1 = "", tag2 = "", tag3 = "", tag4 = "", tag5 = "", tag6 = "", tag7 = "", tag8 = "";
        if (state1) {
            tag1 = PgyApplication.nationTag + ",";
        } else {
            tag1 = "";
        }
        if (state2) {
            tag2 = PgyApplication.proTag + ",";
        } else {
            tag2 = "";
        }
        if (state3) {
            tag3 = PgyApplication.cityTag + ",";
        } else {
            tag3 = "";
        }
        if (state4) {
            tag4 = PgyApplication.disTag + ",";
        } else {
            tag4 = "";
        }
        if (state5) {
            tag5 = PgyApplication.redTag + ",";
        } else {
            tag5 = "";
        }
        if (state6) {
            tag6 = PgyApplication.orangeTag + ",";
        } else {
            tag6 = "";
        }
        if (state7) {
            tag7 = PgyApplication.yellowTag + ",";
        } else {
            tag7 = "";
        }
        if (state8) {
            tag8 = PgyApplication.blueTag + ",";
        } else {
            tag8 = "";
        }

        SharedPreferences sp = getActivity().getSharedPreferences("TAGS", Context.MODE_PRIVATE);
        String tempTags = sp.getString("tags", "");
//		String tags = tag1+tag2+tag3+tag4+tag5+tag6+tag7+tag8+"test_android_shawn,";
        String tags = tag1 + tag2 + tag3 + tag4 + tag5 + tag6 + tag7 + tag8;
        if (!TextUtils.isEmpty(tags) && TextUtils.equals(tags.substring(tags.length() - 1, tags.length()), ",")) {
            tags = tags.substring(0, tags.length() - 1);
            if (TextUtils.equals(tags, tempTags)) {
                return;
            }

            PgyApplication.addPushTags(tags);

            Editor editor = sp.edit();
            editor.putString("tags", tags);
            editor.apply();
        }
    }

    /**
     * 获取预警信息和天气信息
     */
    private void getWeathersInfo(final List<WarningDto> leftList) {
        if (leftList.size() > 0) {
            final List<String> cityidList = new ArrayList<>();
            cityidList.clear();
            for (int i = 0; i < leftList.size(); i++) {
                cityidList.add(leftList.get(i).cityId);
            }
            WeatherAPI.getWeathers2(getActivity(), cityidList, Language.ZH_CN, new AsyncResponseHandler() {
                @Override
                public void onComplete(final List<Weather> contentList) {
                    super.onComplete(contentList);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                            for (int i = 0; i < contentList.size(); i++) {
                                Weather content = contentList.get(i);
                                if (content != null) {
                                    String cityId = leftList.get(i).cityId;
                                    String cityName = leftList.get(i).cityName;
                                    String warningId = leftList.get(i).warningId;
                                    List<WarningDto> tempList = new ArrayList<>();
                                    tempList.clear();

                                    if (i == 0) {//定位城市
                                        disList.clear();
                                        cityList.clear();
                                        proList.clear();
                                        nationList.clear();
                                        for (int m = 0; m < warningList.size(); m++) {
                                            WarningDto dto = warningList.get(m);
                                            String[] array = dto.html.split("-");
                                            String item0 = array[0];

                                            //定位点对应区县级预警
                                            if (TextUtils.equals(item0, warningId)) {
                                                disList.add(dto);
                                                tempList.add(dto);
                                            }

                                            //市级预警
                                            if (warningId.substring(0, 2).equals("11") || warningId.substring(0, 2).equals("31")
                                                    || warningId.substring(0, 2).equals("12") || warningId.substring(0, 2).equals("50")) {
                                                if (TextUtils.equals(item0, warningId.substring(0, 2) + "0000")) {//四个直辖市对应市级预警
                                                    cityList.add(dto);
                                                    tempList.add(dto);
                                                }
                                            } else {
                                                if (TextUtils.equals(item0, warningId.substring(0, 4) + "00")) {//其他省份对应市级预警
                                                    cityList.add(dto);
                                                    tempList.add(dto);
                                                }
                                            }

                                            //除四个直辖市外的省级预警
                                            if (!warningId.startsWith("11") && !warningId.startsWith("31") && !warningId.startsWith("12") && !warningId.startsWith("50")) {
                                                if (TextUtils.equals(item0, warningId.substring(0, 2) + "0000")) {
                                                    proList.add(dto);
                                                }
                                            }

                                            //国家级预警
                                            if (TextUtils.equals(item0, "000000")) {
                                                nationList.add(dto);
                                            }
                                        }

                                        if (disList.size() == 0) {
                                            tvDis.setVisibility(View.GONE);
                                        }else {
                                            tvDis.setText("本地预警" + disList.size() + "条");
                                            tvDis.setVisibility(View.VISIBLE);
                                        }

                                        if (cityList.size() == 0) {
                                            tvCity.setVisibility(View.GONE);
                                        }else {
                                            tvCity.setText("市级预警" + cityList.size() + "条");
                                            tvCity.setVisibility(View.VISIBLE);
                                        }

                                        if (cityId.startsWith("10101") || cityId.startsWith("10102") || cityId.startsWith("10103") || cityId.startsWith("10104")
                                                || proList.size() == 0) {
                                            tvPro.setVisibility(View.GONE);
                                        } else {
                                            tvPro.setVisibility(View.VISIBLE);
                                            tvPro.setText("省级预警" + proList.size() + "条");
                                        }

                                        if (nationList.size() == 0) {
                                            tvNation.setVisibility(View.GONE);
                                        }else {
                                            tvNation.setText("国家级预警" + nationList.size() + "条");
                                            tvNation.setVisibility(View.VISIBLE);
                                        }

                                        //如果区县没有预警则显示对应市级预警
                                        if (disList.size() == 0) {
                                            for (int n = 0; n < warningList.size(); n++) {
                                                WarningDto dto = warningList.get(n);
                                                if (warningId.substring(0, 2).equals("11") || warningId.substring(0, 2).equals("31")
                                                        || warningId.substring(0, 2).equals("12") || warningId.substring(0, 2).equals("50")) {
                                                    if (TextUtils.equals(dto.item0, warningId.substring(0, 2) + "0000")) {
                                                        disList.add(dto);
                                                    }
                                                } else {
                                                    if (TextUtils.equals(dto.item0, warningId.substring(0, 4) + "00")) {
                                                        disList.add(dto);
                                                    }
                                                }
                                            }
                                        }

                                        //如果区县没有预警、市级没有预警则显示对应省级预警
                                        if (disList.size() == 0) {
                                            for (int n = 0; n < warningList.size(); n++) {
                                                WarningDto dto = warningList.get(n);
                                                if (TextUtils.equals(dto.item0, warningId.substring(0, 2) + "0000")) {
                                                    disList.add(dto);
                                                }
                                            }
                                        }

                                        if (disList.size() > 0) {//有预警
                                            int index = 0;
                                            int level = Integer.valueOf(disList.get(0).color);
                                            for (int j = 0; j < disList.size(); j++) {
                                                WarningDto dto = disList.get(j);
                                                if (level < Integer.valueOf(dto.color)) {
                                                    level = Integer.valueOf(dto.color);
                                                    index = j;
                                                }
                                            }

                                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) reWarning.getLayoutParams();
                                            int height1 = 40;//最上面标题栏高度
                                            int height2 = 55;//最下面tab标签高度
                                            int height3 = 250;//预警详情高度
                                            int height4 = 180;//天气实况高度
                                            int totalHeight = height1 + height2 + height3 + height4;
                                            params.topMargin = (int) (height - CommonUtil.dip2px(getActivity(), totalHeight));
                                            reWarning.setLayoutParams(params);
                                            OkHttpWarningDetail("https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/" + disList.get(index).html, disList.get(index).type, disList.get(index).color);

                                            addFactWeather(true, content);
                                            addForecast(true, true, cityId, cityName, content);

                                            RelativeLayout.LayoutParams paramsFact = (RelativeLayout.LayoutParams) reFact.getLayoutParams();
                                            height1 = 40;//最上面标题栏高度
                                            height2 = 55;//最下面tab标签高度
                                            height3 = 180;//天气实况高度
                                            totalHeight = height1 + height2 + height3;
                                            paramsFact.topMargin = (int) (height - CommonUtil.dip2px(getActivity(), totalHeight));
                                            reFact.setLayoutParams(paramsFact);

                                            tvPublishTime2.setVisibility(View.INVISIBLE);
                                        } else {//无预警
                                            addFactWeather(false, content);
                                            addForecast(false, true, cityId, cityName, content);

                                            RelativeLayout.LayoutParams paramsFact = (RelativeLayout.LayoutParams) reFact.getLayoutParams();
                                            int height1 = 40;//最上面标题栏高度
                                            int height2 = 55;//最下面tab标签高度
                                            int height3 = 200;//天气实况高度
                                            int height4 = 40;//三天预报标题高度
                                            int height5 = 205;//三天预报内容高度
                                            int totalHeight = height1 + height2 + height3 + height4 + height5;
                                            paramsFact.topMargin = (int) (height - CommonUtil.dip2px(getActivity(), totalHeight));
                                            reFact.setLayoutParams(paramsFact);

                                            tvPublishTime2.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    } else {//关注列表里的城市
                                        for (int m = 0; m < warningList.size(); m++) {
                                            WarningDto dto = warningList.get(m);
                                            String[] array = dto.html.split("-");
                                            String item0 = array[0];
                                            if (TextUtils.equals(item0, warningId)) {
                                                tempList.add(dto);
                                            }
                                        }

                                        if (tempList.size() > 0) {//有预警
                                            addWarning(tempList, cityName);
                                        } else {//无预警
                                            addForecast(false, false, cityId, cityName, content);
                                        }
                                    }

                                    if (tempList.size() > 0) {
                                        leftList.get(i).html = tempList.get(0).html;
                                        String[] array = leftList.get(i).html.split("-");
                                        String item0 = array[0];
                                        String item1 = array[1];
                                        String item2 = array[2];
                                        leftList.get(i).item0 = item0;
                                        leftList.get(i).provinceId = item0.substring(0, 2);
                                        leftList.get(i).type = item2.substring(0, 5);
                                        leftList.get(i).color = item2.substring(5, 7);
                                        leftList.get(i).time = item1;
                                        leftList.get(i).lng = tempList.get(0).lng;
                                        leftList.get(i).lat = tempList.get(0).lat;
                                        leftList.get(i).name = tempList.get(0).name;
                                    }

                                    JSONObject object = content.getWeatherFactInfo();
                                    try {
                                        String temp = WeatherUtil.lastValue(object.getString("l1"));
                                        String pheCode = WeatherUtil.lastValue(object.getString("l5"));
                                        leftList.get(i).temp = temp;
                                        leftList.get(i).pheCode = pheCode;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //发送广播刷新关注列表信息
                                    Intent intent = new Intent();
                                    intent.setAction(CONST.REFRESH_ATTENTION_LIST);
                                    if (i == 0) {
                                        intent.putExtra("isRefresh", true);
                                    } else {
                                        intent.putExtra("isRefresh", false);
                                    }
                                    intent.putExtra("locationId", locationId);
                                    intent.putExtra("data", leftList.get(i));
                                    getActivity().sendBroadcast(intent);

                                }
                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable error, String content) {
                    super.onError(error, content);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //统计安装次数
                    StatisticUtil.asyncQueryInstall(getActivity());

                    //统计登录次数
                    StatisticUtil.asyncQueryLogin("http://new.12379.tianqi.cn/Api/login", getActivity());

                    //统计点击页面次数
                    StatisticUtil.submitClickCount("1", "首页");
                }
            }).start();
        }
    }

    /**
     * 获取预警信息和天气信息
     *
     * @param cityId
     * @param warningId
     */
    private void getWeatherInfo(final String cityId, final String cityName, final String warningId) {
        if (cityId != null) {
            WeatherAPI.getWeather2(getActivity(), cityId, Language.ZH_CN, new AsyncResponseHandler() {
                @Override
                public void onComplete(Weather content) {
                    super.onComplete(content);
                    if (content != null) {
                        List<WarningDto> tempList = new ArrayList<>();
                        tempList.clear();
                        for (int m = 0; m < warningList.size(); m++) {
                            WarningDto dto = warningList.get(m);
                            String[] array = dto.html.split("-");
                            String item0 = array[0];
                            if (TextUtils.equals(item0, warningId)) {
                                tempList.add(dto);
                            }
                        }

                        WarningDto data;
                        if (tempList.size() > 0) {//有预警
                            data = tempList.get(0);
                        } else {//无预警
                            data = new WarningDto();
                        }
                        data.cityId = cityId;
                        data.cityName = cityName;
                        data.warningId = warningId;
                        JSONObject fact = content.getWeatherFactInfo();
                        try {
                            String temp = WeatherUtil.lastValue(fact.getString("l1"));
                            String pheCode = WeatherUtil.lastValue(fact.getString("l5"));
                            data.temp = temp;
                            data.pheCode = pheCode;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (tempList.size() > 0) {//有预警
                            addWarning(tempList, cityName);
                        } else {//无预警
                            addForecast(false, false, cityId, cityName, content);
                        }

                        //发送广播刷新关注列表信息
                        Intent intent = new Intent();
                        intent.setAction(CONST.REFRESH_ATTENTION_LIST);
                        intent.putExtra("isRefresh", false);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("data", data);
                        intent.putExtras(bundle);
                        getActivity().sendBroadcast(intent);
                    }
                }

                @Override
                public void onError(Throwable error, String content) {
                    super.onError(error, content);
                }
            });
        }
    }

    /**
     * 添加实况信息
     *
     * @param content
     */
    private void addFactWeather(boolean isHaveWarning, Weather content) {
        reFact.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View fView;
        if (isHaveWarning) {
            fView = inflater.inflate(R.layout.layout_fact_detail2, null);
        } else {
            fView = inflater.inflate(R.layout.layout_fact_detail1, null);
        }
        TextView tvPublishTime = fView.findViewById(R.id.tvPublishTime);
        TextView tvTemp = fView.findViewById(R.id.tvTemp);
        ImageView ivPhe = fView.findViewById(R.id.ivPhe);
        TextView tvPhe = fView.findViewById(R.id.tvPhe);
        TextView tvBodyTemp = fView.findViewById(R.id.tvBodyTemp);
        TextView tvHumidity = fView.findViewById(R.id.tvHumidity);
        TextView tvWind = fView.findViewById(R.id.tvWind);
        TextView tvAqi = fView.findViewById(R.id.tvAqi);

        JSONObject object = content.getWeatherFactInfo();
        try {
            if (!object.isNull("l7")) {
                String publishTime = object.getString("l7");
                tvPublishTime.setText(publishTime + "监测");
                tvPublishTime2.setText(publishTime + "监测");
            }
            if (!object.isNull("l1")) {
                String temp = WeatherUtil.lastValue(object.getString("l1"));
                tvTemp.setText(temp);
            }
            if (!object.isNull("l5")) {
                String pheCode = WeatherUtil.lastValue(object.getString("l5"));
                Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
                try {
                    long zao8 = sdf2.parse("06").getTime();
                    long wan8 = sdf2.parse("18").getTime();
                    long current = sdf2.parse(sdf2.format(new Date())).getTime();
                    if (current >= zao8 && current < wan8) {
                        drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
                    } else {
                        drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                drawable.setLevel(Integer.valueOf(pheCode));
                ivPhe.setBackground(drawable);
                tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(pheCode))));
            }
            if (!object.isNull("l12")) {
                String bodyTemp = WeatherUtil.lastValue(object.getString("l12"));
                tvBodyTemp.setText(getString(R.string.body_temp) + "\n" + bodyTemp + getString(R.string.unit_degree));
            }
            if (!object.isNull("l2")) {
                String humidity = WeatherUtil.lastValue(object.getString("l2"));
                tvHumidity.setText(getString(R.string.humidity) + "\n" + humidity + getString(R.string.unit_percent));
            }
            if (!object.isNull("l4")) {
                String windDir = WeatherUtil.lastValue(object.getString("l4"));
                if (!object.isNull("l3")) {
                    String windForce = WeatherUtil.lastValue(object.getString("l3"));
                    tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + "\n" +
                            WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //空气质量
        try {
            JSONObject aqiObj = content.getAirQualityInfo();
            if (!aqiObj.isNull("k3")) {
                String value = WeatherUtil.lastValue(aqiObj.getString("k3"));
                if (!TextUtils.isEmpty(value)) {
                    tvAqi.setText("AQI" + " " + value + "\n" +
                            WeatherUtil.getAqi(getActivity(), Integer.valueOf(value)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reFact.addView(fView);

        reMain.setVisibility(View.VISIBLE);
    }

    /**
     * 动态添加三天预报
     */
    private void addForecast(boolean isHaveWarning, boolean isLocation, String cityId, String cityName, Weather content) {
        if (content == null) {
            return;
        }
        try {
            LinearLayout llMain = new LinearLayout(getActivity());//装载城市名称和预报信息容器
            llMain.setOrientation(LinearLayout.VERTICAL);
            llMain.setBackgroundColor(0x05ffffff);
            LinearLayout llForecast = new LinearLayout(getActivity());//装载预报信息容器
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View titleView;
            if (isHaveWarning) {
                titleView = inflater.inflate(R.layout.attention_forecast_layout2, null);
            } else {
                titleView = inflater.inflate(R.layout.attention_forecast_layout1, null);
            }
            TextView tvCityName = titleView.findViewById(R.id.tvCityName);
            TextView tvWarningCount = titleView.findViewById(R.id.tvWarningCount);
            TextView tvPublishTime = titleView.findViewById(R.id.tvPublishTime);

            if (isLocation) {
                tvCityName.setText(getActivity().getString(R.string.forecast));
                tvWarningCount.setVisibility(View.GONE);
            } else {
                tvWarningCount.setVisibility(View.VISIBLE);
                tvWarningCount.setText(getString(R.string.no_warning));
                if (!TextUtils.isEmpty(cityName)) {
                    tvCityName.setText(cityName);
                }

//				JSONObject object = content.getWeatherFactInfo();
//				try {
//					if (!object.isNull("l7")) {
//						String publishTime = object.getString("l7");
//						if (!TextUtils.isEmpty(publishTime)) {
//							tvPublishTime.setText(publishTime+getString(R.string.publish));
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
            }
            try {
                long six = sdf2.parse("06").getTime();
                long eight = sdf2.parse("08").getTime();
                long eleven = sdf2.parse("11").getTime();
                long eighteen = sdf2.parse("18").getTime();
                long current = sdf2.parse(sdf2.format(new Date())).getTime();
                if (current >= six && current < eight) {
                    tvPublishTime.setText("06:00" + getString(R.string.publish));
                } else if (current >= eight && current < eleven) {
                    tvPublishTime.setText("08:00" + getString(R.string.publish));
                } else if (current >= eleven && current < eighteen) {
                    tvPublishTime.setText("11:00" + getString(R.string.publish));
                } else if (current >= eighteen || current < six) {
                    tvPublishTime.setText("18:00" + getString(R.string.publish));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            llMain.addView(titleView);

            for (int i = 1; i <= 3; i++) {
                WeatherDto dto = new WeatherDto();
                JSONArray timeArray = content.getTimeInfo(i);
                JSONObject timeObj = timeArray.getJSONObject(0);
                dto.week = timeObj.getString("t4");//星期几
                dto.date = timeObj.getString("t1");//日期

                JSONArray weeklyArray = content.getWeatherForecastInfo(i);
                JSONObject weeklyObj = weeklyArray.getJSONObject(0);
                //晚上
                dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
                dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
                dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

                //白天数据缺失时，就使用第二天白天数据
                if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
                    JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
                    dto.highPheCode = Integer.valueOf(secondObj.getString("fa"));
                    dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));

                    int time1 = Integer.valueOf(secondObj.getString("fc"));
                    int time2 = Integer.valueOf(weeklyObj.getString("fd"));
                    if (time1 <= time2) {
                        dto.highTemp = time2 + 2;
                    } else {
                        dto.highTemp = Integer.valueOf(secondObj.getString("fc"));
                    }
                } else {
                    //白天
                    dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
                    dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
                    dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
                }

                View view;
                if (isHaveWarning) {
                    view = inflater.inflate(R.layout.weekly_layout2, null);
                } else {
                    view = inflater.inflate(R.layout.weekly_layout1, null);
                }
                LinearLayout llWeek = (LinearLayout) view.findViewById(R.id.llWeek);
                TextView tvWeek = (TextView) view.findViewById(R.id.tvWeek);
                ImageView ivPheHigh = (ImageView) view.findViewById(R.id.ivPheHigh);
                ImageView ivPheLow = (ImageView) view.findViewById(R.id.ivPheLow);
                TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);

                DisplayMetrics dm = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;
                LayoutParams params = (LinearLayout.LayoutParams) llWeek.getLayoutParams();
                params.width = width / 3;
                llWeek.setLayoutParams(params);

                if (i == 1) {
                    tvWeek.setText(getString(R.string.today));
                } else {
                    String weekStr = getActivity().getString(R.string.week) + dto.week.substring(dto.week.length() - 1, dto.week.length());
                    tvWeek.setText(weekStr);
                }
                Drawable hd = getActivity().getResources().getDrawable(R.drawable.phenomenon_drawable);
                hd.setLevel(dto.highPheCode);
                ivPheHigh.setBackground(hd);
                Drawable ld = getActivity().getResources().getDrawable(R.drawable.phenomenon_drawable_night);
                ld.setLevel(dto.lowPheCode);
                ivPheLow.setBackground(ld);
                tvTemp.setText(dto.highTemp + getString(R.string.unit_degree) + "/" + dto.lowTemp + getString(R.string.unit_degree));
                llForecast.addView(view);
            }
            llMain.addView(llForecast);

            if (isLocation) {
                llContainer.addView(llMain, 0);
            } else {
                llContainer.addView(llMain);
            }

            if (!isLocation) {
                LayoutParams viewParams = (LinearLayout.LayoutParams) llMain.getLayoutParams();
                viewParams.topMargin = 30;
                llMain.setLayoutParams(viewParams);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 动态添加预警信息
     * @param wList
     */
    private void addWarning(final List<WarningDto> wList, final String cityName) {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_layout_add_warning, null);
        LinearLayout llWarning = view.findViewById(R.id.llWarning);
        TextView tvCityName = view.findViewById(R.id.tvCityName);
        TextView tvWarningCount = view.findViewById(R.id.tvWarningCount);
        TextView tvTemp = view.findViewById(R.id.tvTemp);
        ImageView ivPhe = view.findViewById(R.id.ivPhe);
        ImageView ivWarning = view.findViewById(R.id.ivWarning);
        TextView tvWarningName = view.findViewById(R.id.tvWarningName);
        TextView tvWarningTime = view.findViewById(R.id.tvWarningTime);

        final WarningDto data = wList.get(0);
        if (!TextUtils.isEmpty(cityName)) {
            tvCityName.setText(cityName);
        }
        tvWarningCount.setText(getString(R.string.gong) + getString(R.string.publish) + wList.size() + getString(R.string.tiao) + getString(R.string.warning));
        if (!TextUtils.isEmpty(data.temp)) {
            tvTemp.setText(data.temp + getActivity().getString(R.string.unit_degree));
        }
        if (!TextUtils.isEmpty(data.pheCode)) {
            Drawable drawable;
            int current = Integer.valueOf(sdf2.format(new Date()));
            if (current >= 5 && current < 17) {
                drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
            } else {
                drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
            }
            drawable.setLevel(Integer.valueOf(data.pheCode));
            ivPhe.setBackground(drawable);
        }
        if (!TextUtils.isEmpty(data.time)) {
            try {
                tvWarningTime.setText(sdf3.format(sdf1.parse(data.time)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(data.name)) {
            String[] array = data.name.split(getString(R.string.publish));
            tvWarningName.setText(array[1]);
            tvWarningTime.setText(tvWarningTime.getText().toString()+"  "+array[0] + getString(R.string.publish));
        }

        Bitmap bitmap = null;
        if (data.color.equals(CONST.blue[0])) {
            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + data.type + CONST.blue[1] + CONST.imageSuffix);
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.blue[1] + CONST.imageSuffix);
            }
        } else if (data.color.equals(CONST.yellow[0])) {
            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + data.type + CONST.yellow[1] + CONST.imageSuffix);
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.yellow[1] + CONST.imageSuffix);
            }
        } else if (data.color.equals(CONST.orange[0])) {
            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + data.type + CONST.orange[1] + CONST.imageSuffix);
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.orange[1] + CONST.imageSuffix);
            }
        } else if (data.color.equals(CONST.red[0])) {
            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + data.type + CONST.red[1] + CONST.imageSuffix);
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.red[1] + CONST.imageSuffix);
            }
        } else if (data.color.equals(CONST.unknown[0])) {
            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/default" + CONST.imageSuffix);
        }
        ivWarning.setImageBitmap(bitmap);

        llContainer.addView(view);

        LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = 30;
        view.setLayoutParams(params);

        llWarning.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDetail = new Intent(getActivity(), WarningDetailActivity.class);
                intentDetail.putExtra("url", data.html);
                startActivity(intentDetail);
            }
        });

        tvWarningCount.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View arg0) {
                if (wList.size() == 1) {
                    Intent intentDetail = new Intent(getActivity(), WarningDetailActivity.class);
                    intentDetail.putExtra("url", data.html);
                    startActivity(intentDetail);
                } else {
                    Intent intent = new Intent(getActivity(), ShawnWarningListActivity.class);
                    intent.putExtra("isVisible", false);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) wList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 获取预警详情
     */
    private void OkHttpWarningDetail(final String url, final String type, final String color) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject object = new JSONObject(result);
                                        if (!object.isNull("sendTime")) {
                                            tvWarningTime.setText(object.getString("sendTime"));
                                        }

                                        if (!object.isNull("description")) {
                                            tvWarningIntro.setText(object.getString("description"));
                                        }

                                        String name = object.getString("headline");
                                        if (!TextUtils.isEmpty(name)) {
                                            String splitName = "发布";
                                            if (name.contains("更新发布")) {
                                                splitName = "更新发布";
                                            } else if (name.contains("发布")) {
                                                splitName = "发布";
                                            } else if (name.contains("更新")) {
                                                splitName = "更新";
                                            }
                                            String[] array = name.split(splitName);
                                            if (array[1] != null) {
                                                tvWarningName.setText(array[1]);
                                                if (array[1].contains("[") && array[1].contains("]")) {
                                                    tvWarningName.setText(array[1].substring(0, array[1].indexOf("[")));
                                                }
                                            }
                                            if (array[0] != null) {
                                                tvWarningTime.setText(tvWarningTime.getText().toString()+"  "+array[0] + "发布");
                                            }
                                        }

                                        Bitmap bitmap = null;
                                        if (object.getString("severityCode").equals(CONST.blue[0])) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + object.getString("eventType") + CONST.blue[1] + CONST.imageSuffix);
                                            if (bitmap == null) {
                                                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.blue[1] + CONST.imageSuffix);
                                            }
                                        } else if (object.getString("severityCode").equals(CONST.yellow[0])) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + object.getString("eventType") + CONST.yellow[1] + CONST.imageSuffix);
                                            if (bitmap == null) {
                                                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.yellow[1] + CONST.imageSuffix);
                                            }
                                        } else if (object.getString("severityCode").equals(CONST.orange[0])) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + object.getString("eventType") + CONST.orange[1] + CONST.imageSuffix);
                                            if (bitmap == null) {
                                                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.orange[1] + CONST.imageSuffix);
                                            }
                                        } else if (object.getString("severityCode").equals(CONST.red[0])) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + object.getString("eventType") + CONST.red[1] + CONST.imageSuffix);
                                            if (bitmap == null) {
                                                bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/" + "default" + CONST.red[1] + CONST.imageSuffix);
                                            }
                                        } else if (object.getString("severityCode").equals(CONST.unknown[0])) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(getActivity(), "warning/default" + CONST.imageSuffix);
                                        }
                                        ivWarning.setImageBitmap(bitmap);

                                        if (!TextUtils.isEmpty(tvWarningIntro.getText().toString()) && !tvWarningIntro.getText().toString().contains("防御指南")) {
                                            queryWarningGuide(type, color);
                                        }

                                        progressBar.setVisibility(View.GONE);
                                        reMain.setVisibility(View.VISIBLE);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 初始化数据库
     */
    private void queryWarningGuide(String type, String color) {
        DBManager dbManager = new DBManager(getActivity());
        dbManager.openDateBase();
        dbManager.closeDatabase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + type + color + "\"", null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            tvWarningIntro.setText(tvWarningIntro.getText().toString()+"\n预警指南：\n" + cursor.getString(cursor.getColumnIndex("WarningGuide")));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ivMenu:
                Intent intentMenu = new Intent();
                intentMenu.setAction(CONST.BROADCAST_DRAWER);
                getActivity().sendBroadcast(intentMenu);
                break;
            case R.id.ivMy:
                startActivity(new Intent(getActivity(), MyActivity.class));
                break;
            case R.id.tvDis:
                if (disList.size() == 1) {
                    intent = new Intent(getActivity(), WarningDetailActivity.class);
                    intent.putExtra("url", disList.get(0).html);
                    startActivity(intent);
                } else if (disList.size() > 1) {
                    intent = new Intent(getActivity(), ShawnWarningListActivity.class);
                    intent.putExtra("isVisible", false);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) disList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tvCity:
                if (cityList.size() == 1) {
                    intent = new Intent(getActivity(), WarningDetailActivity.class);
                    intent.putExtra("url", cityList.get(0).html);
                    startActivity(intent);
                } else if (cityList.size() > 1) {
                    intent = new Intent(getActivity(), ShawnWarningListActivity.class);
                    intent.putExtra("isVisible", false);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) cityList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tvPro:
                if (proList.size() == 1) {
                    intent = new Intent(getActivity(), WarningDetailActivity.class);
                    intent.putExtra("url", proList.get(0).html);
                    startActivity(intent);
                } else if (proList.size() > 1) {
                    intent = new Intent(getActivity(), ShawnWarningListActivity.class);
                    intent.putExtra("isVisible", false);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) proList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tvNation:
                if (nationList.size() == 1) {
                    intent = new Intent(getActivity(), WarningDetailActivity.class);
                    intent.putExtra("url", nationList.get(0).html);
                    startActivity(intent);
                } else if (nationList.size() > 1) {
                    intent = new Intent(getActivity(), ShawnWarningListActivity.class);
                    intent.putExtra("isVisible", false);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) nationList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.tvInfo:
                NewsDto data = new NewsDto();
                data.title = "实时更新：新型冠状病毒肺炎疫情实时大数据报告";
                data.url = "https://voice.baidu.com/act/newpneumonia/newpneumonia?fraz=partner&paaz=gjyj";
                intent = new Intent(getActivity(), WebviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", data);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    //需要申请的所有权限
    public static String[] allPermissions = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    //拒绝的权限集合
    public static List<String> deniedList = new ArrayList<>();
    /**
     * 申请定位权限
     */
    private void checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocation();
        }else {
            deniedList.clear();
            for (int i = 0; i < allPermissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity(), allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(allPermissions[i]);
                }
            }
            if (deniedList.isEmpty()) {//所有权限都授予
                startLocation();
            }else {
                String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
                Fragment1.this.requestPermissions(permissions, AuthorityUtil.AUTHOR_LOCATION);
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
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        }
                    }
                    if (isAllGranted) {//所有权限都授予
                        startLocation();
                    }else {//只要有一个没有授权，就提示进入设置界面设置
                        AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、电话权限，是否前往设置？");
                    }
                }else {
                    for (int i = 0; i < permissions.length; i++) {
                        if (!Fragment1.this.shouldShowRequestPermissionRationale(permissions[i])) {
                            AuthorityUtil.intentAuthorSetting(getActivity(), "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、电话权限，是否前往设置？");
                            break;
                        }
                    }
                }
                break;
        }
    }

}
