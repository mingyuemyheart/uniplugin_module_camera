package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class WarningDto implements Parcelable{

	public String name;// 预警名称
	public String html;// 详情需要用到的html
	public String time;// 预警发布时间
	public String lat;// 纬度
	public String lng;// 经度
	public String type;//预警类型，如11B09
	public String color;// 预警颜色,红橙黄蓝，id的后两位
	public String provinceId;//省份id
	public String item0;
	public int count;

	public String cityName = null;//城市名称
	public String cityId = null;//城市id
	public String temp;//温度
	public String bodyTemp;//体感温度
	public String humidity;//相对湿度
	public String windDir;//风向
	public String windForce;//风力
	public String warningId = null;//预警id
	public String pheCode = null;//天气现象编号
	public String publishTime = null;//预报发布时间
	
	//一周预报信息
	public List<WeatherDto> foreList = new ArrayList<>();
	
	public String colorName;
	public String nationCount;
	public String proCount;
	public String cityCount;
	public String disCount;


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.html);
		dest.writeString(this.time);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeString(this.type);
		dest.writeString(this.color);
		dest.writeString(this.provinceId);
		dest.writeString(this.item0);
		dest.writeInt(this.count);
		dest.writeString(this.cityName);
		dest.writeString(this.cityId);
		dest.writeString(this.temp);
		dest.writeString(this.bodyTemp);
		dest.writeString(this.humidity);
		dest.writeString(this.windDir);
		dest.writeString(this.windForce);
		dest.writeString(this.warningId);
		dest.writeString(this.pheCode);
		dest.writeString(this.publishTime);
		dest.writeList(this.foreList);
		dest.writeString(this.colorName);
		dest.writeString(this.nationCount);
		dest.writeString(this.proCount);
		dest.writeString(this.cityCount);
		dest.writeString(this.disCount);
	}

	public WarningDto() {
	}

	protected WarningDto(Parcel in) {
		this.name = in.readString();
		this.html = in.readString();
		this.time = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.type = in.readString();
		this.color = in.readString();
		this.provinceId = in.readString();
		this.item0 = in.readString();
		this.count = in.readInt();
		this.cityName = in.readString();
		this.cityId = in.readString();
		this.temp = in.readString();
		this.bodyTemp = in.readString();
		this.humidity = in.readString();
		this.windDir = in.readString();
		this.windForce = in.readString();
		this.warningId = in.readString();
		this.pheCode = in.readString();
		this.publishTime = in.readString();
		this.foreList = new ArrayList<>();
		in.readList(this.foreList, WeatherDto.class.getClassLoader());
		this.colorName = in.readString();
		this.nationCount = in.readString();
		this.proCount = in.readString();
		this.cityCount = in.readString();
		this.disCount = in.readString();
	}

	public static final Creator<WarningDto> CREATOR = new Creator<WarningDto>() {
		@Override
		public WarningDto createFromParcel(Parcel source) {
			return new WarningDto(source);
		}

		@Override
		public WarningDto[] newArray(int size) {
			return new WarningDto[size];
		}
	};
}
