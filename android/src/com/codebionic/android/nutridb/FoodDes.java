package com.codebionic.android.nutridb;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.view.LayoutInflater;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.lang.SecurityException;

import android.util.Log;

public class FoodDes extends Activity
{

    public class OnFoodDesClick implements AdapterView.OnItemClickListener {

        private Cursor mCursor;

        public OnFoodDesClick(Cursor c) {
            mCursor = c;
        }

        public void onItemClick(AdapterView<?> parent, View view, 
            int position, long id) {

                mCursor.moveToPosition(position);
                int i = mCursor.getColumnIndex(NutriProvider.NDB_NO);

                Uri.Builder uri = NutriProvider.FOOD_DES_URI.buildUpon();
                uri.appendPath(NutriProvider.NDB_NO);
                uri.appendPath(mCursor.getString(i));
                uri.appendPath(NutriProvider.ID);
                uri.appendPath(Long.toString(id));

                Intent sintent = new Intent(Intent.ACTION_SEARCH,
                    uri.build(), view.getContext(), FoodNutData.class);

                startActivity(sintent);

                // Closes the activity.
                finish();

            }
    }


    private ListView mListView; // Food description list view
    private UriMatcher mUriMatcher;
    private static final String TAG = "FoodDes";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fooddes);

        mListView = (ListView) findViewById(R.id.fooddes_list);
        mUriMatcher = NutriProvider.buildUriMatcher();

        Intent intent = getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion
            Log.d(TAG, "search suggestion");
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            Log.d(TAG, "query=" + intent.getDataString());
            Uri uri = Uri.parse(Uri.decode(intent.getDataString()));
            showResults(uri);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showResults(Uri uri) {

        String scolumn;     // search on this column
        String[] from;      // for the cursor adaptor 

        switch (mUriMatcher.match(uri)) {
            case NutriProvider.SEARCH_SHRT_DESC:
                scolumn = NutriProvider.SHRT_DESC;
                from = new String[] { NutriProvider.SHRT_DESC };
                break;

            case NutriProvider.SEARCH_LONG_DESC:
                scolumn = NutriProvider.LONG_DESC;
                from = new String[] { NutriProvider.LONG_DESC };
                break;

            default:
                scolumn = NutriProvider.SHRT_DESC;
                from = new String[] { NutriProvider.SHRT_DESC };
        }

        String[] columns = new String[] {
            NutriProvider.ID,
            NutriProvider.NDB_NO,
            scolumn
        };

        String[] sargs = new String[1];
        String sorto = scolumn + " ASC";

        String query = uri.getLastPathSegment();
        if (!TextUtils.isEmpty(query)) {
            sargs[0] = query;
        }

        try {

            // Activity.managedQuery() deprecated by CursorLoader api 11
            Cursor cursor = managedQuery(uri, columns, null, sargs, sorto);

            if ((cursor == null) || (cursor.getCount() < 1)) {

                Intent intent = new Intent(this, NutriError.class);
                String s = getString(R.string.error_no_result);
                intent.putExtra("error", s);
                startActivity(intent);

                // Closes the activity.
                finish();
            }
            else {

                int[] to = new int[] { R.id.fooddeslist_item };

                SimpleCursorAdapter r = new SimpleCursorAdapter(this,
                        R.layout.fooddeslist, cursor, from, to);
                mListView.setAdapter(r);

                mListView.setOnItemClickListener(new OnFoodDesClick(cursor));

            }
        }
        catch (SecurityException e) {
            Log.d(TAG, "Security Exception");
        }
    }
}
