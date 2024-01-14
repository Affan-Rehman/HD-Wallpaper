package com.amazia_iwallcraft.wallpaper;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemAppDetailsList;
import com.amazia_iwallcraft.apiservices.ItemUserList;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    String cid = "", cname = "";
    SharedPref sharedPref;
    Methods methods;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        hideStatusBar();
        methods = new Methods(this);
        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);

        if (getIntent().hasExtra("cid")) {
            cid = getIntent().getStringExtra("cid");
            cname = getIntent().getStringExtra("cname");
        }

        if (sharedPref.getIsFirst()) {
            getAppDetails();
        } else {
            if (!sharedPref.getIsAutoLogin()) {
                new Handler().postDelayed(() -> openMainActivity(), 2000);
            } else {
                if (sharedPref.getLoginType().equals(Constant.LOGIN_TYPE_FB)) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        loadSocialLogin(Constant.LOGIN_TYPE_FB, sharedPref.getAuthID());
                    } else {
                        sharedPref.setIsAutoLogin(false);
                        openMainActivity();
                    }
                } else if (sharedPref.getLoginType().equals(Constant.LOGIN_TYPE_GOOGLE)) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        loadSocialLogin(Constant.LOGIN_TYPE_GOOGLE, sharedPref.getAuthID());
                    } else {
                        sharedPref.setIsAutoLogin(false);
                        openMainActivity();
                    }
                } else {
                    loadLogin(Constant.LOGIN_TYPE_NORMAL, "");
                }
            }
        }

        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constant.GRID_PADDING, r.getDisplayMetrics());
        Constant.columnWidth = (int) ((methods.getScreenWidth() - ((Constant.NUM_OF_COLUMNS + 1) * padding)) / Constant.NUM_OF_COLUMNS);
        Constant.columnHeight = (int) (Constant.columnWidth * 1.44);
    }

    private void loadLogin(final String loginType, final String authID) {
        if (methods.isNetworkAvailable()) {
            Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getLogin(methods.getAPIRequest(Constant.URL_LOGIN, 0, authID, loginType, "", "", "", "", "", sharedPref.getEmail(), sharedPref.getPassword(), "", "", ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        if (response.body().getArrayListUser().get(0).getSuccess().equals("1")) {
                            sharedPref.setLoginDetails(response.body().getArrayListUser().get(0).getId(), response.body().getArrayListUser().get(0).getName(), sharedPref.getUserMobile(), sharedPref.getEmail(), response.body().getArrayListUser().get(0).getImage(), authID, sharedPref.getIsRemember(), sharedPref.getPassword(), loginType);
                            sharedPref.setIsLogged(true);
                        } else {
                            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", loginType);
                            sharedPref.setIsLogged(false);
                        }
                    } else {
                        sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", loginType);
                        sharedPref.setIsLogged(false);
                    }
                    openMainActivity();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    openMainActivity();
                }
            });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openMainActivity();
                }
            }, 1000);
            Toast.makeText(SplashActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSocialLogin(final String loginType, final String authID) {
        if (methods.isNetworkAvailable()) {
            Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getSocialLogin(methods.getAPIRequest(Constant.URL_SOCIAL_LOGIN, 0, authID, loginType, "", "", "", "", sharedPref.getUserName(), sharedPref.getEmail(), "", "", "", ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        if (response.body().getArrayListUser().get(0).getSuccess().equals("1")) {
                            sharedPref.setLoginDetails(response.body().getArrayListUser().get(0).getId(), response.body().getArrayListUser().get(0).getName(), sharedPref.getUserMobile(), sharedPref.getEmail(), response.body().getArrayListUser().get(0).getImage(), authID, sharedPref.getIsRemember(), sharedPref.getPassword(), loginType);
                            sharedPref.setIsLogged(true);
                        }
                    }
                    openMainActivity();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    openMainActivity();
                }
            });
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openMainActivity();
                }
            }, 1000);
            Toast.makeText(SplashActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void getAppDetails() {
        if (methods.isNetworkAvailable()) {
            Call<ItemAppDetailsList> call = APIClient.getClient().create(APIInterface.class).getAppDetails(methods.getAPIRequest(Constant.URL_APP_DETAILS, 0, "", "", "", "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<ItemAppDetailsList>() {
                @Override
                public void onResponse(@NonNull Call<ItemAppDetailsList> call, @NonNull Response<ItemAppDetailsList> response) {
                    if (response.body() != null && response.body().getArrayListAbout() != null && response.body().getArrayListAbout().size() > 0) {
                        Constant.itemAbout = response.body().getArrayListAbout().get(0);

                        Constant.showUpdateDialog = response.body().getArrayListAbout().get(0).isShowAppUpdate();
                        Constant.appVersion = response.body().getArrayListAbout().get(0).getAppUpdateVersion();
                        Constant.appUpdateMsg = response.body().getArrayListAbout().get(0).getAppUpdateMessage();
                        Constant.appUpdateURL = response.body().getArrayListAbout().get(0).getAppUpdateLink();
                        Constant.appUpdateCancel = response.body().getArrayListAbout().get(0).isAppUpdateCancel();

//                        Constant.appUpdateCancel = c.getBoolean("google_play_link");
                        Constant.urlYoutube = response.body().getArrayListAbout().get(0).getYoutubeLink();
                        Constant.urlInstagram = response.body().getArrayListAbout().get(0).getInstagramLink();
                        Constant.urlTwitter = response.body().getArrayListAbout().get(0).getTwitterLink();
                        Constant.urlFacebook = response.body().getArrayListAbout().get(0).getFacebookLink();

                        Constant.isLiveWallpaperEnabled = response.body().getArrayListAbout().get(0).isLiveWallpaperOn();

                        if(response.body().getArrayListAbout().get(0).getArrayListAds() != null && response.body().getArrayListAbout().get(0).getArrayListAds().size() > 0) {
                            switch (response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getAdType()) {
                                case "Admob":
                                case "Facebook":
                                    Constant.publisherAdID = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getPublisherId();
                                    break;
                                case "StartApp":
                                    Constant.startappAppId = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getPublisherId();
                                    break;
                                case "Wortise":
                                    Constant.wortiseAppId = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getPublisherId();
                                    break;
                            }
                            Constant.bannerAdID = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getBannerID();
                            Constant.interstitialAdID = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getInterstitialID();
                            Constant.nativeAdID = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getNativeID();

                            Constant.isBannerAd = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getIsBannerOn().equals("1");
                            Constant.isInterAd = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getIsInterstitialOn().equals("1");
                            Constant.isNativeAd = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getIsNativeOn().equals("1");

                            Constant.interstitialAdShow = Integer.parseInt(response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getInterAdsClick());
                            Constant.nativeAdShow = Integer.parseInt(response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getItemAdsDetails().getNativeAdsPos());

                            Constant.bannerAdType = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getAdType();
                            Constant.interstitialAdType = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getAdType();
                            Constant.nativeAdType = response.body().getArrayListAbout().get(0).getArrayListAds().get(0).getAdType();
                        } else {
                            Constant.isBannerAd = false;
                            Constant.isInterAd = false;
                            Constant.isNativeAd = false;
                        }

                        if(response.body().getArrayListAbout().get(0).getArrayListPages() != null && response.body().getArrayListAbout().get(0).getArrayListPages().size() > 0) {
                            Constant.arrayListPages.clear();
                            for (int i = 0; i < response.body().getArrayListAbout().get(0).getArrayListPages().size(); i++) {
                                if(!response.body().getArrayListAbout().get(0).getArrayListPages().get(i).getId().equals("1")) {
                                    Constant.arrayListPages.add(response.body().getArrayListAbout().get(0).getArrayListPages().get(i));
                                } else {
                                    Constant.itemAbout.setAppDesc(response.body().getArrayListAbout().get(0).getArrayListPages().get(i).getContent());
                                }
                            }
                        }
                    }

                    String version = "";
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = String.valueOf(pInfo.versionCode);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (Constant.showUpdateDialog && !Constant.appVersion.equals(version)) {
                        methods.showUpdateAlert(Constant.appUpdateMsg, true);
                    } else {
                        sharedPref.setAdDetails(Constant.isBannerAd, Constant.isInterAd, Constant.isNativeAd, Constant.bannerAdType,
                                Constant.interstitialAdType, Constant.nativeAdType, Constant.bannerAdID, Constant.interstitialAdID, Constant.nativeAdID, Constant.startappAppId, Constant.interstitialAdShow, Constant.nativeAdShow);
                        sharedPref.setSocialDetails();

                        dbHelper.addtoAbout();
                        openLoginActivity();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemAppDetailsList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            errorDialog(getString(R.string.internet_not_connected), getString(R.string.error_connect_net_tryagain));
        }
    }

    private void errorDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this, R.style.ThemeDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        if (title.equals(getString(R.string.internet_not_connected)) || title.equals(getString(R.string.server_error))) {
            alertDialog.setNegativeButton(getString(R.string.try_again), (dialog, which) -> getAppDetails());
        }

        alertDialog.setPositiveButton(getString(R.string.exit), (dialog, which) -> finish());
        alertDialog.show();
    }

    private void openLoginActivity() {
        Intent intent;
        if (sharedPref.getIsFirst()) {
            sharedPref.setIsFirst(false);
            intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void openMainActivity() {
        Intent intent;
        if (!cid.equals("")) {
            intent = new Intent(SplashActivity.this, WallpaperByCatActivity.class);
            intent.putExtra("cid", cid);
            intent.putExtra("cname", cname);
            intent.putExtra("from", "noti");
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("from", "");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}