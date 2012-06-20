package com.codebionic.android.nutridb;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;

import android.net.Uri;

import android.util.Log;


/*
    To search the short description column (shrt_desc) in the FOOD_DES table, 
    use a content Uri looking like this:
    content://com.codebionic.android.nutridb.NutriProvider/FOOD_DES/shrt_desc

    To get the short description by _id, say 10, use a content Uri looking 
    like this:
    content://com.codebionic.android.nutridb.NutriProvider/FOOD_DES/shrt_desc/10


    content://com.codebionic.android.nutridb.NutriProvider/NUT_DATA/
    FOOD_DES/id/10

*/
public class NutriProvider extends ContentProvider {

    // Tables
    public static final String FOOD_DES = "FOOD_DES";
    public static final String ID = "_id";
    public static final String NDB_NO = "ndb_no";
    public static final String SHRT_DESC = "shrt_desc";
    public static final String LONG_DESC = "long_desc";
    public static final String COMNAME = "comname";
    public static final String MANUFACNAME = "manufacname";

    public static final String NUTR_DEF = "NUTR_DEF";
    public static final String NUTR_NO = "nutr_no";
    public static final String NUTRDESC = "nutrdesc";
    public static final String UNITS = "units";
    public static final String SR_ORDER = "sr_order";

    public static final String NUT_DATA = "NUT_DATA";
    public static final String NUTR_VAL = "nutr_val";

    public static final String NUTRIENT = "NUTRIENT";

    public static final String WEIGHT = "WEIGHT";
    public static final String SEQ = "seq";
    public static final String AMOUNT = "amount";
    public static final String MSRE_DESC = "msre_desc";
    public static final String GM_WGT = "gm_wgt";

    public static final String DAILY_NUT = "DAILY_NUT";
    public static final String DATE = "date";
    public static final String TIME = "time";

    public static final String DAILY_SUM_NUT = "DAILY_SUM_NUT";
    public static final String SUM_NUTR_VAL = "sum_nutr_val";


    // Content URIs for queries
    public static String AUTHORITY =
        "com.codebionic.android.nutridb.NutriProvider";

