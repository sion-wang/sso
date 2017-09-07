package com.anteknets.singlesignon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

/**
 * QQ SSO
 * Created by HAO on 2017/8/28.
 */

class QqSSO extends BaseSSO {
    private static final String TAG = QqSSO.class.getSimpleName();
    private static Tencent mTencent;
    private IUiListener mIUiListener;
    private Activity mActivity;

    QqSSO(Context context, String action) {
        super(context, action);
        mData = new SSOData(SSOConsts.ClientType.QQ, action);
        if (mTencent == null) {
            mTencent = Tencent.createInstance(SSOConsts.QQConsts.QQ_LOGIN_APPID, mContext);
        }
    }

    QqSSO(Context context, String action, Activity activity) {
        super(context, action);
        mData = new SSOData(SSOConsts.ClientType.QQ, action);
        if (mTencent == null) {
            mTencent = Tencent.createInstance(SSOConsts.QQConsts.QQ_LOGIN_APPID, mContext);
        }
        mActivity = activity;
    }

    @Override
    public void login() {
        Log.d(TAG, "login. Activity:" + mActivity.getLocalClassName());
        mIUiListener = new TencentUiListener();
        if (!mTencent.isSessionValid()) {
            mTencent.login(mActivity, "all", mIUiListener);
        } else {
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
            if (ssoCallback != null) {
                SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGIN, 0, "此QQ账号已经登录过");
                ssoCallback.onFailed(ssoError);
            }
            mActivity.finish();
        }
    }

    @Override
    public void logout() {
        Log.d(TAG, "logout.");
        if (mTencent != null) {
            mTencent.logout(mContext);// 注销
            removeAllConfig();

            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGOUT);
            if (ssoCallback != null) {
                SSOData ssoData = new SSOData(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGOUT);
                ssoCallback.onSuccess(ssoData);
            }
        }
    }

    @Override
    public void refreshToken() {
        Log.d(TAG, "refreshToken.");
        if (isExpired()) { //clean config and force logout
            if (mTencent != null) {
                mTencent.logout(mContext);// 注销
                removeAllConfig();

                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);
                if (ssoCallback != null) {
                    SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.REFRESH_TOKEN, SSOConsts.ErrorCode.ERROR_NEED_LOGOUT, "Token expired.");
                    ssoCallback.onFailed(ssoError);
                }
            }
        } else { //Need not to refresh
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);
            if (ssoCallback != null) {
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
                String openId = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_OPENID, "");
                String token = sharedPreferences.getString(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN, "");
                SSOData ssoData = new SSOData(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGOUT);
                ssoData.setOpenId(openId);
                ssoData.setToken(token);
                ssoCallback.onSuccess(ssoData);
            }
        }
    }

    @Override
    public boolean isExpired() {
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

    IUiListener getIUiListener() {
        return mIUiListener;
    }

    private class TencentUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            doComplete((JSONObject) response);
        }

        private void doComplete(JSONObject response) {
            //Toast.makeText(mContext, "QQ调用成功", Toast.LENGTH_SHORT).show();
            String openID = response.optString("openid");
            String accessToken = response.optString("access_token");
            String expires = response.optString("expires_in");
            long expireTime = System.currentTimeMillis() + Long.parseLong(expires) * 1000;
            mTencent.setOpenId(openID);
            mTencent.setAccessToken(accessToken, expires);

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SSOConsts.SSOConfigKey.NAME_SSO_SP, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_OPENID, openID).apply();
            sharedPreferences.edit().putString(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN, accessToken).apply();
            sharedPreferences.edit().putLong(SSOConsts.SSOConfigKey.KEY_SSO_TOKEN_EXPIRATION, expireTime).apply();

            mData.setToken(accessToken);
            mData.setOpenId(openID);
            mData.setExpireTime(expireTime);

            getUserInfo();
        }

        @Override
        public void onError(UiError e) {
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
            if (ssoCallback != null) {
                SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGIN, e.errorCode, e.errorMessage);
                ssoCallback.onFailed(ssoError);
            }
            mActivity.finish();
        }

        @Override
        public void onCancel() {
            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
            if (ssoCallback != null) {
                SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGIN, 0, "");
                ssoCallback.onCancel(ssoError);
            }
            mActivity.finish();
        }

        private void getUserInfo() {

            UserInfo userInfo = new UserInfo(mContext, mTencent.getQQToken());
            userInfo.getUserInfo(new IUiListener() {
                @Override
                public void onComplete(Object response) {
                    //获取nickname信息
                    JSONObject userInfo = (JSONObject) response;
                    String name = userInfo.optString("nickname");
                    String avatarUrl = userInfo.optString("figureurl_qq_2");
                    if (TextUtils.isEmpty(avatarUrl)) {
                        avatarUrl = userInfo.optString("figureurl_2");
                    }
                    mData.setName(name);
                    mData.setAvatarUrl(avatarUrl);

                    SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                    if (ssoCallback != null) {
                        SSOData ssoData = SSOData.clone(SSOConsts.Actions.LOGIN, mData);
                        ssoCallback.onSuccess(ssoData);
                    }
                    mActivity.finish();
                }

                @Override
                public void onError(UiError uiError) {
                    SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                    if (ssoCallback != null) {
                        SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGIN, uiError.errorCode, uiError.errorMessage);
                        ssoCallback.onFailed(ssoError);
                    }
                    mActivity.finish();
                }

                @Override
                public void onCancel() {
                    SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                    if (ssoCallback != null) {
                        SSOError ssoError = new SSOError(SSOConsts.ClientType.QQ, SSOConsts.Actions.LOGIN, 0, "");
                        ssoCallback.onCancel(ssoError);
                    }
                    mActivity.finish();
                }
            });
        }
    }
}
