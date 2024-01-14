package com.amazia_iwallcraft.apiservices;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemUserList implements Serializable {

    @SerializedName("HD_WALLPAPER_APP")
    ArrayList<ItemUser> arrayListUser;

    public static class ItemUser implements Serializable {

        @SerializedName("user_id")
        String id;

        @SerializedName("name")
        String name;

        @SerializedName("email")
        String email;

        @SerializedName("phone")
        String mobile;

        @SerializedName("user_image")
        String image;

        @SerializedName("msg")
        String message;

        @SerializedName("success")
        String success;

        String authID;

        String loginType;

        public ItemUser(String id, String name, String email, String mobile, String authID, String loginType) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.authID = authID;
            this.loginType = loginType;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getLoginType() {
            return loginType;
        }

        public String getAuthID() {
            return authID;
        }

        public String getImage() {
            return image;
        }

        public String getMessage() {
            return message;
        }

        public String getSuccess() {
            return success;
        }
    }

    public ArrayList<ItemUser> getArrayListUser() {
        return arrayListUser;
    }
}