package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemWallpaper;

import java.util.ArrayList;

public class ItemWallpaperList {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemWallpaper> arrayListWallpaper;

    @SerializedName("success")
    String success;

    @SerializedName("message")
    String message;

    @SerializedName("total_records")
    int totalRecords;

    public ArrayList<ItemWallpaper> getArrayListWallpaper() {
        return arrayListWallpaper;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getTotalRecords() {
        return totalRecords;
    }
}