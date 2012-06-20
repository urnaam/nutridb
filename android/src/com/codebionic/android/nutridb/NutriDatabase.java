package com.codebionic.android.nutridb;

import android.content.ContentValues;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteException;

import android.util.Log;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class NutriDatabase {
    
    private static final String TAG = "NutriDatabase";

    private String[] mDBpath;

    private HashMap<String,String> mFoodDesMap;
    private HashMap<String,String> mNutrientMap;
    private HashMap<String,String> mWeightMap;
    private HashMap<String,String> mDailyNutMap;
    private HashMap<String,String> mDailySumNutMap;

    private String DAILY_NUT = "DAILY_NUT";

    private SQLiteDatabase mDB = null;

    public NutriDatabase(String[] dbp) {
        mDBpath = dbp;

        // projection maps for query builder
        // must contain all possible column names
        
        // FOOD_DES
        mFoodDesMap = new HashMap<String,String>();
        mFoodDesMap.put("_id" ,"_ROWID_ AS _id");
        mFoodDesMap.put("shrt_desc","shrt_desc");
        mFoodDesMap.put("long_desc","long_desc");
        mFoodDesMap.put("comname","comname");
        mFoodDesMap.put("manufacname","manufacname");
        mFoodDesMap.put("ndb_no","ndb_no");

        // "NUTR_DEF def, NUT_DATA dat, FOOD_DES des";
        mNutrientMap = new HashMap<String,String>();
        mNutrientMap.put("_id", "def._ROWID_ AS _id");
        mNutrientMap.put("nutrdesc", "def.nutrdesc AS nutrdesc");
        mNutrientMap.put("units", "def.units AS units");
        mNutrientMap.put("sr_order", "def.sr_order as sr_order");
        mNutrientMap.put("nutr_val", "dat.nutr_val AS nutr_val");
        mNutrientMap.put("nutr_no", "nutr_no");
        mNutrientMap.put("ndb_no","ndb_no");
        mNutrientMap.put("des._rowid_", "des._rowid_");

        // WEIGHT
        mWeightMap = new HashMap<String,String>();
        mWeightMap.put("_id", "_ROWID_ AS _id");
        mWeightMap.put("seq","seq");
        mWeightMap.put("ndb_no","ndb_no");
        mWeightMap.put("amount","amount");
        mWeightMap.put("msre_desc","msre_desc");
        mWeightMap.put("gm_wgt","gm_wgt");


        // NUTR_DEF def, NUT_DATA dat, DAILY_NUT dnut;
        mDailyNutMap = new HashMap<String,String>();
        mDailyNutMap.put("_id", "_ROWID_ AS _id");
        mDailyNutMap.put("date","date");
        mDailyNutMap.put("time","time");
        mDailyNutMap.put("ndb_no","ndb_no");
        mDailyNutMap.put("seq","seq");
        mDailyNutMap.put("amount","amount");
        mDailyNutMap.put("weight","weight");



        // NUTR_DEF def, NUT_DATA dat, DAILY_NUT dnut, S
        mDailySumNutMap = new HashMap<String,String>();
        mDailySumNutMap.put("_id", "def._ROWID_ AS _id");
        mDailySumNutMap.put("date","dnut.date AS date");
        mDailySumNutMap.put("time","dnut.time AS time");
        mDailySumNutMap.put("ndb_no","dnut.ndb_no AS ndb_no");
        mDailySumNutMap.put("seq","dnut.seq AS seq");
        mDailySumNutMap.put("amount","dnut.amount AS amount");
        mDailySumNutMap.put("weight","dnut.weight AS weight");
        mDailySumNutMap.put("nutrdesc", "def.nutrdesc AS nutrdesc");
        mDailySumNutMap.put("units", "def.units AS units");
        mDailySumNutMap.put("sr_order", "def.sr_order as sr_order");
        mDailySumNutMap.put("sum_nutr_val", "S.sum_nutr_val AS sum_nutr_val");

        mDB = SQLiteDatabase.openDatabase(mDBpath[0], null,
            SQLiteDatabase.OPEN_READWRITE);

    }

    public Cursor searchShrtDesc(String[] columns, String[] sargs,
        String sorto) {

        Log.d(TAG, "searchShrtDesc()");

        String selection = "shrt_desc LIKE ?";
        String[] selargs = new String[1];

        if (!TextUtils.isEmpty(sargs[0])) {
            selargs[0] = sargs[0].toLowerCase() + "%";
        }
        else {
            selargs[0] = "%";
        }

        String sql = buildQuery("FOOD_DES", mFoodDesMap,
                        columns, selection, selargs,
                        null, null, sorto, null);

        Log.d(TAG, "buildquery() - " + sql);
        return rawQuery(sql, selargs);
    }


    public Cursor getShrtDesc(String[] columns, String[] sargs, String sorto) {

        String selection = "_id = ?";
    
        String sql = buildQuery("FOOD_DES", mFoodDesMap,
                        columns, selection,sargs,
                        null, null, sorto, null);

        Log.d(TAG, "buildquery() - " + sql);
        return rawQuery(sql, sargs);
    }


    public Cursor getNutrient(String[] columns, String[] sargs, String sorto) {

        String tables = 
            "NUTR_DEF def, NUT_DATA dat, FOOD_DES des";

        String selection = 
          "des._rowid_ = ? and des.ndb_no = dat.ndb_no "
          + "and dat.nutr_no = def.nutr_no";

       String sql = buildQuery(tables, mNutrientMap,
                        columns, selection, sargs,
                        null, null, sorto, null);
        Log.d(TAG, "buildquery() - " + sql);

        return rawQuery(sql, sargs);
    }


    public Cursor getWeightNdbno(String[] columns, String[] sargs,
        String sorto) {

        String selection = "ndb_no = ?";

        String sql = buildQuery("WEIGHT", mWeightMap, columns, selection,  sargs,
            null, null, sorto, null);

        Log.d(TAG, "buildquery() - " + sql);
        return rawQuery(sql, sargs);
    }


    public Cursor searchDailySumNutDate(String[] columns, String[] sargs,
        String sorto) {

        Log.d(TAG, "searchDailyNutDate()");

        if (TextUtils.isEmpty(sargs[0])) {
            sargs[0] = "date('now')";
        }

        String tables = "NUTR_DEF def, "
            + "(select dat.nutr_no as nutr_no, "
            + "sum(dat.nutr_val*(dnut.weight/100.0)) as sum_nutr_val "
            + "from NUT_DATA dat inner join DAILY_NUT dnut "
            + "on dat.ndb_no = dnut.ndb_no "
            + "where dnut.date = '"
            + sargs[0]
            + "' group by dat.nutr_no) as S ";

        String selection = "def.nutr_no = S.nutr_no";



        String sql = buildQuery(tables, mDailySumNutMap,
                        columns, selection, sargs,
                        null, null, sorto, null);

        Log.d(TAG, "buildquery() - " + sql);
        return rawQuery(sql, null);
    }


    public long insertDailyNut(ContentValues values) {
        return mDB.insert(DAILY_NUT, null, values);
    }

    

    private String buildQuery(String table, Map<String, String> pmap,
        String[] columns, String selection, String[] sargs,
        String groupBy, String having, String sorto, String limit) {

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);
        builder.setProjectionMap(pmap);

        String sql = builder.buildQuery(columns, selection, sargs,
            groupBy, having, sorto, limit);

        return sql;
    }

    private String buildUnionQuery(String[] selection, String sorto, 
        String limit) {
        
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        String sql = builder.buildUnionQuery (selection, sorto, limit);
        return sql;
    }

    private Cursor rawQuery(String sql, String[] sargs) {

        try {
            if (mDB == null) {
                mDB = SQLiteDatabase.openDatabase(mDBpath[0], null,
                        SQLiteDatabase.OPEN_READWRITE);
            }

            Cursor cursor = mDB.rawQuery(sql, sargs);

            if (cursor == null) {
                Log.d(TAG, "cursor == null");
                return null;
            } else if (!cursor.moveToFirst()) {
                Log.d(TAG, "cursor empty");
                cursor.close();
                return null;
            }
            return cursor;
        }
        catch (SQLiteException e) {
                Log.d(TAG, "SQLiteException");
        }
        return null;
    }

}

