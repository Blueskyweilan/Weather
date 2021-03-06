package com.readboy.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("admin_area")
    public String provinceName;

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
