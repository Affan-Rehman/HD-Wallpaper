package com.amazia_iwallcraft.interfaces;

import com.amazia_iwallcraft.items.ItemCat;
import com.amazia_iwallcraft.items.ItemColors;
import com.amazia_iwallcraft.items.ItemWallpaper;

import java.util.ArrayList;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemWallpaper> arrayListFeatured, ArrayList<ItemWallpaper> arrayListLatest, ArrayList<ItemWallpaper> arrayListPopular, ArrayList<ItemWallpaper> arrayListRecent, ArrayList<ItemCat> arrayListCat, ArrayList<ItemColors> arrayListColors);
}