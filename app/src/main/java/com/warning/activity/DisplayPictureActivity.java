package com.warning.activity;

/**
 * 图片预览
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scene.net.Net;
import com.warning.R;
import com.warning.adapter.EventTypeAdapter;
import com.warning.adapter.PictureAdapter;
import com.warning.adapter.WeatherTypeAdapter;
import com.warning.common.CONST;
import com.warning.dto.PhotoDto;
import com.warning.dto.UploadVideoDto;
import com.warning.util.CommonUtil;
import com.warning.util.OkHttpUtil;
import com.warning.view.PhotoView;
import com.warning.view.ScrollviewGridview;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DisplayPictureActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	public TextView tvTitle = null;
	private String subTitle = "";//副标题
	private ScrollviewGridview mGridView = null;
	private PictureAdapter mAdapter = null;
	private TextView tvPositon = null;//地址
	private TextView tvDate = null;//日期
	private EditText etTitle = null;//标题
	private EditText etContent = null;
	private TextView tvTextCount = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
	private TextView tvRemove = null;//删除按钮
	private TextView tvUpload = null;//上传按钮
	private List<PhotoDto> selectList = new ArrayList<>();
	private ScrollviewGridview gridView1 = null;
	private WeatherTypeAdapter adapter1 = null;
	private List<UploadVideoDto> list1 = new ArrayList<>();
	private String hot_flags = "";//天气类型
	private ScrollviewGridview gridView2 = null;
	private EventTypeAdapter adapter2 = null;
	private List<UploadVideoDto> list2 = new ArrayList<>();
	private String event_flags = "";//事件类型
	private int count = 0;
	private String lat = "0", lng = "0";
	private String proName = "", cityName = "", disName = "", roadName = "", aoiName = "";
	private String position = "";//位置信息
	private PhotoDto data = null;

	//图片预览
	private ViewPager mViewPager = null;
	private MyViewPagerAdapter pagerAdapter = null;
	private ImageView[] imageArray = null;//装载图片的数组
	private ImageView[] ivTips = null;//装载点的数组
	private ViewGroup viewGroup = null;
	private RelativeLayout rePager = null;
	private int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_picture);
		mContext = this;
		initWidget();
		initGridView();
		initViewPager();
		initGridView1();
		initGridView2();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		rePager = (RelativeLayout) findViewById(R.id.rePager);
		viewGroup = (ViewGroup) findViewById(R.id.viewGroup);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvPositon = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvRemove = (TextView) findViewById(R.id.tvRemove);
		tvRemove.setOnClickListener(this);
		tvUpload = (TextView) findViewById(R.id.tvUpload);
		tvUpload.setOnClickListener(this);
		etTitle = (EditText) findViewById(R.id.etTitle);
		etContent = (EditText) findViewById(R.id.etContent);
		etContent.addTextChangedListener(contentWatcher);
		tvTextCount = (TextView) findViewById(R.id.tvTextCount);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				if (!TextUtils.isEmpty(data.workTime)) {
					SharedPreferences sp = getSharedPreferences(data.workTime, Context.MODE_PRIVATE);
					proName = sp.getString("proName", "");
					cityName = sp.getString("cityName", "");
					disName = sp.getString("disName", "");
					roadName = sp.getString("roadName", "");
					aoiName = sp.getString("aoiName", "");
					lat = sp.getString("lat", "");
					lng = sp.getString("lng", "");

					selectList.clear();
					File[] picFileArray = new File(CONST.PICTURE_ADDR+File.separator+data.workTime).listFiles();
					if (picFileArray.length > 0) {
						for (int j = 0; j < picFileArray.length; j++) {
							PhotoDto dto = new PhotoDto();
							dto.workstype = "imgs";
							dto.url = picFileArray[j].getPath();
							selectList.add(dto);
						}
					}

					if (cityName.contains(proName)) {
						tvPositon.setText("拍摄地点："+cityName+disName+roadName+aoiName);
						position = cityName+disName+roadName+aoiName;
					}else {
						tvPositon.setText("拍摄地点："+proName+cityName+disName+roadName+aoiName);
						position = proName+cityName+disName+roadName+aoiName;
					}
					try {
						tvDate.setText("拍摄时间："+sdf2.format(sdf3.parse(data.workTime)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					subTitle = position+"发生";
					etTitle.setText(subTitle);
					etTitle.setSelection(subTitle.length());
					if (proName.contains("北京")) {
						proName = "北京";
					}else if (proName.contains("天津")) {
						proName = "天津";
					}else if (proName.contains("上海")) {
						proName = "上海";
					}else if (proName.contains("重庆")) {
						proName = "重庆";
					}else if (proName.contains("内蒙古")) {
						proName = "内蒙古";
					}else if (proName.contains("广西")) {
						proName = "广西";
					}else if (proName.contains("宁夏")) {
						proName = "宁夏";
					}else if (proName.contains("新疆")) {
						proName = "新疆";
					}else if (proName.contains("西藏")) {
						proName = "西藏";
					}
					tvUpload.setVisibility(View.VISIBLE);
				}
			}
		}
		
		//获取上传标签
		OkHttpLabel("http://new.12379.tianqi.cn/Work/getbiaoqian");
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		for (int i = 0; i < selectList.size(); i++) {
			selectList.get(i).isShowCircle = true;
			if (i < 9) {
				selectList.get(i).isShowCorrect = true;
				count++;
			}
		}
		tvTitle.setText("已选中"+count+"个文件");
		
		mGridView = (ScrollviewGridview) findViewById(R.id.gridView);
		mAdapter = new PictureAdapter(mContext, selectList, DisplayPictureActivity.this);
		mGridView.setAdapter(mAdapter);
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

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		imageArray = new ImageView[selectList.size()];
		for (int i = 0; i < selectList.size(); i++) {
			ImageView image = new ImageView(mContext);
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(image, selectList.get(i).url, null, 0);
			imageArray[i] = image;
		}

		ivTips = new ImageView[selectList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < selectList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_white);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
				for (int i = 0; i < selectList.size(); i++) {
					if(i == arg0){
						ivTips[i].setBackgroundResource(R.drawable.point_white);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}

//					View childAt = mViewPager.getChildAt(i);
//					try {
//						if (childAt != null && childAt instanceof PhotoView) {
//							PhotoView  photoView = (PhotoView) childAt;//得到viewPager里面的页面
//							PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);//把得到的photoView放到这个负责变形的类当中
//							mAttacher.getDisplayMatrix().reset();//得到这个页面的显示状态，然后重置为默认状态
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
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
	 * 输入内容监听器
	 */
	private TextWatcher contentWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (etContent.getText().length() == 0) {
				tvTextCount.setText("(200字以内)");
			}else {
				int count = 200-etContent.getText().length();
				tvTextCount.setText("(还可输入"+count+"字)");
			}
		}
	};
	
	/**
	 * 初始化天气类型gridview
	 */
	private void initGridView1() {
		gridView1 = (ScrollviewGridview) findViewById(R.id.gridView1);
		adapter1 = new WeatherTypeAdapter(mContext, list1);
		gridView1.setAdapter(adapter1);
		ViewGroup.LayoutParams params = gridView1.getLayoutParams();
		params.height = 240;
		gridView1.setLayoutParams(params);
		gridView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list1.size(); i++) {
					if (i == arg2) {
						if (list1.get(i).isSelected == false) {
							list1.get(i).isSelected = true;
							etTitle.setText(subTitle+list1.get(i).weatherName);
							etTitle.setSelection(etTitle.getText().toString().length());
							
							if (TextUtils.equals(list1.get(i).eventType, "11000")) {//自然灾害
								list2.get(0).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "12000")) {//事故灾难
								list2.get(1).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "13000")) {//公共卫生
								list2.get(2).isSelected = true;
							}else if (TextUtils.equals(list1.get(i).eventType, "14000")) {//安全事件
								list2.get(3).isSelected = true;
							}
						}else {
							list1.get(i).isSelected = false;
							etTitle.setText(subTitle+"");
							etTitle.setSelection(etTitle.getText().toString().length());
							
							if (TextUtils.equals(list1.get(i).eventType, "11000")) {//自然灾害
								list2.get(0).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "12000")) {//事故灾难
								list2.get(1).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "13000")) {//公共卫生
								list2.get(2).isSelected = false;
							}else if (TextUtils.equals(list1.get(i).eventType, "14000")) {//安全事件
								list2.get(3).isSelected = false;
							}
						}
						
						if (adapter2 != null) {
							adapter2.notifyDataSetChanged();
						}
						
					}else {
						list1.get(i).isSelected = false;
					}
				}
				if (adapter1 != null) {
					adapter1.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 初始化事件类型gridview
	 */
	private void initGridView2() {
		gridView2 = (ScrollviewGridview) findViewById(R.id.gridView2);
		adapter2 = new EventTypeAdapter(mContext, list2);
		gridView2.setAdapter(adapter2);
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list2.size(); i++) {
					if (i == arg2) {
						if (list2.get(i).isSelected == false) {
							list2.get(i).isSelected = true;
						}else {
							list2.get(i).isSelected = false;
						}
					}
				}
				if (adapter2 != null) {
					adapter2.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 获取上传标签
	 */
	private void OkHttpLabel(final String url) {
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONArray array = new JSONArray(result);
										list2.clear();
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											UploadVideoDto dto = new UploadVideoDto();
											if (!obj.isNull("name")) {
												dto.eventName = obj.getString("name");
											}
											if (!obj.isNull("code")) {
												dto.eventType = obj.getString("code");
											}
											dto.isSelected = false;

											if (!obj.isNull("info") && !TextUtils.isEmpty(obj.getString("info"))) {
												JSONArray itemArray = obj.getJSONArray("info");
												if (itemArray.length() > 0) {
													list1.clear();
												}
												for (int j = 0; j < itemArray.length(); j++) {
													UploadVideoDto itemDto = new UploadVideoDto();
													JSONObject itemObj = itemArray.getJSONObject(j);
													if (!itemObj.isNull("c")) {
														itemDto.count = itemObj.getString("c");
													}
													if (!itemObj.isNull("eventType")) {
														itemDto.weatherType = itemObj.getString("eventType");
													}
													if (!itemObj.isNull("name")) {
														itemDto.weatherName = itemObj.getString("name");
													}
													itemDto.eventType = dto.eventType;
													itemDto.isSelected = false;
													list1.add(itemDto);
												}

												if (adapter1 != null) {
													adapter1.notifyDataSetChanged();
												}

											}
											list2.add(dto);
										}

										if (adapter2 != null) {
											adapter2.notifyDataSetChanged();
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
	 * 删除图片对话框
	 */
	private void deleteDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		final List<PhotoDto> tempList = new ArrayList<PhotoDto>();
		tempList.clear();
		for (int i = 0; i < selectList.size(); i++) {
			PhotoDto dto = selectList.get(i);
			if (dto.isShowCorrect) {
				tempList.add(dto);
			}
		}
		tvMessage.setText("是否删除所选的" + tempList.size() + "个文件？");
		
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
				selectList.removeAll(tempList);
				mAdapter.notifyDataSetChanged();

				CommonUtil.deleteDirectory(CONST.PICTURE_ADDR+File.separator+workTime);

				int count = 0;
				for (int i = 0; i < selectList.size(); i++) {
					if (selectList.get(i).isShowCorrect) {
						count++;
					}
				}
				tvTitle.setText("已选中"+count+"个文件");
				mAdapter.count = count;
				
				//发送刷新未上传广播
//				Toast.makeText(mContext, getString(R.string.delete_all_files), Toast.LENGTH_SHORT).show();
//				Intent intent = new Intent();
//				intent.setAction(CONST.REFRESH_NOTUPLOAD);
//				sendBroadcast(intent);
				if (selectList.size() <= 0) {
					finish();
				}
			}
		});
	}
	
	/**
	 * 检查上传标题dialog
	 */
	private void checkTitleDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 上传图片对话框
	 */
	private void uploadDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		final List<PhotoDto> tempList = new ArrayList<>();
		tempList.clear();
		for (int i = 0; i < selectList.size(); i++) {
			PhotoDto dto = selectList.get(i);
			if (dto.isShowCorrect) {
				tempList.add(dto);
			}
		}
		tvMessage.setText("是否上传所选的" + tempList.size() + "个文件？");
		
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
				uploadPictures("http://new.12379.tianqi.cn/Work/Upload");
			}
		});
	}

	private String workTime = "";
	/**
	 * 上传图片
	 * @param url 接口地址
	 */
	private void uploadPictures(String url) {
		showDialog();
		AjaxParams params = new AjaxParams();
		params.put("token", TOKEN);
		params.put("uid", UID);
		params.put("isadmin", ISINFOER);
		params.put("title", etTitle.getText().toString());
		params.put("content", etContent.getText().toString());
		params.put("latlon", lat+","+lng);
		params.put("hot_flags", hot_flags);
		params.put("event_flags", event_flags);
		params.put("location", position);
		params.put("workstype", "imgs");
		params.put("proname", proName);
		params.put("cityname", cityName);
		params.put("xianname", disName);
		
		final List<PhotoDto> tempList = new ArrayList<>();
		tempList.clear();
		for (int i = 0; i < selectList.size(); i++) {
			PhotoDto dto = selectList.get(i);
			if (dto.isShowCorrect) {
				tempList.add(dto);
			}
		}

		for (int i = 0; i < tempList.size(); i++) {
			File pictureFile = new File(tempList.get(i).getUrl());
			String fileName = pictureFile.getName().substring(0, pictureFile.getName().length()-4);
			if (i == 0) {
				workTime = fileName;
			}
			try {
				params.put("work_time", sdf2.format(sdf1.parse(fileName)));
				params.put("imgs" + Integer.valueOf(i + 1), pictureFile);
			} catch (ParseException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		Net.post(url, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String result) {
				super.onSuccess(result);
				try {
					JSONObject obj = new JSONObject(result);
					if (!obj.isNull("status")) {
						if (obj.getInt("status") == 1) {//上传成功
							cancelDialog();
							selectList.removeAll(tempList);
							mAdapter.notifyDataSetChanged();

							CommonUtil.deleteDirectory(CONST.PICTURE_ADDR+File.separator+workTime);

							int count = 0;
							for (int i = 0; i < selectList.size(); i++) {
								if (selectList.get(i).isShowCorrect) {
									count++;
								}
							}
							tvTitle.setText("已选中"+count+"个文件");
							mAdapter.count = count;
							if (selectList.size() <= 0) {
								Intent intent = new Intent();
								intent.putExtra("fileName", workTime);
								setResult(RESULT_OK, intent);

								uploadSuccessDialog();
							}
						}else {//上传失败
							Toast.makeText(mContext, "上传失败！", Toast.LENGTH_SHORT).show();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onLoading(long count, long current) {
				super.onLoading(count, current);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				cancelDialog();
				Toast.makeText(mContext, "上传失败！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 上传视频成功对话框
	 */
	private void uploadSuccessDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_success_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				exit();
			}
		});
	}
	
	private void exitDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText("确定退出编辑界面？");
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
				finish();
			}
		});
	}

	private void exit() {
		if (rePager.getVisibility() == View.VISIBLE) {
			scaleColloseAnimation(rePager, index);
			rePager.setVisibility(View.GONE);
		}else {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		exit();
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			exit();
			break;
		case R.id.tvRemove:
			deleteDialog();
			break;
		case R.id.tvUpload:
			if (TextUtils.isEmpty(etTitle.getText().toString())) {
				checkTitleDialog();
			}else {
				uploadDialog();
			}
			break;

		default:
			break;
		}
	}
}
