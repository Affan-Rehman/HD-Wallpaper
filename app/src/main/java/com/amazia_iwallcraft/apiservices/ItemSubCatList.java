package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemSubCat;

import java.util.ArrayList;

public class ItemSubCatList {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemSubCat> arrayListSubCat;

    @SerializedName("success")
    String success;

    @SerializedName("message")
    String message;

    public ArrayList<ItemSubCat> getArrayListSubCat() {
        return arrayListSubCat;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}