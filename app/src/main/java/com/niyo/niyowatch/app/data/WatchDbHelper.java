package com.niyo.niyowatch.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by oriharel on 4/1/14.
 */
public class WatchDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = WatchDbHelper.class.getSimpleName();
    public static final String ACCOUNTS_TABLE = "accounts";

    private static final String TABLE_ACCOUNTS_CREATE =
            "create table "+ACCOUNTS_TABLE+" ("
            + AccountsTableColumns._ID + " integer primary key autoincrement, "
            + AccountsTableColumns.NAME + " TEXT, "
            + AccountsTableColumns.AMOUNT +  " DOUBLE(16,8), "
            + AccountsTableColumns.DATE + " DATE, "
            + AccountsTableColumns.UPDATE_TIME + " TIMESTAMP);";

    public WatchDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_TAG, "onCreate started");
        db.execSQL(TABLE_ACCOUNTS_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
