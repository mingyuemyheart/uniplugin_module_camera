package com.warning.adapter;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.PhotoDto;

/**
 * 积分详情
 * @author shawn_sun
 *
 */

public class ScoreDetailAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<PhotoDto>();
	
	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvTitle;
		TextView tvPosition;
		TextView tvTime;
		ImageView ivCheck;
		TextView tvScore;
		LinearLayout llShare;
		TextView tvShare;
	}
	
	private ViewHolder mHolder = null;
	
	public ScoreDetailAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_score_detail, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.ivCheck = (ImageView) convertView.findViewById(R.id.ivCheck);
			mHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
			mHolder.llShare = (LinearLayout) convertView.findViewById(R.id.llShare);
			mHolder.tvShare = (TextView) convertView.findViewById(R.id.tvShare);
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
		
		if (!TextUtils.isEmpty(dto.getWorkTime())) {
			mHolder.tvTime.setText("拍摄时间："+dto.getWorkTime());
		}
		
		FinalBitmap finalBitmap = FinalBitmap.create(mContext);
		finalBitmap.display(mHolder.imageView, dto.getUrl(), null, 10);
		
		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}
		
		if (TextUtils.equals(dto.scoreType, "0")) {//视频上传
			if (TextUtils.equals(dto.status, "1")) {//未审核
				mHolder.ivCheck.setImageResource(R.drawable.iv_check1);
			}else if (TextUtils.equals(dto.status, "2")) {//审核通过
				mHolder.ivCheck.setImageResource(R.drawable.iv_check2);
			}else if (TextUtils.equals(dto.status, "3")) {//审核拒绝
				mHolder.ivCheck.setImageResource(R.drawable.iv_check3);
			}
			mHolder.ivCheck.setVisibility(View.VISIBLE);
			mHolder.tvScore.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivCheck.setVisibility(View.GONE);
			if (TextUtils.equals(dto.scoreType, "5")) {//视频转发
				mHolder.tvScore.setVisibility(View.GONE);
				if (!TextUtils.isEmpty(dto.shareTimes)) {
					mHolder.llShare.setVisibility(View.VISIBLE);
					mHolder.tvShare.setText(dto.shareTimes);
				}
			}else {
				mHolder.tvScore.setVisibility(View.VISIBLE);
				mHolder.llShare.setVisibility(View.GONE);
			}
		}
		
		if (dto.score >= 0) {
			mHolder.tvScore.setText("+"+dto.score);
			mHolder.tvScore.setBackgroundResource(R.drawable.bg_score_add);
		}else {
			mHolder.tvScore.setText(""+dto.score);
			mHolder.tvScore.setBackgroundResource(R.drawable.bg_score_minuse);
		}

		return convertView;
	}

}
