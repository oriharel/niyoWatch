package com.niyo.niyowatch.app.data;

import android.net.Uri;

/**
 * Created by oriharel on 4/1/14.
 */
public class DataConstans {
    public static final String AUTHORITY = "com.niyo.watch.provider";
    public static String SCHEME = "content://";
    public static final String ACCOUNTS = "/accounts";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.radar.account";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.radar.account";
    public static final Uri ACCOUNTS_URI =  Uri.parse(SCHEME + AUTHORITY + ACCOUNTS);
    public static final String[] ACCOUNTS_SUMMARY_PROJECTION = new String[] {
            AccountsTableColumns._ID,
            AccountsTableColumns.NAME,
            AccountsTableColumns.AMOUNT,
            AccountsTableColumns.DATE,
            AccountsTableColumns.UPDATE_TIME
    };
}
