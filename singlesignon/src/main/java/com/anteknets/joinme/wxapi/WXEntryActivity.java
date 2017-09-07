package com.anteknets.joinme.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.anteknets.singlesignon.SSOCallback;
import com.anteknets.singlesignon.SSOConsts;
import com.anteknets.singlesignon.SSOData;
import com.anteknets.singlesignon.SSOError;
import com.anteknets.singlesignon.SSOManager;
import com.anteknets.singlesignon.WXUserModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private String code;
    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    private String GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
    private String GetTokenValid = "https://api.weixin.qq.com/sns/auth?access_token=ACCESS_TOKEN&openid=OPENID";
    private String GetTokenRefresh = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
    private WXUserModel userModel;
    private IWXAPI api;
    private String openid;
    private String access_token;
    private String expires_in;
    private String refresh_token;
    private String expires_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, SSOConsts.WeChatConsts.APP_ID, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
//            ErrorCode：ERR_OK = 0(用户同意)；ERR_AUTH_DENIED = -4（用户拒绝授权）；ERR_USER_CANCEL = -2（用户取消）
            case BaseResp.ErrCode.ERR_OK:
                // 得到所需的code
                code = ((SendAuth.Resp) baseResp).code;
//                Log.e("", "code返回码---------" + code);
//                Toast.makeText(this, "code " + code, Toast.LENGTH_SHORT).show();

                String get_access_token = getCodeRequest(code);
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(get_access_token, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                            /*{
                                "access_token":"ACCESS_TOKEN",
                                    "expires_in":7200,
                                    "refresh_token":"REFRESH_TOKEN",
                                    "openid":"OPENID",
                                    "scope":"SCOPE",
                                    "unionid":"o6_bmasdasdsad6_2sgVt7hMZOPfL"
                            }*/

                        access_token = response.optString("access_token");//1
                        openid = response.optString("openid");//2
                        expires_in = response.optString("expires_in"); //3
                        refresh_token = response.optString("refresh_token");   //4
                        String scope = response.optString("scope");
                        expires_refresh = String.valueOf(SSOConsts.WeChatConsts.REFRESH_TOKEN_VALIDITY_PERIOD);

                        long expiration = System.currentTimeMillis() + Long.parseLong(expires_in) * 1000;
                        long refreshExpiration = System.currentTimeMillis() + Long.parseLong(expires_refresh) * 1000;

                        SharedPreferences sharedPreferences = getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_OPENID, openid).apply();
                        sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN, access_token).apply();
                        sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN, refresh_token).apply();
                        sharedPreferences.edit().putLong(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN_EXPIRATION, expiration).apply();
                        sharedPreferences.edit().putLong(SSOConsts.SSOConfigKey.KEY_SSO_REFRESH_TOKEN_EXPIRATION, refreshExpiration).apply();

                        String get_user_info_url = getUserInfo(access_token, openid);
                        String token = getTokenValid(access_token, openid);
                        getTokenValid(token);

                        getUserInfo(get_user_info_url);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                        if (ssoCallback != null) {
                            SSOError ssoError = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGIN, statusCode, responseString);
                            ssoCallback.onFailed(ssoError);
                        }
                    }
                });

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                SSOError ssoError = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGIN, baseResp.errCode, "ERR_USER_CANCEL");
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                if (ssoCallback != null) {
                    ssoCallback.onCancel(ssoError);
                }
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                SSOError ssoErrorDeny = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGIN, baseResp.errCode, "ERR_AUTH_DENIED");
                SSOCallback ssoCallbackDeny = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                if (ssoCallbackDeny != null) {
                    ssoCallbackDeny.onCancel(ssoErrorDeny);
                }
                finish();
                break;
        }

    }

    //检查Token是否有效
    private void getTokenValid(String token) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                JSONObject tokens = response;
                try {
//                    {"errcode":0,"errmsg":"ok"}
                    String string = response.getString("errmsg");
                    String errcode = response.getString("errcode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void getUserInfo(String user_info_url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(user_info_url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                userModel = new WXUserModel();
                userModel.setRefresh_token(refresh_token);
                userModel.setOpenid(openid);
                userModel.setAccess_token(access_token);
                userModel.setExpires_in(expires_in);
                userModel.setExpires_refresh(expires_refresh);
                userModel.setNickname(response.optString("nickname"));
                userModel.setUnionid(response.optString("unionid"));
                userModel.setHeadimgurl(response.optString("headimgurl"));

                SSOData ssoData = SSOData.create(SSOConsts.Actions.LOGIN, userModel);
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                if (ssoCallback != null) {
                    ssoCallback.onSuccess(ssoData);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                if (ssoCallback != null) {
                    SSOError ssoError = new SSOError(SSOConsts.ClientType.WeChat, SSOConsts.Actions.LOGIN, statusCode, responseString);
                    ssoCallback.onFailed(ssoError);
                }
            }
        });
    }

    /**
     * 获取access_token的URL（微信）
     *
     * @param code 授权时，微信回调给的
     * @return URL
     */
    private String getCodeRequest(String code) {
        String result = null;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(SSOConsts.WeChatConsts.APP_ID));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(SSOConsts.WeChatConsts.APP_SERECET));
        GetCodeRequest = GetCodeRequest.replace("CODE", urlEnodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }

    /**
     * 获取用户个人信息的URL（微信）
     *
     * @param access_token 获取access_token时给的
     * @param openid       获取access_token时给的
     * @return URL
     */
    private String getUserInfo(String access_token, String openid) {
        String result = null;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN", urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID", urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }

    private String getTokenValid(String access_token, String openid) {
        String result = null;
        GetTokenValid = GetTokenValid.replace("ACCESS_TOKEN", urlEnodeUTF8(access_token));
        GetTokenValid = GetTokenValid.replace("OPENID", urlEnodeUTF8(openid));
        result = GetTokenValid;
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
