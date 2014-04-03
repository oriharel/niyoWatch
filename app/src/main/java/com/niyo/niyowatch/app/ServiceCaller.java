package com.niyo.niyowatch.app;

/**
 * Created by oriharel on 3/29/14.
 */
public interface ServiceCaller {

    public abstract void success(Object data);
    public abstract void failure(Object data, String description);
}
