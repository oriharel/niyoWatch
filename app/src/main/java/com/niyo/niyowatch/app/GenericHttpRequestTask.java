package com.niyo.niyowatch.app;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by oriharel on 3/29/14.
 */
public class GenericHttpRequestTask extends AsyncTask<String, Void, JSONObject> {

    private ServiceCaller _caller;
    private static final String LOG_TAG = GenericHttpRequestTask.class.getSimpleName();
    public GenericHttpRequestTask(ServiceCaller caller) {
        _caller = caller;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        try {
            URL url = new URL(params[0]);
            Log.d(LOG_TAG, "Starting http request for "+params[0]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int sc = con.getResponseCode();
            Log.d(LOG_TAG, url.getHost()+" returned "+sc);

            if (sc == 200) {
                InputStream is = con.getInputStream();
                String response = readResponse(is);
                Log.d(LOG_TAG, "got response "+response);
                is.close();
                JSONObject jsonRes = new JSONObject(response);
                return jsonRes;
            }
            else if (sc == 401) {
                Log.e(LOG_TAG, "Server authentication error, please try again");
                String errorMsg = readResponse(con.getErrorStream());
                JSONObject jsonError = new JSONObject("{errorCode:"+sc+"}");
                return jsonError;
            }
            else {
                Log.e(LOG_TAG, "Server  error, please try again");
                String errorMsg = readResponse(con.getErrorStream());
                JSONObject jsonError = new JSONObject("{errorCode:"+sc+"}");
                return jsonError;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result == null) {
                _caller.failure("Result is error", "Gor error code");
            }
            else if (result.has("errorCode")) {
                _caller.failure(result.get("errorCode"), "Gor error code");
            }
            else {
                Log.d(LOG_TAG, "Http request succeeded");
                _caller.success(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
