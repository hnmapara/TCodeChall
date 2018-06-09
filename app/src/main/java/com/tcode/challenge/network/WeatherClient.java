package com.tcode.challenge.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.util.Pair;

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
    private MutableLiveData<WeatherModel> todayWeatherLiveData;
    private MutableLiveData<Map<Integer, WeatherModel>> next5DayWeatherMapLiveData;

    private MediatorLiveData<String> mald = new MediatorLiveData<>();

    public interface IOnFinish {
        void onSuccess(boolean isSuccess);
    }

    private WeatherClient() {
        todayWeatherLiveData = new MutableLiveData<>();
        next5DayWeatherMapLiveData = new MutableLiveData<>();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        mRetrofitService = restAdapter.create(WeatherApiService.class);
    }

    /**
     * Zip live data and set mediator livedata only when all the data is available
     * @param lv1
     * @param lv2
     * @return
     */
    LiveData<Pair<String, String>> zipLivedata(final LiveData<String> lv1, final LiveData<String> lv2) {
        final MyMediatorLiveData mld =  new MyMediatorLiveData();
        mld.addSource(lv1, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mld.val1 = s;
                mld.update();
            }
        });
        mld.addSource(lv2, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mld.val2 = s;
                mld.update();
            }
        });

        return mld;
    }

    static class MyMediatorLiveData extends MediatorLiveData<Pair<String, String>> {
        String val1 = null;
        String val2 = null;

        void update() {
            String localVal1 = val1;
            String localVal2 = val2;
            if (localVal1 != null && localVal2 != null) {
                this.setValue(new Pair<>(localVal1, localVal2));
            }
        }
    }


    public static synchronized WeatherClient getInstance() {
        if (mInstance == null) {
            mInstance = new WeatherClient();
        }
        return mInstance;
    }

    @Nullable
    public LiveData<WeatherModel> getTodayWeather() {
        fetchAsyncWeatherToday();
        return todayWeatherLiveData;
    }

    @Nullable
    public LiveData<Map<Integer, WeatherModel>> getNext5DayWeatherMap() {
        return next5DayWeatherMapLiveData;
    }

    public void reset() {
        todayWeatherLiveData.setValue(null);
        next5DayWeatherMapLiveData.setValue(null);
    }

    public void fetchAsyncWeather5Days() {
        // All five days call are executed in one single thread - not an ideal way to do.
        // A better way would be to use countdown latch and submit all five calls to threadpool executor
        // and latch.await on the caller background thread until all the five threads are finished or interrupted.
        // OR we can rely on Rxjava - to perform all of these - to execute  on io threads and combine all observables before
        // delivering result to the caller
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                    Map<Integer, WeatherModel> next5DayWeatherMap = new TreeMap<>();
                    for (int i=1; i<=5; i++) {
                        Call<WeatherModel> call = mRetrofitService.getNthDayWeather(i);
                        success = success && executeAndStore( next5DayWeatherMap, call, i);
                    }                
                    if (success) {
                        next5DayWeatherMapLiveData.postValue(next5DayWeatherMap);
                    }
            }
        });
    }

    @WorkerThread
    private boolean executeAndStore(Map<Integer, WeatherModel> next5DayWeatherMap, Call<WeatherModel> call, int day) {
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

    public void fetchAsyncWeatherToday() {
        Log.d(TAG, "fetching today's weather");
        Call<WeatherModel> call = mRetrofitService.getTodaysWeather();
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse :" + response.body().toString());
                    todayWeatherLiveData.setValue(response.body());
                } else {
                    Log.e(TAG, "onResponse " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                Log.e(TAG, "onFailure", t);
            }
        });
    }
}

