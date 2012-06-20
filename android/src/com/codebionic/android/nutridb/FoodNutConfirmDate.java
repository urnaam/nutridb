package com.codebionic.android.nutridb;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;

import android.content.Intent;

import android.net.Uri;
import android.net.Uri.Builder;

import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

public class FoodNutConfirmDate extends Activity
{
    // date and time
    private static int sYear;
    private static int sMonth;
    private static int sDay;
    private static int sHour;
    private static int sMinute;
    private static int sSecond;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    static final int DATE_DIALOG_ID = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodnutconfirmdate);

        Button todayButton = 
            (Button) findViewById(R.id.foodnutconfirmdate_today);
        todayButton.setOnClickListener(new OnTodayClick());

        Button pickButton =
            (Button) findViewById(R.id.foodnutconfirmdate_pick);
        pickButton.setOnClickListener(new OnPickClick());

        final Calendar cal = Calendar.getInstance();
        sYear = cal.get(Calendar.YEAR);
        sMonth = cal.get(Calendar.MONTH);
        sDay = cal.get(Calendar.DAY_OF_MONTH);
        sHour = cal.get(Calendar.HOUR_OF_DAY);
        sMinute = cal.get(Calendar.MINUTE);
        sSecond = cal.get(Calendar.SECOND);

        mDateSetListener = new OnDateSet();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener,
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


    private void setResult(String date, String time) {
        Uri uri = NutriProvider.DAILY_NUT_URI;

        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        intent.putExtra(NutriProvider.DATE, date);
        intent.putExtra(NutriProvider.TIME, time);

        setResult(RESULT_OK, intent);
    }

    private class OnDateSet implements DatePickerDialog.OnDateSetListener {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {

            sYear = year;
            sMonth = monthOfYear;
            sDay = dayOfMonth;

            String date = NutriDB.dateISO8601(sYear, sMonth, sDay);
            String time = NutriDB.getTimeNow();

            setResult(date, time);
            // Closes the activity.
            finish();
        }
    } // class OnDateSet

    private class OnTodayClick implements View.OnClickListener {

        public OnTodayClick() { }            

        public void onClick(View v) {

            String date = NutriDB.getToday();
            String time = NutriDB.getTimeNow();

            setResult(date, time);
            // Closes the activity.
            finish();
        }
    } // class OnTodayClick

    private class OnPickClick implements View.OnClickListener {

        public OnPickClick() { }            

        public void onClick(View v) {
            showDialog(DATE_DIALOG_ID);
        }
    } // class OnPickClick


} // class FoodNutConfirmDate
