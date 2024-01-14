package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemCat;

import java.util.ArrayList;

public class ItemCatList {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemCat> arrayListCat;

    @SerializedName("success")
    String success;

    @SerializedName("message")
    String message;

    public ArrayList<ItemCat> getArrayListCat() {
        return arrayListCat;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}