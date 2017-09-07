package com.anteknets.singlesignon;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HAO on 2017/8/28.
 */

public abstract class SSOItem{
    protected String mType;
    protected String mAction;

    protected Map<String, String> mExtras = new HashMap<>();

    public SSOItem(String type, String action) {
        this.mType = type;
        this.mAction = action;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        mAction = action;
    }

    public String getExtra(String key) {
        return mExtras.get(key);
    }

    public void putExtra(String key, String value) {
        this.mExtras.put(key, value);
    }

    protected void setExtras(Map<String, String> extras) {
        this.mExtras = extras;
    }
    protected Map<String, String> getExtras() {
        return mExtras;
    }
}
