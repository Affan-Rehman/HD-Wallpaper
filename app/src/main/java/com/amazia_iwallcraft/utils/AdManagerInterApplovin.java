package com.amazia_iwallcraft.utils;

import android.app.Activity;
import android.content.Context;

import com.applovin.mediation.ads.MaxInterstitialAd;

public class AdManagerInterApplovin {
    static MaxInterstitialAd interstitialAd;
    private final Context ctx;

    public AdManagerInterApplovin(Context ctx) {
        this.ctx = ctx;
    }

    public void createAd() {
        interstitialAd = new MaxInterstitialAd(Constant.interstitialAdID, (Activity) ctx);
        interstitialAd.loadAd();
    }

    public MaxInterstitialAd getAd() {
        return interstitialAd;
    }

    public static void setAd(MaxInterstitialAd appLovinInter) {
        interstitialAd = appLovinInter;
    }
}