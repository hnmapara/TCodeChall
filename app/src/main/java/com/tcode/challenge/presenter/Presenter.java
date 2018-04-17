package com.tcode.challenge.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tcode.challenge.Utility;
import com.tcode.challenge.network.WeatherClient;
import com.tcode.challenge.WeatherModel;
import com.tcode.challenge.ui.MainActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by mapara on 2/25/18.
 */
public class Presenter {
    private static final String TAG = "Presenter";

    public interface IPresenterImpl {
        void showToday(WeatherModel weatherModel);
        void showCloudiness(boolean show);
        void showNext5DaysAndSD(Collection<WeatherModel> weatherModels, double standarDeviation);
        void reset();
    }
    private IPresenterImpl mPresenterImpl;

    public Presenter(@NonNull IPresenterImpl presenterImpl) {
        mPresenterImpl = presenterImpl;
    }

    public void showToday() {
        WeatherModel today = WeatherClient.getInstance().getTodayWeather();
        if (today != null) {
            mPresenterImpl.showToday(today);
            mPresenterImpl.showCloudiness(today.getClouds().getCloudinessInPercentage() > 50);

        } else {
            fetchToday();
        }
    }
    private void fetchToday() {
        WeatherClient.getInstance().fetchAsyncWeatherToday(new WeatherClient.IOnFinish() {
            @Override
            public void onSuccess(boolean isSuccess) {
                WeatherModel today = WeatherClient.getInstance().getTodayWeather();;
                if (mPresenterImpl != null) {
                    mPresenterImpl.showToday(today);
                    mPresenterImpl.showCloudiness(today.getClouds().getCloudinessInPercentage() > 50);
                }
            }
        });
    }

    public void showNext5days() {
        Map<Integer, WeatherModel> dayToWeatherMap = WeatherClient.getInstance().getNext5DayWeatherMap();

        if (dayToWeatherMap != null) {
            showNext5Days(dayToWeatherMap.values());
        }
    }

    public void fetchNext5Days() {
        WeatherClient.getInstance().fetchAsyncWeather5Days(new WeatherClient.IOnFinish() {
            @Override
            public void onSuccess(boolean isSuccess) {
                if (isSuccess) {
                    Map<Integer, WeatherModel> dayToWeatherMap = WeatherClient.getInstance().getNext5DayWeatherMap();
                    showNext5Days(dayToWeatherMap.values());
                } else {
                    Log.e(TAG, "fetchNext5Days : error");
                }
            }
        });
    }

    public void setContext(@Nullable MainActivity activity) {
        Log.d(TAG, "setContext" + activity);
        mPresenterImpl = activity;
    }

    private void showNext5Days(@NonNull final Collection<WeatherModel> next5daysWeather) {
        if (mPresenterImpl != null) {
            List<Float> degrees = new ArrayList<>(next5daysWeather.size());
            for(WeatherModel wm : next5daysWeather) {
                degrees.add(wm.getWeather().getTempInC());
            }
            mPresenterImpl.showNext5DaysAndSD(next5daysWeather, Utility.calculateStandardDeviation(degrees));
        }
    }

    public void cancel() {
        Log.d(TAG, "cancel");
        mPresenterImpl = null;
    }

    public void reset() {
        Log.d(TAG, "reset");
        WeatherClient.getInstance().reset();
        mPresenterImpl.reset();
    }
}

