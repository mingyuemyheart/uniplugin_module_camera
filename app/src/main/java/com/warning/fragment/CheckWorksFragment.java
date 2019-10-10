package com.warning.fragment;

/**
 * 内容审核
 * @author shawn_sun
 *
 */

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.activity.OnlinePictureActivity;
import com.warning.activity.OnlineVideoActivity;
import com.warning.adapter.CheckWorksAdapter;
import com.warning.adapter.DialogRefuseAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.swipemenulistview.SwipeMenu;
import com.warning.swipemenulistview.SwipeMenuCreator;
import com.warning.swipemenulistview.SwipeMenuItem;
import com.warning.swipemenulistview.SwipeMenuListView;
import com.warning.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.VerticalSwipeRefreshLayout;

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

import static com.warning.activity.BaseActivity.AREAS;
import static com.warning.activity.BaseActivity.TOKEN;
import static com.warning.activity.BaseActivity.UID;

public class CheckWorksFragment extends Fragment implements OnRefreshListener{
	
	private SwipeMenuListView mListView = null;
	private CheckWorksAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 20;
	private String check_url = "http://new.12379.tianqi.cn/Work/examine";
	private String do_check_url = "http://new.12379.tianqi.cn/Work/do_examine";
	private VerticalSwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private String checkStatus = "1";//审核状态，1为未审核，2为通过，3为拒绝
	private TextView tvPrompt = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_check_works, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initListView(view);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (VerticalSwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
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
		OkhttpList(check_url);
	}
	
