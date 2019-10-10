package com.warning.activity;

/**
 * 内容审核
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.message.UmengNotifyClickActivity;
import com.warning.R;
import com.warning.fragment.CheckWorksFragment;
import com.warning.view.MainViewPager;

public class CheckWorksActivity extends UmengNotifyClickActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tv1, tv2, tv3;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_works);
		initWidget();
		initViewPager();
	}

	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("内容审核");
		tv1 = (TextView) findViewById(R.id.tv1);
		tv1.setOnClickListener(new MyOnClickListener(0));
		tv2 = (TextView) findViewById(R.id.tv2);
		tv2.setOnClickListener(new MyOnClickListener(1));
		tv3 = (TextView) findViewById(R.id.tv3);
		tv3.setOnClickListener(new MyOnClickListener(2));
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		for (int i = 1; i <= 3; i++) {
			Fragment fragment = new CheckWorksFragment();
			Bundle bundle = new Bundle();
			bundle.putString("status", i+"");//审核状态，1为未审核，2为通过，3为拒绝
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}
			
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override	
		public void onPageSelected(int arg0) {
			if (arg0 == 0) {
				tv1.setBackgroundResource(R.drawable.btn_upload_selected);
				tv1.setTextColor(getResources().getColor(R.color.white));
				tv2.setBackgroundColor(getResources().getColor(R.color.white));
				tv2.setTextColor(getResources().getColor(R.color.blue));
				tv3.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv3.setTextColor(getResources().getColor(R.color.blue));
			}else if (arg0 == 1) {
				tv1.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv1.setTextColor(getResources().getColor(R.color.blue));
				tv2.setBackgroundColor(getResources().getColor(R.color.blue));
				tv2.setTextColor(getResources().getColor(R.color.white));
				tv3.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv3.setTextColor(getResources().getColor(R.color.blue));
			}else if (arg0 == 2) {
				tv1.setBackgroundColor(getResources().getColor(R.color.transparent));
				tv1.setTextColor(getResources().getColor(R.color.blue));
				tv2.setBackgroundColor(getResources().getColor(R.color.white));
				tv2.setTextColor(getResources().getColor(R.color.blue));
				tv3.setBackgroundResource(R.drawable.btn_unupload_selected);
				tv3.setTextColor(getResources().getColor(R.color.white));
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index);
			}
		}
	};

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
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

		default:
			break;
		}
	}

}
