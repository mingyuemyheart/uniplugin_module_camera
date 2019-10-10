package com.warning.adapter;

/**
 * 未上传
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warning.activity.MyUploadActivity;
import com.warning.R;
import com.warning.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MyUnuploadAdapter extends BaseAdapter implements View.OnClickListener {
	
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public boolean isShowDelete = false;
	private MyUploadActivity activity = null;

	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvTitle;
		TextView tvPosition;
		TextView tvTime;
		ImageView ivDelete;
	}
	
	private ViewHolder mHolder = null;
	
	public MyUnuploadAdapter(MyUploadActivity activity, List<PhotoDto> mArrayList) {
		this.activity = activity;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_unupload, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.ivDelete = (ImageView) convertView.findViewById(R.id.ivDelete);
			mHolder.ivDelete.setOnClickListener(this);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		mHolder.ivDelete.setTag(position);
		
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
			try {
				mHolder.tvTime.setText("拍摄时间："+sdf2.format(sdf1.parse(dto.getWorkTime())));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		if (!TextUtils.isEmpty(dto.url)) {
			FinalBitmap finalBitmap = FinalBitmap.create(activity);
			finalBitmap.display(mHolder.imageView, dto.url, null, 10);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_default_news);
		}

		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		if (isShowDelete) {
			mHolder.ivDelete.setVisibility(View.VISIBLE);
			if (dto.isDelete) {
				mHolder.ivDelete.setImageResource(R.drawable.umeng_update_btn_check_on_holo_light);
			}else {
				mHolder.ivDelete.setImageResource(R.drawable.umeng_update_btn_check_off_focused_holo_light);
			}
		}else {
			mHolder.ivDelete.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	@Override
	public void onClick(View v) {
		try {
			int index = (Integer) v.getTag();
			PhotoDto data = mArrayList.get(index);
			switch (v.getId()) {
				case R.id.ivDelete:
					if (data.isDelete) {
						data.isDelete = false;
						mHolder.ivDelete.setImageResource(R.drawable.umeng_update_btn_check_off_focused_holo_light);
					}else {
						data.isDelete = true;
						mHolder.ivDelete.setImageResource(R.drawable.umeng_update_btn_check_on_holo_light);
					}
					notifyDataSetChanged();

					//判断是否显示删除按钮
					boolean isShowDelete = false;
					for (int i = 0; i < mArrayList.size(); i++) {
						if (mArrayList.get(i).isDelete) {
							isShowDelete = true;
						}
					}
					if (isShowDelete) {
						activity.tvControl.setVisibility(View.VISIBLE);
					}else {
						activity.tvControl.setVisibility(View.INVISIBLE);
					}
					break;

				default:
					break;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

}
