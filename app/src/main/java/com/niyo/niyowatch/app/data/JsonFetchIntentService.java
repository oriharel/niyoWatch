package com.niyo.niyowatch.app.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.niyo.niyowatch.app.GenericHttpRequestTask;
import com.niyo.niyowatch.app.ServiceCaller;
import com.niyo.niyowatch.app.WatchApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * Created by oriharel on 4/1/14.
 */
public class JsonFetchIntentService extends IntentService {

    public static final String LOG_TAG = JsonFetchIntentService.class.getSimpleName();
    public static final String URLS_EXTRA = "urls";

    public JsonFetchIntentService() {
        super("jsons");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        List<String> urls = intent.getStringArrayListExtra(URLS_EXTRA);

        ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                ((WatchApplication)getApplication()).decreaseFetchers();
                JSONArray result = (JSONArray)data;
                Log.d(LOG_TAG, "received "+result);
                try {

                    for (int i = 0; i < result.length(); i++) {

                        JSONObject firstAccountOb = result.getJSONObject(i);
                        String accountName = firstAccountOb.getString("name");

                        ContentValues values = new ContentValues();
                        values.put(AccountsTableColumns.NAME, accountName);
                        values.put(AccountsTableColumns.AMOUNT, firstAccountOb.getString("amount"));
                        values.put(AccountsTableColumns.DATE, firstAccountOb.getString(("date")));
                        values.put(AccountsTableColumns.UPDATE_TIME, Calendar.getInstance().getTimeInMillis());
                        Log.d(LOG_TAG, "values date is "+values.getAsString(AccountsTableColumns.DATE));
                        Uri uri = Uri.parse(DataConstans.SCHEME+DataConstans.AUTHORITY+DataConstans.ACCOUNTS);
                        Log.d(LOG_TAG, "uri is "+uri);
                        getContentResolver().insert(uri, values);
                    }


                } catch (JSONException e) {
                    ((WatchApplication)getApplication()).decreaseFetchers();
                    e.printStackTrace();
                }

            }

            @Override
            public void failure(Object data, String description) {
                Log.e(LOG_TAG, "Error fetching data");
            }
        };

        for (String urlStr : urls) {

            Log.d(LOG_TAG, "sending url " + urlStr);
            GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
            try {
                ((WatchApplication)getApplication()).increaseFetchers();
                task.execute(urlStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error!", e);
            }
        }

    }
}
