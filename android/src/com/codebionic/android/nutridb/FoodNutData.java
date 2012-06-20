package com.codebionic.android.nutridb;

import android.app.Activity;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ContentValues;

import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;

import android.text.InputType;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnFocusChangeListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TextView;

import android.util.Log;

import java.util.List;

public class FoodNutData extends Activity implements View.OnFocusChangeListener
{
    static final int PICK_DATE = 0;

    private static final String TAG = "FoodNutData";

    private ListView mListView;

    private Button mCalcButton;
    private Button mAddButton;

    private EditText mAmountEntry;

    private Spinner mSpinner;
    private View mFoodNutDataHeader;
    private TextView mFoodNutHeaderAmount;

    private SimpleCursorAdapter mCursorAdapter;

    // Common Household measure of a food item
    private String mSeq;    // seq no in table WEIGHT (0 => per 100 gram)
    private Double mAmount; // amount in common household measure ie 2 cup
    private Double mGmwgt;  // gram weight of that measure ie 2 cup = 270 gram

    private Double mSpecAmount; // Specified amount ie 1.5 cups
    private Double mWeight; // gram weight specified amount

    private String mNdbno;
    private String mDate;
    private String mTime;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodnutdata);

        mSeq = "0";    
        mWeight = 100.0;
        mSpecAmount = 100.0;
        mAmount = 100.0;
        mGmwgt = 100.0;

        mListView = (ListView) findViewById(R.id.foodnutdata_list);

        mCalcButton = (Button) findViewById(R.id.foodnutdata_calc);
        mCalcButton.setOnClickListener(new OnCalcClick());

        mAddButton = (Button) findViewById(R.id.foodnutdata_add);
        mAddButton.setOnClickListener(new OnAddClick());

        mAmountEntry = (EditText) findViewById(R.id.foodnutdata_amount_entry);
        mAmountEntry.setText(Double.toString(mSpecAmount),
             TextView.BufferType.EDITABLE);

        mAmountEntry.setOnFocusChangeListener(this);

        mSpinner = (Spinner) findViewById(R.id.foodnutdata_weight_spinner);

        mFoodNutDataHeader = 
            getLayoutInflater().inflate(R.layout.foodnutdatahead, null);

        mFoodNutHeaderAmount = (TextView)
            mFoodNutDataHeader.findViewById(R.id.foodnutdatahead_amount);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            Uri uri = Uri.parse(Uri.decode(intent.getDataString()));

            List<String> paths = uri.getPathSegments();
            mNdbno = paths.get(2);
            showSpinner(mNdbno);
            showResults(ContentUris.parseId(uri));
        }
    }


    protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {

        if (requestCode == PICK_DATE) {
            if (resultCode == RESULT_OK) {
                addNutrient(data.getStringExtra(NutriProvider.DATE),
                    data.getStringExtra(NutriProvider.TIME));
            }
        }

        // Closes the activity.
        finish();
    }


    // http://stackoverflow.com/questions/1109022/
    //  how-to-close-hide-the-android-soft-keyboard
    private void setAmountEntryFocus(boolean isFocused)
    {
        mAmountEntry.setCursorVisible(isFocused);
        mAmountEntry.setFocusable(isFocused);
        mAmountEntry.setFocusableInTouchMode(isFocused);

        if (isFocused) {
            mAmountEntry.requestFocus();
        }
    }


    /**
     * Query the db for the household measures used for the selected food item 
     * and create the spinner.
     */
    private void showSpinner(String ndbno) {
        Log.d(TAG,"showSpinner");

        String[] columns = new String[] {
            NutriProvider.ID,
            NutriProvider.SEQ,
            NutriProvider.AMOUNT,
            NutriProvider.MSRE_DESC,
            NutriProvider.GM_WGT,
        };

        String[] sargs = new String[1];
        sargs[0] = ndbno;

        String sorto = NutriProvider.SEQ + " ASC";

        try {

            Uri uri = NutriProvider.WEIGHT_NDB_NO_URI;

            // Activity.managedQuery() deprecated by CursorLoader api 11
            Cursor cursor = managedQuery(uri, columns, null, sargs, sorto);

            if (cursor == null) {
                Log.d(TAG,"showSpinnet() Empty cursor");
            }
            else if (cursor.getCount() < 1) {
                Log.d(TAG,"showSpinnet() No result");
            }
            else {

                String[] from = new String[] { NutriProvider.MSRE_DESC };
                int[] to = new int[] { android.R.id.text1 };

                SimpleCursorAdapter r = new SimpleCursorAdapter(this,
                        android.R.layout.simple_spinner_item, cursor, from, to);

                r.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(r);

                mSpinner.setOnItemSelectedListener(new OnSpinnerSelect(cursor));
            }
        }
        catch (SecurityException e) {
            Log.d(TAG,"showSpinnet() Security Exception");
        }

    }

    /** 
     * Query the database and display the results - the nutrient values of a
     * selected food item.
     */
    private void showResults(long id) {
        Log.d(TAG,"showResults() id=" + id);

        String[] columns = new String[] {
            NutriProvider.ID,
            NutriProvider.NUTRDESC,
            NutriProvider.UNITS,
            NutriProvider.NUTR_VAL
        };

        String sorto = NutriProvider.SR_ORDER +" ASC";

        try {

            Uri uri = 
                ContentUris.withAppendedId(NutriProvider.NUTRIENT_URI, id);

            // Activity.managedQuery() deprecated by CursorLoader api 11
            Cursor cursor = managedQuery(uri, columns, null, null, sorto);

            if (cursor == null) {
                Log.d(TAG,"showResults() Empty cursor");
            }
            else if (cursor.getCount() < 1) {
                Log.d(TAG,"showResults() No results");
            }
            else {
                String[] from = new String[] { 
                    NutriProvider.NUTRDESC,
                    NutriProvider.NUTR_VAL,
                    NutriProvider.UNITS
                };

                int[] to = new int[] { 
                    R.id.nutdata_desc, 
                    R.id.nutdata_val,
                    R.id.nutdata_units
                };

                mCursorAdapter = new SimpleCursorAdapter(this,
                        R.layout.nutdata, cursor, from, to);

                int colindex = cursor.getColumnIndex(NutriProvider.NUTR_VAL);
                mCursorAdapter.setViewBinder(new WeightViewBinder(colindex));

                mListView.addHeaderView(mFoodNutDataHeader);
                mListView.setAdapter(mCursorAdapter);

            }
        }
        catch (SecurityException e) {
            Log.d(TAG,"showResults() Security Exception");
        }
    }


    private void updateWeight() {
        if ((mAmountEntry.getText()).length() != 0) {
            String s = (mAmountEntry.getText()).toString();
            mSpecAmount = Double.valueOf(s);
        }
        else {
            mSpecAmount = mAmount;
            mAmountEntry.setText(Double.toString(mSpecAmount),
                TextView.BufferType.EDITABLE);
        }

        mWeight = (mGmwgt * mSpecAmount)/mAmount;
        mFoodNutHeaderAmount.setText(String.format("%.3f", mWeight));
    }



    /** Add the selected amount of nutrient to the DB. */
    private Uri addNutrient(String date, String time) {

        updateWeight();

        Uri uri = NutriProvider.DAILY_NUT_URI;
        ContentValues cval = new ContentValues();

        cval.put(NutriProvider.DATE, date);
        cval.put(NutriProvider.TIME, time);
        cval.put(NutriProvider.NDB_NO, mNdbno);
        cval.put(NutriProvider.SEQ, mSeq);
        cval.put(NutriProvider.AMOUNT, mSpecAmount);
        cval.put(NutriProvider.WEIGHT, mWeight);

        Uri ruri = getContentResolver().insert(uri, cval);

        return ruri;
    }
    /**
     * The spinner picks one of the common household measures .
     */
    private class OnSpinnerSelect implements AdapterView.OnItemSelectedListener {

        private Cursor mCursor;

        public OnSpinnerSelect(Cursor c) {
            mCursor = c;
        }

        /**
         * Select the common household measure and recalculate the weight of
         * the food item in terms of that measure.
         */
        public void onItemSelected(AdapterView<?> parent, View view, 
            int position, long id) {

                mCursor.moveToPosition(position);

                int i = mCursor.getColumnIndex(NutriProvider.SEQ);
                mSeq = mCursor.getString(i);
                
                i = mCursor.getColumnIndex(NutriProvider.AMOUNT);
                mAmount = mCursor.getDouble(i);

                i = mCursor.getColumnIndex(NutriProvider.GM_WGT);
                mGmwgt = mCursor.getDouble(i);

                updateWeight();

                mCursorAdapter.notifyDataSetChanged();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }

    } // class OnSpinnerSelect


    /**
     * Nutrient values are referenced to 100 grams of a food item. This
     * viewbinder scales those values to the actual weight of the food item.
     */
    private class WeightViewBinder implements SimpleCursorAdapter.ViewBinder {

        private int mColumnIndex;

        public WeightViewBinder(int c) {
            mColumnIndex = c;
        }

        /** Scale the nutrient values before displaying */
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            if (columnIndex == mColumnIndex) {
                double nvalue = cursor.getDouble(columnIndex);
                nvalue = (nvalue * mWeight.doubleValue())/100.0;
                TextView tv = (TextView) view;
                tv.setText(String.format("%.3f", nvalue));
                return true;
            }

            return false;
        }
    }


    /** Calculate the weight of a food item */
    private class OnCalcClick implements View.OnClickListener {

        public OnCalcClick() { }            

        /** Recalculate the weight (in grams) */
        public void onClick(View v) {

            setAmountEntryFocus(false);

            if ((mAmountEntry.getText()).length() != 0) {
                String s = (mAmountEntry.getText()).toString();
                mSpecAmount = Double.valueOf(s);

                mWeight = (mGmwgt * mSpecAmount)/mAmount;
                mFoodNutHeaderAmount.setText(String.format("%.3f", mWeight));

                mCursorAdapter.notifyDataSetChanged();
            }

            setAmountEntryFocus(true);
        }

    }

    private class OnAddClick implements View.OnClickListener {

        public OnAddClick() { }            

        public void onClick(View v) {

            Uri uri = NutriProvider.DAILY_NUT_URI;

            Intent sintent = new Intent(Intent.ACTION_PICK,
                uri, v.getContext(), FoodNutConfirmDate.class);
            startActivityForResult(sintent, PICK_DATE);
        }
    }


    // http://stackoverflow.com/questions/1109022/
    //  how-to-close-hide-the-android-soft-keyboard
    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (v == mAmountEntry) {
            InputMethodManager im = (InputMethodManager)
                v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (hasFocus) {
                //open keyboard
                im.showSoftInput(mAmountEntry,InputMethodManager.SHOW_IMPLICIT);

            }
            else { 
                //close keyboard
                im.hideSoftInputFromWindow(mAmountEntry.getWindowToken(), 
                    InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

} //class FoodNutData



