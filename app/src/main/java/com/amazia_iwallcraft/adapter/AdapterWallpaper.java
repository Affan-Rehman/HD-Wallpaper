package com.amazia_iwallcraft.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemSuccessList;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.interfaces.RecyclerViewClickListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;
import com.wortise.ads.AdError;
import com.wortise.ads.natives.GoogleNativeAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdapterWallpaper extends RecyclerView.Adapter {

    ArrayList<ItemWallpaper> arrayList;
    Context context;
    SharedPref sharedPref;
    RecyclerViewClickListener recyclerViewClickListener;
    Methods methods;

    final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();
    List<NativeAdDetails> nativeAdsStartApp = new ArrayList<>();

    public AdapterWallpaper(Context context, ArrayList<ItemWallpaper> arrayList, RecyclerViewClickListener recyclerViewClickListener) {
        this.arrayList = arrayList;
        this.context = context;
        methods = new Methods(context);
        sharedPref = new SharedPref(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        loadNativeAds();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        LikeButton likeButton;
        TextView tv_title;
        RoundedImageView iv_wallpaper;

        private MyViewHolder(View view) {
            super(view);
            iv_wallpaper = view.findViewById(R.id.iv_wallpaper);
            likeButton = view.findViewById(R.id.button_wall_fav);
            tv_title = view.findViewById(R.id.tv_wall_cat);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_native_ad;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
            rl_native_ad = view.findViewById(R.id.rl_native_ad);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType >= 1000) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ads, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wallpaper, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).likeButton.setLiked(arrayList.get(position).getIsFav());
            ((MyViewHolder) holder).tv_title.setText(arrayList.get(position).getTitle());

            int placeholder;
            if(arrayList.get(position).getType().equals(Constant.TAG_PORTRAIT)) {
                placeholder = R.drawable.ic_placeholder_portrait;
            } else if(arrayList.get(position).getTitle().equals(Constant.TAG_LANDSCAPE)) {
                placeholder = R.drawable.ic_placeholder_landscape;
            } else {
                placeholder = R.drawable.ic_placeholder_square;
            }
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .resize(methods.getImageThumbWidth(arrayList.get(holder.getAbsoluteAdapterPosition()).getType()), methods.getImageThumbHeight(arrayList.get(holder.getAbsoluteAdapterPosition()).getType()))
                    .centerCrop()
                    .placeholder(placeholder)
                    .into(((MyViewHolder) holder).iv_wallpaper);

            if (sharedPref.isLogged()) {
                ((MyViewHolder) holder).likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        try {
                            loadFav(holder.getAbsoluteAdapterPosition());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        try {
                            loadFav(holder.getAbsoluteAdapterPosition());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ((MyViewHolder) holder).likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!sharedPref.isLogged()) {
                            methods.clickLogin();
                        }
                    }
                });
            }

            ((MyViewHolder) holder).iv_wallpaper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewClickListener.onClick(holder.getAbsoluteAdapterPosition());
                }
            });
        } else if (holder instanceof ADViewHolder) {
            if (isAdLoaded) {
                if (((ADViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                    switch (Constant.nativeAdType) {
                        case Constant.AD_TYPE_ADMOB:
                        case Constant.AD_TYPE_FACEBOOK:
                            if (mNativeAdsAdmob.size() >= 1) {

                                int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);

                                NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob_grid, null);
                                populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                            }
                            break;
                        case Constant.AD_TYPE_STARTAPP:
                            int i = new Random().nextInt(nativeAdsStartApp.size() - 1);

                            RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_startapp_grid, null);
                            populateStartAppNativeAdView(nativeAdsStartApp.get(i), nativeAdView);

                            ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                            ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                            ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                            break;
                        case Constant.AD_TYPE_APPLOVIN:
                            MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Constant.nativeAdID, context);
                            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                                @Override
                                public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                                    nativeAdView.setPadding(0, 0, 0, 10);
                                    nativeAdView.setBackgroundColor(Color.WHITE);
                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                                }

                                @Override
                                public void onNativeAdClicked(final MaxAd ad) {
                                }
                            });

                            nativeAdLoader.loadAd();
                            break;
                        case Constant.AD_TYPE_WORTISE:
                            if (!((ADViewHolder) holder).isAdRequested) {
                                GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                        context, Constant.nativeAdID, new GoogleNativeAd.Listener() {
                                    @Override
                                    public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                                    }

                                    @Override
                                    public void onNativeFailed(@NonNull GoogleNativeAd googleNativeAd, @NonNull AdError adError) {
                                        ((ADViewHolder) holder).isAdRequested = false;
                                    }

                                    @Override
                                    public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                    }

                                    @Override
                                    public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                        NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob_grid, null);
                                        populateUnifiedNativeAdView(nativeAd, adView);
                                        ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                        ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                        ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                    }
                                });
                                googleNativeAd.load();
                                ((ADViewHolder) holder).isAdRequested = true;
                            }
                            break;
                    }
                }
            }
        }
    }

    private void loadFav(int pos) {
        if (sharedPref.isLogged()) {
            if (methods.isNetworkAvailable()) {
                Call<ItemSuccessList> call = APIClient.getClient().create(APIInterface.class).getDoFavourite(methods.getAPIRequest(Constant.URL_DO_FAV, 0, arrayList.get(pos).getId(), "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), "Wallpaper"));
                call.enqueue(new Callback<ItemSuccessList>() {
                    @Override
                    public void onResponse(@NonNull Call<ItemSuccessList> call, @NonNull Response<ItemSuccessList> response) {
                        try {
                            if (response.body() != null && response.body().getArrayListSuccess() != null) {
                                if (response.body().getArrayListSuccess().size() > 0) {
                                    arrayList.get(pos).setIsFav(response.body().getArrayListSuccess().get(0).getSuccess().equals("true"));
                                    Toast.makeText(context, response.body().getArrayListSuccess().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ItemSuccessList> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            } else {
                Toast.makeText(context, context.getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            methods.clickLogin();
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void hideHeader() {
        try {
//            ProgressViewHolder.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return 1000 + position;
        } else {
            return position;
        }
    }

//    public void setType(String type) {
//        this.type = type;
//        setColumnWidthHeight(type);
//    }

//    private void setColumnWidthHeight(String type) {
//        if (type.equals("") || type.equals(context.getString(R.string.portrait))) {
//            columnWidth = methods.getColumnWidth(3, 3);
//            columnHeight = (int) (columnWidth * 1.55);
//        } else if (type.equals(context.getString(R.string.landscape))) {
//            columnWidth = methods.getColumnWidth(2, 3);
//            columnHeight = (int) (columnWidth * 0.54);
//        } else {
//            columnWidth = methods.getColumnWidth(3, 3);
//            columnHeight = columnWidth;
//        }
//    }

    @SuppressLint("MissingPermission")
    private void loadNativeAds() {
        if (Constant.isNativeAd) {
            switch (Constant.nativeAdType) {
                case Constant.AD_TYPE_ADMOB:
                case Constant.AD_TYPE_FACEBOOK:
                    AdLoader.Builder builder = new AdLoader.Builder(context, Constant.nativeAdID);
                    AdLoader adLoader = builder.forNativeAd(
                                    new NativeAd.OnNativeAdLoadedListener() {
                                        @Override
                                        public void onNativeAdLoaded(@NotNull NativeAd nativeAd) {
                                            mNativeAdsAdmob.add(nativeAd);
                                            if(mNativeAdsAdmob.size() == 5) {
                                                isAdLoaded = true;
                                            }
                                        }
                                    })
                            .build();

                    // Load the Native Express ad.
                    Bundle extras = new Bundle();
                    if (ConsentInformation.getInstance(context).getConsentStatus() != ConsentStatus.PERSONALIZED) {
                        extras.putString("npa", "1");
                    }
                    AdRequest adRequest;
                    if (Constant.nativeAdType.equals(Constant.AD_TYPE_ADMOB)) {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    } else {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                                .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                                .build();
                    }

                    adLoader.loadAds(adRequest, 5);
                    break;
                case Constant.AD_TYPE_STARTAPP:
                    StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                    nativeAd.loadAd(new NativeAdPreferences()
                            .setAdsNumber(3)
                            .setAutoBitmapDownload(true)
                            .setPrimaryImageSize(2), new AdEventListener() {
                        @Override
                        public void onReceiveAd(Ad ad) {
                            nativeAdsStartApp.addAll(nativeAd.getNativeAds());
                            isAdLoaded = true;
                        }

                        @Override
                        public void onFailedToReceiveAd(Ad ad) {
                        }
                    });
                    break;
                case Constant.AD_TYPE_APPLOVIN:
                    isAdLoaded = true;
                    break;
                case Constant.AD_TYPE_WORTISE:
                    isAdLoaded = true;
                    break;
            }
        }
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                float scale = context.getResources().getDisplayMetrics().density;

                int maxHeightPixels = 175;
                int maxHeightDp = (int) (maxHeightPixels * scale + 0.5f);

                if (child instanceof ImageView) { //Images
                    ImageView imageView = (ImageView) child;
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight(maxHeightDp);

                } else { //Videos
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    params.height = maxHeightDp;
                    child.setLayoutParams(params);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < mediaView.getChildCount(); i++) {
            View view = mediaView.getChildAt(i);
            if (view instanceof ImageView) {
                ((ImageView) view).setAdjustViewBounds(true);
            }
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    private void populateStartAppNativeAdView(NativeAdDetails nativeAdDetails, RelativeLayout nativeAdView) {
        ImageView icon = nativeAdView.findViewById(R.id.icon);
        TextView title = nativeAdView.findViewById(R.id.title);
        TextView description = nativeAdView.findViewById(R.id.description);
        Button button = nativeAdView.findViewById(R.id.button);

        icon.setImageBitmap(nativeAdDetails.getImageBitmap());
        title.setText(nativeAdDetails.getTitle());
        description.setText(nativeAdDetails.getDescription());
        button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
    }

    public void destroyNativeAds() {
        try {
            for (int i = 0; i < mNativeAdsAdmob.size(); i++) {
                mNativeAdsAdmob.get(i).destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}