package com.warning.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.warning.R;
import com.warning.adapter.MyUnuploadAdapter;
import com.warning.adapter.VideoWallAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 我的上传
 */

public class MyUploadActivity extends BaseActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	public TextView tvControl = null;
	private TextView tv1, tv2;

	private ListView mListView1 = null;
	private VideoWallAdapter mAdapter1 = null;
	private List<PhotoDto> mList1 = new ArrayList<>();
	private int page = 1;
	private int pageSize = 20;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private String baseUrl = "http://new.12379.tianqi.cn/Work/getmylist";

	private ListView mListView2 = null;
	private static MyUnuploadAdapter mAdapter2 = null;
	private static List<PhotoDto> mList2 = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_upload);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView1();
		initListView2();
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
		mList1.clear();
		page = 1;
		OkhttpList(baseUrl);
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("我的上传");
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setText("删除");
		tvControl.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv1.setOnClickListener(this);
		tv2 = (TextView) findViewById(R.id.tv2);
		tv2.setOnClickListener(this);

		refresh();
	}

	/**
	 * 初始化listview
	 */
	private void initListView1() {
		mListView1 = (ListView) findViewById(R.id.listView1);
		mAdapter1 = new VideoWallAdapter(mContext, mList1);
		mListView1.setAdapter(mAdapter1);
		mListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList1.get(arg2);
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
			}
		});
		mListView1.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OkhttpList(baseUrl);
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}

	/**
	 * 获取我的上传
	 */
	private void OkhttpList(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("p", page+"");
				builder.add("size", pageSize+"");
				builder.add("token", TOKEN);
				builder.add("uid", UID);
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
										final JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														JSONArray array = new JSONArray(object.getString("info"));
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
															if (!obj.isNull("picture")) {
																dto.portraitUrl = obj.getString("picture");
															}
															if (!obj.isNull("username")) {
																dto.setUserName(obj.getString("username"));
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

															if (!TextUtils.isEmpty(dto.getWorkTime())) {
																mList1.add(dto);
															}
														}
													}

													refreshLayout.setRefreshing(false);
													if (mAdapter1 != null) {
														mAdapter1.notifyDataSetChanged();
													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														try {
															String msg = object.getString("msg");
															if (msg != null) {
																Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
	 * 初始化listview
	 */
	private void initListView2() {
		getLocalUnupload();
		mListView2 = (ListView) findViewById(R.id.listView2);
		mAdapter2 = new MyUnuploadAdapter(MyUploadActivity.this, mList2);
		mListView2.setAdapter(mAdapter2);
		mListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList2.get(arg2);
				Intent intent = new Intent();
				if (dto.workstype.equals("imgs")) {
					intent.setClass(mContext, DisplayPictureActivity.class);
				}else {
					intent.setClass(mContext, DisplayVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1000);
			}
		});
		mListView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAdapter2.isShowDelete == false) {
					mAdapter2.isShowDelete = true;
					mAdapter2.notifyDataSetChanged();
				}
				return true;
			}
		});
	}

	/**
	 * 获取本地没有上传视频、图片文件
	 */
	private void getLocalUnupload() {
		mList2.clear();

		HashMap<String, String> thumbnailMap = new HashMap<>();//存放缩略图集合
		thumbnailMap.clear();
		File thumbFile = new File(CONST.THUMBNAIL_ADDR);//视频缩略图
		if (thumbFile.exists()) {
			for (int i = 0; i < thumbFile.listFiles().length; i++) {
				File localFile = thumbFile.listFiles()[i];
				if (localFile.exists()) {
					String filePath = localFile.getPath();
					String fileName = localFile.getName();
					if (filePath == null) {
						return;
					}
					if (fileName == null) {
						return;
					}
					fileName = fileName.substring(0, fileName.length()-4);
					thumbnailMap.put(fileName, filePath);
				}
			}
		}

		final List<PhotoDto> videos = new ArrayList<>();//视频
		File videoFiles = new File(CONST.VIDEO_ADDR);//视频
		if (videoFiles.exists()) {
			for (int i = 0; i < videoFiles.listFiles().length; i++) {
				File localFile = videoFiles.listFiles()[i];
				if (localFile.exists()) {
					String filePath = localFile.getPath();
					String fileName = localFile.getName();
					if (filePath == null) {
						return;
					}
					if (fileName == null) {
						return;
					}

					fileName = fileName.substring(0, fileName.length()-4);
					SharedPreferences sp = getSharedPreferences(fileName, Context.MODE_PRIVATE);
					String proName = sp.getString("proName", "");
					String cityName = sp.getString("cityName", "");
					String disName = sp.getString("disName", "");
					String roadName = sp.getString("roadName", "");
					String aoiName = sp.getString("aoiName", "");
					String lat = sp.getString("lat", "");
					String lng = sp.getString("lng", "");

					PhotoDto dto = new PhotoDto();
					dto.lat = lat;
					dto.lng = lng;
					dto.workstype = "video";
					dto.workTime = fileName;
					dto.videoUrl = filePath;
					if (thumbnailMap.containsKey(fileName)) {
						dto.url = thumbnailMap.get(fileName);
					}
					if (cityName.contains(proName)) {
						dto.location = cityName+disName+roadName+aoiName;
					}else {
						dto.location = proName+cityName+disName+roadName+aoiName;
					}
					if (!TextUtils.isEmpty(dto.location)) {//判断定位信息不为空时才加入list
						videos.add(dto);
					}

				}
			}
		}

		//添加图片信息
		final List<PhotoDto> imgs = new ArrayList<>();//图片
		File files = new File(CONST.PICTURE_ADDR);
		if (files.exists()) {
			File[] fileArray = files.listFiles();
			for (int i = 0; i < fileArray.length; i++) {
				PhotoDto dto = new PhotoDto();
				dto.workstype = "imgs";
				dto.workTime = fileArray[i].getName();

				SharedPreferences sp = getSharedPreferences(dto.workTime, Context.MODE_PRIVATE);
				String proName = sp.getString("proName", "");
				String cityName = sp.getString("cityName", "");
				String disName = sp.getString("disName", "");
				String roadName = sp.getString("roadName", "");
				String aoiName = sp.getString("aoiName", "");
				String lat = sp.getString("lat", "");
				String lng = sp.getString("lng", "");

				File[] picFileArray = new File(CONST.PICTURE_ADDR+File.separator+fileArray[i].getName()).listFiles();
				if (picFileArray.length > 0) {
					List<PhotoDto> tempList = new ArrayList<>();
					tempList.clear();
					for (int j = 0; j < picFileArray.length; j++) {
						PhotoDto data = new PhotoDto();
						data.workstype = "imgs";
						data.url = picFileArray[j].getPath();
						String name = picFileArray[j].getName();
						data.workTime = name.substring(0, name.length()-4);
						tempList.add(data);
					}
					dto.lat = lat;
					dto.lng = lng;
					dto.url = picFileArray[0].getPath();
					dto.picList.addAll(tempList);
				}

				if (cityName.contains(proName)) {
					dto.location = cityName+disName+roadName+aoiName;
				}else {
					dto.location = proName+cityName+disName+roadName+aoiName;
				}
				if (!TextUtils.isEmpty(dto.location)) {//判断定位信息不为空时才加入list
					imgs.add(dto);
				}
			}
		}

		mList2.addAll(videos);
		mList2.addAll(imgs);
		//按时间排序
		Collections.sort(mList2, new Comparator<PhotoDto>() {
			@Override
			public int compare(PhotoDto lhs, PhotoDto rhs) {
				return Double.valueOf(rhs.workTime).compareTo(Double.valueOf(lhs.workTime));
			}
		});

		if (mList2.size() > 0 && mAdapter2 != null) {
			mAdapter2.notifyDataSetChanged();
		}

		CommonUtil.deleteDirectory(CONST.OLD_VIDEO_ADDR);//删除旧版本视频文件
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (videos.size() == 0) {
					CommonUtil.deleteDirectory(CONST.THUMBNAIL_ADDR);//删除缩略图文件夹
					CommonUtil.deleteDirectory(CONST.VIDEO_ADDR);//删除视频文件夹
				}
				if (imgs.size() == 0) {
					CommonUtil.deleteDirectory(CONST.PICTURE_ADDR);//删除图片文件夹
				}
			}
		}).start();
	}

	/**
	 * 删除本地文件对话框
	 * @param message
	 * @param content
	 */
	private void deleteLocalFileDialog(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		tvContent.setText(content);
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
				deleteLocalFile();
			}
		});
	}

	/**
	 * 删除本地没有上传视频、图片文件
	 */
	private void deleteLocalFile() {
		List<PhotoDto> tempList = new ArrayList<>();
		tempList.addAll(mList2);
		mList2.clear();
		for (int i = 0; i < tempList.size(); i++) {
			PhotoDto dto = tempList.get(i);
			if (dto.isDelete) {
				CommonUtil.deleteFile(CONST.THUMBNAIL_ADDR+File.separator+dto.workTime+".jpg");//删除缩略图
				CommonUtil.deleteFile(CONST.VIDEO_ADDR+File.separator+dto.workTime+".mp4");//删除视频
				CommonUtil.deleteDirectory(CONST.PICTURE_ADDR+File.separator+dto.workTime);//删除图片及文件夹
			}else {
				mList2.add(dto);
			}
		}
		if (mAdapter2 != null) {
			tvControl.setVisibility(View.GONE);
			mAdapter2.isShowDelete = false;
			mAdapter2.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tv1:
				tv1.setBackgroundResource(R.drawable.btn_upload_selected);
				tv1.setTextColor(getResources().getColor(R.color.white));
				tv2.setBackgroundResource(R.drawable.btn_unupload_unselected);
				tv2.setTextColor(getResources().getColor(R.color.blue));
				if (refreshLayout != null) {
					refreshLayout.setVisibility(View.VISIBLE);
				}
				if (mListView2 != null) {
					mListView2.setVisibility(View.GONE);
				}
				break;
			case R.id.tv2:
				tv1.setBackgroundResource(R.drawable.btn_upload_unselected);
				tv1.setTextColor(getResources().getColor(R.color.blue));
				tv2.setBackgroundResource(R.drawable.btn_unupload_selected);
				tv2.setTextColor(getResources().getColor(R.color.white));
				if (refreshLayout != null) {
					refreshLayout.setVisibility(View.GONE);
				}
				if (mListView2 != null) {
					mListView2.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.tvControl:
				deleteLocalFileDialog("确定删除所选文件？", "");
				break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mAdapter2 != null) {
			if (mAdapter2.isShowDelete) {
				mAdapter2.isShowDelete = false;
				for (int i = 0; i < mList2.size(); i++) {
					mList2.get(i).isDelete = false;
				}
				mAdapter2.notifyDataSetChanged();

				if (tvControl.getVisibility() == View.VISIBLE) {
					tvControl.setVisibility(View.GONE);
				}
			}else {
				finish();
			}
		} else {
			finish();
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1000:
					if (data != null && data.hasExtra("fileName")) {
						String fileName = data.getExtras().getString("fileName");
						for (int i = 0; i < mList2.size(); i++) {
							if (TextUtils.equals(fileName, mList2.get(i).workTime)) {
								mList2.remove(i);
								break;
							}
						}
						if (mAdapter2 != null) {
							mAdapter2.notifyDataSetChanged();
						}
						refresh();
					}
					break;
			}
		}
	}
}
