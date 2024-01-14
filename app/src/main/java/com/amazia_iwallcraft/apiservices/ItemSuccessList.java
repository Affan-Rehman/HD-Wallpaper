package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemSuccess;

import java.util.ArrayList;

public class ItemSuccessList {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemSuccess> arrayListSuccess;

    public ArrayList<ItemSuccess> getArrayListSuccess() {
        return arrayListSuccess;
    }
}