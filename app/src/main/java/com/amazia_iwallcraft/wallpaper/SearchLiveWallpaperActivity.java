package com.amazia_iwallcraft.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.amazia_iwallcraft.adapter.AdapterLiveWallpaper;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemWallpaperList;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.interfaces.RecyclerViewClickListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.EndlessRecyclerViewScrollListener;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchLiveWallpaperActivity extends AppCompatActivity {

    DBHelper dbHelper;
    Toolbar toolbar;
    RecyclerView rv_wallpapers;
    AdapterLiveWallpaper adapter;
    ArrayList<ItemWallpaper> arrayList;
    ProgressBar progressBar;
    Methods methods;
    InterAdListener interAdListener;
    Boolean isOver = false, isScroll = false, isLoading = false;
    TextView textView_empty;
    int page = 1, totalRecord = 0;
    StaggeredGridLayoutManager grid;
    FloatingActionButton fab;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_wall);

        sharedPref = new SharedPref(this);

        grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Constant.arrayListLiveWallpapers.clear();
                Constant.arrayListLiveWallpapers.addAll(arrayList);
                Constant.arrayListLiveWallpapers.removeAll(Collections.singleton(null));

                int real_pos = Constant.arrayListLiveWallpapers.indexOf(arrayList.get(position));

                Intent intent = new Intent(SearchLiveWallpaperActivity.this, LiveWallpapersDetailsActivity.class);
                intent.putExtra("pos", real_pos);
                startActivity(intent);
            }
        };

        dbHelper = new DBHelper(this);
        methods = new Methods(this, interAdListener);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        LinearLayout ll_ad = findViewById(R.id.ll_ad_search);
        dbHelper.getAbout();
        methods.showBannerAd(ll_ad);

        toolbar = this.findViewById(R.id.toolbar_wall_by_cat);
        toolbar.setTitle(getString(R.string.search));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayList = new ArrayList<>();

        progressBar = findViewById(R.id.pb_wallcat);
        textView_empty = findViewById(R.id.tv_empty_wallcat);

        fab = findViewById(R.id.fab);
        rv_wallpapers = findViewById(R.id.rv_wall_by_cat);
        rv_wallpapers.setHasFixedSize(true);
        rv_wallpapers.setLayoutManager(grid);

        rv_wallpapers.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getWallpapers();
                        }
                    }, 0);
                }
            }
        });

        rv_wallpapers.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                int firstVisibleItem = grid.findFirstVisibleItemPosition();
//
//                if (firstVisibleItem > 6) {
//                    fab.show();
//                } else {
//                    fab.hide();
//                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv_wallpapers.smoothScrollToPosition(0);
            }
        });

        getWallpapers();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        menu.findItem(R.id.menu_filter).setVisible(false);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            page=1;
            isOver = false;
            Constant.search_item = s;
            getWallpapers();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void getWallpapers() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (methods.isNetworkAvailable()) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);
            Call<ItemWallpaperList> call = APIClient.getClient().create(APIInterface.class).getLiveWallBySearch(methods.getAPIRequest(Constant.URL_LIVE_WALL_SEARCH, 0, "", "", "", Constant.search_item, "", "", "", "", "", "", new SharedPref(SearchLiveWallpaperActivity.this).getUserId(), ""), String.valueOf(page));
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    if (response.body() != null && response.body().getArrayListWallpaper() != null) {
                        if (response.body().getArrayListWallpaper().size() == 0) {
                            isOver = true;
                            setEmpty();
                        } else {
                            totalRecord = totalRecord + response.body().getArrayListWallpaper().size();
                            for (int i = 0; i < response.body().getArrayListWallpaper().size(); i++) {
                                dbHelper.addWallpaper(response.body().getArrayListWallpaper().get(i), "search", "");

                                arrayList.add(response.body().getArrayListWallpaper().get(i));

                                if (Constant.isNativeAd) {
                                    int abc = arrayList.lastIndexOf(null);
                                    if (((arrayList.size() - (abc + 1)) % Constant.nativeAdShow == 0) && (response.body().getArrayListWallpaper().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
                                        arrayList.add(null);
                                    }
                                }
                            }

                            page = page + 1;
                            setAdapter();
                        }

                    } else {
                        isOver = true;
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                }

                @Override
                public void onFailure(@NonNull Call<ItemWallpaperList> call, @NonNull Throwable t) {
                    call.cancel();
                    setEmpty();
                    isOver = true;
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                }
            });
        } else {
            setAdapter();
            isLoading = false;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterLiveWallpaper(SearchLiveWallpaperActivity.this, arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, "");
                }
            });
            AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapter);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.5f));
            rv_wallpapers.setAdapter(adapterAnim);
            setEmpty();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void setEmpty() {
        progressBar.setVisibility(View.INVISIBLE);
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
            rv_wallpapers.setVisibility(View.GONE);
        } else {
            rv_wallpapers.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroyNativeAds();
        }
        super.onDestroy();
    }
}