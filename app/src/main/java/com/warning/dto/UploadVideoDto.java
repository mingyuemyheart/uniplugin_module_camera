package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传视频
 * @author shawn_sun
 *
 */

public class UploadVideoDto implements Parcelable{

	public String weatherType;//天气类型,wt01雪，wt02雨，wt03冰雹，wt04晴，wt05霾，wt06大风，wt07沙尘
	public String weatherName;//天气名称
	public String eventType;//事件类型,et01自然灾害，et02事故灾害，et03公共卫生，et04社会安全
	public String eventName;//事件名称
	public boolean isSelected = false;//是否已选择
	public String count;
	public List<UploadVideoDto> weatherList = new ArrayList<UploadVideoDto>();//热门推荐list


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.weatherType);
		dest.writeString(this.weatherName);
		dest.writeString(this.eventType);
		dest.writeString(this.eventName);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
		dest.writeString(this.count);
		dest.writeTypedList(this.weatherList);
	}

	public UploadVideoDto() {
	}

	protected UploadVideoDto(Parcel in) {
		this.weatherType = in.readString();
		this.weatherName = in.readString();
		this.eventType = in.readString();
		this.eventName = in.readString();
		this.isSelected = in.readByte() != 0;
		this.count = in.readString();
		this.weatherList = in.createTypedArrayList(UploadVideoDto.CREATOR);
	}

	public static final Creator<UploadVideoDto> CREATOR = new Creator<UploadVideoDto>() {
		@Override
		public UploadVideoDto createFromParcel(Parcel source) {
			return new UploadVideoDto(source);
		}

		@Override
		public UploadVideoDto[] newArray(int size) {
			return new UploadVideoDto[size];
		}
	};
}
