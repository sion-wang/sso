package com.anteknets.singlesignon;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * SSOManager
 * Created by HAO on 2017/8/24.
 */

public class SSOManager {
    private static final String TAG = SSOManager.class.getSimpleName();
    private static SSOManager _instance;
    private Map<String, SSOCallback> mSSOCallbackMap;
    private String mSSOType = "";

    private SSOManager() {
        mSSOCallbackMap = new HashMap<>();
    }

    public static SSOManager getInstance() {
        if (_instance == null) {
            _instance = new SSOManager();
        }
        return _instance;
    }

    public void setSSOType(String ssoType){
        Log.d(TAG, "Set SSO type: " + mSSOType);
        mSSOType = ssoType;
    }

    public SSOCallback getSSOCallback(String action){
        return mSSOCallbackMap.get(action);
    }

    public void login(Context context, SSOCallback callback) {
        Log.d(TAG, "login. SSO type: " + mSSOType);
        mSSOCallbackMap.put(SSOConsts.Actions.LOGIN, callback);
        Intent ssoIntent = new Intent(context, SSOSignInActivity.class);
        ssoIntent.putExtra(SSOConsts.KEY_SSO_TYPE, mSSOType);
        context.startActivity(ssoIntent);
    }

    public void logout(Context context, SSOCallback callback) {
        Log.d(TAG, "logout. SSO type: " + mSSOType);
        mSSOCallbackMap.put(SSOConsts.Actions.LOGOUT, callback);
        switch (mSSOType){
            case SSOConsts.ClientType.GOOGLE:
                GoogleSSO googleSSO = new GoogleSSO(context, SSOConsts.Actions.LOGOUT);
                googleSSO.execute();
                break;
            case SSOConsts.ClientType.QQ:
                QqSSO qqSSO = new QqSSO(context, SSOConsts.Actions.LOGOUT);
                qqSSO.execute();
                break;
            case SSOConsts.ClientType.WeChat:
                WeChatSSO weChatSSO = new WeChatSSO(context, SSOConsts.Actions.LOGOUT);
                weChatSSO.execute();
                break;
            default:
                SSOError ssoError = new SSOError(mSSOType, SSOConsts.Actions.LOGOUT, 0, "Not defined SSO type.");
                callback.onFailed(ssoError);
                break;
        }
    }

    public void refreshToken(Context context, SSOCallback callback){
        Log.d(TAG, "refreshToken. SSO type: " + mSSOType);
        mSSOCallbackMap.put(SSOConsts.Actions.REFRESH_TOKEN, callback);
        switch (mSSOType){
            case SSOConsts.ClientType.GOOGLE:
                GoogleSSO googleSSO = new GoogleSSO(context, SSOConsts.Actions.REFRESH_TOKEN);
                googleSSO.execute();
                break;
            case SSOConsts.ClientType.QQ:
                QqSSO qqSSO = new QqSSO(context, SSOConsts.Actions.REFRESH_TOKEN);
                qqSSO.execute();
                break;
            case SSOConsts.ClientType.WeChat:
                WeChatSSO weChatSSO = new WeChatSSO(context, SSOConsts.Actions.REFRESH_TOKEN);
                weChatSSO.execute();
                break;
            default:
                SSOError ssoError = new SSOError(mSSOType, SSOConsts.Actions.REFRESH_TOKEN, 0, "Not defined SSO type.");
                callback.onFailed(ssoError);
                break;
        }
    }
}
