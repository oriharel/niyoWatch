package com.niyo.niyowatch.app.data;

import android.provider.BaseColumns;

/**
 * Created by oriharel on 4/1/14.
 */
public class AccountsTableColumns implements BaseColumns {

    public static final String NAME = "name";
    public static final String AMOUNT = "amount";
    public static final String DATE = "date";
    public static final String UPDATE_TIME = "update_time";

    public static final int COLUMN_ID_PATH_INDEX = 1;
    public static final int COLUMN_NAME_INDEX = 2;
    public static final int COLUMN_AMOUNT_INDEX = 3;
    public static final int COLUMN_DATE_INDEX = 4;
    public static final int COLUMN_UPDATE_TIME_INDEX = 5;
}
