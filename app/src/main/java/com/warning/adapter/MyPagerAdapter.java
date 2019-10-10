package com.warning.adapter;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * viewpager+fragment适配器
 * @author shawn_sun
 */
public class MyPagerAdapter extends PagerAdapter {
	
	private Activity activity;
	private List<Fragment> fragments;
	
	public MyPagerAdapter(Activity activity, List<Fragment> fragments){
		this.activity = activity;
		this.fragments = fragments;
	}
	
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
			FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
			ft.add(fragment, fragment.getClass().getSimpleName());
//			ft.commit();
			ft.commitAllowingStateLoss();
			/**
			 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
			 * 会在进程的主线程中,用异步的方式来执行。
			 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
			 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
			 */
			activity.getFragmentManager().executePendingTransactions();
		}

		if (fragment.getView().getParent() == null) {
			container.addView(fragment.getView()); // 为viewpager增加布局
		}
		return fragment.getView();
	}
}
