package com.amazia_iwallcraft.wallpaper;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemAppDetailsList;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutActivity extends AppCompatActivity {

    Toolbar toolbar;
    WebView webView;
    TextView textView_appname, textView_email, textView_website, textView_company, textView_contact, textView_version;
    ImageView imageView_logo;
    LinearLayout ll_email, ll_website, ll_company, ll_contact;
    String website, email, desc, applogo, appname, appversion, appauthor, appcontact;
    DBHelper dbHelper;
    ProgressDialog pbar;
    Methods methods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        toolbar = this.findViewById(R.id.toolbar_about);
        toolbar.setTitle(getString(R.string.menu_about));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pbar = new ProgressDialog(this);
        pbar.setMessage(getResources().getString(R.string.loading));
        pbar.setCancelable(false);

        webView = findViewById(R.id.webView);
        textView_appname = findViewById(R.id.textView_about_appname);
        textView_email = findViewById(R.id.textView_about_email);
        textView_website = findViewById(R.id.textView_about_site);
        textView_company = findViewById(R.id.textView_about_company);
        textView_contact = findViewById(R.id.textView_about_contact);
        textView_version = findViewById(R.id.textView_about_appversion);
        imageView_logo = findViewById(R.id.imageView_about_logo);

        ll_email = findViewById(R.id.ll_email);
        ll_website = findViewById(R.id.ll_website);
        ll_contact = findViewById(R.id.ll_contact);
        ll_company = findViewById(R.id.ll_company);

        getAppDetails();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void getAppDetails() {
        if (methods.isNetworkAvailable()) {
            pbar.show();
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

                    if (pbar.isShowing()) {
                        pbar.dismiss();
                    }

                    new SharedPref(AboutActivity.this).setSocialDetails();
                    setVariables();
                    dbHelper.addtoAbout();
                }

                @Override
                public void onFailure(@NonNull Call<ItemAppDetailsList> call, @NonNull Throwable t) {
                    if (pbar.isShowing()) {
                        pbar.dismiss();
                    }
                    call.cancel();
                }
            });
        } else {
            if (dbHelper.getAbout()) {
                setVariables();
            }
        }
    }

    public void setVariables() {

        appname = Constant.itemAbout.getAppName();
        applogo = Constant.itemAbout.getAppLogo();
        desc = Constant.itemAbout.getAppDesc();
        appversion = Constant.itemAbout.getAppVersion();
        appauthor = Constant.itemAbout.getAuthor();
        appcontact = Constant.itemAbout.getContact();
        email = Constant.itemAbout.getEmail();
        website = Constant.itemAbout.getWebsite();

        textView_appname.setText(appname);
        if (!email.trim().isEmpty()) {
            ll_email.setVisibility(View.VISIBLE);
            textView_email.setText(email);
        }

        if (!website.trim().isEmpty()) {
            ll_website.setVisibility(View.VISIBLE);
            textView_website.setText(website);
        }

        if (!appauthor.trim().isEmpty()) {
            ll_company.setVisibility(View.VISIBLE);
            textView_company.setText(appauthor);
        }

        if (!appcontact.trim().isEmpty()) {
            ll_contact.setVisibility(View.VISIBLE);
            textView_contact.setText(appcontact);
        }

        if (!appversion.trim().isEmpty()) {
            textView_version.setText(appversion);
        }

        if (applogo.trim().isEmpty()) {
            imageView_logo.setVisibility(View.GONE);
        } else {
            Picasso
                    .get()
                    .load(applogo)
                    .into(imageView_logo);
        }

        String mimeType = "text/html";
        String encoding = "utf-8";

        String text;
        if (methods.isDarkMode()) {
            text = "<html><head>"
                    + "<style> body{color:#fff !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + desc
                    + "</body></html>";
        } else {
            text = "<html><head>"
                    + "<style> body{color:#000 !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + desc
                    + "</body></html>";
        }

        webView.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            webView.loadData(text, mimeType, encoding);
        } else {
            webView.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
        }
    }
}