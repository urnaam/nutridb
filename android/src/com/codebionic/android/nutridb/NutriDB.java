package com.codebionic.android.nutridb;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;

import android.content.Intent;

import android.database.Cursor;
import android.database.MatrixCursor;

import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Calendar;

public class NutriDB extends Activity
{

    private static final String TAG = "NutriDB"; 

    private View mNutSumDataHeader;
    private TextView mNutSumDataHeaderAmount;

    private ListView mListView;
    private int mFoodSearchBy;


    private String[] mDailyNutSumColumns = new String[] {
            NutriProvider.ID,
            NutriProvider.NUTRDESC,
            NutriProvider.SUM_NUTR_VAL,
            NutriProvider.UNITS
    };

    private MatrixCursor mEmptyCursor;
    private SimpleCursorAdapter mCursorAdapter;

    // date and time
    private static int sYear;
    private static int sMonth;
    private static int sDay;
    private static int sHour;
    private static int sMinute;
    private static int sSecond;

    
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    static final int DATE_DIALOG_ID = 0;

    // Returns a date string in a ISO 8601 format.
    public static String dateISO8601(int year, int month, int day) {

        StringBuilder s = new StringBuilder(16);
        s.append(year).append("-")
        .append(pad(month + 1)).append("-")
        .append(day);

        return s.toString();
    }

    // Returns time string in a ISO 8601 format.
    public static String timeISO8601(int hour, int minute, int second) {

        StringBuilder s = new StringBuilder(16);
        s.append(pad(hour)).append(":")
        .append(pad(minute)).append(":")
        .append(pad(second));
        
        return s.toString();
    }

    public static String getToday() {

        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return dateISO8601(year, month, day);
    }

    public static String getTimeNow() {

        final Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        return timeISO8601(hour, minute, second);
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        mFoodSearchBy = NutriProvider.SEARCH_SHRT_DESC;

        Button searchButton = (Button) findViewById(R.id.main_search);
        OnSearch onsearch = new OnSearch();
        searchButton.setOnClickListener(onsearch);

        Button calendarButton = (Button) findViewById(R.id.main_calendar);
        OnCalendar oncalendar = new OnCalendar();
        calendarButton.setOnClickListener(oncalendar);

        mListView = (ListView) findViewById(R.id.main_list);

        mNutSumDataHeader =
            getLayoutInflater().inflate(R.layout.nutsumhead, null);

        mNutSumDataHeaderAmount = (TextView)
            mNutSumDataHeader.findViewById(R.id.nutsumhead_amount);

        String[] pnames = NutriInstaller.getAPKExpansionFiles(this);
        if (pnames.length == 0) {
            Log.d(TAG, "pnames=0");
            Intent intent = new Intent(this, NutriInstaller.class);
            startActivity(intent);
        }
        Log.d(TAG, "pnames=" + pnames[0]);


        mEmptyCursor = new MatrixCursor(mDailyNutSumColumns, 1);

        Object[] emptyDailyNutSumRow = new Object[] {
            new Integer(1),
            new String("No data for selected date"),
            new String(""),
            new String("")
        };
        mEmptyCursor.addRow(emptyDailyNutSumRow);
        setCursorAdaptor();

        final Calendar cal = Calendar.getInstance();
        sYear = cal.get(Calendar.YEAR);
        sMonth = cal.get(Calendar.MONTH);
        sDay = cal.get(Calendar.DAY_OF_MONTH);
        sHour = cal.get(Calendar.HOUR_OF_DAY);
        sMinute = cal.get(Calendar.MINUTE);
        sSecond = cal.get(Calendar.SECOND);

        mDateSetListener = new OnDateSet();

        updateDailyNut(dateISO8601(sYear, sMonth, sDay));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDailyNut(dateISO8601(sYear, sMonth, sDay));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent()");
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion
            Log.d(TAG, "search suggestion");
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            Log.d(TAG, "handleIntent(ACTION_SEARCH)");

            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "handleIntent query=" + query);

            Uri.Builder uribuild;

            switch (mFoodSearchBy) {

                case NutriProvider.SEARCH_SHRT_DESC:
                    uribuild = NutriProvider.FOOD_DES_SHRT_DESC_URI.buildUpon();
                break;

                case NutriProvider.SEARCH_LONG_DESC:
                    uribuild = NutriProvider.FOOD_DES_LONG_DESC_URI.buildUpon();
                break;

                default:
                    uribuild = NutriProvider.FOOD_DES_SHRT_DESC_URI.buildUpon();
                break;
            }
                    
            uribuild.appendPath(query);
            Uri uri = uribuild.build();

            Intent sintent = new Intent(Intent.ACTION_SEARCH,
                uribuild.build(), this, FoodDes.class);

            startActivity(sintent);

        }
    }


    private void setCursorAdaptor() {

        String[] from = new String[] { 
            NutriProvider.NUTRDESC,
            NutriProvider.SUM_NUTR_VAL,
            NutriProvider.UNITS
        };

        int[] to = new int[] { 
            R.id.nutdata_desc, 
            R.id.nutdata_val,
            R.id.nutdata_units
        };

        mCursorAdapter = new SimpleCursorAdapter(this,
            R.layout.nutdata, mEmptyCursor, from, to);

        mListView.addHeaderView(mNutSumDataHeader);
        mListView.setAdapter(mCursorAdapter);
    }

    /** Queries the DB and displays the daily nutrient intake */
    private void updateDailyNut(String date) {

        String sorto = NutriProvider.SR_ORDER +" ASC";
        String[] sargs = new String[1];
        sargs[0] = date;

        mNutSumDataHeaderAmount.setText(date);

        try {

            Uri uri = NutriProvider.DAILY_SUM_NUT_DATE_URI;

            // Activity.managedQuery() deprecated by CursorLoader api 11
            Cursor cursor = 
                managedQuery(uri, mDailyNutSumColumns, null, sargs, sorto);

            if (cursor == null) {
                Log.d(TAG,"showResults() Empty cursor");
                cursor = mEmptyCursor;
            }
            else if (cursor.getCount() < 1) {
                Log.d(TAG,"showResults() No results");
                cursor = mEmptyCursor;
            }
            mCursorAdapter.changeCursor(cursor);

        }
        catch (SecurityException e) {
            Log.d(TAG,"showResults() Security Exception");
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            sYear, sMonth, sDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(sYear, sMonth, sDay);
                break;
        }
    }    




    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    private class OnDateSet implements DatePickerDialog.OnDateSetListener {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            sYear = year;
            sMonth = monthOfYear;
            sDay = dayOfMonth;
            updateDailyNut(dateISO8601(sYear, sMonth, sDay));
        }
    } // class OnDateSet

    private class OnSearch implements View.OnClickListener {
        public OnSearch() {
        }

        public void onClick(View v) {
            onSearchRequested();
        }
    } //  class OnSearch

    private class OnCalendar implements View.OnClickListener {
        public OnCalendar() {
        }

        public void onClick(View v) {
            showDialog(DATE_DIALOG_ID);
        }

    } // class OnCalendar


} // class NutriDB
