package com.anteknets.singlesignon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Google SSO
 * Created by HAO on 2017/8/24.
 */

class GoogleSSO extends BaseSSO {

    private static final String TAG = GoogleSSO.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;

    GoogleSSO(Context context, String action) {
        super(context, action);
        mData = new SSOData(SSOConsts.ClientType.GOOGLE, action);
        buildGoogleAPIClient(context);
    }

    GoogleSSO(Context context, String action, Activity activity) {
        super(context, action);
        mData = new SSOData(SSOConsts.ClientType.GOOGLE, action);
        buildGoogleAPIClient(context);
        mActivity = activity;
    }

    @Override
    public void login() {
        Log.d(TAG, "login. Activity:" + mActivity.getLocalClassName());
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mActivity.startActivityForResult(signInIntent, SSOConsts.GoogleConsts.RC_SIGN_IN_CODE);
    }

    @Override
    public void logout() {
        Log.d(TAG, "logout.");
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGOUT);
                            if (status.isSuccess()) {
                                if (ssoCallback != null) {
                                    SSOData ssoData = new SSOData(SSOConsts.ClientType.GOOGLE, SSOConsts.Actions.LOGOUT);
                                    ssoCallback.onSuccess(ssoData);
                                }
                            } else {
                                if (ssoCallback != null) {
                                    SSOError ssoError = new SSOError(SSOConsts.ClientType.GOOGLE, SSOConsts.Actions.LOGOUT, 0, status.getStatusMessage());
                                    ssoCallback.onFailed(ssoError);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.LOGOUT);
                if (ssoCallback != null) {
                    SSOError ssoError = new SSOError(SSOConsts.ClientType.GOOGLE,  SSOConsts.Actions.LOGOUT, 0, i + "");
                    ssoCallback.onFailed(ssoError);
                }
            }
        });
    }

    @Override
    public void refreshToken() {
        Log.d(TAG, "refreshToken.");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        final SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(SSOConsts.Actions.REFRESH_TOKEN);

        if (pendingResult.isDone()) {
            // There's immediate result available.
            Log.d(TAG, "refreshToken opr.isDone");
            GoogleSignInResult result = pendingResult.get();
            if (ssoCallback != null) {
                SSOData ssoData = SSOData.create(SSOConsts.Actions.REFRESH_TOKEN, result);
                ssoCallback.onSuccess(ssoData);
            }
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            Log.d(TAG, "refreshToken setResultCallback");
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull final GoogleSignInResult googleSignInResult) {
                    if (ssoCallback != null) {
                        if (googleSignInResult.isSuccess()) {
                            SSOData ssoData = SSOData.create(SSOConsts.Actions.REFRESH_TOKEN, googleSignInResult);
                            ssoCallback.onSuccess(ssoData);
                        } else {
                            SSOError ssoError = new SSOError(SSOConsts.ClientType.GOOGLE, SSOConsts.Actions.REFRESH_TOKEN, 0, googleSignInResult.getStatus().getStatusMessage());
                            ssoCallback.onFailed(ssoError);
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    private void buildGoogleAPIClient(Context context) {
        if (mGoogleApiClient != null) return;
        // [START configure_signin]
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SSOConsts.GoogleConsts.CLIENT_ID)
                .requestEmail()
                .build();


        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(context)
//                .enableAutoManage((FragmentActivity) context, (GoogleApiClient.OnConnectionFailedListener) context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "The error message is : " + connectionResult.getErrorMessage());
                SSOCallback ssoCallback = SSOManager.getInstance().getSSOCallback(mAction);
                if (ssoCallback != null) {
                    SSOError ssoError = new SSOError(SSOConsts.ClientType.GOOGLE, mAction, connectionResult.getErrorCode(), connectionResult.getErrorMessage());
                    ssoCallback.onFailed(ssoError);
                }
            }
        });
    }
}
