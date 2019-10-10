package com.warning.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.UploadVideoDto;

import java.util.List;

/**
 * 天气类型
 * @author shawn_sun
 *
 */

public class WeatherTypeAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<UploadVideoDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvType;
		LinearLayout llContent;
	}
	
	private ViewHolder mHolder = null;
	
	public WeatherTypeAdapter(Context context, List<UploadVideoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_weather_type, null);
			mHolder = new ViewHolder();
			mHolder.tvType = (TextView) convertView.findViewById(R.id.tvType);
			mHolder.llContent = (LinearLayout) convertView.findViewById(R.id.llContent);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		UploadVideoDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.weatherName)) {
			mHolder.tvType.setText(dto.weatherName);
		}
		if (dto.isSelected) {
			mHolder.llContent.setBackgroundResource(R.drawable.bg_hot_recmmond_selected);
			mHolder.tvType.setTextColor(Color.WHITE);
		}else {
			mHolder.llContent.setBackgroundResource(R.drawable.bg_hot_recmmond_unselected);
			mHolder.tvType.setTextColor(mContext.getResources().getColor(R.color.text_color4));
		}
		
		return convertView;
	}

}
