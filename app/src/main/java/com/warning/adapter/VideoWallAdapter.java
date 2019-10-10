package com.warning.adapter;

/**
 * 直报列表
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.PhotoDto;

public class VideoWallAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	private final int TYPE1 = 0, TYPE2 = 1;//两种布局标识
	private int width = 0;

	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvTitle;
		TextView tvPosition;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public VideoWallAdapter(Context context, List<PhotoDto> mArrayList) {
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
				convertView = mInflater.inflate(R.layout.adapter_video_wall_top, null);
			}else if (type == TYPE2){
				convertView = mInflater.inflate(R.layout.adapter_video_wall, null);
			}
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
			mHolder.tvTitle.setVisibility(View.VISIBLE);
		}else {
			mHolder.tvTitle.setVisibility(View.GONE);
		}
		
		if (!TextUtils.isEmpty(dto.location)) {
			mHolder.tvPosition.setText("拍摄地点："+dto.location);
			mHolder.tvPosition.setVisibility(View.VISIBLE);
		}else {
			mHolder.tvPosition.setVisibility(View.GONE);
		}
		
		if (!TextUtils.isEmpty(dto.workTime)) {
			mHolder.tvTime.setText("拍摄时间："+dto.workTime);
		}

		if (!TextUtils.isEmpty(dto.url)) {
		    int corner = 0;
		    if (dto.isTop) {
		    	corner = 0;
			}else {
		    	corner = 10;
			}
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.url, null, corner);

			if (dto.isTop) {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*9/16);
				mHolder.imageView.setLayoutParams(params);
			}
		}

		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

}
