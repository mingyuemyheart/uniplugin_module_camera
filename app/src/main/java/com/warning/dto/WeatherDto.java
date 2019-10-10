package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherDto implements Parcelable{

	//平滑曲线
	public int hourlyTemp = 0;//逐小时温度
	public String hourlyTime = null;//逐小时时间
	public int hourlyCode = 0;//天气现象编号
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	
	//列表、趋势
	public String week = null;//周几
	public String date = null;//日期
	public String lowPhe = null;//晚上天气现象
	public int lowPheCode = 0;//晚上天气现象编号
	public int lowTemp = 0;//最低气温
	public float lowX = 0;//最低温度x轴坐标点
	public float lowY = 0;//最低温度y轴坐标点
	public String highPhe = null;//白天天气现象
	public int highPheCode = 0;//白天天气现象编号
	public int highTemp = 0;//最高气温
	public float highX = 0;//最高温度x轴坐标点
	public float highY = 0;//最高温度y轴坐标点


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.hourlyTemp);
		dest.writeString(this.hourlyTime);
		dest.writeInt(this.hourlyCode);
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
		dest.writeString(this.week);
		dest.writeString(this.date);
		dest.writeString(this.lowPhe);
		dest.writeInt(this.lowPheCode);
		dest.writeInt(this.lowTemp);
		dest.writeFloat(this.lowX);
		dest.writeFloat(this.lowY);
		dest.writeString(this.highPhe);
		dest.writeInt(this.highPheCode);
		dest.writeInt(this.highTemp);
		dest.writeFloat(this.highX);
		dest.writeFloat(this.highY);
	}

	public WeatherDto() {
	}

	protected WeatherDto(Parcel in) {
		this.hourlyTemp = in.readInt();
		this.hourlyTime = in.readString();
		this.hourlyCode = in.readInt();
		this.x = in.readFloat();
		this.y = in.readFloat();
		this.week = in.readString();
		this.date = in.readString();
		this.lowPhe = in.readString();
		this.lowPheCode = in.readInt();
		this.lowTemp = in.readInt();
		this.lowX = in.readFloat();
		this.lowY = in.readFloat();
		this.highPhe = in.readString();
		this.highPheCode = in.readInt();
		this.highTemp = in.readInt();
		this.highX = in.readFloat();
		this.highY = in.readFloat();
	}

	public static final Creator<WeatherDto> CREATOR = new Creator<WeatherDto>() {
		@Override
		public WeatherDto createFromParcel(Parcel source) {
			return new WeatherDto(source);
		}

		@Override
		public WeatherDto[] newArray(int size) {
			return new WeatherDto[size];
		}
	};
}
