package com.warning.adapter;

/**
 * 我的消息
 * @author shawn_sun
 *
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
import com.warning.util.CommonUtil;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

public class MyMsgAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;

	private final class ViewHolder {
		ImageView imageView;
		TextView tvName;
		TextView tvTime;
		TextView tvContent;
		ImageView ivPoint;
	}

	private ViewHolder mHolder = null;

	public MyMsgAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_my_msg, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
			mHolder.ivPoint = (ImageView) convertView.findViewById(R.id.ivPoint);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.msgUrl)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.msgUrl, null, (int)CommonUtil.dip2px(mContext, 35));
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_arc_blue);
		}
		if (!TextUtils.isEmpty(dto.msgName)) {
			mHolder.tvName.setText(dto.msgName);
		}
		if (!TextUtils.isEmpty(dto.msgTime)) {
			mHolder.tvTime.setText(dto.msgTime);
		}
		if (!TextUtils.isEmpty(dto.msgContent)) {
			mHolder.tvContent.setText(dto.msgContent);
		}
		if (TextUtils.equals(dto.isRead, "0")) {//未读
			mHolder.ivPoint.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivPoint.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

}
