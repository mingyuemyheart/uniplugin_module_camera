package com.warning.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.warning.R;
import com.warning.dto.YiqingDto;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 疫情适配器
 * @author shawn_sun
 */
public class YiqingAdapter extends BaseAdapter{

	private Context context;
	private LayoutInflater mInflater;
	private List<YiqingDto> mArrayList;
	private DecimalFormat df = new DecimalFormat("###,###,###");

	private static final class ViewHolder {
		TextView tvNameZn, tvNameEn,tvCount,tvDeathCount;
	}

	public YiqingAdapter(Context context, List<YiqingDto> mArrayList) {
		this.context = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_yiqing, null);
			mHolder = new ViewHolder();
			mHolder.tvNameZn = convertView.findViewById(R.id.tvNameZn);
			mHolder.tvNameEn = convertView.findViewById(R.id.tvNameEn);
			mHolder.tvCount = convertView.findViewById(R.id.tvCount);
			mHolder.tvDeathCount = convertView.findViewById(R.id.tvDeathCount);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		YiqingDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.nameZn)) {
			mHolder.tvNameZn.setText(dto.nameZn);
		}

		if (!TextUtils.isEmpty(dto.nameEn)) {
			mHolder.tvNameEn.setText(dto.nameEn);
		}

		if (!TextUtils.isEmpty(dto.count)) {
			mHolder.tvCount.setText(df.format(Long.valueOf(dto.count)));
		}

		if (!TextUtils.isEmpty(dto.death_count)) {
			mHolder.tvDeathCount.setText(df.format(Long.valueOf(dto.death_count)));
		}

		return convertView;
	}

}
