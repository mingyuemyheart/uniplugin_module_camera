package com.warning.activity;

/**
 * 在线预览图片
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.socialize.UMShareAPI;
import com.warning.R;
import com.warning.adapter.OnlinePictureAdapter;
import com.warning.adapter.OnlineVideoAdapter;
import com.warning.dto.PhotoDto;
import com.warning.util.CommonUtil;
import com.warning.util.DialogUtil;
import com.warning.util.EmojiMapUtil;
import com.warning.util.OkHttpUtil;
import com.warning.util.StatisticUtil;
import com.warning.view.PhotoView;

import net.tsz.afinal.FinalBitmap;

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
import uk.co.senab.photoview.PhotoViewAttacher;

public class OnlinePictureActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private GridView mGridView = null;
	private OnlinePictureAdapter gridAdapter = null;
	private ViewPager mViewPager = null;
	private MyViewPagerAdapter pagerAdapter = null;
	private ImageView[] imageArray = null;//装载图片的数组
	private ImageView[] ivTips = null;//装载点的数组
	private ViewGroup viewGroup = null;
	private RelativeLayout rePager = null;
	private LinearLayout llBack = null;//返回按钮
	private PhotoDto data = null;
	private List<String> urlList = new ArrayList<>();//存放图片的list

	private ListView mListView = null;
	private OnlineVideoAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 1000;

	private TextView tvSubTitle = null;//标题
	private TextView tvPosition = null;//地址
	private TextView tvDate = null;//日期
	private TextView tvTitle = null;//标题
	private TextView tvContent = null;//内容描述
	private ImageView ivPortrait = null;//头像
	private TextView tvNickName = null;
	private TextView tvPlayCount = null;//浏览次数
	private TextView tvCommentCount = null;//评论次数
	private EditText etComment = null;
	private ImageView ivClear = null;
	private TextView tvSubmit = null;
	private ImageView ivPraise = null;//点赞
	private TextView tvPraise = null;//点赞次数
	private int praiseCount = 0;//该条视频被点赞总数
	private boolean isPraise = false;//该条视频当前登录用户是否点过赞
	private ImageView ivShare2 = null;//分享
	private RelativeLayout reContent = null;
	private int index = 0;

	private LinearLayout llContainer = null;
	private int width = 0;
	private float density = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_picture);
		mContext = this;
		initWidget();
		initGridView();
		initViewPager();
		initListView();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		rePager = (RelativeLayout) findViewById(R.id.rePager);
		viewGroup = (ViewGroup) findViewById(R.id.viewGroup);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvSubTitle = (TextView) findViewById(R.id.tvSubTitle);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvContent = (TextView) findViewById(R.id.tvContent);
		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		tvPlayCount = (TextView) findViewById(R.id.tvPlayCount);
		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ivPraise = (ImageView) findViewById(R.id.ivPraise);
		ivPraise.setOnClickListener(this);
		tvPraise = (TextView) findViewById(R.id.tvPraise);
		tvPraise.setOnClickListener(this);
		ivShare2 = (ImageView) findViewById(R.id.ivShare2);
		ivShare2.setOnClickListener(this);
		etComment = (EditText) findViewById(R.id.etComment);
		etComment.addTextChangedListener(watcher);
		ivClear = (ImageView) findViewById(R.id.ivClear);
		ivClear.setOnClickListener(this);
		tvSubmit = (TextView) findViewById(R.id.tvSubmit);
		tvSubmit.setOnClickListener(this);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;

		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				urlList.clear();
				urlList.addAll(data.getUrlList());

				tvPosition.setText("拍摄地点："+data.getLocation());
				tvTitle.setText("直报详情");
				tvSubTitle.setText(data.title);
				if (!TextUtils.isEmpty(data.content)) {
					tvContent.setText("内容描述："+data.content);
					tvContent.setVisibility(View.VISIBLE);
				}else {
					tvContent.setVisibility(View.GONE);
				}
				tvDate.setText("拍摄时间："+data.getWorkTime());
				if (!TextUtils.isEmpty(data.portraitUrl)) {
					FinalBitmap finalBitmap = FinalBitmap.create(mContext);
					LayoutParams params = ivPortrait.getLayoutParams();
					if (params != null) {
						finalBitmap.display(ivPortrait, data.portraitUrl, null, params.width);
					}else {
						finalBitmap.display(ivPortrait, data.portraitUrl, null, 0);
					}
				}else {
					ivPortrait.setImageResource(R.drawable.iv_portrait);
				}
				if (!TextUtils.isEmpty(data.nickName)) {
					tvNickName.setText(data.nickName);
				}else if (!TextUtils.isEmpty(data.getUserName())) {
					tvNickName.setText(data.getUserName());
				}else if (!TextUtils.isEmpty(data.phoneNumber)) {
					if (data.phoneNumber.length() >= 7) {
						tvNickName.setText(data.phoneNumber.replace(data.phoneNumber.substring(3, 7), "****"));
					}else {
						tvNickName.setText(data.phoneNumber);
					}
				}
				if (!TextUtils.isEmpty(data.showTime)) {
					tvPlayCount.setText(data.showTime+"次浏览");
				}
//				if (data.getCommentCount() != null) {
//					tvCommentCount.setText("评论" + "（"+data.getCommentCount()+"）");
//				}
//				if (data.praiseCount != null) {
//					tvPraise.setText(data.praiseCount);
//				}

				//获取评论列表和点赞状态
				OkhttpComment("http://new.12379.tianqi.cn/Work/getpinglun");

				//上传播放或浏览次数
				StatisticUtil.asyncQueryCount("http://new.12379.tianqi.cn/Work/addshowtime", data.videoId);
			}
		}
	}

	/**
	 * 评论监听
	 */
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				ivClear.setVisibility(View.VISIBLE);
				tvSubmit.setVisibility(View.VISIBLE);
			}else {
				ivClear.setVisibility(View.GONE);
				tvSubmit.setVisibility(View.GONE);
			}
		}
	};

	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.gridView);
		gridAdapter = new OnlinePictureAdapter(mContext, urlList);
		mGridView.setAdapter(gridAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (rePager.getVisibility() == View.GONE) {
					if (mViewPager != null) {
						mViewPager.setCurrentItem(arg2);
					}
					scaleExpandAnimation(rePager, arg2);
					rePager.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		imageArray = new ImageView[urlList.size()];
		for (int i = 0; i < urlList.size(); i++) {
			ImageView image = new ImageView(mContext);
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(image, urlList.get(i), null, 0);
			imageArray[i] = image;
		}

		ivTips = new ImageView[urlList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < urlList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_white);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);
		}

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		pagerAdapter = new MyViewPagerAdapter(imageArray);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				index = arg0;
				for (int i = 0; i < urlList.size(); i++) {
					if(i == arg0){
						ivTips[i].setBackgroundResource(R.drawable.point_white);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}

//					View childAt = mViewPager.getChildAt(i);
//                    try {
//                        if (childAt != null && childAt instanceof PhotoView) {
//                        	PhotoView  photoView = (PhotoView) childAt;//得到viewPager里面的页面
//                        	PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);//把得到的photoView放到这个负责变形的类当中
//                            mAttacher.getDisplayMatrix().reset();//得到这个页面的显示状态，然后重置为默认状态
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private class MyViewPagerAdapter extends PagerAdapter {

		private ImageView[] mImageViews;

		public MyViewPagerAdapter(ImageView[] imageViews) {
			this.mImageViews = imageViews;
		}

		@Override
		public int getCount() {
			return mImageViews.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mImageViews[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			Drawable drawable = mImageViews[position].getDrawable();
			photoView.setImageDrawable(drawable);
			container.addView(photoView, 0);
			photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
				@Override
				public void onPhotoTap(View view, float v, float v1) {
					scaleColloseAnimation(rePager, index);
					rePager.setVisibility(View.GONE);
				}
			});
			return photoView;
		}

	}

	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new OnlineVideoAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * 获取评论列表和点赞状态
	 */
	private void OkhttpComment(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("wid", data.videoId);
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
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													reContent.setVisibility(View.VISIBLE);
													if (!object.isNull("iszan")) {
														int isZan = object.getInt("iszan");
														if (isZan == 0) {//没有点赞
															ivPraise.setImageResource(R.drawable.iv_unlike);
															isPraise = false;
														}else if (isZan == 1) {//点过赞
															ivPraise.setImageResource(R.drawable.iv_like);
															isPraise = true;
														}
													}
													if (!object.isNull("countzan")) {
														String countZan = object.getString("countzan");
														if (!TextUtils.isEmpty(countZan)) {
															praiseCount = Integer.parseInt(countZan);
															tvPraise.setText(countZan);
														}
													}
													if (!object.isNull("plcount")) {
														tvCommentCount.setText("评论 （"+object.getInt("plcount")+"）");
													}
													if (!object.isNull("pinglun")) {
														mList.clear();
														JSONArray array = object.getJSONArray("pinglun");
														for (int i = 0; i < array.length(); i++) {
															JSONObject itemObj = array.getJSONObject(i);
															PhotoDto dto = new PhotoDto();
															if (!itemObj.isNull("uid")) {
																dto.uid = itemObj.getString("uid");
															}
															if (!itemObj.isNull("id")) {
																dto.commentId = itemObj.getString("id");
															}
															if (!itemObj.isNull("picture")) {
																dto.portraitUrl = itemObj.getString("picture");
															}
															if (!itemObj.isNull("nickname")) {
																dto.nickName = itemObj.getString("nickname");
															}
															if (!itemObj.isNull("phonenumber")) {
																dto.phoneNumber = itemObj.getString("phonenumber");
															}
															if (!itemObj.isNull("createtime")) {
																dto.createTime = itemObj.getString("createtime");
															}
															if (!itemObj.isNull("content")) {
																dto.comment = EmojiMapUtil.replaceCheatSheetEmojis(itemObj.getString("content"));
															}
															mList.add(dto);
														}
														if (mAdapter != null) {
															mAdapter.notifyDataSetChanged();
														}
													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
	 * 点赞
	 */
	private void OkhttpPraise(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("token", TOKEN);
				builder.add("uid", UID);
				builder.add("wid", data.videoId);
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
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													praiseCount = praiseCount+1;
													tvPraise.setText(praiseCount+"");
													ivPraise.setImageResource(R.drawable.iv_like);
													isPraise = true;
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
	 * 添加一条评论
	 */
	private void OkhttpAddComment(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", TOKEN);
		builder.add("uid", UID);
		builder.add("wid", data.videoId);
		builder.add("content", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													etComment.setText("");

													//获取评论列表和点赞状态
													OkhttpComment("http://new.12379.tianqi.cn/Work/getpinglun");
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
	 * 隐藏虚拟键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (etComment != null) {
			CommonUtil.hideInputSoft(etComment, mContext);
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 放大动画
	 * @param view
	 */
	private void scaleExpandAnimation(View view, int index) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
		if (index == 0) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 1) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 2) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 3) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 4) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 5) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 6) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.4f);
		}else if (index == 7) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.4f);
		}else if (index == 8) {
			scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.4f);
		}
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1.0f);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}

	/**
	 * 缩小动画
	 * @param view
	 */
	private void scaleColloseAnimation(View view, int index) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
		if (index == 0) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 1) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 2) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.0f);
		}else if (index == 3) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 4) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 5) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.2f);
		}else if (index == 6) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF,0.4f);
		}else if (index == 7) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.4f);
		}else if (index == 8) {
			scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
					Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0.4f);
		}
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (rePager.getVisibility() == View.VISIBLE) {
			scaleColloseAnimation(rePager, index);
			rePager.setVisibility(View.GONE);
			return false;
		}else {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (rePager.getVisibility() == View.VISIBLE) {
				scaleColloseAnimation(rePager, index);
				rePager.setVisibility(View.GONE);
			}else {
				finish();
			}
			break;
		case R.id.ivPraise:
			if (TextUtils.isEmpty(TOKEN)) {
				startActivityForResult(new Intent(mContext, LoginActivity.class), 1);
			}else {
				if (isPraise == false) {
					OkhttpPraise("http://new.12379.tianqi.cn/Work/setincZan");
				}else {
					Toast.makeText(mContext, "您已点过赞", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.ivShare2:
			CommonUtil.share(OnlinePictureActivity.this, data.title, data.url, data.url);
			StatisticUtil.OkhttpShare("http://new.12379.tianqi.cn/Work/getfxzf", data.videoId, data.uid);
			break;
		case R.id.ivClear:
			if (etComment != null) {
				etComment.setText("");
			}
			break;
		case R.id.tvSubmit:
			if (TextUtils.isEmpty(TOKEN)) {
				startActivityForResult(new Intent(mContext, LoginActivity.class), 2);
			}else {
				if (!TextUtils.isEmpty(etComment.getText().toString())) {
					OkhttpAddComment("http://new.12379.tianqi.cn/Work/addpinglun");
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1://点赞
				DialogUtil.welcomeDialog(mContext);
				if (isPraise == false) {
					OkhttpPraise("http://new.12379.tianqi.cn/Work/setincZan");
				}else {
					Toast.makeText(mContext, "您已点过赞", Toast.LENGTH_SHORT).show();
				}
				break;
			case 2:
				DialogUtil.welcomeDialog(mContext);
				if (!TextUtils.isEmpty(etComment.getText().toString())) {
					OkhttpAddComment("http://new.12379.tianqi.cn/Work/addpinglun");
				}
				break;

			default:
				break;
			}
		}else {
			//umeng分享回调
			UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		}
	}

}
