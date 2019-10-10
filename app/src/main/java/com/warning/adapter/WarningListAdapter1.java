package com.warning.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.WarningDto;

public class WarningListAdapter1 extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WarningDto> mArrayList = null;
	public HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
	private int totalCount = 0;
	
	private final class ViewHolder {
		TextView tvName;//预警信息名称
	}
	
	private ViewHolder mHolder = null;
	
	public WarningListAdapter1(Context context, List<WarningDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < mArrayList.size(); i++) {
			if (i == 0) {
				isSelected.put(i, true);
			}else {
				isSelected.put(i, false);
			}
			totalCount += mArrayList.get(i).count;
		}
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
			convertView = mInflater.inflate(R.layout.warning_list_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		if (!TextUtils.equals(dto.type, "999999")) {
			mHolder.tvName.setText(dto.name+"("+dto.count+")");
		}else {
			mHolder.tvName.setText(dto.name+"("+totalCount+")");
		}
		
		if (isSelected.get(position) == true) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
			mHolder.tvName.setBackgroundResource(R.drawable.bg_warning_selected);
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvName.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}
		
		return convertView;
	}

}
