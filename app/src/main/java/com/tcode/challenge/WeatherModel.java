package com.tcode.challenge;

/**
 * Created by mapara on 2/24/18.
 */

import com.google.gson.annotations.SerializedName;

/**
 *
     {
        coord: {
            lon: -122.42,
            lat: 37.77
        },
        weather: {
            temp: 14.77,
            pressure: 1007,
            humidity: 85
        },
        wind: {
            speed: 0.51,
            deg: 284
        },
        rain: {
            3h: 1
        },
        clouds: {
            cloudiness: 65
        },
        name: "San Francisco"
     }
 */
public class WeatherModel {

    @SerializedName("name")
    private String mName;

    @SerializedName("weather")
    private Weather mWeather;

    @SerializedName("wind")
    private Wind mWind;

    @SerializedName("clouds")
    private Clouds mClouds;

    public Weather getWeather() {
        return mWeather;
    }

    public Wind getWind() {
        return mWind;
    }

    public Clouds getClouds() {
        return mClouds;
    }

    public String getName() {
        return mName;
    }

    public static class Weather{
        @SerializedName("temp")
        private float mTempInC;

        public float getTempInC() {
            return mTempInC;
        }

        @Override
        public String toString() {
            return "Weather{" +
                    "mTempInC=" + mTempInC +
                    '}';
        }
    }

    public static class Clouds {
        @SerializedName("cloudiness")
        private int mCloudinessInPercentage;

        public int getCloudinessInPercentage() {
            return mCloudinessInPercentage;
        }

        @Override
        public String toString() {
            return "Clouds{" +
                    "mCloudinessInPercentage=" + mCloudinessInPercentage +
                    '}';
        }
    }
    public static class Wind {
        @SerializedName("speed")
        private float mSpeed;

        public float getSpeed() {
            return mSpeed;
        }

        @Override
        public String toString() {
            return "Wind{" +
                    "mSpeed=" + mSpeed +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "WeatherModel{" +
                "mName='" + mName + '\'' +
                ", mWeather=" + mWeather +
                ", mWind=" + mWind +
                ", mClouds=" + mClouds +
                '}';
    }
}
