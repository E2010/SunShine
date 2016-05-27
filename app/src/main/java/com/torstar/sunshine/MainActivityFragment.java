package com.torstar.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.torstar.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String TAG = MainActivityFragment.class.getSimpleName();

    private ForecastAdapter mWeatherListAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        // Prepare for Adapter, set up Cursor
        String locationSetting = Utility.getPreferredLocation(getContext());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,
                System.currentTimeMillis()
        );
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, sortOrder);

        // Adapter
        mWeatherListAdapter = new ForecastAdapter(getContext(), cursor, 0);

        // Set Listview Adapter
        ListView weatherListview = (ListView)rootView.findViewById(R.id.listview_forecast);

        weatherListview.setAdapter(mWeatherListAdapter);

        weatherListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String weather = mWeatherListAdapter.getItem(position);

                // Open Detail View
                Intent intent = new Intent(getContext(), DetailAntivity.class);
                //intent.putExtra(Intent.EXTRA_TEXT, weather);

                startActivity(intent);
                //Toast toast = Toast.makeText(getContext(), weather, Toast.LENGTH_SHORT);
                //toast.show();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String postCode = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        //FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getContext(),mWeatherListAdapter);
        //fetchWeatherTask.execute(postCode);
    }

    /*public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        protected String[] doInBackground(String... params){
            String postCode = "";
            if (params != null) {
                postCode = params[0];
            }

            // Fetch Data From Network
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try{
                // Build URL with Uri Builder
                String mode = "json";
                String unit = "metric";
                String days = "7";
                String appId = getContext().getString(R.string.weather_cloud_api_key);


                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_KEY = "q";
                final String FORMAT_KEY = "mode";
                final String UNIT_KEY = "units";
                final String DAYS_KEY = "cnt";
                final String APPID_KEY = "APPID";

                Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_KEY, postCode)
                        .appendQueryParameter(FORMAT_KEY, mode)
                        .appendQueryParameter(UNIT_KEY, unit)
                        .appendQueryParameter(DAYS_KEY, days)
                        .appendQueryParameter(APPID_KEY,appId);

                URL url = new URL(builder.toString());
                Log.v(TAG, url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null){
                    forecastJsonStr = null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if (buffer.length()==0){
                    forecastJsonStr = null;
                }

                forecastJsonStr = buffer.toString();
            } catch (IOException e){
                Log.e(TAG, "Error", e);
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                Log.v(TAG, forecastJsonStr);
                String[] w = getWeatherDataFromJson(forecastJsonStr, 7);
                return w;
            } catch (Throwable t) {
                Log.e(TAG, "Could not get data properly");
            };

            return null;
        }

        @Override
        protected void onPostExecute(String[] daysWeather) {
            if (daysWeather!= null) {
                mWeatherListAdapter.clear();
                mWeatherListAdapter.addAll(daysWeather);
            }
            //mWeatherListAdapter.notifyDataSetChanged();
        }

        *//* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         *//*
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        *//**
         * Prepare the weather high/lows for presentation.
         *//*
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String unitPref = prefs.getString(getString(R.string.pref_unit_key),getString(R.string.pref_unit_metric));

            Log.v(TAG, unitPref);

            long roundedHigh = 0;
            long roundedLow = 0;
            if (unitPref.equals(getString(R.string.pref_unit_metric))) {
                roundedHigh = Math.round(high);
                roundedLow = Math.round(low);
            } else {
                roundedHigh = Math.round(high*1.8 + 32);
                roundedLow = Math.round(low*1.8 + 32);
            }

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        *//**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         *//*
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            //for (String s : resultStrs) {
            //}
            return resultStrs;

        }

    }*/
}
