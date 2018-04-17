package com.tcode.challenge.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.tcode.challenge.WeatherModel;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mapara on 2/25/18.
 */

public class WeatherClient {
    private static final String TAG = "WeatherClient";
    private static final String BASE_URL = "http://twitter-code-challenge.s3-website-us-east-1.amazonaws.com/";

    private static final String THREADPOOL_PREFIX = TAG + "-ThreadPool";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    //The number of threads to keep in the pool
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    private static ScheduledThreadPoolExecutor sExecutor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, THREADPOOL_PREFIX);
        }
    });

    private static WeatherClient mInstance;

    private WeatherApiService mRetrofitService;
    private WeatherModel todayWeather;
    private Map<Integer, WeatherModel> next5DayWeatherMap;

    public interface IOnFinish {
        void onSuccess(boolean isSuccess);
    }

    private WeatherClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        mRetrofitService = restAdapter.create(WeatherApiService.class);
    }


    public static synchronized WeatherClient getInstance() {
        if (mInstance == null) {
            mInstance = new WeatherClient();
        }
        return mInstance;
    }

    @Nullable
    public WeatherModel getTodayWeather() {
        return todayWeather;
    }

    @Nullable
    public Map<Integer, WeatherModel> getNext5DayWeatherMap() {
        return next5DayWeatherMap;
    }

    public void reset() {
        todayWeather = null;
        next5DayWeatherMap = null;
    }

    public void fetchAsyncWeather5Days(@Nullable final IOnFinish listener) {
        // All five days call are executed in one single thread - not an ideal way to do.
        // A better way would be to use countdown latch and submit all five calls to threadpool executor
        // and latch.await on the caller background thread until all the five threads are finished or interrupted.
        // OR we can rely on Rxjava - to perform all of these - to execute  on io threads and combine all observables before
        // delivering result to the caller
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                    next5DayWeatherMap = new TreeMap<>();
                    for (int i=1; i<=5; i++) {
                        Call<WeatherModel> call = mRetrofitService.getNthDayWeather(i);
                        success = success && executeAndStore(call, i);
                    }                
                    if (listener != null) {
                        listener.onSuccess(success);
                    }
            }
        });
    }

    @WorkerThread
    private boolean executeAndStore(Call<WeatherModel> call, int day) {
        try {
            Response<WeatherModel> response = call.execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "onResponse : ["+day+"] :" + response.body().toString());
                next5DayWeatherMap.put(day, response.body());
                return true;
            }
        } catch (IOException iex) {
            Log.e(TAG, "fetchAsyncWeather5Days -" + day, iex);
        }
        return false;
    }

    public void fetchAsyncWeatherNthDay(int nthDay, @Nullable final IOnFinish listener) {
        Log.d(TAG, "fetching " + nthDay + "th day weather");
        Call<WeatherModel> call = mRetrofitService.getNthDayWeather(nthDay);
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse :" + response.body().toString());
                    if (listener != null) {
                        listener.onSuccess(true);
                    }
                } else {
                    Log.e(TAG, "onResponse " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                if (listener != null) {
                    listener.onSuccess(false);
                }
                Log.e(TAG, "onFailure", t);
            }
        });
    }

    public void fetchAsyncWeatherToday(@Nullable final IOnFinish listener) {
        Log.d(TAG, "fetching today's weather");
        Call<WeatherModel> call = mRetrofitService.getTodaysWeather();
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse :" + response.body().toString());
                    todayWeather = response.body();
                    if (listener != null) {
                        listener.onSuccess(true);
                    }
                } else {
                    Log.e(TAG, "onResponse " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                if (listener != null) {
                    listener.onSuccess(false);
                }
                Log.e(TAG, "onFailure", t);
            }
        });
    }
}

