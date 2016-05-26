package com.torstar.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.torstar.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Created by eileenzhang on 16-05-24.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private Context mContext;
    private ArrayAdapter<String> mForecastAdapter;

    @Override
    protected String[] doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        String locationSetting = params[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecaseJsonString = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try{
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationSetting)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();

            URL url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            forecaseJsonString = buffer.toString();


        } catch (IOException e){
            Log.e(TAG, "Error", e);

            return null;
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }

            if (reader != null){
                try {
                    reader.close();
                } catch (final IOException e){
                    Log.e(TAG, "Error closing stream", e);
                }

            }
        }

        try {
            return getWeatherDataFromJson(forecaseJsonString, locationSetting);
        } catch (JSONException e){
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (strings != null && mForecastAdapter != null){
            mForecastAdapter.clear();
            mForecastAdapter.addAll(strings);
        }
    }

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter){
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }

    private String getReadableDateString(long timeInDays){
        long time = timeInDays * 24 * 60 * 60 * 1000;
        Date date = new Date(time);

        SimpleDateFormat formater = new SimpleDateFormat("E, MMM, d");
        return formater.format(date).toString();
    }

    private String formatHighLows(double high, double low){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String unitType = prefs.getString(
                mContext.getString(R.string.pref_unit_key),
                mContext.getString(R.string.pref_unit_metric));

        if (unitType.equals(mContext.getString(R.string.pref_unit_imperial))) {
            high = high * 1.8 + 32;
            low = low *1.8 + 32;
        } else if(!unitType.equals(mContext.getString(R.string.pref_unit_metric))){
            Log.e(TAG, "Unsupported unit type");
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String returnString = roundedHigh + "/" + roundedLow;
        return returnString;
        //return Long.toString(roundedHigh) + "/" + Long.toString(roundedLow);
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " =  ? ",
                new String[]{locationSetting},
                null);

        if (cursor.moveToFirst()){
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = cursor.getLong(locationIdIndex);
        } else {
            ContentValues value = new ContentValues();

            value.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            value.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            value.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
            value.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);

            Uri uri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    value);

            locationId = ContentUris.parseId(uri);
        }

        cursor.close();

        return locationId;
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv){
        String[] returnString = new String[cvv.size()];

        for (int i = 0; i < cvv.size(); i++){
            ContentValues value = cvv.elementAt(i);
            String highAndLow = formatHighLows(
                    value.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
                    value.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            returnString[i] = getReadableDateString(value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE)) + " - " +
                    value.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC) + " - " +
                    highAndLow;

        }

        return returnString;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        final String OWM_LAT = "lat";
        final String OWM_LONG = "lon";

        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMP = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESC = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);

            double coordLat = coordJSON.getDouble(OWM_LAT);
            double coordLong = coordJSON.getDouble(OWM_LONG);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            long locationId = addLocation(locationSetting, cityName, coordLat, coordLong);

            Vector<ContentValues> cvvVector = new Vector<ContentValues>(weatherArray.length());

            Date now = new Date();
            long nowInDays = WeatherContract.normalizeDate(now.getTime());

            for(int i = 0; i < weatherArray.length(); i++){
                long date;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                JSONObject dayJson = weatherArray.getJSONObject(i);
                date = nowInDays + i;

                pressure = dayJson.getDouble(OWM_PRESSURE);
                humidity = dayJson.getInt(OWM_HUMIDITY);
                windSpeed = dayJson.getDouble(OWM_WINDSPEED);
                windDirection = dayJson.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject =
                        dayJson.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESC);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject tempObject = dayJson.getJSONObject(OWM_TEMP);
                high = tempObject.getDouble(OWM_MAX);
                low = tempObject.getDouble(OWM_MIN);

                ContentValues weatherValue = new ContentValues();

                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_DATE, date);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValue.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cvvVector.add(weatherValue);
            }



            // Add to database
            if (cvvVector.size() > 0){
                ContentValues[] cvvArray = new ContentValues[cvvVector.size()];
                cvvVector.toArray(cvvArray);
                mContext.getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        cvvArray
                );
            }

            // Sort order:  Ascending, by date.
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert
            Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
                    null, null, null, sortOrder);

            cvvVector = new Vector<ContentValues>(cur.getCount());
            if ( cur.moveToFirst() ) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cur, cv);
                    cvvVector.add(cv);
                } while (cur.moveToNext());
            }

            Log.d(TAG, "FetchWeatherTask Complete. " + cvvVector.size() + " Inserted");

            String[] resultString = convertContentValuesToUXFormat(cvvVector);

            return resultString;
        } catch (JSONException e){
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

}
