package com.amazia_iwallcraft.apiservices;

import com.amazia_iwallcraft.items.ItemWallpaper;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemFavList implements Serializable {

    @SerializedName("HD_WALLPAPER_APP")
    ItemFavPost itemFavPost;

    public ItemFavPost getItemFavPost() {
        return itemFavPost;
    }

    public class ItemFavPost implements Serializable {

        @SerializedName("wallpaper_list")
        ArrayList<ItemWallpaper> arrayListWallpapers;

        @SerializedName("live_wallpaper_list")
        ArrayList<ItemWallpaper> arrayListLiveWallpapers;

        public ArrayList<ItemWallpaper> getArrayListWallpapers() {
            return arrayListWallpapers;
        }

        public ArrayList<ItemWallpaper> getArrayListLiveWallpapers() {
            return arrayListLiveWallpapers;
        }
    }
}