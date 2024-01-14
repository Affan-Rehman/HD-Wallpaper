package com.amazia_iwallcraft.wallpaper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemAppDetailsList;
import com.amazia_iwallcraft.apiservices.ItemColorsList;
import com.amazia_iwallcraft.fragments.FragmentDashboard;
import com.amazia_iwallcraft.fragments.FragmentWallDownloaded;
import com.amazia_iwallcraft.fragments.FragmentWallFeatured;
import com.amazia_iwallcraft.fragments.FragmentWallRated;
import com.amazia_iwallcraft.fragments.FragmentWallRecent;
import com.amazia_iwallcraft.interfaces.AdConsentListener;
import com.amazia_iwallcraft.utils.AdConsent;
import com.amazia_iwallcraft.utils.AdManagerInterAdmob;
import com.amazia_iwallcraft.utils.AdManagerInterApplovin;
import com.amazia_iwallcraft.utils.AdManagerInterStartApp;
import com.amazia_iwallcraft.utils.AdManagerInterWortise;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Methods methods;
    DBHelper dbHelper;
    FragmentManager fm;
    LinearLayout ll_ad;
    Toolbar toolbar;
    AdConsent adConsent;
    DrawerLayout drawer;
    SharedPref sharedPref;
    NavigationView navigationView;
    MenuItem menu_login;
    ImageView iv_fb, iv_twitter, iv_yt, iv_insta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    getPackageName(), //Insert your own package name.
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ll_ad = findViewById(R.id.ll_adView);

        fm = getSupportFragmentManager();

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        toggle.setHomeAsUpIndicator(R.mipmap.nav);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        toggle.setDrawerIndicatorEnabled(false);

        Constant.isLiveWallpaperEnabled = sharedPref.getIsLiveWallpaper();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        iv_fb = findViewById(R.id.iv_fb);
        iv_insta = findViewById(R.id.iv_insta);
        iv_twitter = findViewById(R.id.iv_twitter);
        iv_yt = findViewById(R.id.iv_yt);

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                methods.showBannerAd(ll_ad);
            }
        });

        getAppDetails();
        if (Constant.arrayListColors.size() == 0 || !sharedPref.getIsColorSaved()) {
            getColors();
        }

        Menu menu = navigationView.getMenu();
        menu_login = menu.findItem(R.id.nav_login);
        changeLoginName();

        FragmentDashboard f1 = new FragmentDashboard();
        loadFrag(f1, getResources().getString(R.string.home), fm);
        navigationView.setCheckedItem(R.id.nav_home);

        if(!methods.getPerNotificationStatus()) {
            methods.permissionDialog();
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

                        if (response.body().getArrayListAbout().get(0).getArrayListAds() != null && response.body().getArrayListAbout().get(0).getArrayListAds().size() > 0) {
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

                        if (response.body().getArrayListAbout().get(0).getArrayListPages() != null && response.body().getArrayListAbout().get(0).getArrayListPages().size() > 0) {
                            Constant.arrayListPages.clear();
                            for (int i = 0; i < response.body().getArrayListAbout().get(0).getArrayListPages().size(); i++) {
                                if (!response.body().getArrayListAbout().get(0).getArrayListPages().get(i).getId().equals("1")) {
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
                        methods.showUpdateAlert(Constant.appUpdateMsg, false);
                    } else {
                        adConsent.checkForConsent();
                        dbHelper.addtoAbout();

                        sharedPref.setIsLiveWallpaper(Constant.isLiveWallpaperEnabled);

                        methods.initializeAds();

                        sharedPref.setAdDetails(Constant.isBannerAd, Constant.isInterAd, Constant.isNativeAd, Constant.bannerAdType,
                                Constant.interstitialAdType, Constant.nativeAdType, Constant.bannerAdID, Constant.interstitialAdID, Constant.nativeAdID, Constant.startappAppId, Constant.interstitialAdShow, Constant.nativeAdShow);
                        sharedPref.setSocialDetails();
                        setSocialButtons();

                        if (Constant.isInterAd) {
                            switch (Constant.interstitialAdType) {
                                case Constant.AD_TYPE_ADMOB:
                                case Constant.AD_TYPE_FACEBOOK:
                                    AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
                                    adManagerInterAdmob.createAd();
                                    break;
                                case Constant.AD_TYPE_STARTAPP:
                                    AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(getApplicationContext());
                                    adManagerInterStartApp.createAd();
                                    break;
                                case Constant.AD_TYPE_APPLOVIN:
                                    AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(MainActivity.this);
                                    adManagerInterApplovin.createAd();
                                    break;
                                case Constant.AD_TYPE_WORTISE:
                                    AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(MainActivity.this);
                                    adManagerInterWortise.createAd();
                                    break;
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemAppDetailsList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            FragmentDashboard f1 = new FragmentDashboard();
            loadFrag(f1, getResources().getString(R.string.home), fm);
            navigationView.setCheckedItem(R.id.nav_home);

            adConsent.checkForConsent();
            dbHelper.getAbout();
        }
    }

    private void getColors() {
        if (methods.isNetworkAvailable()) {
            Call<ItemColorsList> call = APIClient.getClient().create(APIInterface.class).getColors(methods.getAPIRequest(Constant.URL_COLORS, 0, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<ItemColorsList>() {
                @Override
                public void onResponse(@NonNull Call<ItemColorsList> call, @NonNull Response<ItemColorsList> response) {
                    if (response.body() != null && response.body().getArrayListColors() != null) {
                        dbHelper.removeColors();
                        Constant.arrayListColors.clear();
                        Constant.arrayListColors.addAll(response.body().getArrayListColors());
                        for (int i = 0; i < Constant.arrayListColors.size(); i++) {
                            dbHelper.addtoColorList(Constant.arrayListColors.get(i));
                        }
                        sharedPref.setColorSaved();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemColorsList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() != 0) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount() - 1).getTag();
            if (title.equals(getString(R.string.dashboard)) || title.equals(getString(R.string.home))) {

                int vp_pos = FragmentDashboard.bottomNavigationMenu.getSelectedItemId();
                if (vp_pos == R.id.nav_bottom_latest) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.home));
                } else if (vp_pos == R.id.nav_bottom_cat) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.categories));
                } else if (vp_pos == R.id.nav_bottom_live_wallpapers) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.live_wallpapers));
                } else if (vp_pos == R.id.nav_bottom_popular) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.popular));
                } else if (vp_pos == R.id.nav_bottom_profile) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.profile));
                }

                navigationView.setCheckedItem(R.id.nav_home);
            }
            super.onBackPressed();
        } else {
            exitDialog();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        clickNav(item.getItemId());
        return true;
    }

    private void clickNav(int item) {
        if (item == R.id.nav_home) {
            FragmentDashboard fhome = new FragmentDashboard();
            loadFrag(fhome, getResources().getString(R.string.home), fm);
        } else if (item == R.id.nav_recent) {
            FragmentWallRecent frecent = new FragmentWallRecent();
            loadFrag(frecent, getResources().getString(R.string.recently_viewed), fm);
        } else if (item == R.id.nav_featured) {
            FragmentWallFeatured ffeatured = new FragmentWallFeatured();
            loadFrag(ffeatured, getResources().getString(R.string.featured), fm);
        } else if (item == R.id.nav_rated) {
            FragmentWallRated frated = new FragmentWallRated();
            loadFrag(frated, getResources().getString(R.string.rated), fm);
        } else if (item == R.id.nav_downloaded) {
            FragmentWallDownloaded fdownloaded = new FragmentWallDownloaded();
            loadFrag(fdownloaded, getResources().getString(R.string.most_downloaded), fm);
        } else if (item == R.id.nav_setting) {
            Intent intent_set = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent_set);
        } else if (item == R.id.nav_login) {
            methods.clickLogin();
        }
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        FragmentTransaction ft = fm.beginTransaction();
//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (!name.equals(getString(R.string.home))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.frame_layout, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.frame_layout, f1, name);
        }

        ft.commitAllowingStateLoss();

        getSupportActionBar().setTitle(name);
    }

    public void setSocialButtons() {
        if (!sharedPref.getFB().equals("")) {
            iv_fb.setVisibility(View.VISIBLE);
            iv_fb.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sharedPref.getFB()));
                startActivity(intent);
            });
        } else {
            iv_fb.setVisibility(View.GONE);
        }
        if (!sharedPref.getInsta().equals("")) {
            iv_insta.setVisibility(View.VISIBLE);
            iv_insta.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sharedPref.getInsta()));
                startActivity(intent);
            });
        } else {
            iv_insta.setVisibility(View.GONE);
        }
        if (!sharedPref.getTwitter().equals("")) {
            iv_twitter.setVisibility(View.VISIBLE);
            iv_twitter.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sharedPref.getTwitter()));
                startActivity(intent);
            });
        } else {
            iv_twitter.setVisibility(View.GONE);
        }
        if (!sharedPref.getYoutube().equals("")) {
            iv_yt.setVisibility(View.VISIBLE);
            iv_yt.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sharedPref.getYoutube()));
                startActivity(intent);
            });
        } else {
            iv_yt.setVisibility(View.GONE);
        }
    }

    private void exitDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.ThemeDialog);

        alert.setTitle(getString(R.string.exit));
        alert.setMessage(getString(R.string.sure_exit));
        alert.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }

    private void changeLoginName() {
        if (menu_login != null) {
            if (new SharedPref(MainActivity.this).isLogged()) {
                menu_login.setTitle(getResources().getString(R.string.logout));
                menu_login.setIcon(getResources().getDrawable(R.mipmap.logout));
            } else {
                menu_login.setTitle(getResources().getString(R.string.login));
                menu_login.setIcon(getResources().getDrawable(R.mipmap.login));
            }
        }
    }

    @Override
    protected void onResume() {
        changeLoginName();
        super.onResume();
    }
}