    public static final Uri FOOD_DES_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + FOOD_DES);

    public static final Uri FOOD_DES_SHRT_DESC_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + FOOD_DES + "/" + SHRT_DESC);

    public static final Uri FOOD_DES_LONG_DESC_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + FOOD_DES + "/" + LONG_DESC);

    public static final Uri NUT_DATA_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + NUT_DATA);

    public static final Uri NUTRIENT_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + NUTRIENT);

    public static final Uri WEIGHT_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + WEIGHT);

    public static final Uri WEIGHT_NDB_NO_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + WEIGHT + "/" + NDB_NO);

    public static final Uri DAILY_NUT_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + DAILY_NUT);

    public static final Uri DAILY_SUM_NUT_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + DAILY_SUM_NUT);

    public static final Uri DAILY_SUM_NUT_DATE_URI = 
        Uri.parse("content://" + AUTHORITY + "/" + DAILY_SUM_NUT + "/" + DATE);



    // multiple rows
    public static final String FOOD_DES_MROW_MIME_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE +
        "/vnd.com.codebionic.android.nutridb.NutriProvider.FOOD_DES";

    // single row
    public static final String FOOD_DES_ROW_MIME_TYPE =
        ContentResolver.CURSOR_ITEM_BASE_TYPE +
        "/vnd.com.codebionic.android.nutridb.NutriProvider.FOOD_DES";

    // URI Matcher
    public static final int SEARCH_SHRT_DESC = 0;
    public static final int SEARCH_LONG_DESC = 1;
    public static final int SEARCH_COMNAME = 2;
    public static final int SEARCH_MANUFACNAME = 3;

    private static final int GET_SHRT_DESC = 10;
    private static final int GET_LONG_DESC = 11;
    private static final int GET_COMNAME = 12;
    private static final int GET_MANUFACNAME = 13;

    private static final int GET_NUT_DATA = 30;
    private static final int GET_NUTRIENT = 31;

    private static final int GET_WEIGHT = 40;
    private static final int SEARCH_WEIGHT_NDB_NO = 41;

    private static final int GET_DAILY_NUT = 50;
    private static final int SEARCH_DAILY_NUT_DATE = 51;
    private static final int INSERT_DAILY_NUT = 55;

    private static final int SEARCH_DAILY_SUM_NUT_DATE = 60;



    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final String TAG = "NutriProvider";

    public static UriMatcher buildUriMatcher() {

        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);

        String fd = FOOD_DES + "/";
        matcher.addURI(AUTHORITY, fd + SHRT_DESC + "/*", SEARCH_SHRT_DESC);
        matcher.addURI(AUTHORITY, fd + SHRT_DESC + "/#", GET_SHRT_DESC);

        matcher.addURI(AUTHORITY, fd + LONG_DESC + "/*", SEARCH_LONG_DESC);
        matcher.addURI(AUTHORITY, fd + LONG_DESC + "/#", GET_LONG_DESC);

        matcher.addURI(AUTHORITY, NUT_DATA + "/#", GET_NUT_DATA);
        matcher.addURI(AUTHORITY, NUTRIENT + "/#", GET_NUTRIENT);

        String w =  WEIGHT + "/";
        matcher.addURI(AUTHORITY, w + "#", GET_WEIGHT);
        matcher.addURI(AUTHORITY, w + NDB_NO, SEARCH_WEIGHT_NDB_NO);

        String d = DAILY_NUT + "/";
        matcher.addURI(AUTHORITY, DAILY_NUT, INSERT_DAILY_NUT);
        matcher.addURI(AUTHORITY, d + DATE + "/*", SEARCH_DAILY_NUT_DATE);

        String s = DAILY_SUM_NUT + "/";
        matcher.addURI(AUTHORITY, s + DATE, SEARCH_DAILY_SUM_NUT_DATE);
        
        return matcher;
    }

    private NutriDatabase mDB;

    @Override
    public boolean onCreate () {
        String[] pname = NutriInstaller.getAPKExpansionFiles(getContext());
        if (pname.length != 0) {
            mDB = new NutriDatabase(pname);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String selection,
                        String[] sargs, String sorto) {

         Log.d(TAG, "uri=" + uri);

        switch (sUriMatcher.match(uri)) {
            case SEARCH_SHRT_DESC:
                if (sargs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return searchShrtDesc(uri, proj, sargs, sorto);

            case SEARCH_LONG_DESC:
                if (sargs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return searchLongDesc(uri, proj, sargs, sorto);

            case GET_SHRT_DESC:
                return getShrtDesc(uri, proj, sargs, sorto);

            case GET_NUT_DATA:
                return getNutData(uri, proj, sargs, sorto);

            case GET_NUTRIENT:
                return getNutrient(uri, proj, sargs, sorto);

            case SEARCH_WEIGHT_NDB_NO:
                if (sargs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return getWeightNdbno(uri, proj, sargs, sorto);

            case SEARCH_DAILY_SUM_NUT_DATE:
                if (sargs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return searchDailySumNutDate(uri, proj, sargs, sorto);

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

    }

    private Cursor searchShrtDesc(Uri uri, String[] proj, String[] sargs,
                        String sorto) {
         Log.d(TAG, "searchShrtDesc() uri=" + uri);
         Log.d(TAG, "sargs=" + sargs[0]);
        return mDB.searchShrtDesc(proj, sargs, sorto);
    }

    private Cursor searchLongDesc(Uri uri, String[] proj, String[] sargs,
                        String sorto) {
         Log.d(TAG, "searchLongDesc() uri=" + uri);
         Log.d(TAG, "sargs=" + sargs[0]);
        throw new UnsupportedOperationException();
    }


    private Cursor getShrtDesc(Uri uri, String[] proj, String[] sargs,
                        String sorto) {

        Log.d(TAG, "getShrtDesc() - " + uri);

        Long id = ContentUris.parseId(uri);

        String[] selargs = new String[1];
        selargs[0] = id.toString();

        return mDB.getShrtDesc(proj, selargs, sorto);
    }


    private Cursor getNutData(Uri uri, String[] proj, String[] sargs,
                        String sorto) {
        throw new UnsupportedOperationException();
    }


    private Cursor getNutrient(Uri uri, String[] proj, String[] sargs,
                        String sorto) {

        Log.d(TAG, "getNutrient() - " + uri);

        Long id = ContentUris.parseId(uri);

        String[] selargs = new String[1];
        selargs[0] = id.toString();

        return mDB.getNutrient(proj, selargs, sorto);
    }


    private Cursor getWeightNdbno(Uri uri, String[] proj, String[] sargs,
                        String sorto) {

        Log.d(TAG, "getWeightNdbno() - " + uri);

        return mDB.getWeightNdbno(proj, sargs, sorto);
    }

    private Cursor searchDailySumNutDate(Uri uri, String[] proj, String[] sargs,
                        String sorto) {

        Log.d(TAG, "searchDailyNutDate() - " + uri);
        Log.d(TAG, "sargs=" + sargs[0]);
        return mDB.searchDailySumNutDate(proj, sargs, sorto);
    }


    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case SEARCH_SHRT_DESC:
                return FOOD_DES_MROW_MIME_TYPE;

            case GET_SHRT_DESC:
                return FOOD_DES_ROW_MIME_TYPE;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert() uri=" + uri);

        switch (sUriMatcher.match(uri)) {

            case INSERT_DAILY_NUT:
                return insertDailyNut(uri, values);

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

    }

    private Uri insertDailyNut(Uri uri, ContentValues values) {
        long rowId = mDB.insertDailyNut(values);

        if (rowId > 0) {
            Uri ruri = ContentUris.withAppendedId(DAILY_NUT_URI, rowId);
            getContext().getContentResolver().notifyChange(ruri, null);
            return ruri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }





}
