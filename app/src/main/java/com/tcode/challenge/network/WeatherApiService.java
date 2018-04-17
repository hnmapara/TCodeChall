package com.tcode.challenge.network;

import com.tcode.challenge.WeatherModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit Api Service to parse json response
 */
public interface WeatherApiService {

    @GET("future_{nthday}.json")
    Call<WeatherModel> getNthDayWeather(@Path("nthday") int nth);

    @GET("current.json")
    Call<WeatherModel> getTodaysWeather();

}
