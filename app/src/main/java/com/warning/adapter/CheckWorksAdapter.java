package com.warning.adapter;

/**
 * 内容审核
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

public class CheckWorksAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	
	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvTitle;
		TextView tvAddress;
		TextView tvTime;
		TextView tvUserName;
		TextView tvStatus; 
	}
	
	private ViewHolder mHolder = null;
	
	public CheckWorksAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		// menu type count
		return 3;
	}
	
	@Override
	public int getItemViewType(int position) {
		// current menu type
		PhotoDto data = mArrayList.get(position);
		if (TextUtils.equals(data.status, "1")) {//未审核
			return 0;
		}else if (TextUtils.equals(data.status, "2")) {//审核通过
			return 1;
		}else if (TextUtils.equals(data.status, "3")) {//审核拒绝
			return 2;
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_check_works, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvStatus = (TextView) convertView.findViewById(R.id.tvStatus);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.getLocation())) {
			mHolder.tvAddress.setText("拍摄地点："+dto.getLocation());
		}
		
		if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.getUserName())) {
			mHolder.tvUserName.setText(dto.getUserName());
		}else if (!TextUtils.isEmpty(dto.phoneNumber)) {
			if (dto.phoneNumber.length() >= 7) {
				mHolder.tvUserName.setText(dto.phoneNumber.replace(dto.phoneNumber.substring(3, 7), "****"));
			}else {
				mHolder.tvUserName.setText(dto.phoneNumber);
			}
		}
		
		mHolder.tvTitle.setText(dto.getTitle());
		
		if (TextUtils.equals(dto.status, "1")) {//未审核
			mHolder.tvStatus.setText("未审核");
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.yellow));
		}else if (TextUtils.equals(dto.status, "2")) {//审核通过
			mHolder.tvStatus.setText("审核通过");
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.green));
		}else if (TextUtils.equals(dto.status, "3")) {//审核拒绝
			mHolder.tvStatus.setText("审核拒绝");
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.red));
		}
		
		FinalBitmap finalBitmap = FinalBitmap.create(mContext);
		finalBitmap.display(mHolder.imageView, dto.getUrl(), null, 10);
		
		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		if (!TextUtils.isEmpty(dto.getWorkTime())) {
			mHolder.tvTime.setText("拍摄时间："+dto.getWorkTime());
		}
		
		return convertView;
	}

}
