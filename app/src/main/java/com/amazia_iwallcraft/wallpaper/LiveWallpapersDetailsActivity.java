package com.amazia_iwallcraft.wallpaper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.eventbus.EventAction;
import com.eventbus.GlobalBus;
import com.amazia_iwallcraft.adapter.AdapterTags;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemSuccessList;
import com.amazia_iwallcraft.apiservices.ItemWallpaperList;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.RecyclerItemClickListener;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ortiz.touchview.TouchImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveWallpapersDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Methods methods;
    private DBHelper dbHelper;
    private SharedPref sharedPref;
    private ViewPager viewpager;
    LikeButton likeButton;
    FloatingActionButton btn_download;
    MaterialButton btn_setas;

    private int position;
    private Dialog dialog_rate;
    private ConstraintLayout coordinatorLayout;
    private ProgressDialog progressDialog;
    private BottomSheetDialog dialog_report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_wall_details);

        progressDialog = new ProgressDialog(LiveWallpapersDetailsActivity.this);
        progressDialog.setMessage(getString(R.string.loading));

        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);
        methods = new Methods(this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                methods.saveImage(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getImage(), type, coordinatorLayout, "live_wall");
            }
        });
        methods.forceRTLIfSupported(getWindow());

        toolbar = this.findViewById(R.id.toolbar_wall_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(LiveWallpapersDetailsActivity.this, R.color.bg));
            if (!methods.isDarkMode()) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 55;
            toolbar.setLayoutParams(params);
        }
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        position = getIntent().getIntExtra("pos", 0);

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        coordinatorLayout = findViewById(R.id.rl);
        btn_download = findViewById(R.id.btn_details_download);
        likeButton = findViewById(R.id.btn_details_fav);
        btn_setas = findViewById(R.id.btn_details_setas);

        loadViewed(position);

        methods.showBannerAd(ll_adView);

        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewpager = findViewById(R.id.vp_wall_details);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(position);

        btn_download.setOnClickListener(v -> {
            methods.showInter(0, getString(R.string.download));
        });

        btn_setas.setOnClickListener(v -> {
            methods.showInter(0, getString(R.string.set_wallpaper));
        });

        likeButton.setLiked(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getIsFav());
        if (sharedPref.isLogged()) {
            likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    try {
                        loadFav(viewpager.getCurrentItem());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    try {
                        loadFav(viewpager.getCurrentItem());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!sharedPref.isLogged()) {
                        methods.clickLogin();
                    }
                }
            });
        }

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                position = viewpager.getCurrentItem();
                likeButton.setLiked(Constant.arrayListLiveWallpapers.get(position).getIsFav());
                loadViewed(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int position) {
            }

            @Override
            public void onPageScrollStateChanged(int position) {
            }
        });

        loadWallpaperDetails(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpaper_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.menu_setwall) {
            methods.showInter(0, getString(R.string.set_wallpaper));
        } else if (itemId == R.id.menu_rate) {
            if (sharedPref.isLogged()) {
                openRateDialog();
            } else {
                methods.clickLogin();
            }
        } else if (itemId == R.id.menu_share) {
            methods.showInter(0, getString(R.string.share));
        } else if (itemId == R.id.menu_details) {
            showDetailDialog();
        } else if (itemId == R.id.menu_report) {
            showReportDialog();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return Constant.arrayListLiveWallpapers.size();

        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.layout_vp_live_wallpaper, container, false);
            assert imageLayout != null;

            final TouchImageView iv_wallpaper = imageLayout.findViewById(R.id.iv_wallpaper);
            final CircularProgressBar progressBar = imageLayout.findViewById(R.id.pb_wall_details);

            Glide.with(LiveWallpapersDetailsActivity.this)
                    .asGif()
                    .load(Constant.arrayListLiveWallpapers.get(position).getImage())
                    .placeholder(R.drawable.placeholder_wall)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(iv_wallpaper);

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void loadWallpaperDetails(int pos) {
        try {
            Call<ItemWallpaperList> call = APIClient.getClient().create(APIInterface.class).getLiveWallDetails(methods.getAPIRequest(Constant.URL_LIVE_WALL_DETAILS, 0, "", "", "", "", Constant.arrayListLiveWallpapers.get(pos).getId(), "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    if (response.body() != null && response.body().getArrayListWallpaper() != null && response.body().getArrayListWallpaper().size() > 0) {
                        Constant.arrayListLiveWallpapers.get(pos).setUserRating(response.body().getArrayListWallpaper().get(0).getUserRating());
                        Constant.arrayListLiveWallpapers.get(pos).setTags(response.body().getArrayListWallpaper().get(0).getTags());
                        dbHelper.updateTags(Constant.arrayListLiveWallpapers.get(pos).getId(), Constant.arrayListLiveWallpapers.get(pos).getTags());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemWallpaperList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadViewed(int pos) {
        try {
            Call<ItemWallpaperList> call = APIClient.getClient().create(APIInterface.class).getWallpaperView(methods.getAPIRequest(Constant.URL_WALLPAPER_VIEW, 0, "", "", "", "", Constant.arrayListLiveWallpapers.get(pos).getId(), "", "", "", "", "", sharedPref.getUserId(), "LiveWallpaper"));
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    int tot = Integer.parseInt(Constant.arrayListLiveWallpapers.get(pos).getTotalViews());
                    Constant.arrayListLiveWallpapers.get(pos).setTotalViews("" + (tot + 1));
                }

                @Override
                public void onFailure(@NonNull Call<ItemWallpaperList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDownloadCount(int pos) {
        try {
            Call<ItemSuccessList> call = APIClient.getClient().create(APIInterface.class).getWallpaperDownloadCount(methods.getAPIRequest(Constant.URL_WALLPAPER_DOWNLOAD_COUNT, 0, "", "", "", "", Constant.arrayListLiveWallpapers.get(pos).getId(), "", "", "", "", "", sharedPref.getUserId(), "LiveWallpaper"));
            call.enqueue(new Callback<ItemSuccessList>() {
                @Override
                public void onResponse(@NonNull Call<ItemSuccessList> call, @NonNull Response<ItemSuccessList> response) {
                    if (response.body() != null && response.body().getArrayListSuccess() != null && response.body().getArrayListSuccess().size() > 0) {
                        Constant.arrayListLiveWallpapers.get(pos).setTotalDownloads(response.body().getArrayListSuccess().get(0).getTotalDownloads());
                        dbHelper.updateView(Constant.arrayListLiveWallpapers.get(pos).getId(), Constant.arrayListLiveWallpapers.get(pos).getTotalViews(), Constant.arrayListLiveWallpapers.get(pos).getTotalDownloads());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemSuccessList> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRateDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_rating, null);

        dialog_rate = new BottomSheetDialog(LiveWallpapersDetailsActivity.this);
        dialog_rate.setContentView(view);
        dialog_rate.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_rate.show();

        final RatingBar ratingBar = dialog_rate.findViewById(R.id.rb_add);
        ratingBar.setRating(1);
        final MaterialButton button_submit = dialog_rate.findViewById(R.id.button_submit_rating);
        final MaterialButton button_later = dialog_rate.findViewById(R.id.button_later_rating);
        final TextView textView = dialog_rate.findViewById(R.id.tv_rate);

        if (Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getUserRating()==null || Constant.arrayList.get(viewpager.getCurrentItem()).getUserRating().equals("0")) {
            textView.setText(getString(R.string.rate_this_wall));
        } else {
            textView.setText(getString(R.string.thanks_for_rating));
            ratingBar.setRating(Float.parseFloat(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getUserRating()));
        }

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingBar.getRating() != 0) {
                    if (methods.isNetworkAvailable()) {
                        loadRatingApi(String.valueOf(ratingBar.getRating()));
                    } else {
                        methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.internet_not_connected));
                    }
                } else {
                    Toast.makeText(LiveWallpapersDetailsActivity.this, getString(R.string.enter_rating), Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_rate.dismiss();
            }
        });
    }

    private void loadRatingApi(final String rate) {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(LiveWallpapersDetailsActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));

        Call<ItemSuccessList> call = APIClient.getClient().create(APIInterface.class).getDoRateWallpaper(methods.getAPIRequest(Constant.URL_RATE_WALLPAPER, 0, "", "", "", "", Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getId(), rate, "", "", "", "", sharedPref.getUserId(), "LiveWallpaper"));
        call.enqueue(new Callback<ItemSuccessList>() {
            @Override
            public void onResponse(@NonNull Call<ItemSuccessList> call, @NonNull Response<ItemSuccessList> response) {
                if (response.body() != null && response.body().getArrayListSuccess() != null) {
                    if (response.body().getArrayListSuccess().size() > 0) {
                        methods.showSnackBar(coordinatorLayout, response.body().getArrayListSuccess().get(0).getMessage());

                        Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).setAverageRate(String.valueOf(response.body().getArrayListSuccess().get(0).getTotalRate()));
                        Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).setUserRating(String.valueOf(rate));

                        dialog_rate.dismiss();
                    }
                } else {
                    methods.showSnackBar(coordinatorLayout, getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemSuccessList> call, @NonNull Throwable t) {
                call.cancel();
                methods.showSnackBar(coordinatorLayout, getString(R.string.server_error));
            }
        });
    }

    private void showReportDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.layout_report, null);

        dialog_report = new BottomSheetDialog(LiveWallpapersDetailsActivity.this);
        dialog_report.setContentView(view);
        dialog_report.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_report.show();

        final EditText editText_report;
        MaterialButton button_submit;

        button_submit = dialog_report.findViewById(R.id.button_report_submit);
        editText_report = dialog_report.findViewById(R.id.et_report);

        button_submit.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText_report.getText().toString().trim().isEmpty()) {
                    Toast.makeText(LiveWallpapersDetailsActivity.this, getString(R.string.enter_report), Toast.LENGTH_SHORT).show();
                } else {
                    if (sharedPref.isLogged()) {
                        loadReportSubmit(editText_report.getText().toString());
//                        Toast.makeText(LiveWallpapersDetailsActivity.this, "Report is not available in demo app", Toast.LENGTH_SHORT).show();
                    } else {
                        methods.clickLogin();
                    }
                }
            }
        });
    }

    private void showDetailDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.layout_wallpaper_details, null);

        dialog_report = new BottomSheetDialog(LiveWallpapersDetailsActivity.this);
        dialog_report.setContentView(view);
        dialog_report.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_report.show();

        RecyclerView rv_tags = view.findViewById(R.id.rv_tags);
        ;
        AdapterTags adapterTags;

        RatingBar ratingBar = view.findViewById(R.id.rating_wall_details);
        TextView tv_views = view.findViewById(R.id.tv_wall_details_views);
        TextView tv_downloads = view.findViewById(R.id.tv_wall_details_downloads);
        TextView tv_cat = view.findViewById(R.id.tv_details_cat);

        ratingBar.setRating(Float.parseFloat(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getAverageRate()));

        tv_cat.setText(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getTitle());
        tv_views.setText(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getTotalViews());
        tv_downloads.setText(Constant.arrayListLiveWallpapers.get(viewpager.getCurrentItem()).getTotalDownloads());

        rv_tags.setLayoutManager(new LinearLayoutManager(LiveWallpapersDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rv_tags.setItemAnimator(new DefaultItemAnimator());

        ArrayList<String> arrayListTags = new ArrayList<>(Arrays.asList(Constant.arrayListLiveWallpapers.get(position).getTags().split(",")));
        adapterTags = new AdapterTags(arrayListTags);
        rv_tags.setAdapter(adapterTags);

        rv_tags.addOnItemTouchListener(new RecyclerItemClickListener(LiveWallpapersDetailsActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Constant.search_item = arrayListTags.get(position);
                Intent intent = new Intent(LiveWallpapersDetailsActivity.this, SearchWallActivity.class);
                startActivity(intent);
            }
        }));

    }

    public void loadReportSubmit(String report) {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();
            Call<ItemSuccessList> call = APIClient.getClient().create(APIInterface.class).getReport(methods.getAPIRequest(Constant.URL_REPORT, 0, "", "", "", report, Constant.arrayListLiveWallpapers.get(position).getId(), "", "", "", "", "", sharedPref.getUserId(), "LiveWallpaper"));
            call.enqueue(new Callback<ItemSuccessList>() {
                @Override
                public void onResponse(@NonNull Call<ItemSuccessList> call, @NonNull Response<ItemSuccessList> response) {
                    if (response.body() != null && response.body().getArrayListSuccess() != null && response.body().getArrayListSuccess().size() > 0) {
                        try {
                            dialog_report.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(LiveWallpapersDetailsActivity.this, response.body().getArrayListSuccess().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ItemSuccessList> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    call.cancel();
                    Toast.makeText(LiveWallpapersDetailsActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFav(final int posi) {
        if (sharedPref.isLogged()) {
            if (methods.isNetworkAvailable()) {
                Call<ItemSuccessList> call = APIClient.getClient().create(APIInterface.class).getDoFavourite(methods.getAPIRequest(Constant.URL_DO_FAV, 0, Constant.arrayListLiveWallpapers.get(posi).getId(), "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), "LiveWallpaper"));
                call.enqueue(new Callback<ItemSuccessList>() {
                    @Override
                    public void onResponse(@NonNull Call<ItemSuccessList> call, @NonNull Response<ItemSuccessList> response) {
                        if (response.body() != null && response.body().getArrayListSuccess() != null) {
                            if (response.body().getArrayListSuccess().size() > 0) {
                                Constant.arrayListLiveWallpapers.get(posi).setIsFav(response.body().getArrayListSuccess().get(0).getSuccess().equals("true"));
                                Toast.makeText(LiveWallpapersDetailsActivity.this, response.body().getArrayListSuccess().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ItemSuccessList> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            } else {
                Toast.makeText(LiveWallpapersDetailsActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            methods.clickLogin();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDelete(EventAction eventAction) {
        try {
            loadDownloadCount(viewpager.getCurrentItem());
            GlobalBus.getBus().removeStickyEvent(eventAction);
        } catch (Exception e) {
            GlobalBus.getBus().removeStickyEvent(eventAction);
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }
}