package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;
import com.amazia_iwallcraft.items.ItemAbout;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemAppDetailsList implements Serializable {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemAbout> arrayListAbout;

    public ArrayList<ItemAbout> getArrayListAbout() {
        return arrayListAbout;
    }
}