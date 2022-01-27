package com.camera.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.camera.R;
import com.camera.dto.PhotoDto;
import com.camera.util.CommonUtil;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;

/**
 * 本地图片预览
 */
public class DisplayPictureAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<PhotoDto> mArrayList;

	private final class ViewHolder {
		ImageView imageView,imageView1;
	}
	
	public DisplayPictureAdapter(Context context, ArrayList<PhotoDto> mArrayList) {
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
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_display_picture, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.imageView1 = convertView.findViewById(R.id.imageView1);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.imageView1.setTag(position);

		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.url)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.url, null, 0);
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = CommonUtil.widthPixels(mContext) / 3;
			params.height = CommonUtil.widthPixels(mContext) / 3;
			mHolder.imageView.setLayoutParams(params);
		}

		if (dto.isSelected) {
			mHolder.imageView1.setImageResource(R.drawable.bg_select_orange);
		}else {
			mHolder.imageView1.setImageResource(R.drawable.bg_select_gray);
		}

		mHolder.imageView1.setOnClickListener(v -> {
			dto.isSelected = !dto.isSelected;
			notifyDataSetChanged();
			if (selectListener != null) {
				selectListener.onSelected();
			}
		});
		
		return convertView;
	}

	private SelectListener selectListener;

	public void setSelectListener(SelectListener selectListener) {
		this.selectListener = selectListener;
	}

	public interface SelectListener {
		void onSelected();
	}

}
