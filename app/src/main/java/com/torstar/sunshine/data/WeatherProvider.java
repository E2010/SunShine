package com.torstar.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME + "." +
                        WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME + "." +
                        WeatherContract.LocationEntry._ID
        );
    }

    private static final String sLocationSetting =
            WeatherContract.LocationEntry.TABLE_NAME + "." +
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." +
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.TABLE_NAME + "." +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int uriType = sUriMatcher.match(uri);

        switch (uriType) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case LOCATION:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            case WEATHER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            case WEATHER_WITH_LOCATION:
                retCursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                return retCursor;
            case WEATHER_WITH_LOCATION_AND_DATE:
                retCursor = getWeatherByLocationSettingAndDate(uri,projection,sortOrder);
                return retCursor;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match){
            case LOCATION:
                rowsUpdated = db.update(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WEATHER:
                //normalizeDate(values);
                rowsUpdated = db.update(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case LOCATION:
                long locationId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if (locationId > 0){
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(locationId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case WEATHER:
                //normalizeDate(values);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match){
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for (ContentValues value: values){
                        //normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id > 0) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        if (selection == null) selection = "1"; // this is useful to delete all rows in a table

        switch (match){
            case LOCATION:
                rowsDeleted = db.delete(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case WEATHER:
                rowsDeleted = db.delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
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

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationStringFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
                );
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder){
        String locationSetting = WeatherContract.WeatherEntry.getLocationStringFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSetting,
                new String[]{locationSetting},
                null,
                null,
                sortOrder);
    }

    private void normalizeDate(ContentValues values){
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)){
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }
}
