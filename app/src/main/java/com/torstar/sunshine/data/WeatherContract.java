package com.torstar.sunshine.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static long normalizeDate(long startDateInMillis){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
        String tempDate0 = dateFormat.format(startDateInMillis);
        // normalize the start date to the beginning of the (UTC) day
        GregorianCalendar date = (GregorianCalendar)GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        //GregorianCalendar date = new GregorianCalendar();

        String tempDate1 = dateFormat.format(date.getTimeInMillis());

        date.setTimeInMillis(startDateInMillis);
        String tempDate2 = dateFormat.format(date.getTimeInMillis());
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        String tempDate3 = dateFormat.format(date.getTimeInMillis());

        long timeInMillis = date.getTimeInMillis();

        long timeInDays = timeInMillis / 1000 / 60 / 60 / 24;

        Log.v(TAG, "timeInDays = " + String.valueOf(timeInDays));

        return timeInDays;
    }

    public static final class LocationEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

    public static final class WeatherEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

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

        public static Uri buildWeatherUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationString) {
            return CONTENT_URI.buildUpon().appendPath(locationString).build();
        }

        public static Uri buildWeatherLocationWithStartDate(String locationString, long startDateInMillisec){
            long normalizedDate = normalizeDate(startDateInMillisec);
            return CONTENT_URI.buildUpon().appendPath(locationString).
                    appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationString, long dateInMillisec){
            return CONTENT_URI.buildUpon().appendPath(locationString)
                    .appendPath(Long.toString(normalizeDate(dateInMillisec))).build();
        }

        public static String getLocationStringFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri){
            String dateString = uri.getQueryParameter(COLUMN_DATE);

            if (dateString != null && dateString.length() > 0){
                return Long.parseLong(dateString);
            } else {
                return 0;
            }
        }
    }
}
