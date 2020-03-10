package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class YiqingDto implements Parcelable {

    public String count, death_count, nameEn, nameZn, mapid;
    public double lat, lng;

    public YiqingDto() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.count);
        dest.writeString(this.death_count);
        dest.writeString(this.nameEn);
        dest.writeString(this.nameZn);
        dest.writeString(this.mapid);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }

    protected YiqingDto(Parcel in) {
        this.count = in.readString();
        this.death_count = in.readString();
        this.nameEn = in.readString();
        this.nameZn = in.readString();
        this.mapid = in.readString();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    public static final Creator<YiqingDto> CREATOR = new Creator<YiqingDto>() {
        @Override
        public YiqingDto createFromParcel(Parcel source) {
            return new YiqingDto(source);
        }

        @Override
        public YiqingDto[] newArray(int size) {
            return new YiqingDto[size];
        }
    };
}
