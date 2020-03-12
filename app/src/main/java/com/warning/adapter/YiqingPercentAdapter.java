package com.warning.adapter;

import android.content.Context;
import android.graphics.Color;
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
 * 疫情比例适配器
 * @author shawn_sun
 */
public class YiqingPercentAdapter extends BaseAdapter{

	private Context context;
	private LayoutInflater mInflater;
	private List<YiqingDto> mArrayList;

	private static final class ViewHolder {
		TextView tvNameZn,tvCount,tvPopulation,tvRatio,tvDeathCount,tvInfection_mortality;
	}

	public YiqingPercentAdapter(Context context, List<YiqingDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_yiqing_percent, null);
			mHolder = new ViewHolder();
			mHolder.tvNameZn = convertView.findViewById(R.id.tvNameZn);
			mHolder.tvPopulation = convertView.findViewById(R.id.tvPopulation);
			mHolder.tvRatio = convertView.findViewById(R.id.tvRatio);
			mHolder.tvCount = convertView.findViewById(R.id.tvCount);
			mHolder.tvDeathCount = convertView.findViewById(R.id.tvDeathCount);
			mHolder.tvInfection_mortality = convertView.findViewById(R.id.tvInfection_mortality);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		YiqingDto dto = mArrayList.get(position);

		if (position % 2 == 0) {
			convertView.setBackgroundColor(Color.WHITE);
		} else {
			convertView.setBackgroundColor(0xff9FCFF7);
		}

		if (!TextUtils.isEmpty(dto.nameZn)) {
			mHolder.tvNameZn.setText(dto.nameZn);
		} else {
			mHolder.tvNameZn.setText("");
		}

		if (!TextUtils.isEmpty(dto.count)) {
			mHolder.tvCount.setText(dto.count);
		} else {
			mHolder.tvCount.setText("");
		}

		if (!TextUtils.isEmpty(dto.ratio)) {
			mHolder.tvRatio.setText(dto.ratio);
		} else {
			mHolder.tvRatio.setText("");
		}

		if (!TextUtils.isEmpty(dto.population)) {
			mHolder.tvPopulation.setText(dto.population);
		} else {
			mHolder.tvPopulation.setText("");
		}

		if (!TextUtils.isEmpty(dto.infection_mortality)) {
			mHolder.tvInfection_mortality.setText(dto.infection_mortality);
		} else {
			mHolder.tvInfection_mortality.setText("");
		}

		if (!TextUtils.isEmpty(dto.death_count)) {
			mHolder.tvDeathCount.setText(dto.death_count);
		} else {
			mHolder.tvDeathCount.setText("");
		}

		return convertView;
	}

}
