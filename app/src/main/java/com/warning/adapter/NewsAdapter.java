package com.warning.adapter;

import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.NewsDto;

/**
 * 咨询适配器
 * @author shawn_sun
 *
 */

public class NewsAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<NewsDto> mArrayList = null;
	private final int TYPE1 = 0, TYPE2 = 1;
	private int width = 0;
	
	private final class ViewHolder {
		ImageView imageView;
		TextView tvTitle;
		TextView tvTime;
		ImageView ivTop;
	}
	
	private ViewHolder mHolder = null;
	
	public NewsAdapter(Context context, List<NewsDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (mArrayList.get(position).isTop) {
			return TYPE1;
		}else {
			return TYPE2;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		if (convertView == null) {
			if (type == TYPE1) {
				convertView = mInflater.inflate(R.layout.adapter_zixun_top, null);
			}else {
				convertView = mInflater.inflate(R.layout.adapter_news, null);
			}
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.ivTop = (ImageView) convertView.findViewById(R.id.ivTop);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		NewsDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.imgUrl)) {
			int corner = 0;
			if (dto.isTop) {
				corner = 0;
			}else {
				corner = 10;
			}
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, corner);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_default_news);
		}
		if (dto.isTop) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*9/16);
			mHolder.imageView.setLayoutParams(params);
		}

		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}

		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(mContext.getString(R.string.publish_time)+": "+dto.time);
		}

		if (!TextUtils.isEmpty(dto.isToTop)) {
			if (TextUtils.equals(dto.isToTop, "1")) {
				mHolder.ivTop.setVisibility(View.VISIBLE);
			}else {
				mHolder.ivTop.setVisibility(View.GONE);
			}
		}
		
		return convertView;
	}

}
