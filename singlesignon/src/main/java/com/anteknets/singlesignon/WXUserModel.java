package com.anteknets.singlesignon;

import java.util.List;

/**
 * Created by Administrator on 2017\8\15 0015.
 */


public class WXUserModel {
    /**
     * city :
     * country : JP
     * headimgurl : http://wx.qlogo.cn/mmopen/1Wx003ticsx9623oNhUJicqQQ4I6sv0IpB6oPzDicyic3KNWToja9s2n474ZZpy5eqkUQloKS2VKtPAEVZfsiaDqnNsm8q0ELkpwL/0
     * language : zh_CN
     * nickname : 人生、 月影
     * openid : oKlgQ0xfnCU9NeZVfnQy-WDmOLwo
     * privilege : []
     * province : Tokyo
     * sex : 1
     * unionid : oqQbfwIDdKCPt9hEvFXbHO2bQudQ
     */

    private String city;
    private String country;
    private String headimgurl;
    private String language;
    private String nickname;
    private String openid;
    private String province;
    private int sex;
    private String unionid;
    private List<?> privilege;
    /**
     * access_token : ACCESS_TOKEN
     * expires_in : 7200
     * refresh_token : REFRESH_TOKEN
     * scope : SCOPE
     */

    private String access_token;
    private String expires_in;
    private String refresh_token;
    private String scope;
    private String expires_refresh;
    public void setExpires_refresh(String expires_refresh) {
        this.expires_refresh = expires_refresh;
    }

    public String getExpires_refresh() {
        return expires_refresh;
    }



    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public List<?> getPrivilege() {
        return privilege;
    }

    public void setPrivilege(List<?> privilege) {
        this.privilege = privilege;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
