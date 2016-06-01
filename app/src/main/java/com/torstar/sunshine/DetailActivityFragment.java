package com.torstar.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.torstar.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER = 0;

    private static final String[] WEATHER_DETAILS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    private static final int COL_ID = 0;
    private static final int COL_DATE = 1;
    private static final int COL_DESC = 2;
    private static final int COL_MAX = 3;
    private static final int COL_MIN = 4;
    private static final int COL_HUMIDITY = 5;
    private static final int COL_WIND_SPEED = 6;
    private static final int COL_WIND_DIRECTION = 7;
    private static final int COL_PRESSURE = 8;
    private static final int COL_CONDITION_ID = 9;

    private ShareActionProvider mShareActionProvider;
    private String mWeatherStr;
    private Uri mContentUri;

    private ImageView mIconView;
    private TextView mDayView;
    private TextView mDateView;
    private TextView mHighView;
    private TextView mLowView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private TextView mDescView;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_antivity, container, false);

        Bundle args = getArguments();
        if (args != null){
            mContentUri = args.getParcelable(DETAIL_URI);
        }

        mIconView = (ImageView)rootView.findViewById(R.id.iconView);
        mDayView = (TextView)rootView.findViewById(R.id.day_textView);
        mDateView = (TextView)rootView.findViewById(R.id.date_textView);
        mHighView = (TextView)rootView.findViewById(R.id.high_textView);
        mLowView = (TextView)rootView.findViewById(R.id.low_textView);
        mDescView = (TextView)rootView.findViewById(R.id.detail_desc_textView);
        mHumidityView = (TextView)rootView.findViewById(R.id.humidity_textView);
        mWindView = (TextView)rootView.findViewById(R.id.wind_textView);
        mPressureView = (TextView)rootView.findViewById(R.id.pressure_textView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_antivity, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mWeatherStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mContentUri != null){
            CursorLoader cursorLoader = new CursorLoader(
                    getContext(),
                    mContentUri,
                    WEATHER_DETAILS,
                    null,
                    null,
                    null
            );
            return cursorLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()){
            return;
        }

        long timeInDays = data.getLong(COL_DATE);
        String dayName = Utility.getDayName(getContext(), timeInDays);
        mDayView.setText(dayName);

        String dateName = Utility.getFormattedMonthDay(getContext(), timeInDays);
        TextView dateView = (TextView)getView().findViewById(R.id.date_textView);
        dateView.setText(dateName);

        String high = Utility.formatTemperature(getContext(), data.getDouble(COL_MAX),Utility.isMetric(getContext()));
        String low = Utility.formatTemperature(getContext(), data.getDouble(COL_MIN),Utility.isMetric(getContext()));
        mHighView.setText(high);
        mLowView.setText(low);

        double humidity = data.getDouble(COL_HUMIDITY);
        String humidityStr = getString(R.string.format_humidity,humidity);
        mHumidityView.setText(humidityStr);

        double windSpeed = data.getDouble(COL_WIND_SPEED);
        double windDirection = data.getDouble(COL_WIND_DIRECTION);
        String windStr = Utility.getFormattedWind(getContext(), windSpeed, windDirection);
        mWindView.setText(windStr);

        double pressure = data.getDouble(COL_PRESSURE);
        String pressureStr = getString(R.string.format_pressure, pressure);
        mPressureView.setText(pressureStr);

        String desc = data.getString(COL_DESC);
        mDescView.setText(desc);

        int conditionId = data.getInt(COL_CONDITION_ID);
        int iconId = Utility.getArtResourceForWeatherCondition(conditionId);
        mIconView.setImageResource(iconId);

        mWeatherStr = String.format("%s, %s - %s - %s/%s", dayName, dateName, desc, high, low);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String locationSetting){
        Uri uri = mContentUri;

        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    locationSetting,
                    date*24*60*60*1000);
            mContentUri = updateUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");

        if (mWeatherStr != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, mWeatherStr);
        }

        return shareIntent;
    }
}
