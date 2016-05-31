package com.torstar.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by eileenzhang on 16-05-26.
 */
public class ForecastAdapter extends CursorAdapter{
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;

    private Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        int weatherId = cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(cursor.getPosition());
        int iconId = (viewType == VIEW_TYPE_TODAY) ? Utility.getArtResourceForWeatherCondition(weatherId) : Utility.getIconResourceForWeatherCondition(weatherId);
        viewHolder.iconView.setImageResource(iconId);

        String date = Utility.getFriendlyDayString(context, cursor.getLong(MainActivityFragment.COL_WEATHER_DATE));
        viewHolder.dateView.setText(date);

        String desc = cursor.getString(MainActivityFragment.COL_WEATHER_DESC);
        viewHolder.descView.setText(desc);

        String high = Utility.formatTemperature(
                context,
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP),
                Utility.isMetric(context)
        );
        viewHolder.highTempView.setText(high);

        String low = Utility.formatTemperature(
                context,
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP),
                Utility.isMetric(context)
        );
        viewHolder.lowTempView.setText(low);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if (viewType==VIEW_TYPE_TODAY){
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forcast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY: VIEW_TYPE_FUTURE;
    }

   /* private String formatHighLows(double high, double low){
        boolean isMetric = Utility.isMetric(mContext);
        String highLows = Utility.formatTemperature(high, isMetric) + "/" +
                Utility.formatTemperature(low, isMetric);
        return highLows;
    }

    private String convertCursorRowToUXFormat(Cursor cursor){
        double high = cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
        double low = cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);

        String formatHighLow = formatHighLows(high, low);

        String formatedWeather = Utility.formatDate(cursor.getLong(MainActivityFragment.COL_WEATHER_DATE)) + " - " +
                cursor.getString(MainActivityFragment.COL_WEATHER_DESC) + " - " +
                formatHighLow;

        return formatedWeather;
    }*/

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view){
            iconView = (ImageView)view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            descView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView)view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView)view.findViewById(R.id.list_item_low_textview);
        }
    }
}
