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
import android.widget.TextView;

import com.torstar.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int DETAIL_LOADER = 0;

    private static final String[] WEATHER_DETAILS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final int COL_ID = 0;
    private static final int COL_DATE = 1;
    private static final int COL_DESC = 2;
    private static final int COL_MAX = 3;
    private static final int COL_MIN = 4;

    private ShareActionProvider mShareActionProvider;
    private String mForecastUriStr;
    private String mWeatherStr;

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
        Intent intent= getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail_antivity, container, false);

        if (intent != null){
            mForecastUriStr = intent.getDataString();

            //TextView textView = (TextView)rootView.findViewById(R.id.detail_textView);
            //textView.setText(mForecastUriStr);
        }


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
        Uri uri = Uri.parse(mForecastUriStr);


        CursorLoader cursorLoader = new CursorLoader(
                getContext(),
                uri,
                WEATHER_DETAILS,
                null,
                null,
                null
        );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()){
            return;
        }

        long timeInDays = data.getLong(COL_DATE);

        String formatData = Utility.formatDate(timeInDays);

        String highLows = Utility.formatTemperature(data.getDouble(COL_MAX),Utility.isMetric(getContext())) + "/" +
                Utility.formatTemperature(data.getDouble(COL_MIN),Utility.isMetric(getContext()));

        mWeatherStr = formatData + " - " +
                data.getString(COL_DESC) + " - " +
                highLows;

        TextView textView = (TextView)getView().findViewById(R.id.detail_textView);
        textView.setText(mWeatherStr);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
