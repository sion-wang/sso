package com.anteknets.singlesignon;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

/**
 * WeChat SSO
 * Created by Sion Wang on 2017/9/4.
 */

class WeChatSSO extends BaseSSO {
    private static final String TAG = WeChatSSO.class.getSimpleName();

    WeChatSSO(Context context, String action) {
        super(context, action);
    }

    @Override
    void login() {
        Log.d(TAG, "login.");
        IWXAPI api = WXAPIFactory.createWXAPI(mContext, SSOConsts.WeChatConsts.APP_ID, true);
        // 将该app注册到微信
        api.registerApp(SSOConsts.WeChatConsts.APP_ID);

        if (!api.isWXAppInstalled()) {
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
            if (ssoCallback != null) {
                SSOError ssoError = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGIN, 0, "请先安装微信");
                ssoCallback.onFailed(ssoError);
            }
            return;
        }

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "app_wechat";
        api.sendReq(req);
    }

    @Override
    void logout() {
        Log.d(TAG, "logout.");
        SSOData ssoData = new SSOData(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGOUT);
        SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGOUT);
        if (ssoCallback != null) {
            ssoCallback.onSuccess(ssoData);
        }
    }

    @Override
    void refreshToken() {
        Log.d(TAG, "refreshToken.");
        if(isExpired()){
            if (isRefreshTokenExpired()){ //Token and RefreshToken both expired. Force logout.
                Log.d(TAG, "Token and RefreshToken both expired. Force logout.");
                removeAllConfig();
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);
                if (ssoCallback != null) {
                    SSOError ssoError = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.REFRESH_TOKEN, SSOConsts.ErrorCode.ERROR_NEED_LOGOUT, "RefreshToken expired.");
                    ssoCallback.onFailed(ssoError);
                }
            } else { //get new token by api
                Log.d(TAG, "Get new token by api.");
                requestRefreshToken();
            }
        } else { //Need not to refresh
            Log.d(TAG, "Need not to refresh.");
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);
            if (ssoCallback != null) {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
                String openId = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_OPENID, "");
                String token = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN, "");
                SSOData ssoData = new SSOData(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGOUT);
                ssoData.setOpenId(openId);
                ssoData.setToken(token);
                ssoCallback.onSuccess(ssoData);
            }
        }
    }

    @Override
    boolean isExpired() {
        boolean isInValid = false;
        // 获取access_token的失效时间
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
        long expiration = sharedPreferences.getLong(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN_EXPIRATION, 0);
        long l = System.currentTimeMillis();
        if ((expiration - l) / 1000 < 0) {
            // 此时表示已过期
            isInValid = true;
        }

        return isInValid;
    }

    private boolean isRefreshTokenExpired(){
        boolean isInValid = false;
        // 获取access_token的失效时间
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
        long expiration = sharedPreferences.getLong(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN_EXPIRATION, 0);
        long l = System.currentTimeMillis();
        if ((expiration - l) / 1000 < 0) {
            // 此时表示已过期
            isInValid = true;
        }

        return isInValid;
    }


    private void requestRefreshToken() {
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
        String refreshToken = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN, "");
        String refreshTokenUrl = genRefreshTokenUrl(SSOConsts.WeChatConsts.APP_ID, refreshToken);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(refreshTokenUrl, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                String access_token = response.optString("access_token");
                String expires_in = response.optString("expires_in");
                long expiration = System.currentTimeMillis() + Long.parseLong(expires_in) * 1000;

                sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN, access_token).apply();
                sharedPreferences.edit().putLong(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN_EXPIRATION, expiration).apply();

                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);
                if (ssoCallback != null) {
                    String openId = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_OPENID, "");
                    SSOData ssoData = new SSOData(SSOConsts.ClientType.WeChat, SSOConsts.Actions.REFRESH_TOKEN);
                    ssoData.setOpenId(openId);
                    ssoData.setToken(access_token);
                    ssoCallback.onSuccess(ssoData);
                }
            }
        });
    }

    private static final String REFRESH_TOKEN_URL_SAMPLE = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";

    private String genRefreshTokenUrl(String appId, String refreshToken) {
        String result = REFRESH_TOKEN_URL_SAMPLE;
        result = result.replace("REFRESH_TOKEN", urlEnodeUTF8(refreshToken));
        result = result.replace("APPID", urlEnodeUTF8(appId));
        return result;
    }

    private String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
