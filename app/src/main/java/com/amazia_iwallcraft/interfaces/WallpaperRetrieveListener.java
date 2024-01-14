package com.amazia_iwallcraft.interfaces;

import com.amazia_iwallcraft.items.ItemWallpaper;

import java.util.ArrayList;

public interface WallpaperRetrieveListener {
    void onSuccess(ArrayList<ItemWallpaper> arrayListWallpaper);
}
