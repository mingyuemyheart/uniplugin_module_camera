package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsDto implements Parcelable{

	public String title;
	public String time;
	public String content;
	public String url;
	public String imgUrl;
	public String isToTop;//是否置顶，1置顶
	public String lat;
	public String lng;
	public boolean isTop = false;//是否显示大图
	public String show_type;

	public NewsDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.title);
		dest.writeString(this.time);
		dest.writeString(this.content);
		dest.writeString(this.url);
		dest.writeString(this.imgUrl);
		dest.writeString(this.isToTop);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeByte(this.isTop ? (byte) 1 : (byte) 0);
		dest.writeString(this.show_type);
	}

	protected NewsDto(Parcel in) {
		this.title = in.readString();
		this.time = in.readString();
		this.content = in.readString();
		this.url = in.readString();
		this.imgUrl = in.readString();
		this.isToTop = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.isTop = in.readByte() != 0;
		this.show_type = in.readString();
	}

	public static final Creator<NewsDto> CREATOR = new Creator<NewsDto>() {
		@Override
		public NewsDto createFromParcel(Parcel source) {
			return new NewsDto(source);
		}

		@Override
		public NewsDto[] newArray(int size) {
			return new NewsDto[size];
		}
	};
}
