package com.tcode.challenge.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.tcode.challenge.WeatherModel;
import com.tcode.challenge.network.WeatherClient;

import java.util.Map;

public class MainActivityViewModel extends ViewModel {
    private LiveData<WeatherModel> todayWeatherLiveData;
    private LiveData<Map<Integer, WeatherModel>> next5DayWeatherMapLiveData;

    public MainActivityViewModel() {
        todayWeatherLiveData = WeatherClient.getInstance().getTodayWeather();
        next5DayWeatherMapLiveData = WeatherClient.getInstance().getNext5DayWeatherMap();
    }

    public LiveData<WeatherModel> getTodayWeatherLiveData() {
        return todayWeatherLiveData;
    }

    public LiveData<Map<Integer, WeatherModel>> getNext5DayWeatherMapLiveData() {
        return next5DayWeatherMapLiveData;
    }

    public void fetchTodayAsync() {
        WeatherClient.getInstance().fetchAsyncWeatherToday();
    }

    public void fetchNext5DaysAsync() {
        WeatherClient.getInstance().fetchAsyncWeather5Days();
    }
}
