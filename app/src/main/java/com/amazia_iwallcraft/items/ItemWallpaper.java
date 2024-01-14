package com.amazia_iwallcraft.items;

import com.google.gson.annotations.SerializedName;

public class ItemWallpaper {

	@SerializedName("post_id")
	String id;

	@SerializedName("post_title")
	String title;

	@SerializedName("post_image")
	String image;

	@SerializedName("views")
	String totalViews;

	@SerializedName("total_rate")
	String averageRate;

	@SerializedName("downloads")
	String totalDownloads;

	@SerializedName("tags")
	String tags ="";

	@SerializedName("type")
	String type;

	@SerializedName("favourite")
	boolean isFav = false;

	@SerializedName("total_wallpaper")
	int totalWallpapers = 0;

	@SerializedName("user_rating")
	String userRating = "0";

	public ItemWallpaper(String id, String title, String image, String totalViews, String averageRate, String totalDownloads, String tags, String type, boolean isFav) {
		this.id = id;
		this.title = title;
		this.image = image;
		this.totalViews = totalViews;
		this.averageRate = averageRate;
		this.totalDownloads = totalDownloads;
		this.tags = tags;
		this.type = type;
		this.isFav = isFav;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getImage() {
		return image;
	}

	public String getTotalViews() {
		return totalViews;
	}

	public String getAverageRate() {
		return averageRate;
	}

	public void setTotalViews(String totalViews) {
		this.totalViews = totalViews;
	}

	public void setAverageRate(String averageRate) {
		this.averageRate = averageRate;
	}

	public String getTotalDownloads() {
		return totalDownloads;
	}

	public void setTotalDownloads(String totalDownloads) {
		this.totalDownloads = totalDownloads;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public String getUserRating() {
		return userRating;
	}

	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}

	public boolean getIsFav() {
		return isFav;
	}

	public void setIsFav(boolean fav) {
		isFav = fav;
	}

	public int getTotalWallpapers() {
		return totalWallpapers;
	}
}
