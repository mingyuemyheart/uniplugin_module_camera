package com.warning.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.UploadVideoDto;

/**
 * 事件类型
 * @author shawn_sun
 *
 */

public class EventTypeAdapter extends BaseAdapter {
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<UploadVideoDto> mArrayList = new ArrayList<UploadVideoDto>();
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvType;
	}
	
	private ViewHolder mHolder = null;
	
	public EventTypeAdapter(Context context, List<UploadVideoDto> mArrayList) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_event_type, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvType = (TextView) convertView.findViewById(R.id.tvType);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		UploadVideoDto dto = mArrayList.get(position);
		//11000自然灾害，12000事故灾难，13000公共卫生，14000社会安全
		if (TextUtils.equals(dto.eventType, "11000")) {
			if (dto.isSelected) {
				mHolder.imageView.setImageResource(R.drawable.iv_nature_selected);
			}else {
				mHolder.imageView.setImageResource(R.drawable.iv_nature_unselected);
			}
		}else if (TextUtils.equals(dto.eventType, "12000")) {
			if (dto.isSelected) {
				mHolder.imageView.setImageResource(R.drawable.iv_disaster_selected);
			}else {
				mHolder.imageView.setImageResource(R.drawable.iv_disaster_unselected);
			}
		}else if (TextUtils.equals(dto.eventType, "13000")) {
			if (dto.isSelected) {
				mHolder.imageView.setImageResource(R.drawable.iv_health_selected);
			}else {
				mHolder.imageView.setImageResource(R.drawable.iv_health_unselected);
			}
		}else if (TextUtils.equals(dto.eventType, "14000")) {
			if (dto.isSelected) {
				mHolder.imageView.setImageResource(R.drawable.iv_safe_selected);
			}else {
				mHolder.imageView.setImageResource(R.drawable.iv_safe_unselected);
			}
		}
		
		mHolder.tvType.setText(dto.eventName);
		if (dto.isSelected) {
			mHolder.tvType.setTextColor(Color.WHITE);
		}else {
			mHolder.tvType.setTextColor(mContext.getResources().getColor(R.color.text_color4));
		}
		
		return convertView;
	}

}
