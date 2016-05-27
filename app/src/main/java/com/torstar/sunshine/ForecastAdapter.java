package com.torstar.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.torstar.sunshine.data.WeatherContract;

/**
 * Created by eileenzhang on 16-05-26.
 */
public class ForecastAdapter extends CursorAdapter{
    private Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView)view;
        textView.setText(convertCursorRowToUXFormat(cursor));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forcast, parent, false);
        return view;
    }

    private String formatHighLows(double high, double low){
        boolean isMetric = Utility.isMetric(mContext);
        String highLows = Utility.formatTemperature(high, isMetric) + "/" +
                Utility.formatTemperature(low, isMetric);
        return highLows;
    }

    private String convertCursorRowToUXFormat(Cursor cursor){
        int index_max = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int index_min = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int index_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
        int index_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);

        double high = cursor.getDouble(index_max);
        double low = cursor.getDouble(index_min);

        String formatHighLow = formatHighLows(high, low);

        String formatedWeather = Utility.formatDate(cursor.getLong(index_date)) + " - " +
                cursor.getString(index_desc) + " - " +
                formatHighLow;

        return formatedWeather;
    }
}
