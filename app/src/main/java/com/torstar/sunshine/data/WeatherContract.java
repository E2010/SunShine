package com.torstar.sunshine.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by eileenzhang on 16-05-16.
 */
public class WeatherContract {
    private static final String TAG = WeatherContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.torstar.sunshine";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static long normalizeDate(long startDate){
        // normalize the start date to the beginning of the (UTC) day
        GregorianCalendar date = (GregorianCalendar)GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

        date.setTime(new Date(startDate));
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        long timeInMillis = date.getTimeInMillis();

        long timeInDays = timeInMillis / 1000 / 60 / 60 / 24;

        Log.v(TAG, "timeInDays = " + String.valueOf(timeInDays));

        return timeInDays;
    }

    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
    }

    public static final class WeatherEntry implements BaseColumns {
        public static final String TABLE_NAME = "weather";

        public static final String COLUMN_LOC_KEY = "location_id";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_SHORT_DESC = "short_desc";
        public static final String COLUMN_MIN_TEMP = "min_temp";
        public static final String COLUMN_MAX_TEMP = "max_temp";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND_SPEED = "wind";
        public static final String COLUMN_DEGREES = "degrees";
    }
}
