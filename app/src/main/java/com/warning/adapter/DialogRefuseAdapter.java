package com.warning.adapter;

/**
 * 审核拒绝
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.PhotoDto;

import java.util.ArrayList;
import java.util.List;

public class DialogRefuseAdapter extends BaseAdapter{

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		TextView tvReason;
	}

	private ViewHolder mHolder = null;

	public DialogRefuseAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_dialog_refuse, null);
			mHolder = new ViewHolder();
			mHolder.tvReason = (TextView) convertView.findViewById(R.id.tvReason);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);
		mHolder.tvReason.setText(dto.refuseReason);
		return convertView;
	}

}
