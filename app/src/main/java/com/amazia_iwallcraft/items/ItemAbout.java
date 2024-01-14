package com.amazia_iwallcraft.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemAbout {

	@SerializedName("app_name")
 	String appName;
	@SerializedName("app_logo")
	String appLogo;
	@SerializedName("app_version")
	String appVersion;
	@SerializedName("app_company")
	String author;
	@SerializedName("app_contact")
	String contact;
	@SerializedName("app_email")
	String email;
	@SerializedName("app_website")
	String website;

	@SerializedName("facebook_link")
	String facebookLink;
	@SerializedName("twitter_link")
	String twitterLink;
	@SerializedName("instagram_link")
	String instagramLink;
	@SerializedName("youtube_link")
	String youtubeLink;
	@SerializedName("google_play_link")
	String googlePlayStoreLink;

	@SerializedName("app_update_hide_show")
	boolean showAppUpdate;
	@SerializedName("app_update_version_code")
	String appUpdateVersion;
	@SerializedName("app_update_desc")
	String appUpdateMessage;
	@SerializedName("app_update_link")
	String appUpdateLink;
	@SerializedName("app_update_cancel_option")
	boolean appUpdateCancel;

	@SerializedName("live_wallpaper_on_off")
	boolean liveWallpaperOn;

	@SerializedName("ads_list")
	ArrayList<ItemAds> arrayListAds;
	@SerializedName("page_list")
	ArrayList<ItemPage> arrayListPages;

	String appDesc;

	public ItemAbout(String app_name, String app_logo, String app_desc, String app_version, String author, String contact, String email, String website) {
		this.appName = app_name;
		this.appLogo = app_logo;
		this.appDesc = app_desc;
		this.appVersion = app_version;
		this.author = author;
		this.contact = contact;
		this.email = email;
		this.website = website;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppLogo() {
		return appLogo;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getAuthor() {
		return author;
	}

	public String getContact() {
		return contact;
	}

	public String getEmail() {
		return email;
	}

	public String getWebsite() {
		return website;
	}

	public String getAppDesc() {
		return appDesc;
	}

	public void setAppDesc(String appDesc) {
		this.appDesc = appDesc;
	}

	public String getFacebookLink() {
		return facebookLink;
	}

	public String getTwitterLink() {
		return twitterLink;
	}

	public String getInstagramLink() {
		return instagramLink;
	}

	public String getYoutubeLink() {
		return youtubeLink;
	}

	public String getGooglePlayStoreLink() {
		return googlePlayStoreLink;
	}

	public boolean isShowAppUpdate() {
		return showAppUpdate;
	}

	public String getAppUpdateVersion() {
		return appUpdateVersion;
	}

	public String getAppUpdateMessage() {
		return appUpdateMessage;
	}

	public String getAppUpdateLink() {
		return appUpdateLink;
	}

	public boolean isAppUpdateCancel() {
		return appUpdateCancel;
	}

	public boolean isLiveWallpaperOn() {
		return liveWallpaperOn;
	}

	public ArrayList<ItemAds> getArrayListAds() {
		return arrayListAds;
	}

	public ArrayList<ItemPage> getArrayListPages() {
		return arrayListPages;
	}

	public static class ItemAds implements Serializable {

		@SerializedName("ad_id")
		String id;

		@SerializedName("ads_name")
		String adType;

		@SerializedName("ads_info")
		ItemAdsDetails itemAdsDetails;

		public static class ItemAdsDetails implements Serializable {

			@SerializedName("publisher_id")
			String publisherId;

			@SerializedName("banner_on_off")
			String isBannerOn;

			@SerializedName("interstitial_on_off")
			String isInterstitialOn;

			@SerializedName("native_on_off")
			String isNativeOn;

			@SerializedName("banner_id")
			String bannerID;

			@SerializedName("interstitial_id")
			String interstitialID;

			@SerializedName("native_id")
			String nativeID;

			@SerializedName("interstitial_clicks")
			String interAdsClick;

			@SerializedName("native_position")
			String nativeAdsPos;

			public String getPublisherId() {
				return publisherId;
			}

			public String getIsBannerOn() {
				return isBannerOn;
			}

			public String getIsInterstitialOn() {
				return isInterstitialOn;
			}

			public String getIsNativeOn() {
				return isNativeOn;
			}

			public String getBannerID() {
				return bannerID;
			}

			public String getInterstitialID() {
				return interstitialID;
			}

			public String getNativeID() {
				return nativeID;
			}

			public String getInterAdsClick() {
				return interAdsClick;
			}

			public String getNativeAdsPos() {
				return nativeAdsPos;
			}
		}

		public String getId() {
			return id;
		}

		public String getAdType() {
			return adType;
		}

		public ItemAdsDetails getItemAdsDetails() {
			return itemAdsDetails;
		}
	}
}