	private void initWidget(View view) {
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		checkStatus = getArguments().getString("status", "1");//审核状态

		refresh();
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		mListView = (SwipeMenuListView) view.findViewById(R.id.listView);
		mAdapter = new CheckWorksAdapter(getActivity(), mList);
		mListView.setAdapter(mAdapter);
		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu) {
				switch (menu.getViewType()) {
				case 0:
					createMenu1(menu, "审核\n通过", "审核\n拒绝");
					break;
				case 1:
					createMenu1(menu, "未\n审核", "审核\n拒绝");
					break;
				case 2:
					createMenu1(menu, "未\n审核", "审核\n通过");
					break;
				}
			}
			private void createMenu1(SwipeMenu menu, String name1, String name2) {
				SwipeMenuItem item1 = new SwipeMenuItem(getActivity());
				item1.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
				item1.setWidth((int) CommonUtil.dip2px(getActivity(), 50));
				item1.setTitle(name1);
				item1.setTitleColor(getResources().getColor(R.color.white));
				item1.setTitleSize(14);
				menu.addMenuItem(item1);
				SwipeMenuItem item2 = new SwipeMenuItem(getActivity());
				item2.setBackground(new ColorDrawable(Color.rgb(0xE5, 0x18, 0x5E)));
				item2.setWidth((int) CommonUtil.dip2px(getActivity(), 50));
				item2.setTitle(name2);
				item2.setTitleColor(getResources().getColor(R.color.white));
				item2.setTitleSize(14);
				menu.addMenuItem(item2);
			}
		};
		mListView.setMenuCreator(creator);
		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index) {
				PhotoDto dto = mList.get(position);
				switch (index) {
				case 0:
					if (menu.getViewType() == 0) {
						dto.status = "2";
						OkhttpCheck(do_check_url, dto.videoId, "2", "");
					}else if (menu.getViewType() == 1) {
						dto.status = "1";
						OkhttpCheck(do_check_url, dto.videoId, "1", "");
					}else if (menu.getViewType() == 2) {
						dto.status = "1";
						OkhttpCheck(do_check_url, dto.videoId, "1", "");
					}
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					break;
				case 1:
					if (menu.getViewType() == 0) {
						dto.status = "3";
						refuseDialog("选择拒绝原因", dto.videoId);
					}else if (menu.getViewType() == 1) {
						dto.status = "3";
						refuseDialog("选择拒绝原因", dto.videoId);
					}else if (menu.getViewType() == 2) {
						dto.status = "2";
						OkhttpCheck(do_check_url, dto.videoId, "2", "");
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
					break;
				}
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(getActivity(), OnlinePictureActivity.class);
				}else {
					intent.setClass(getActivity(), OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OkhttpList(check_url);
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}
	
	/**
	 * 获取审核列表
	 */
	private void OkhttpList(final String url) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(TOKEN)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("p", page+"");
				builder.add("size", pageSize+"");
				builder.add("uid", UID);
				builder.add("token", TOKEN);
				builder.add("areas", AREAS);
				builder.add("status", checkStatus);
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										final JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														JSONArray array = new JSONArray(object.getString("info"));
														if (array.length() <= 0) {
															refreshLayout.setRefreshing(false);
															if (TextUtils.equals(checkStatus, "1")) {//未审核
																tvPrompt.setText("暂无未审核的作品");
															}else if (TextUtils.equals(checkStatus, "2")) {//审核通过
																tvPrompt.setText("暂无审核通过的作品");
															}else if (TextUtils.equals(checkStatus, "3")) {//审核拒绝
																tvPrompt.setText("暂无审核拒绝的作品");
															}
															tvPrompt.setVisibility(View.VISIBLE);
															return;
														}else {
															tvPrompt.setVisibility(View.GONE);
														}

														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = (JSONObject) array.opt(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("id")) {
																dto.setVideoId(obj.getString("id"));
															}
															if (!obj.isNull("uid")) {
																dto.uid = obj.getString("uid");
															}
															if (!obj.isNull("title")) {
																dto.setTitle(obj.getString("title"));
															}
															if (!obj.isNull("content")) {
																dto.content = obj.getString("content");
															}
															if (!obj.isNull("status")) {
																dto.status = obj.getString("status");
															}
															if (!obj.isNull("create_time")) {
																dto.setCreateTime(obj.getString("create_time"));
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
																dto.setLocation(obj.getString("location"));
															}
															if (!obj.isNull("nickname")) {
																dto.nickName = obj.getString("nickname");
															}
															if (!obj.isNull("username")) {
																dto.setUserName(obj.getString("username"));
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
																dto.setCommentCount(obj.getString("comments"));
															}
															if (!obj.isNull("work_time")) {
																dto.setWorkTime(obj.getString("work_time"));
															}
															if (!obj.isNull("workstype")) {
																dto.setWorkstype(obj.getString("workstype"));
															}
															if (!obj.isNull("videoshowtime")) {
																dto.showTime = obj.getString("videoshowtime");
															}
															if (!obj.isNull("worksinfo")) {
																String worksinfo = obj.getString("worksinfo");
																if (!TextUtils.isEmpty(worksinfo)) {
                                                                    JSONObject workObj = new JSONObject(worksinfo);
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
															}

															if (!TextUtils.isEmpty(dto.getWorkTime())) {
																mList.add(dto);
															}
														}
													}

													refreshLayout.setRefreshing(false);
													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
														tvPrompt.setVisibility(View.GONE);
													}

												}else {
													//失败
													if (!object.isNull("msg")) {
														try {
															String msg = object.getString("msg");
															if (msg != null) {
																Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
															}
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
	 * 审核
	 */
	private void OkhttpCheck(final String url, final String workid, final String status, final String refuseReason) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(TOKEN)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("uid", UID);
				builder.add("token", TOKEN);
				builder.add("workid", workid);
				builder.add("status", status);
				if (TextUtils.equals(status, "3")) {
					builder.add("infocontent", refuseReason);
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//审核成功
													Toast.makeText(getActivity(), "审核成功", Toast.LENGTH_SHORT).show();
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

	private List<PhotoDto> setRefuseReason() {
		List<PhotoDto> list = new ArrayList<>();
		PhotoDto dto = new PhotoDto();
		dto.refuseReason = "内容重复";
		list.add(dto);
		dto = new PhotoDto();
		dto.refuseReason = "内容不符";
		list.add(dto);
		dto = new PhotoDto();
		dto.refuseReason = "内容违规";
		list.add(dto);
		return list;
	}

	private void refuseDialog(String message, final String videoId) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_refuse, null);
		TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);
		final EditText etContent = (EditText) dialogView.findViewById(R.id.etContent);
		TextView tvNegtive = (TextView) dialogView.findViewById(R.id.tvNegtive);
		TextView tvPositive = (TextView) dialogView.findViewById(R.id.tvPositive);
		ListView listView = (ListView) dialogView.findViewById(R.id.listView);
		final List<PhotoDto> list = new ArrayList<>();
		list.clear();
		list.addAll(setRefuseReason());
		DialogRefuseAdapter adapter = new DialogRefuseAdapter(getActivity(), list);
		listView.setAdapter(adapter);

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		tvMessage.setText(message);
		tvNegtive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		tvPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(etContent.getText().toString())) {
					Toast.makeText(getActivity(), "请输入拒绝原因！", Toast.LENGTH_SHORT).show();
					return;
				}
				dialog.dismiss();
				OkhttpCheck(do_check_url, videoId, "3", etContent.getText().toString());
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PhotoDto dto = list.get(position);
				if (!TextUtils.isEmpty(dto.refuseReason)) {
					etContent.setText(dto.refuseReason);
					etContent.setSelection(etContent.getText().toString().length());
				}
			}
		});

	}
	
}
