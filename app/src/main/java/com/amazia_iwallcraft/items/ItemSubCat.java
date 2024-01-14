package com.amazia_iwallcraft.items;

import com.google.gson.annotations.SerializedName;

public class ItemSubCat {

    @SerializedName("post_id")
    String id;

    @SerializedName("post_title")
    String name;

    @SerializedName("post_image")
    String image;

    @SerializedName("total_wallpaper")
    String totalWallpapers;

    public ItemSubCat(String id, String name, String image, String totalWallpapers) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.totalWallpapers = totalWallpapers;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getTotalWallpapers() {
        return totalWallpapers;
    }
}