package com.anteknets.singlesignon;

/**
 * Created by HAO on 2017/8/28.
 */

public class SSOError extends SSOItem {
    private int mErrorCode;
    private String mErrorMsg;

    public SSOError(String type, String action, int code, String msg) {
        super(type, action);
        this.mErrorCode = code;
        this.mErrorMsg = msg;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        mErrorMsg = errorMsg;
    }
}
