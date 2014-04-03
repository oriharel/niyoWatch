package com.niyo.niyowatch.app;

import android.app.Application;

/**
 * Created by oriharel on 4/3/14.
 */
public class WatchApplication extends Application {

    private int fetchersCounter = 0;
    private ServiceCaller _callback;

    public void increaseFetchers(){
        fetchersCounter++;
        if (fetchersCounter > 0) {
            _callback.success(null);
        }
    }

    public void decreaseFetchers(){
        fetchersCounter--;
        if (fetchersCounter < 1) {
            _callback.failure(null, null);
        }
    }

    public Boolean isFetching(){
        return fetchersCounter > 0;
    }

    public void registerProgressCallback(ServiceCaller callback) {
        _callback = callback;
    }
}
