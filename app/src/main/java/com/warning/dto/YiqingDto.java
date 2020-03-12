package com.warning.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class YiqingDto implements Parcelable {

    public String count, death_count, nameEn, nameZn, mapid, ratio, death_rate, infection_mortality, population;
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
        dest.writeString(this.ratio);
        dest.writeString(this.death_rate);
        dest.writeString(this.infection_mortality);
        dest.writeString(this.population);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }

    protected YiqingDto(Parcel in) {
        this.count = in.readString();
        this.death_count = in.readString();
        this.nameEn = in.readString();
        this.nameZn = in.readString();
        this.mapid = in.readString();
        this.ratio = in.readString();
        this.death_rate = in.readString();
        this.infection_mortality = in.readString();
        this.population = in.readString();
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
