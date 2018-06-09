package com.tcode.challenge.ui;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcode.challenge.R;
import com.tcode.challenge.Utility;
import com.tcode.challenge.WeatherModel;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mNext5DayContainer;
    private TextView mCurrentTemperatureView;
    private View mCloudIndicator;
    private TextView mWindSpeed;
    private TextView mCityName;
    private ViewGroup mIndicatorGroup;
    private MainActivityViewModel mViewModel;

    private static String[] sWeekdays = new DateFormatSymbols().getWeekdays();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mViewModel.getTodayWeatherLiveData().observe(this, new Observer<WeatherModel>() {
            @Override
            public void onChanged(@Nullable WeatherModel weatherModel) {
                showToday(weatherModel);
                showCloudiness(weatherModel.getClouds().getCloudinessInPercentage() > 50);
            }
        });

        mViewModel.getNext5DayWeatherMapLiveData().observe(this, new Observer<Map<Integer, WeatherModel>>() {
            @Override
            public void onChanged(@Nullable Map<Integer, WeatherModel> integerWeatherModelMap) {
                Collection<WeatherModel> next5daysWeather = integerWeatherModelMap.values();
                List<Float> degrees = new ArrayList<>(next5daysWeather.size());
                for(WeatherModel wm : next5daysWeather) {
                    degrees.add(wm.getWeather().getTempInC());
                }
                showNext5DaysAndSD(next5daysWeather, Utility.calculateStandardDeviation(degrees));
            }
        });
    }

    private void initView() {
        mCurrentTemperatureView = (TextView) findViewById(R.id.temperature);
        mNext5DayContainer = (ViewGroup) findViewById(R.id.next_5_container);
        mCloudIndicator = findViewById(R.id.cloud_indicator);
        mWindSpeed = (TextView) findViewById(R.id.wind_speed);
        mIndicatorGroup = (ViewGroup) findViewById(R.id.sun_indicator_container);
        mCityName = (TextView) findViewById(R.id.city_name);

        findViewById(R.id.fetch_5days_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewModel.fetchNext5DaysAsync();
            }
        });

        findViewById(R.id.resetbutton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    private TextView makeTextView(String text) {
        return makeColoredTextView(text, Color.BLACK);
    }

    private TextView makeColoredTextView(String text, int textColor) {
        TextView t = new TextView(getBaseContext());
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        t.setTextColor(textColor);
        t.setText(text);
        return t;
    }

    public void showNext5DaysAndSD(@NonNull final Collection<WeatherModel> next5daysWeather, final double standarDeviation) {
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!MainActivity.this.isFinishing()) {
                    mNext5DayContainer.removeAllViews();
                    for (WeatherModel wm : next5daysWeather) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        String temperature = sWeekdays[calendar.get(Calendar.DAY_OF_WEEK)] + " : "
                                + getString(R.string.temperature, wm.getWeather().getTempInC(), Utility.celsiusToFahrenheit(wm.getWeather().getTempInC()));
                        mNext5DayContainer.addView(makeTextView(temperature));
                    }
                    mNext5DayContainer.addView(makeColoredTextView(getString(R.string.standard_deviation,standarDeviation), Color.WHITE));
                }
            }
        });
    }

    public void showToday(@NonNull WeatherModel currentWeather) {
        mIndicatorGroup.setVisibility(View.VISIBLE);
        mCityName.setText(getString(R.string.current_temperature, currentWeather.getName() ));
        mCurrentTemperatureView.setText(getString(R.string.temperature, currentWeather.getWeather().getTempInC(), Utility.celsiusToFahrenheit(currentWeather.getWeather().getTempInC())));
        mWindSpeed.setText(getString(R.string.wind_speed, currentWeather.getWind().getSpeed()));
    }

    public void showCloudiness(boolean isVisible) {
        mCloudIndicator.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public void reset() {
        mNext5DayContainer.removeAllViews();
        mIndicatorGroup.setVisibility(View.INVISIBLE);
        mViewModel.fetchTodayAsync();
    }
}
