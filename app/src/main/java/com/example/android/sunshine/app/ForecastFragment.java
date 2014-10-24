package com.example.android.sunshine.app;

/**
 * Created by Administrador on 12/09/14.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    private static final String[] FORECAST_COLUMNS = {
// In this case the id needs to be fully qualified with a table name, since
// the content provider joins the location & weather tables in the background
// (both have an _id column)
// On the one hand, that's annoying. On the other, you can search the weather table
// using the location set by the user, which is only in the Location table.
// So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these
// must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    private SimpleCursorAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created. This
// fragment only uses one loader, so we don't care about checking the id.

// To only show current and future dates, get the String representation for today,
// and filter the query to return weather only for dates after or including today.
// Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

// Sort order: Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

// Now create and return a CursorLoader that will take care of
// creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onStart(){
        super.onStart();
        //updateWeather(); //No more update every time, we'll get the data through refresh
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Not necessary anymore
        // Create some dummy data for the ListView. Here's a sample weekly forecast
        /*String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data)); */

        mForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
// the column names to use to fill the textviews
                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
// the textviews to fill with the data pulled from the columns above
                new int[]{R.id.list_item_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_high_textview,
                        R.id.list_item_low_textview
                },
                0
        );

        mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utility.isMetric(getActivity());
                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP: {
// we have to do some formatting and possibly a conversion
                        ((TextView) view).setText(Utility.formatTemperature(
                                cursor.getDouble(columnIndex), isMetric));
                        return true;
                    }
                    case COL_WEATHER_DATE: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    /*String dateString = Utility.formatDate(cursor.getString(COL_WEATHER_DATE));
                    String weatherDescription = cursor.getString(COL_WEATHER_DESC);
                    boolean isMetric = Utility.isMetric(getActivity());
                    String high = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP),isMetric);
                    String low = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
                    String detailString = String.format("%s - %s - %s/%s",dateString,weatherDescription,high,low);
                    Intent showDetail = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, detailString);*/
                    Intent showDetail = new Intent(getActivity(),DetailActivity.class).putExtra(DetailActivity.DATE_KEY,cursor.getString(COL_WEATHER_DATE));
                    startActivity(showDetail);
                }
            }
        });


        return rootView;
    }


}

