package com.warning.activity;

/**
 * 我的消息
 * Created by shawn on 2017/9/4.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.warning.R;
import com.warning.adapter.MyMsgAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyMsgActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvControl = null;
    private ListView listView = null;
    private MyMsgAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private int page = 1, pageSize = 20;
    private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
    private TextView tvPrompt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_msg);
        mContext = this;
        initRefreshLayout();
        initWidget();
        initListView();
    }

    /**
     * 初始化下拉刷新布局
     */
    private void initRefreshLayout() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
        refreshLayout.setProgressViewEndTarget(true, 300);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        page = 1;
        mList.clear();
        OkhttpMsg("http://new.12379.tianqi.cn/Work/getnewuserMes");
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("我的消息");
        tvPrompt = (TextView) findViewById(R.id.tvPrompt);
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("一键全读");

        refresh();
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.listView);
        mAdapter = new MyMsgAdapter(mContext, mList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                Intent intent = new Intent();
                if (dto.getWorkstype().equals("imgs")) {
                    intent.setClass(mContext, OnlinePictureActivity.class);
                }else {
                    intent.setClass(mContext, OnlineVideoActivity.class);
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
                OkhttpMsgStatus("http://new.12379.tianqi.cn/Work/readMes", dto, false);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
                    page += 1;
                    OkhttpMsg("http://new.12379.tianqi.cn/Work/getnewuserMes");
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
            }
        });
    }

    /**
     * 获取消息列表信息
     * @param url
     */
    private void OkhttpMsg(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("uid", UID);
                builder.add("token", TOKEN);
                builder.add("p", page+"");
                builder.add("size", pageSize+"");
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
                                        JSONObject object = new JSONObject(result);
                                        if (!object.isNull("unread")) {
                                            final String unread = object.getString("unread");
                                            if (!TextUtils.isEmpty(unread)) {
                                                int c = Integer.valueOf(unread);
                                                if (c > 0) {
                                                    tvControl.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                        if (!object.isNull("info")) {
                                            JSONArray array = object.getJSONArray("info");
                                            for (int i = 0; i < array.length(); i++) {
                                                PhotoDto dto = new PhotoDto();
                                                JSONObject obj = array.getJSONObject(i);
                                                if (!obj.isNull("msg_status")) {
                                                    dto.isRead = obj.getString("msg_status");
                                                }
                                                if (!obj.isNull("msg_pic")) {
                                                    dto.msgUrl = obj.getString("msg_pic");
                                                }
                                                if (!obj.isNull("msg_nickname")) {
                                                    dto.msgName = obj.getString("msg_nickname");
                                                }
                                                if (!obj.isNull("msg_time")) {
                                                    dto.msgTime = obj.getString("msg_time");
                                                }
                                                if (!obj.isNull("msg_content")) {
                                                    dto.msgContent = obj.getString("msg_content");
                                                }
                                                if (!obj.isNull("msg_id")) {
                                                    dto.msgId = obj.getString("msg_id");
                                                }

                                                //直报
                                                if (!obj.isNull("id")) {
                                                    dto.videoId = obj.getString("id");
                                                }
                                                if (!obj.isNull("uid")) {
                                                    dto.uid = obj.getString("uid");
                                                }
                                                if (!obj.isNull("title")) {
                                                    dto.title = obj.getString("title");
                                                }
                                                if (!obj.isNull("content")) {
                                                    dto.content = obj.getString("content");
                                                }
                                                if (!obj.isNull("create_time")) {
                                                    dto.createTime = obj.getString("create_time");
                                                }
                                                if (!obj.isNull("latlon")) {
                                                    String latlon = obj.getString("latlon");
                                                    if (!TextUtils.isEmpty(latlon) && !TextUtils.equals(latlon, ",")) {
                                                        String[] latLngArray = latlon.split(",");
                                                        dto.lat = latLngArray[0];
                                                        dto.lng = latLngArray[1];
                                                    }
                                                }
                                                if (!obj.isNull("location")) {
                                                    dto.location = obj.getString("location");
                                                }
                                                if (!obj.isNull("nickname")) {
                                                    dto.nickName = obj.getString("nickname");
                                                }
                                                if (!obj.isNull("username")) {
                                                    dto.userName = obj.getString("username");
                                                }
                                                if (!obj.isNull("picture")) {
                                                    dto.portraitUrl = obj.getString("picture");
                                                }
                                                if (!obj.isNull("phonenumber")) {
                                                    dto.phoneNumber = obj.getString("phonenumber");
                                                }
                                                if (!obj.isNull("praise")) {
                                                    dto.setPraiseCount(obj.getString("praise"));
                                                }
                                                if (!obj.isNull("comments")) {
                                                    dto.commentCount = obj.getString("comments");
                                                }
                                                if (!obj.isNull("work_time")) {
                                                    dto.workTime = obj.getString("work_time");
                                                }
                                                if (!obj.isNull("workstype")) {
                                                    dto.workstype = obj.getString("workstype");
                                                }
                                                if (!obj.isNull("videoshowtime")) {
                                                    dto.showTime = obj.getString("videoshowtime");
                                                }
                                                if (!obj.isNull("worksinfo")) {
                                                    JSONObject workObj = new JSONObject(obj.getString("worksinfo"));

                                                    //视频
                                                    if (!workObj.isNull("video")) {
                                                        JSONObject video = workObj.getJSONObject("video");
                                                        if (!video.isNull("ORG")) {//腾讯云结构解析
                                                            JSONObject ORG = video.getJSONObject("ORG");
                                                            if (!ORG.isNull("url")) {
                                                                dto.videoUrl = ORG.getString("url");
                                                            }
                                                            if (!video.isNull("SD")) {
                                                                JSONObject SD = video.getJSONObject("SD");
                                                                if (!SD.isNull("url")) {
                                                                    dto.sd = SD.getString("url");
                                                                }
                                                            }
                                                            if (!video.isNull("HD")) {
                                                                JSONObject HD = video.getJSONObject("HD");
                                                                if (!HD.isNull("url")) {
                                                                    dto.hd = HD.getString("url");
                                                                    dto.videoUrl = HD.getString("url");
                                                                }
                                                            }
                                                            if (!video.isNull("FHD")) {
                                                                JSONObject FHD = video.getJSONObject("FHD");
                                                                if (!FHD.isNull("url")) {
                                                                    dto.fhd = FHD.getString("url");
                                                                }
                                                            }
                                                        }else {
                                                            dto.videoUrl = video.getString("url");
                                                        }
                                                    }
                                                    if (!workObj.isNull("thumbnail")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
                                                        if (!imgObj.isNull("url")) {
                                                            dto.setUrl(imgObj.getString("url"));
                                                        }
                                                    }

                                                    //图片
                                                    List<String> urlList = new ArrayList<>();
                                                    if (!workObj.isNull("imgs1")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                            dto.setUrl(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs2")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs3")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs4")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs5")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs6")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs7")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs8")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    if (!workObj.isNull("imgs9")) {
                                                        JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
                                                        if (!imgObj.isNull("url")) {
                                                            urlList.add(imgObj.getString("url"));
                                                        }
                                                    }
                                                    dto.setUrlList(urlList);
                                                }

                                                mList.add(dto);
                                            }
                                        }

                                        refreshLayout.setRefreshing(false);
                                        if (mList.size() > 0) {
                                            tvPrompt.setVisibility(View.GONE);
                                            if (mAdapter != null) {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }else {
                                            tvPrompt.setText("暂无消息");
                                            tvPrompt.setVisibility(View.VISIBLE);
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
     * 更改消息状态
     * @param url
     * @param flag true为一键全读，false为单条
     */
    private void OkhttpMsgStatus(final String url, final PhotoDto data, final boolean flag) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("uid", UID);
                builder.add("token", TOKEN);
                if (data != null) {//单条
                    if (TextUtils.isEmpty(data.msgId) || TextUtils.isEmpty(data.isRead)) {
                        return;
                    }
                    builder.add("id", data.msgId);
                    builder.add("status", "1");
                }else {//一键全读

                }
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
                                        if (!obj.isNull("status")) {
                                            String status = obj.getString("status");
                                            if (TextUtils.equals(status, "1")) {//成功
                                                if (data != null) {
                                                    data.isRead = "1";
                                                }
                                                if (flag) {
                                                    for (int i = 0; i < mList.size(); i++) {
                                                        mList.get(i).isRead = "1";
                                                    }
                                                    tvControl.setVisibility(View.GONE);
                                                }
                                                if (mAdapter != null) {
                                                    mAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.tvControl:
                OkhttpMsgStatus("http://new.12379.tianqi.cn/Work/readMes", null, true);
                break;
        }
    }
}
