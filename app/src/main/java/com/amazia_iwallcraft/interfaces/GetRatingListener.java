package com.amazia_iwallcraft.interfaces;

public interface GetRatingListener {
    void onStart();
    void onEnd(String success, String message, float rating);
}
