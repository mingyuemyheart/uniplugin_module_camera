package com.camera.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.camera.R;
import com.camera.adapter.DisplayPictureAdapter;
import com.camera.dto.PhotoDto;
import com.camera.view.PhotoView;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片预览
 */
public class DisplayPictureActivity extends BaseActivity implements OnClickListener, DisplayPictureAdapter.SelectListener {
	
	private Context mContext = null;
	private DisplayPictureAdapter mAdapter;
	private ArrayList<PhotoDto> dataList = new ArrayList<>();
	private ConstraintLayout clSelect,clDelete;
	private TextView tvSelect;

	//图片预览
	private ViewPager mViewPager = null;
	private ImageView[] ivTips = null;//装载点的数组
	private ViewGroup viewGroup = null;
	private ConstraintLayout clPager = null;
	private int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_picture);
		mContext = this;
		initWidget();
		initGridView();
		initViewPager();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		clPager = findViewById(R.id.clPager);
		viewGroup = findViewById(R.id.viewGroup);
		clSelect = findViewById(R.id.clSelect);
		clSelect.setOnClickListener(this);
		clDelete = findViewById(R.id.clDelete);
		clDelete.setOnClickListener(this);
		tvSelect = findViewById(R.id.tvSelect);
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		dataList.clear();
		if (getIntent().hasExtra("fileName")) {
			String fileName = getIntent().getStringExtra("fileName");
			File files = new File(getExternalFilesDir(null)+"/picture/"+fileName);
			if (files.exists()) {
				File[] f = files.listFiles();
				for (int i = 0; i < f.length; i++) {
					PhotoDto dto = new PhotoDto();
					dto.url = f[i].getAbsolutePath();
					dataList.add(dto);
				}
			}
		}
		GridView gridView = findViewById(R.id.gridView);
		mAdapter = new DisplayPictureAdapter(mContext, dataList);
		mAdapter.setSelectListener(this);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
			if (clPager.getVisibility() == View.GONE) {
				if (mViewPager != null) {
					mViewPager.setCurrentItem(arg2);
				}
				scaleExpandAnimation(clPager, arg2);
				clPager.setVisibility(View.VISIBLE);
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
		ImageView[] imageArray = new ImageView[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			ImageView image = new ImageView(mContext);
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(image, dataList.get(i).url, null, 0);
			imageArray[i] = image;
		}

		ivTips = new ImageView[dataList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < dataList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_black);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);
		}

		mViewPager = findViewById(R.id.viewPager);
		MyViewPagerAdapter pagerAdapter = new MyViewPagerAdapter(imageArray);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				index = arg0;
				for (int i = 0; i < dataList.size(); i++) {
					if(i == arg0){
						ivTips[i].setBackgroundResource(R.drawable.point_black);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}
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
			photoView.setOnPhotoTapListener((view, v, v1) -> {
				scaleColloseAnimation(clPager, index);
				clPager.setVisibility(View.GONE);
			});
			return photoView;
		}

	}

	private void exit() {
		if (clPager.getVisibility() == View.VISIBLE) {
			scaleColloseAnimation(clPager, index);
			clPager.setVisibility(View.GONE);
		}else {
			Intent intent = new Intent();
			intent.putParcelableArrayListExtra("dataList", dataList);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		exit();
		return false;
	}

	@Override
	public void onSelected() {
		int selectCount = 0;
		for (int i = 0; i < dataList.size(); i++) {
			PhotoDto dto = dataList.get(i);
			if (dto.isSelected) {
				selectCount++;
			}
		}
		if (selectCount == dataList.size()) {
			tvSelect.setText("取消全选");
		} else {
			tvSelect.setText("全选");
		}
	}

	/**
	 * 删除图片对话框
	 */
	private void deleteDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = view.findViewById(R.id.llNegative);
		LinearLayout llPositive = view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		final List<PhotoDto> tempList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			PhotoDto dto = dataList.get(i);
			if (dto.isSelected) {
				tempList.add(dto);
			}
		}
		tvMessage.setText("是否删除所选的" + tempList.size() + "个文件？");

		llNegative.setOnClickListener(arg0 -> dialog.dismiss());
		llPositive.setOnClickListener(arg0 -> {
			dialog.dismiss();
			for (int i = 0; i < tempList.size(); i++) {
				PhotoDto temp = tempList.get(i);
				if (!TextUtils.isEmpty(temp.url)) {
					File file = new File(temp.url);
					if (file.exists()) {
						file.delete();
					}
				}
				dataList.remove(temp);
			}
			tempList.clear();
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			if (dataList.size() == 0) {
				clSelect.setVisibility(View.GONE);
				clDelete.setVisibility(View.GONE);
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.clSelect:
				if (TextUtils.equals(tvSelect.getText().toString(), "全选")) {
					tvSelect.setText("取消全选");
					for (int i = 0; i < dataList.size(); i++) {
						PhotoDto dto = dataList.get(i);
						dto.isSelected = true;
					}
				} else {
					tvSelect.setText("全选");
					for (int i = 0; i < dataList.size(); i++) {
						PhotoDto dto = dataList.get(i);
						dto.isSelected = false;
					}
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.clDelete:
				deleteDialog();
				break;

		default:
			break;
		}
	}
}
