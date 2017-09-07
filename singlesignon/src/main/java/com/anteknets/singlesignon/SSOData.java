package com.anteknets.singlesignon;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;

/**
 * Created by HAO on 2017/8/28.
 */

public class SSOData extends SSOItem{
    private String mToken;
    private String mOpenId;
    private long mExpireTime;

    private String mName;
    private String mAvatarUrl;
    private String mIdentity;

    public SSOData(String type, String action) {
        super(type, action);
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public String getOpenId() {
        return mOpenId;
    }

    public void setOpenId(String openId) {
        mOpenId = openId;
    }

    public long getExpireTime() {
        return mExpireTime;
    }

    public void setExpireTime(long expireTime) {
        mExpireTime = expireTime;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public String getIdentity() {
        return mIdentity;
    }

    public void setIdentity(String identity) {
        mIdentity = identity;
    }

    public static SSOData clone(SSOData data) {
        return clone(data.getAction(), data);
    }
    public static SSOData clone(String action, SSOData data) {
        SSOData newOne = new SSOData(data.getType(), action);
        newOne.setAvatarUrl(data.getAvatarUrl());
        newOne.setName(data.getName());
        newOne.setOpenId(data.getOpenId());
        newOne.setExpireTime(data.getExpireTime());
        newOne.setIdentity(data.getIdentity());
        newOne.setToken(data.getToken());
        newOne.setExtras(data.getExtras());
        return newOne;
    }

    public static SSOData create(String action, GoogleSignInResult result){
        SSOData ssoData = new SSOData(SSOConsts.ClientType.GOOGLE, action);
        if (result.getSignInAccount() != null){
            ssoData.setName(result.getSignInAccount().getDisplayName());
            ssoData.setToken(result.getSignInAccount().getIdToken());
            ssoData.setOpenId(result.getSignInAccount().getId());
            if (result.getSignInAccount().getPhotoUrl() != null){
                ssoData.setAvatarUrl(result.getSignInAccount().getPhotoUrl().toString());
            }
        }
        return ssoData;
    }

    public static SSOData create(String action, WXUserModel userModel){
        SSOData ssoData = new SSOData(SSOConsts.ClientType.WeChat, action);
        ssoData.setName(userModel.getNickname());
        ssoData.setToken(userModel.getAccess_token());
        ssoData.setOpenId(userModel.getOpenid());
        ssoData.setExpireTime(System.currentTimeMillis() + Long.parseLong(userModel.getExpires_in()) * 1000);
        ssoData.setAvatarUrl(userModel.getHeadimgurl());
        return ssoData;
    }
}
