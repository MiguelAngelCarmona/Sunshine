package com.example.android.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Administrador on 24/09/14.
 */
public class TestDb extends AndroidTestCase {

    public static String LOG_TAG = TestDb.class.getName();
    static final String TEST_LOCATION = "29780";
    static final String TEST_DATE = "20141020";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }
    public void simpleReadWriteTest(){
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Location
        long rowId = simpleTest(db, WeatherContract.LocationEntry.TABLE_NAME,getLocationValues(),getLocationColumns());
        //Weather
        simpleTest(db, WeatherContract.WeatherEntry.TABLE_NAME,getWeatherValues(rowId),getWeatherColumns());
        dbHelper.close();
    }
    public long simpleTest(SQLiteDatabase database,String tableName, ContentValues tableValues, String[] tableColumns){
        long rowId = database.insert(tableName, null, tableValues);
        assertTrue(rowId != -1);
        Cursor cursorTest=database.query(tableName,tableColumns,null,null,null,null,null,null);
        if (cursorTest.moveToFirst()){
            ContentValues readValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursorTest,readValues);
            assertEquals(readValues,tableValues);
        }else{
            fail("Something wrong..");
        }
        return rowId;
    }

    public String[] getLocationColumns(){
        String[] columns = {
                WeatherContract.LocationEntry._ID,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        }; return columns;
    }
    public String[] getWeatherColumns(){
        String[] weatherColumns = {
                WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES
        }; return weatherColumns;
    }
    static ContentValues getLocationValues(){
        String testName = "Nerja";

        Double testLat = 36.7496;
        Double testLong = -3.876;
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, testLat);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, testLong);
        return values;
    }
    static ContentValues getWeatherValues(long locationRowId){
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }

    public void testInsertReadDb() {
        String testName = "Nerja";
        String testLocationSetting = "29780";
        Double testLat = 36.7496;
        Double testLong = -3.876;

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, testLat);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, testLong);

        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
        //if we got a row
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row: " + locationRowId);

        String[] columns = {
                WeatherContract.LocationEntry._ID,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };
        //cursor:primary interface to the query results
        Cursor cursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                columns,
                null, //Columns for "where" clause
                null, //values for "where" clause
                null, //columns to group by
                null, //columns to filter by rows groups
                null // sort order
        );

        //We get the value of each column
        if (cursor.moveToFirst()) {
            int locationIndex = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
            double longitude = cursor.getDouble(longIndex);

            //Check values
            assertEquals(testName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLat, latitude);
            assertEquals(testLong, longitude);
            //Add data to weather
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            String testDatetext = "20142310";
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, testDatetext);
            String testDesc = "Windy";
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, testDesc);
            double testWeatherId = 321;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, testWeatherId);
            double testMinTemp = 19;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, testMinTemp);
            double testMaxTemp = 30;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, testMaxTemp);
            double testHum = 60;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, testHum);
            double testPres = 1020;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, testPres);
            double testSpeed = 10;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, testSpeed);
            double testDegrees = 25;
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, testDegrees);

            long weatherRowId;
            weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
            //if we got a row
            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "New weatherRow: " + weatherRowId);

            String[] weatherColumns = {
                    WeatherContract.WeatherEntry._ID,
                    WeatherContract.WeatherEntry.COLUMN_LOC_KEY,
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                    WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                    WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                    WeatherContract.WeatherEntry.COLUMN_DEGREES
            };
            //cursor:primary interface to the query results
            Cursor weatherCursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                    weatherColumns,
                    null, //Columns for "where" clause
                    null, //values for "where" clause
                    null, //columns to group by
                    null, //columns to filter by rows groups
                    null // sort order
            );

            //We get the value of each column
            if (weatherCursor.moveToFirst()) {
                int weatherIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LOC_KEY);
                String locationKey = weatherCursor.getString(weatherIndex);
                int dateTextIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT);
                String dateText = weatherCursor.getString(dateTextIndex);
                int descIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
                String desc = weatherCursor.getString(descIndex);
                int minIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
                double min = weatherCursor.getDouble(minIndex);
                int maxIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
                double max = weatherCursor.getDouble(maxIndex);
                int humIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY);
                double hum = weatherCursor.getDouble(humIndex);
                int pressIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE);
                double press = weatherCursor.getDouble(pressIndex);
                int windIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED);
                double wind = weatherCursor.getDouble(windIndex);
                int degrIndex = weatherCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES);
                double degr = weatherCursor.getDouble(degrIndex);

                //Check values
                //assertEquals(locationRowId, locationKey);
                assertEquals(testDatetext, dateText);
                assertEquals(testDesc, desc);
                assertEquals(testMinTemp, min);
                assertEquals(testMaxTemp, max);
                assertEquals(testHum, hum);
                assertEquals(testPres, press);
                assertEquals(testSpeed, wind);
                assertEquals(testDegrees, degr);

            } else {
                fail("No values returned");
            }
        }
    }
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}