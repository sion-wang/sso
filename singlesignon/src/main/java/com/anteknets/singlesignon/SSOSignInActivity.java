package com.anteknets.singlesignon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * SSO sign in view
 * Created by HAO on 2017/4/19.
 */

public class SSOSignInActivity extends Activity {
    private static final String TAG = SSOSignInActivity.class.getSimpleName();
    private BaseSSO mSSOClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String ssoType = intent.getStringExtra(SSOConsts.KEY_SSO_TYPE);
        switch (ssoType){
            case SSOConsts.ClientType.GOOGLE:
                mSSOClient = new GoogleSSO(this, SSOConsts.Actions.LOGIN, this);
                mSSOClient.execute();
                break;
            case SSOConsts.ClientType.QQ:
                mSSOClient= new QqSSO(this, SSOConsts.Actions.LOGIN, this);
                mSSOClient.execute();
                break;
            case SSOConsts.ClientType.WeChat:
                mSSOClient= new WeChatSSO(this, SSOConsts.Actions.LOGIN);
                mSSOClient.execute();
                finish();
                break;
            default:
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                SSOError ssoError = new SSOError(ssoType, SSOConsts.Actions.LOGIN, 0, "Not defined SSO type.");
                ssoCallback.onFailed(ssoError);
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode : " + requestCode + " resultCode " + resultCode);
        if (requestCode == SSOConsts.GoogleConsts.RC_SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "Does get auth success? " + result.getStatus().isSuccess());
            if (result.isSuccess()){
                SSOData ssoData = SSOData.create(SSOConsts.Actions.LOGIN, result);
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                ssoCallback.onSuccess(ssoData);
            } else {
                SSOError ssoError = new SSOError(SSOConsts.ClientType.GOOGLE, SSOConsts.Actions.LOGIN, 0, "");
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGIN);
                ssoCallback.onFailed(ssoError);
            }
            finish();
        } else if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, ((QqSSO) mSSOClient).getIUiListener());
        }
    }
}
