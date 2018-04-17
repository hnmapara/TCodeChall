package com.tcode.challenge.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcode.challenge.presenter.Presenter;
import com.tcode.challenge.R;
import com.tcode.challenge.Utility;
import com.tcode.challenge.WeatherModel;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Presenter.IPresenterImpl {
    private Presenter mPresenter;

    private ViewGroup mNext5DayContainer;
    private TextView mCurrentTemperatureView;
    private View mCloudIndicator;
    private TextView mWindSpeed;
    private TextView mCityName;
    private ViewGroup mIndicatorGroup;

    private static String[] sWeekdays = new DateFormatSymbols().getWeekdays();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new Presenter(this);
        initView();
        mPresenter.showToday();
        mPresenter.showNext5days();
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
                mPresenter.fetchNext5Days();
            }
        });

        findViewById(R.id.resetbutton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPresenter.reset();
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

    @Override
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

    @Override
    public void showToday(@NonNull WeatherModel currentWeather) {
        mIndicatorGroup.setVisibility(View.VISIBLE);
        mCityName.setText(getString(R.string.current_temperature, currentWeather.getName() ));
        mCurrentTemperatureView.setText(getString(R.string.temperature, currentWeather.getWeather().getTempInC(), Utility.celsiusToFahrenheit(currentWeather.getWeather().getTempInC())));
        mWindSpeed.setText(getString(R.string.wind_speed, currentWeather.getWind().getSpeed()));
    }

    @Override
    public void showCloudiness(boolean isVisible) {
        mCloudIndicator.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void reset() {
        mNext5DayContainer.removeAllViews();
        mIndicatorGroup.setVisibility(View.INVISIBLE);
        mPresenter.showToday();
    }
}
