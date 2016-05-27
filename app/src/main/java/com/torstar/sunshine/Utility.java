package com.torstar.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
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

    static String formatTemperature(double temp, boolean isMetric){
        if (!isMetric) {
            temp = temp * 1.8 + 32;
        }
        return Long.toString(Math.round(temp));
    }

    static String formatDate(long dateInDays){
        Date date = new Date(dateInDays*24*60*60*1000);
        return DateFormat.getDateInstance().format(date);
    }
}
