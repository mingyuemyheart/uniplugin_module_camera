package com.warning.adapter;

/**
 * 本地图片预览
 */

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.warning.activity.DisplayPictureActivity;
import com.warning.R;
import com.warning.dto.PhotoDto;

public class PictureAdapter extends BaseAdapter implements OnClickListener{
	
	private Context mContext = null;
	private DisplayPictureActivity activity = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	public int count = 0;//选中个数
	private int width = 0;
	
	private final class ViewHolder{
		ImageView imageView;
		ImageView imageView1;
	}
	
	private ViewHolder mHolder = null;
	
	@SuppressWarnings("deprecation")
	public PictureAdapter(Context context, List<PhotoDto> mArrayList, DisplayPictureActivity activity) {
		mContext = context;
		this.activity = activity;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		
		for (int i = 0; i < mArrayList.size(); i++) {
			if (mArrayList.get(i).isShowCorrect) {
				count++;
			}
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
			convertView = mInflater.inflate(R.layout.adapter_photo_wall, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
			mHolder.imageView1.setOnClickListener(this);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.imageView1.setTag(position);
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.getUrl())) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.getUrl(), null, 0);
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width/3;
			params.height = width/4;
			mHolder.imageView.setLayoutParams(params);
		}
		
		if (dto.isShowCircle) {
			mHolder.imageView1.setImageResource(R.drawable.iv_grid_unselect);
			if (dto.isShowCorrect) {
				mHolder.imageView1.setImageResource(R.drawable.iv_grid_select);
			}else {
				mHolder.imageView1.setImageResource(R.drawable.iv_grid_unselect);
			}
		}
		
		return convertView;
	}

	@Override
	public void onClick(View v) {
		int index = (Integer) v.getTag();
		PhotoDto data = mArrayList.get(index);
		switch (v.getId()) {
		case R.id.imageView1:
				if (data.isShowCorrect) {
					data.isShowCorrect = false;
					count--;
					notifyDataSetChanged();
				}else {
					if (count >= 9) {
						Toast.makeText(mContext, "最多一次可上传9张图片", Toast.LENGTH_SHORT).show();
						return;
					}else {
						data.isShowCorrect = true;
						count++;
					}
				}
				activity.tvTitle.setText("已选中"+count+"个文件");
				notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

}
