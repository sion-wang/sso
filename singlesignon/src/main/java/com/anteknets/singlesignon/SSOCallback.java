package com.anteknets.singlesignon;

/**
 * SSO Callback
 * Created by HAO on 2017/8/24.
 */

public interface SSOCallback {
    void onSuccess(SSOData data);
    void onFailed(SSOError error);
    void onCancel(SSOError error);
}
