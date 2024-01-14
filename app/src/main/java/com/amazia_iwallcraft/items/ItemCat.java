package com.amazia_iwallcraft.items;

import com.google.gson.annotations.SerializedName;

public class ItemCat {

    @SerializedName("post_id")
    String id;

    @SerializedName("post_title")
    String name;

    @SerializedName("post_image")
    String image;

    public ItemCat(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
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

    public String getTotalWallpaper() {
        return "0";
    }
}