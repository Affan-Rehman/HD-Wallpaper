package com.amazia_iwallcraft.utils;

import android.net.Uri;

import com.amazia_iwallcraft.wallpaper.BuildConfig;
import com.amazia_iwallcraft.items.ItemAbout;
import com.amazia_iwallcraft.items.ItemColors;
import com.amazia_iwallcraft.items.ItemPage;
import com.amazia_iwallcraft.items.ItemWallpaper;

import java.io.Serializable;
import java.util.ArrayList;

public class Constant implements Serializable {

    private static final long serialVersionUID = 1L;

    //server url
    public static final String SERVER_URL = BuildConfig.SERVER_URL;

    public static final String URL_APP_DETAILS = "app_details";
    public static final String URL_LOGIN = "login";
    public static final String URL_SOCIAL_LOGIN = "social_login";
    public static final String URL_REGISTRATION = "signup";
    public static final String URL_FORGOT_PASSWORD = "forgot_password";
    public static final String URL_PROFILE = "profile";
    public static final String URL_PROFILE_UPDATE = "profile_update";

    public static final String URL_CATEGORIES = "category";
    public static final String URL_SUB_CATEGORIES = "subcategory";
    public static final String URL_COLORS = "colors";
    public static final String URL_WALLPAPER_BY_CAT = "cat_wallpaper";
    public static final String URL_WALLPAPER_BY_SUB_CAT = "subcat_wallpaper";
    public static final String URL_WALLPAPER_BY_LATEST = "latest_wallpaper";
    public static final String URL_WALLPAPER_BY_POPULAR = "popular_wallpaper";
    public static final String URL_WALLPAPER_BY_FEATURED = "featured_wallpaper";
    public static final String URL_WALLPAPER_BY_DOWNLOAD = "most_download_wallpaper";
    public static final String URL_WALLPAPER_BY_RATED = "high_rated_wallpaper";
    public static final String URL_WALLPAPER_BY_RECENT = "recent_wallpaper";
    public static final String URL_WALLPAPER_BY_RANDOM = "wallpaper_random";
    public static final String URL_WALLPAPER_BY_FAV = "user_favourite_post_list";
    public static final String URL_WALLPAPER_BY_SEARCH = "search_wallpaper";
    public static final String URL_FAV_WALLPAPER = "post_favourite";
    public static final String URL_WALLPAPER_DETAILS = "wallpaper_details";
    public static final String URL_DO_FAV = "favorite_post";
    public static final String URL_WALLPAPER_VIEW = "post_view";
    public static final String URL_RATE_WALLPAPER = "post_rate";
    public static final String URL_WALLPAPER_DOWNLOAD_COUNT = "post_download";
    public static final String URL_REPORT = "user_reports";

    public static final String URL_LIVE_WALL_LATEST = "latest_live_wallpaper";
    public static final String URL_LIVE_WALL_POPULAR = "popular_live_wallpaper";
    public static final String URL_LIVE_WALL_DOWNLOADS = "most_download_live_wallpaper";
    public static final String URL_LIVE_WALL_RATED = "high_rated_live_wallpaper";
    public static final String URL_LIVE_WALL_SEARCH = "search_live_wallpaper";
    public static final String URL_LIVE_WALL_DETAILS = "live_wallpaper_details";

    public static final String TAG_PORTRAIT = "Portrait";
    public static final String TAG_LANDSCAPE = "Landscape";
    public static final String TAG_SQUARE = "Square";

    public static final String LOGIN_TYPE_NORMAL = "normal";
    public static final String LOGIN_TYPE_GOOGLE = "google";
    public static final String LOGIN_TYPE_FB = "facebook";

    public static final String DARK_MODE_ON = "on";
    public static final String DARK_MODE_OFF = "off";
    public static final String DARK_MODE_SYSTEM = "system";

    public static final String AD_TYPE_ADMOB = "Admob";
    public static final String AD_TYPE_FACEBOOK = "Facebook";
    public static final String AD_TYPE_STARTAPP = "StartApp";
    public static final String AD_TYPE_APPLOVIN = "AppLovins MAX";
    public static final String AD_TYPE_WORTISE = "Wortise";

    // Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 2;

    // Gridview image padding
    public static final int GRID_PADDING = 3; // in dp

    public static ArrayList<ItemWallpaper> arrayList = new ArrayList<>();
    public static ArrayList<ItemWallpaper> arrayListLiveWallpapers = new ArrayList<>();
    public static ArrayList<ItemColors> arrayListColors = new ArrayList<>();
    public static ArrayList<ItemPage> arrayListPages = new ArrayList<>();

    public static ItemAbout itemAbout;
    public static int columnWidth = 0;
    public static int columnHeight = 0;

    public static String search_item = "";
    public static Uri uri_set;

    public static Boolean isUpdate = false, isBannerAd = true, isInterAd = true, isNativeAd = false, isLiveWallpaperEnabled = true,
            isPortrait = true, isLandscape = true, isSquare = true, showUpdateDialog = true, appUpdateCancel = false;
    public static String bannerAdType = "admob", interstitialAdType = "admob", nativeAdType = "admob",
            publisherAdID = "", bannerAdID = "", interstitialAdID = "", nativeAdID = "", startappAppId = "",
            appVersion = "1", appUpdateMsg = "", appUpdateURL = "", wortiseAppId = "",
            urlYoutube = "", urlFacebook = "", urlInstagram = "", urlTwitter = "";

    public static int interstitialAdShow = 5;
    public static int adCount = 0;

    public static int nativeAdShow = 12;

    public static Boolean isColorOn = true;
}