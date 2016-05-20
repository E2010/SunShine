package com.torstar.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by eileenzhang on 16-05-20.
 */
public class WeatherProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Weather Uri
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER);

        // Weather with Location Uri
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);

        // Weather with Location and Date
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);

        // Location Uri
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION, LOCATION);

        return uriMatcher;
    }
}
