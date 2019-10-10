package com.warning.fragment;

/**
 * 资讯
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.warning.R;
import com.warning.activity.ZixunDetailActivity;
import com.warning.adapter.NewsAdapter;
import com.warning.common.CONST;
import com.warning.dto.NewsDto;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class Fragment3 extends Fragment implements OnRefreshListener{
	
	private ListView listView1 = null;
	private NewsAdapter mAdapter1 = null;
	private List<NewsDto> zixunList = new ArrayList<>();
	private String ZIXUNURL = "http://new.12379.tianqi.cn/infomes/data/12379/tzgg/tfsj.json";//资讯请求地址
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private MyBroadCastReceiver mReceiver = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment3, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initBroadCast();
		initRefreshLayout(view);
		initWidget(view);
		initListView1(view);
	}

	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Fragment3.class.getName());
		getActivity().registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), Fragment3.class.getName())) {
				refresh();
			}
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
		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
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
		OkhttpZixun(ZIXUNURL);
		StatisticUtil.submitClickCount("5", "资讯");
	}
	
	private void initWidget(View view) {
//		refresh();
	}
	
	/**
	 * 获取资讯信息
	 */
	private void OkhttpZixun(final String url) {
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										zixunList.clear();
										final List<NewsDto> elseList = new ArrayList<>();//不置顶list
										elseList.clear();
										final List<NewsDto> topList = new ArrayList<>();//置顶list
										topList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											NewsDto dto = new NewsDto();
											if (i == 0) {
												dto.isTop = true;
											}
											if (!obj.isNull("title")) {
												dto.title = obj.getString("title");
											}
											if (!obj.isNull("time")) {
												dto.time = obj.getString("time");
											}
											if (!obj.isNull("content")) {
												dto.content = obj.getString("content");
											}
											if (!obj.isNull("url")) {
												dto.url = obj.getString("url");
											}
											if (!obj.isNull("img")) {
												dto.imgUrl = obj.getString("img");
											}
											if (!obj.isNull("istop")) {
												dto.isToTop = obj.getString("istop");
												if (TextUtils.equals(dto.isToTop, "1")) {
													topList.add(dto);
												}else {
													elseList.add(dto);
												}
											}
										}
										zixunList.addAll(topList);
										zixunList.addAll(elseList);

										if (mAdapter1 != null) {
											mAdapter1.notifyDataSetChanged();
										}
										refreshLayout.setRefreshing(false);
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
	
	private void initListView1(View view) {
		listView1 = (ListView) view.findViewById(R.id.listView1);
		mAdapter1 = new NewsAdapter(getActivity(), zixunList);
		listView1.setAdapter(mAdapter1);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(getActivity(), ZixunDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", zixunList.get(arg2));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
}
