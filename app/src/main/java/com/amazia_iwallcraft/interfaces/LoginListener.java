package com.amazia_iwallcraft.interfaces;

public interface LoginListener {
    void onStart();
    void onEnd(String success, String loginSuccess, String message, String userId, String userName, String email, String phone, String userImage);
}