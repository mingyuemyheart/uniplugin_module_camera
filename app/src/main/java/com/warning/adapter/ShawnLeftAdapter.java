package com.warning.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.warning.R;
import com.warning.activity.WarningDetailActivity;
import com.warning.common.CONST;
import com.warning.dto.WarningDto;
import com.warning.util.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订阅城市列表
 */
public class ShawnLeftAdapter extends BaseAdapter implements OnClickListener{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;
	public boolean isDelete = false;//是否是删除状态
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH", Locale.CHINA);
	
	private final class ViewHolder {
		ImageView ivDelete,ivWarning,ivPhe;
		TextView tvCityName,tvTemp;
	}
	
	public ShawnLeftAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_layout_left, null);
			mHolder = new ViewHolder();
			mHolder.ivDelete = convertView.findViewById(R.id.ivDelete);
			mHolder.ivDelete.setOnClickListener(this);
			mHolder.ivWarning = convertView.findViewById(R.id.ivWarning);
			mHolder.ivWarning.setOnClickListener(this);
			mHolder.tvCityName = convertView.findViewById(R.id.tvCityName);
			mHolder.tvTemp = convertView.findViewById(R.id.tvTemp);
			mHolder.ivPhe = convertView.findViewById(R.id.ivPhe);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.ivWarning.setTag(position);
		mHolder.ivDelete.setTag(position);
		
		WarningDto dto = mArrayList.get(position);
        Bitmap bitmap = null;
        if (dto.color != null && dto.type != null) {
        	if (dto.color.equals(CONST.blue[0])) {
        		bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
        		if (bitmap == null) {
        			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
        		}
        	}else if (dto.color.equals(CONST.yellow[0])) {
        		bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
        		if (bitmap == null) {
        			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
        		}
        	}else if (dto.color.equals(CONST.orange[0])) {
        		bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
        		if (bitmap == null) {
        			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
        		}
        	}else if (dto.color.equals(CONST.red[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
				if (bitmap == null) {
					bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
				}
			}else if (dto.color.equals(CONST.unknown[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/default"+CONST.imageSuffix);
			}
        	mHolder.ivWarning.setImageBitmap(bitmap);
        	mHolder.ivWarning.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivWarning.setVisibility(View.INVISIBLE);
		}
		
		mHolder.tvCityName.setText(dto.cityName);
		
		if (!TextUtils.isEmpty(dto.temp)) {
			mHolder.tvTemp.setText(dto.temp+mContext.getString(R.string.unit_degree));
		}
		
		if (!TextUtils.isEmpty(dto.pheCode)) {
			Drawable drawable;
			int current = Integer.valueOf(sdf1.format(new Date()));
			if (current >= 5 && current < 17) {
				drawable = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable);
			}else {
				drawable = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable_night);
			}
			drawable.setLevel(Integer.valueOf(dto.pheCode));
			mHolder.ivPhe.setBackground(drawable);
		}
		
		if (position != 0) {//第一个为定位城市，不可删除
			if (isDelete) {
				mHolder.ivDelete.setVisibility(View.VISIBLE);
			}else {
				mHolder.ivDelete.setVisibility(View.GONE);
			}
			mHolder.ivDelete.setImageResource(R.drawable.iv_city_delete);
		}else {
			mHolder.ivDelete.setVisibility(View.VISIBLE);
			mHolder.ivDelete.setImageResource(R.drawable.iv_location_red);
		}
		
		return convertView;
	}

	@Override
	public void onClick(View v) {
		try {
			int index = (Integer) v.getTag();
			WarningDto data = mArrayList.get(index);
			switch (v.getId()) {
			case R.id.ivDelete:
				mArrayList.remove(index);
				notifyDataSetChanged();
				
				//发送广播删除添加的view
				Intent intent = new Intent();
				intent.setAction(CONST.BROADCAST_REMOVE);
				intent.putExtra("index", index);
				mContext.sendBroadcast(intent);
				break;
			case R.id.ivWarning:
				intent = new Intent(mContext, WarningDetailActivity.class);
				intent.putExtra("url", data.html);
				mContext.startActivity(intent);
				break;
				
			default:
				break;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

}
