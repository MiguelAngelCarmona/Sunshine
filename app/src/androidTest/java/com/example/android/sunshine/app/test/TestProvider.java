package com.example.android.sunshine.app.test;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDbHelper;

/**
 * Created by Administrador on 24/09/14.
 */
public class TestProvider extends AndroidTestCase {

    public static String LOG_TAG = TestProvider.class.getName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testInsertReadProvider() {

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = TestDb.getLocationValues();

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
        //if we got a row
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row: " + locationRowId);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, values);

// Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, values);
            //Add data to weather
            ContentValues weatherValues = TestDb.getWeatherValues(locationRowId);

            long weatherRowId;
            weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
            //if we got a row
            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "New weatherRow: " + weatherRowId);

            Cursor weatherCursor = mContext.getContentResolver().query(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    null, //return all columns
                    null, //columns for where clause
                    null, //values for where clause
                    null //columns to group by

            );

        if (weatherCursor.moveToFirst()){
            TestDb.validateCursor(weatherCursor, weatherValues);}
        else{
            fail("No weather data returned");
        }

        weatherCursor.close();
        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, values);
        // Get the joined Weather and Location data
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        if (weatherCursor.moveToFirst()){
            TestDb.validateCursor(weatherCursor, weatherValues);}
        else{
            fail("No weather data returned");
        }
        weatherCursor.close();
        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        if (weatherCursor.moveToFirst()){
            TestDb.validateCursor(weatherCursor, weatherValues);}
        else{
            fail("No weather data returned");
        }

        weatherCursor.close();
        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.builWeatherLocationWithDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        if (weatherCursor.moveToFirst()){
        TestDb.validateCursor(weatherCursor, weatherValues);}
        else{
            fail("No weather data returned");
        }
        dbHelper.close();

        }


    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);
        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);
        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.builWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE, type);
        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(WeatherContract.LocationEntry.CONTENT_TYPE, type);
        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(WeatherContract.LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(WeatherContract.LocationEntry.CONTENT_ITEM_TYPE, type);
        }

    // The target api annotation is needed for the call to keySet -- we wouldn't want
// to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

}