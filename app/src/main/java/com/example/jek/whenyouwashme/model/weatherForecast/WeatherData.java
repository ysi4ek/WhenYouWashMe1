package com.example.jek.whenyouwashme.model.weatherForecast;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jek on 29.08.2017.
 */

public class WeatherData {

    @SerializedName("temp")
    public int temp;

    @SerializedName("temp_main")
    public int tempMain;


    public int temp_max;
    public int pressure;

    @SerializedName("clouds")
    public String clouds; //weather/main:

    @SerializedName("speed")
    public String windSpeed;

    @SerializedName("deg")
    public String windDirection;

    public List<Tag> tags;

    public WeatherData() {
    }
}