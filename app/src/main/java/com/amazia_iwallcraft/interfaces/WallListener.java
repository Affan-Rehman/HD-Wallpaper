package com.amazia_iwallcraft.interfaces;

import com.amazia_iwallcraft.items.ItemWallpaper;

import java.util.ArrayList;

public interface WallListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemWallpaper> arrayListCat, int totalNumber);
}