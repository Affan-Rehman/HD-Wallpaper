package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemColors;

import java.util.ArrayList;

public class ItemColorsList {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemColors> arrayListColors;

    @SerializedName("success")
    String success;

    @SerializedName("message")
    String message;

    public ArrayList<ItemColors> getArrayListColors() {
        return arrayListColors;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}