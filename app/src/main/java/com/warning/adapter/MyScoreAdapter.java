package com.warning.adapter;

import java.util.ArrayList;
import java.util.List;

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

/**
 * 我的积分
 * @author shawn_sun
 *
 */

public class MyScoreAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<PhotoDto>();
	
	private final class ViewHolder{
		ImageView ivMethod;
		TextView tvMethod;
		TextView tvScore;
	}
	
	private ViewHolder mHolder = null;
	
	public MyScoreAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_my_score, null);
			mHolder = new ViewHolder();
			mHolder.ivMethod = (ImageView) convertView.findViewById(R.id.ivMethod);
			mHolder.tvMethod = (TextView) convertView.findViewById(R.id.tvMethod);
			mHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		mHolder.tvMethod.setText(dto.scoreName);
		mHolder.tvScore.setText(dto.score+"");
		
		//0上传视频、1注册、 2 登录、 3 评论、 4分享、 5转发、 6举报、7审核通过
		if (TextUtils.equals(dto.scoreType, "0")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_0);
		}else if (TextUtils.equals(dto.scoreType, "2")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_2);
		}else if (TextUtils.equals(dto.scoreType, "3")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_3);
		}else if (TextUtils.equals(dto.scoreType, "4")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_4);
		}else if (TextUtils.equals(dto.scoreType, "5")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_5);
		}else if (TextUtils.equals(dto.scoreType, "6")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_6);
		}else if (TextUtils.equals(dto.scoreType, "7")) {
			mHolder.ivMethod.setImageResource(R.drawable.method_7);
		}
		
		return convertView;
	}

}
