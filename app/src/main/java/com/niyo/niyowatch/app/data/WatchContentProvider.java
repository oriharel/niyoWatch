package com.niyo.niyowatch.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by oriharel on 4/1/14.
 */
public class WatchContentProvider extends ContentProvider {

    private static final String LOG_TAG = WatchContentProvider.class.getSimpleName();
    private WatchDbHelper _dbHelper;
    public static final String DATABASE_NAME = "niyowatch.db";
    public static final int DATABASE_VERSION = 1;

    public static final int ACCOUNTS = 1;
    public static final int ACCOUNT = 2;

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> sAccountsProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(DataConstans.AUTHORITY, "accounts", ACCOUNTS);
        sUriMatcher.addURI(DataConstans.AUTHORITY, "accounts/#", ACCOUNT);

        sAccountsProjectionMap = new HashMap<String, String>();
        sAccountsProjectionMap.put(AccountsTableColumns._ID, AccountsTableColumns._ID);
        sAccountsProjectionMap.put(AccountsTableColumns.NAME, AccountsTableColumns.NAME);
        sAccountsProjectionMap.put(AccountsTableColumns.AMOUNT, AccountsTableColumns.AMOUNT);
        sAccountsProjectionMap.put(AccountsTableColumns.DATE, AccountsTableColumns.DATE);
        sAccountsProjectionMap.put(AccountsTableColumns.UPDATE_TIME, AccountsTableColumns.UPDATE_TIME);
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate started");
        Context context = getContext();
        setDbHelper(new WatchDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION));
        return getWritableDb() == null ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query started with "+uri);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(WatchDbHelper.ACCOUNTS_TABLE);
        qb.setProjectionMap(sAccountsProjectionMap);

        switch (sUriMatcher.match(uri)) {
            case ACCOUNTS:
                break;
            case ACCOUNT:
                qb.appendWhere(
                        AccountsTableColumns.NAME +
                                "=" +
                                uri.getPathSegments().get(AccountsTableColumns.COLUMN_NAME_INDEX)
                );
                break;

        }

        Log.d(LOG_TAG, "going to query with selection "+selection);
        Log.d(LOG_TAG, "projection is " + getArrayAsString(projection));
        Log.d(LOG_TAG, "selectionArgs is "+getArrayAsString(selectionArgs));
        Log.d(LOG_TAG, "sort order is "+sortOrder);

        String orderBy = AccountsTableColumns.NAME;

        Cursor cursor = qb.query(getReadbleDb(), projection, selection, selectionArgs, null, null, orderBy);

        Log.d(LOG_TAG, "got "+cursor.getCount()+" results from uri "+uri);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    public static String getArrayAsString(Object[] array)
    {
        String result = "";
        if (array != null) {
            for (Object object : array) {
                result += object.toString() + ", ";
            }
        }
        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ACCOUNTS:
                return DataConstans.CONTENT_TYPE;

            // If the pattern is for account names, returns the account name content type.
            case ACCOUNT:
                return DataConstans.CONTENT_ITEM_TYPE;

            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert started "+uri);
        String nameValue = values.getAsString(AccountsTableColumns.NAME);
        String dateValue = values.getAsString(AccountsTableColumns.DATE);
        Log.d(LOG_TAG, "the account name is "+nameValue);
        Log.d(LOG_TAG, "the date is "+ dateValue);

        String selection = AccountsTableColumns.NAME+" = '"+nameValue+"' AND "+AccountsTableColumns.DATE+" = '"+dateValue+"'";
        Log.d(LOG_TAG, "selection is: "+selection);
        int deleteResult = delete(uri, selection, null);
        Log.d(LOG_TAG, "trying to delete first, got "+deleteResult+" delete results");
        String table = WatchDbHelper.ACCOUNTS_TABLE;
        getWritableDb().insert(table, AccountsTableColumns.NAME, values);
        Log.d(LOG_TAG, "notifying "+uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getWritableDb();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for friends, does a delete
            // based on the incoming "where" columns and arguments.
            case ACCOUNTS:
                Log.d(LOG_TAG, "trying to delete for selection: "+selection);
                count = db.delete(
                        WatchDbHelper.ACCOUNTS_TABLE,  // The database table name
                        selection,                     // The incoming where clause column names
                        selectionArgs                  // The incoming where clause values
                );
                break;

            // If the incoming URI matches a single friend ID, does the delete based on the
            // incoming data, but modifies the where clause to restrict it to the
            // particular friend ID.
            case ACCOUNT:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired friend ID.
                 */
                finalWhere =
                        AccountsTableColumns.NAME +                              // The ID column name
                                " = " +                                          // test for equality
                                uri.getPathSegments().                           // the incoming note ID
                                        get(AccountsTableColumns.COLUMN_NAME_INDEX) +
                                " AND "+ AccountsTableColumns.DATE + " = "+ uri.getPathSegments().get(AccountsTableColumns.COLUMN_DATE_INDEX);

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                // Performs the delete.
                count = db.delete(
                        WatchDbHelper.ACCOUNTS_TABLE,  // The database table name.
                        finalWhere,                // The final WHERE clause
                        selectionArgs                  // The incoming where clause values.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getWritableDb();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case ACCOUNTS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                        WatchDbHelper.ACCOUNTS_TABLE, // The database table name.
                        values,                   // A map of column names and new values to use.
                        selection,                    // The where clause column names.
                        selectionArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case ACCOUNT:
                // From the incoming URI, get the note ID
                String entryId = uri.getPathSegments().get(AccountsTableColumns.COLUMN_ID_PATH_INDEX);

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere =
                        AccountsTableColumns._ID +                              // The ID column name
                                " = " +                                          // test for equality
                                uri.getPathSegments().                           // the incoming note ID
                                        get(AccountsTableColumns.COLUMN_ID_PATH_INDEX)
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (selection !=null) {
                    finalWhere = finalWhere + " AND " + selection;
                }


                // Does the update and returns the number of rows updated.
                count = db.update(
                        WatchDbHelper.ACCOUNTS_TABLE, // The database table name.
                        values,                   // A map of column names and new values to use.
                        finalWhere,               // The final WHERE clause to use
                        // placeholders for whereArgs
                        selectionArgs                 // The where clause column values to select on, or
                        // null if the values are in the where argument.
                );
                break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }

    public void setDbHelper(WatchDbHelper dbHelper) {
        this._dbHelper = dbHelper;
    }

    public SQLiteDatabase getWritableDb() {
        return getDbHelper().getWritableDatabase();
    }

    public SQLiteDatabase getReadbleDb() {
        return getDbHelper().getReadableDatabase();
    }

    public SQLiteOpenHelper getDbHelper() {
        return this._dbHelper;
    }
}
