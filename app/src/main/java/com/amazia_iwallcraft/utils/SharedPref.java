package com.amazia_iwallcraft.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.amazia_iwallcraft.wallpaper.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SharedPref {

    Context context;
    EncryptData encryptData;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String TAG_UID = "uid", TAG_USERNAME = "name", TAG_EMAIL = "email", TAG_MOBILE = "mobile", TAG_USER_IMAGE = "userImage", TAG_REMEMBER = "rem",
            TAG_PASSWORD = "pass", SHARED_PREF_AUTOLOGIN = "autologin", TAG_LOGIN_TYPE = "loginType", TAG_AUTH_ID = "auth_id",
            TAG_NIGHT_MODE = "nightmode", TAG_IS_LOGGED = "islogged", TAG_AD_IS_BANNER = "isbanner", TAG_AD_IS_INTER = "isinter",
            TAG_AD_IS_NATIVE = "isnative", TAG_AD_ID_BANNER = "id_banner", TAG_AD_ID_INTER = "id_inter", TAG_AD_ID_NATIVE = "id_native",
            TAG_AD_NATIVE_POS = "native_pos", TAG_AD_INTER_POS = "inter_pos", TAG_AD_TYPE_BANNER = "type_banner", TAG_AD_TYPE_INTER = "type_inter",
            TAG_AD_TYPE_NATIVE = "type_native", TAG_STARTAPP_ID = "startapp_id", TAG_COLOR_SAVE = "color_save",
            TAG_FB = "fb", TAG_INSTA = "insta", TAG_TWITTER = "twitter", TAG_YOUTUBE = "youtube";

    public SharedPref(Context context) {
        this.context = context;
        encryptData = new EncryptData(context);
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsNotification() {
        return sharedPreferences.getBoolean("noti", true);
    }

    public void setIsNotification(Boolean isNotification) {
        editor.putBoolean("noti", isNotification);
        editor.apply();
    }

    public Boolean getIsLiveWallpaper() {
        return sharedPreferences.getBoolean("live_wall", true);
    }

    public void setIsLiveWallpaper(Boolean live_wall) {
        editor.putBoolean("live_wall", live_wall);
        editor.apply();
    }

    public void setIsFirst(Boolean flag) {
        editor.putBoolean("firstopen", flag);
        editor.apply();
    }

    public Boolean getIsFirst() {
        return sharedPreferences.getBoolean("firstopen", true);
    }

    public void setIsLogged(Boolean isLogged) {
        editor.putBoolean(TAG_IS_LOGGED, isLogged);
        editor.apply();
    }

    public boolean isLogged() {
        return sharedPreferences.getBoolean(TAG_IS_LOGGED, false);
    }

    public void setLoginDetails(String id, String name, String mobile, String email, String userImage, String authID, Boolean isRemember, String password, String loginType) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_UID, encryptData.encrypt(id));
        editor.putString(TAG_USERNAME, encryptData.encrypt(name));
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.putString(TAG_USER_IMAGE, encryptData.encrypt(userImage));
        editor.putString(TAG_PASSWORD, encryptData.encrypt(password));
        editor.putString(TAG_LOGIN_TYPE, encryptData.encrypt(loginType));
        editor.putString(TAG_AUTH_ID, encryptData.encrypt(authID));
        editor.apply();
    }

    public void setSocialDetails() {
        editor.putString(TAG_FB, Constant.urlFacebook);
        editor.putString(TAG_INSTA, Constant.urlInstagram);
        editor.putString(TAG_TWITTER, Constant.urlTwitter);
        editor.putString(TAG_YOUTUBE, Constant.urlYoutube);
        editor.apply();
    }

    public void setRemeber(Boolean isRemember) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_PASSWORD, "");
        editor.apply();
    }

    public String getUserId() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_UID, ""));
    }

    public void setUserName(String userName) {
        editor.putString(TAG_USERNAME, encryptData.encrypt(userName));
        editor.apply();
    }

    public String getUserName() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_USERNAME, ""));
    }

    public void setEmail(String email) {
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.apply();
    }

    public String getEmail() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_EMAIL, ""));
    }

    public void setUserMobile(String mobile) {
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.apply();
    }

    public String getUserMobile() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_MOBILE, ""));
    }

    public void setUserImage(String image) {
        editor.putString(TAG_USER_IMAGE, encryptData.encrypt(image));
        editor.apply();
    }

    public String getUserImage() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_USER_IMAGE, ""));
    }

    public String getPassword() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_PASSWORD, ""));
    }

    public Boolean isRemember() {
        return sharedPreferences.getBoolean(TAG_REMEMBER, false);
    }

    public Boolean getIsAutoLogin() {
        return sharedPreferences.getBoolean(SHARED_PREF_AUTOLOGIN, false);
    }

    public void setIsAutoLogin(Boolean isAutoLogin) {
        editor.putBoolean(SHARED_PREF_AUTOLOGIN, isAutoLogin);
        editor.apply();
    }

    public Boolean getIsRemember() {
        return sharedPreferences.getBoolean(TAG_REMEMBER, false);
    }

    public String getLoginType() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_LOGIN_TYPE, ""));
    }

    public String getAuthID() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_AUTH_ID, ""));
    }

    public String getDarkMode() {
        return sharedPreferences.getString(TAG_NIGHT_MODE, Constant.DARK_MODE_ON);
    }

    public void setDarkMode(String nightMode) {
        editor.putString(TAG_NIGHT_MODE, nightMode);
        editor.apply();
    }

    public boolean getIsColorSaved() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return sharedPreferences.getString(TAG_COLOR_SAVE, "").equals(df.format(c));
    }

    public void setColorSaved() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        editor.putString(TAG_COLOR_SAVE, df.format(c));
        editor.apply();
    }

    public void setAdDetails(boolean isBanner, boolean isInter, boolean isNative, String typeBanner, String typeInter, String typeNative,
                             String idBanner, String idInter, String idNative, String startapp_id, int interPos, int nativePos) {
        editor.putBoolean(TAG_AD_IS_BANNER, isBanner);
        editor.putBoolean(TAG_AD_IS_INTER, isInter);
        editor.putBoolean(TAG_AD_IS_NATIVE, isNative);
        editor.putString(TAG_AD_TYPE_BANNER, encryptData.encrypt(typeBanner));
        editor.putString(TAG_AD_TYPE_INTER, encryptData.encrypt(typeInter));
        editor.putString(TAG_AD_TYPE_NATIVE, encryptData.encrypt(typeNative));
        editor.putString(TAG_AD_ID_BANNER, encryptData.encrypt(idBanner));
        editor.putString(TAG_AD_ID_INTER, encryptData.encrypt(idInter));
        editor.putString(TAG_AD_ID_NATIVE, encryptData.encrypt(idNative));
        editor.putString(TAG_STARTAPP_ID, encryptData.encrypt(startapp_id));
        editor.putInt(TAG_AD_NATIVE_POS, interPos);
        editor.putInt(TAG_AD_INTER_POS, nativePos);
        editor.apply();
    }

    public void getAdDetails() {
        Constant.bannerAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_BANNER, Constant.AD_TYPE_ADMOB));
        Constant.interstitialAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_INTER, Constant.AD_TYPE_ADMOB));
        Constant.nativeAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_NATIVE, Constant.AD_TYPE_ADMOB));

        Constant.bannerAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_BANNER, ""));
        Constant.interstitialAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_INTER, ""));
        Constant.nativeAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_NATIVE, ""));

        Constant.startappAppId = encryptData.decrypt(sharedPreferences.getString(TAG_STARTAPP_ID, ""));

        Constant.interstitialAdShow = sharedPreferences.getInt(TAG_AD_INTER_POS, 5);
        Constant.nativeAdShow = sharedPreferences.getInt(TAG_AD_NATIVE_POS, 9);
    }

    public String getFB() {
        return sharedPreferences.getString(TAG_FB,"");
    }

    public String getTwitter() {
        return sharedPreferences.getString(TAG_TWITTER,"");
    }

    public String getInsta() {
        return sharedPreferences.getString(TAG_INSTA,"");
    }

    public String getYoutube() {
        return sharedPreferences.getString(TAG_YOUTUBE,"");
    }
}