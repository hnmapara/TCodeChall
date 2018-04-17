package com.tcode.challenge;

import android.support.annotation.NonNull;

import java.util.List;

public class Utility {
    /**
     * Converts temperature in Celsius to temperature in Fahrenheit.
     *
     * @param temperatureInCelsius Temperature in Celsius to convert.
     * @return Temperature in Fahrenheit.
     */
    public static float celsiusToFahrenheit(float temperatureInCelsius) {
        return temperatureInCelsius * 1.8f + 32;
    }

    /**
     * Calculate standard deviation on given list of temparature in C
     * @param degrees List
     * @return
     */
    public static double calculateStandardDeviation(@NonNull List<Float> degrees) {
        float mean = 0.0F;
        int size = degrees.size();

        if (size < 2) {
            throw new IllegalArgumentException("Can't calculate SD with size =" + size);
        }

        for (float degree : degrees) {
            mean += degree/size;
        }

        float deviationSquareSum = 0.0F;
        for (float degree : degrees) {
            deviationSquareSum += Math.pow(degree-mean, 2);
        }

        float deviationSquare = deviationSquareSum/(size-1);
        return Math.sqrt(deviationSquare);
    }
}
