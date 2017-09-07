package com.anteknets.singlesignon;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * BaseSSO
 * Created by HAO on 2017/8/24.
 */

abstract class BaseSSO {
    Context mContext;
    String mAction;
    SSOData mData;

    abstract void login();

    abstract void logout();

    abstract void refreshToken();

    abstract boolean isExpired();

    BaseSSO(Context context, String action) {
        mContext = context;
        mAction = action;
    }

    void execute(){
        switch (mAction){
            case SSOConsts.Actions.LOGIN:
                login();
                break;
            case SSOConsts.Actions.REFRESH_TOKEN:
                refreshToken();
                break;
            case SSOConsts.Actions.LOGOUT:
                logout();
                break;
        }
    }

    void removeAllConfig(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(SSOConsts.SSOConfigKey.KEY_SSO_OPENID).apply();
        sharedPreferences.edit().remove(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN).apply();
        sharedPreferences.edit().remove(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN_EXPIRATION).apply();
        sharedPreferences.edit().remove(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN).apply();
        sharedPreferences.edit().remove(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN_EXPIRATION).apply();
    }
}
