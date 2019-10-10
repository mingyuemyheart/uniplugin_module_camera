package com.warning.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.CityDto;

import java.util.List;

/**
 * 城市选择
 */

public class CityAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<CityDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView imageView;
	}
	
	private ViewHolder mHolder = null;
	
	public CityAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_city, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		CityDto dto = mArrayList.get(position);
		if (TextUtils.equals(dto.proName, dto.cityName)) {
			mHolder.tvName.setText(dto.cityName + " - " +dto.disName);
		}else {
			mHolder.tvName.setText(dto.proName + " - " +dto.cityName + " - " +dto.disName);
		}
		if (dto.isLocation) {
			mHolder.imageView.setVisibility(View.VISIBLE);
			mHolder.imageView.setImageResource(R.drawable.iv_location_red);
		}else {
			if (dto.isSelected) {
				mHolder.imageView.setVisibility(View.VISIBLE);
				mHolder.imageView.setImageResource(R.drawable.iv_city_selected);
			}else {
				mHolder.imageView.setVisibility(View.INVISIBLE);
			}
		}
		
		return convertView;
	}

}
