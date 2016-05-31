package com.torstar.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.torstar.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eileenzhang on 16-05-26.
 */
public class Utility {
    public static boolean isMetric(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric));

        return unit.equals(context.getString(R.string.pref_unit_metric));
    }

    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    static String formatTemperature(Context context, double temp, boolean isMetric){
        if (!isMetric) {
            temp = temp * 1.8 + 32;
        }

        int formatStrId = R.string.format_temperature;
        return context.getString(formatStrId, temp);
    }

    static String formatDate(long dateInDays){
        Date date = new Date(dateInDays*24*60*60*1000);
        return DateFormat.getDateInstance().format(date);
    }

    static String getFriendlyDayString(Context context, long dateInDays){
        long currentInDays = WeatherContract.normalizeDate(System.currentTimeMillis());

        if (dateInDays == currentInDays){
            String today = context.getString(R.string.today);
            int formatStrId = R.string.format_full_friendly_date;
            return context.getString(formatStrId,
                    today,
                    getFormattedMonthDay(context, dateInDays));
        } else if (dateInDays < currentInDays + 7){
            return getDayName(context, dateInDays);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
            long tempTime = dateInDays*24*60*60*1000;
            return dateFormat.format(tempTime);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateIndays){
        SimpleDateFormat dateFormat= new SimpleDateFormat("MMMM dd");
        long tempTime = dateIndays*24*60*60*1000;
        String formatedDateStr = dateFormat.format(tempTime);
        return formatedDateStr;
    }

    public static String getDayName(Context context, long dateIndays){
        long currentInDays = WeatherContract.normalizeDate(System.currentTimeMillis());

        if (dateIndays == currentInDays){
            return context.getString(R.string.today);
        } else if ((dateIndays - 1) == currentInDays) {
            return context.getString(R.string.tomorrow);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            long tempTime = dateIndays*24*60*60*1000;
            return dateFormat.format(tempTime);
        }
    }

    public static String getFormattedWind(Context context, double windSpeed, double windDegree){
        int windFormatId;

        if (isMetric(context)){
            windFormatId = R.string.format_wind_metric;
        } else {
            windFormatId = R.string.format_wind_imperial;
            windSpeed = windSpeed * 0.621371192237334f;
        }

        String direction = "Unknown";
        if (windDegree >= 337.5 || windDegree < 22.5) {
            direction = "N";
        } else if (windDegree >= 22.5 && windDegree < 67.5) {
            direction = "NE";
        } else if (windDegree >= 67.5 && windDegree < 112.5) {
            direction = "E";
        } else if (windDegree >= 112.5 && windDegree < 157.5) {
            direction = "SE";
        } else if (windDegree >= 157.5 && windDegree < 202.5) {
            direction = "S";
        } else if (windDegree >= 202.5 && windDegree < 247.5) {
            direction = "SW";
        } else if (windDegree >= 247.5 && windDegree < 292.5) {
            direction = "W";
        } else if (windDegree >= 292.5 && windDegree < 337.5) {
            direction = "NW";
        }

        return context.getString(windFormatId, windSpeed, direction);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

}
