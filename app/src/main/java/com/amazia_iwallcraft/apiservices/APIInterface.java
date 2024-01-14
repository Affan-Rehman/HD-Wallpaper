package com.amazia_iwallcraft.apiservices;

import com.amazia_iwallcraft.utils.Constant;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @FormUrlEncoded
    @POST(Constant.URL_APP_DETAILS)
    Call<ItemAppDetailsList> getAppDetails(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_LOGIN)
    Call<ItemUserList> getLogin(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_SOCIAL_LOGIN)
    Call<ItemUserList> getSocialLogin(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_REGISTRATION)
    Call<ItemUserList> getRegistration(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_FORGOT_PASSWORD)
    Call<ItemUserList> getForgotPassword(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_PROFILE)
    Call<ItemUserList> getProfile(@Field("data") String data);

    @POST(Constant.URL_PROFILE_UPDATE)
    Call<ItemUserList> getProfileUpdate(@Body RequestBody imageFile);

    @FormUrlEncoded
    @POST(Constant.URL_CATEGORIES)
    Call<ItemCatList> getCategories(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_SUB_CATEGORIES)
    Call<ItemSubCatList> getSubCategories(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_COLORS)
    Call<ItemColorsList> getColors(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_LATEST)
    Call<ItemWallpaperList> getWallpapersByLatest(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_CAT)
    Call<ItemWallpaperList> getWallpapersByCat(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_SUB_CAT)
    Call<ItemWallpaperList> getWallpapersBySubCat(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_POPULAR)
    Call<ItemWallpaperList> getWallpapersByPopular(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_FEATURED)
    Call<ItemWallpaperList> getWallpapersByFeatured(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_RATED)
    Call<ItemWallpaperList> getWallpapersByRated(@Field("data") String data);


    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_DOWNLOAD)
    Call<ItemWallpaperList> getWallpapersByDownload(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_RECENT)
    Call<ItemWallpaperList> getWallpapersByRecent(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_FAV)
    Call<ItemFavList> getWallpapersByFav(@Field("data") String data);


    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_SEARCH)
    Call<ItemWallpaperList> getWallpapersBySearch(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_BY_RANDOM)
    Call<ItemWallpaperList> getWallpapersByRandom(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_FAV_WALLPAPER)
    Call<ItemSuccessList> getDoFavourite(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_DETAILS)
    Call<ItemWallpaperList> getWallpaperDetails(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_LATEST)
    Call<ItemWallpaperList> getLiveWallByLatest(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_POPULAR)
    Call<ItemWallpaperList> getLiveWallByPopular(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_DOWNLOADS)
    Call<ItemWallpaperList> getLiveWallByDownloads(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_RATED)
    Call<ItemWallpaperList> getLiveWallByRated(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_SEARCH)
    Call<ItemWallpaperList> getLiveWallBySearch(@Field("data") String data, @Query("page") String page);

    @FormUrlEncoded
    @POST(Constant.URL_LIVE_WALL_DETAILS)
    Call<ItemWallpaperList> getLiveWallDetails(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_VIEW)
    Call<ItemWallpaperList> getWallpaperView(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_RATE_WALLPAPER)
    Call<ItemSuccessList> getDoRateWallpaper(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_WALLPAPER_DOWNLOAD_COUNT)
    Call<ItemSuccessList> getWallpaperDownloadCount(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constant.URL_REPORT)
    Call<ItemSuccessList> getReport(@Field("data") String data);
}