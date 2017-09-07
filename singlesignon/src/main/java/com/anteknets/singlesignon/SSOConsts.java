package com.anteknets.singlesignon;

/**
 * SSO Constants
 * Created by HAO on 2017/8/24.
 */

public class SSOConsts {

    public interface Actions {
        String LOGIN = "login";
        String LOGOUT = "logout";
        String REFRESH_TOKEN = "refresh.token";
    }

    public interface ClientType {
        String GOOGLE = "google";
        String WeChat = "wechat";
        String QQ = "qq";
    }

    public interface ErrorCode {
        int ERROR_NEED_LOGOUT = -901;
    }

    public interface SSOConfigKey {
        String NAME_SSO_SP = "name.sso.sp";

        String KEY_SSO_OPENID = "key.sso.openid";
        String KEY_SSO_TOKEN = "key.sso.token";
        String KEY_SSO_TOKEN_EXPIRATION = "key.sso.token.expiration";
        String KEY_SSO_REFRESH_TOKEN = "key.sso.refresh.token"; //for wechat
        String KEY_SSO_REFRESH_TOKEN_EXPIRATION = "key.sso.refresh.token.expiration"; //for wechat
    }

    interface GoogleConsts {
        int RC_SIGN_IN_CODE = 9001;
        String YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube";
        String CLIENT_ID = "402740751129-tl46nn8ukqlrkodrb5f9g977nko45c9f.apps.googleusercontent.com";
    }

    interface QQConsts {
        String QQ_LOGIN_APPID = "1106344890";
        String QQ_SCHEME = "tencent" + QQ_LOGIN_APPID;
        String QQ_APP_KEY = "qqWNIod13atSRCKx";
    }

    public interface WeChatConsts {
        String APP_ID = "wx2775e509cacda27b";
        String APP_SERECET = "66d3fa2f56f1a637ab0926ac2f8c6a71";
        int REFRESH_TOKEN_VALIDITY_PERIOD = 30 * 24 * 3600; //30天
    }

    public static final String KEY_SSO_TYPE = "key.sso.type";
}
