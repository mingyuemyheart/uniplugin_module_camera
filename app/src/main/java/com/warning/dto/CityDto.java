package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class CityDto implements Parcelable{

	public String disName = null;//区域名称
	public String cityId = null;//城市id
	public String cityName = null;//城市名称
	public String proName = null;//省份名称
	public boolean isSelected = false;//是否已经是关注城市
	public boolean isLocation = false;//是否是定位城市
	public String warningId = null;//预警id


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.disName);
		dest.writeString(this.cityId);
		dest.writeString(this.cityName);
		dest.writeString(this.proName);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isLocation ? (byte) 1 : (byte) 0);
		dest.writeString(this.warningId);
	}

	public CityDto() {
	}

	protected CityDto(Parcel in) {
		this.disName = in.readString();
		this.cityId = in.readString();
		this.cityName = in.readString();
		this.proName = in.readString();
		this.isSelected = in.readByte() != 0;
		this.isLocation = in.readByte() != 0;
		this.warningId = in.readString();
	}

	public static final Creator<CityDto> CREATOR = new Creator<CityDto>() {
		@Override
		public CityDto createFromParcel(Parcel source) {
			return new CityDto(source);
		}

		@Override
		public CityDto[] newArray(int size) {
			return new CityDto[size];
		}
	};
}
