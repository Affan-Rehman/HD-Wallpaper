package com.amazia_iwallcraft.wallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.amazia_iwallcraft.adapter.AdapterColors;
import com.amazia_iwallcraft.adapter.AdapterSubCategories;
import com.amazia_iwallcraft.adapter.AdapterWallpaper;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemSubCatList;
import com.amazia_iwallcraft.apiservices.ItemWallpaperList;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.interfaces.RecyclerViewClickListener;
import com.amazia_iwallcraft.items.ItemSubCat;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.EndlessRecyclerViewScrollListener;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.RecyclerItemClickListener;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WallpaperByCatActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Methods methods;
    private RecyclerView rv_wallpapers, rv_sub_cat;
    private AdapterSubCategories adapterSubCategories;
    private AdapterWallpaper adapterWallpaper;
    private ArrayList<ItemSubCat> arrayListSubCat;
    private ArrayList<ItemWallpaper> arrayListWallpapers;
    private CircularProgressBar progressBar;
    private TextView textView_empty;
    private SearchView searchView; 
    private int page = 1, totalRecord = 0;
    private StaggeredGridLayoutManager grid;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    private String wallType = "", wallTempType = "", color_ids = "", subCatId = "", databaseTable = "",
            databaseID = "", from = "", catID = "", catName = "";
    APIInterface apiInterface;
    AdapterColors adapterColors = null;
    Call<ItemWallpaperList> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_by_cat);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        InterAdListener interAdListener = new InterAdListener() {
            @Override
            public void onClick(int pos, String type) {
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayListWallpapers);
                Constant.arrayList.removeAll(Collections.singleton(null));

                int real_pos = Constant.arrayList.indexOf(arrayListWallpapers.get(pos));

                Intent intent = new Intent(WallpaperByCatActivity.this, WallPaperDetailsActivity.class);
                if (subCatId.equals("")) {
                    intent.putExtra("list_type", getString(R.string.categories));
                    intent.putExtra("cid", catID);
                } else {
                    intent.putExtra("list_type", getString(R.string.sub_categories));
                    intent.putExtra("cid", subCatId);
                }
                intent.putExtra("pos", real_pos);
                intent.putExtra("page", page);
                intent.putExtra("wallType", wallType);
                intent.putExtra("color_ids", color_ids);

                startActivity(intent);
            }
        };

        catID = getIntent().getStringExtra("cid");
        catName = getIntent().getStringExtra("cname");
        from = getIntent().getStringExtra("from");
        
        Toolbar toolbar = findViewById(R.id.toolbar_wall_by_cat);
        toolbar.setTitle(catName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DBHelper(WallpaperByCatActivity.this);
        methods = new Methods(WallpaperByCatActivity.this, interAdListener);

        arrayListSubCat = new ArrayList<>();
        arrayListWallpapers = new ArrayList<>();

        progressBar = findViewById(R.id.pb_cat);
        textView_empty = findViewById(R.id.tv_empty_cat);
        rv_wallpapers = findViewById(R.id.rv_cat);
        grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rv_wallpapers.setLayoutManager(grid);

        rv_sub_cat = findViewById(R.id.rv_sub_cat);
        rv_sub_cat.setLayoutManager(new LinearLayoutManager(WallpaperByCatActivity.this, LinearLayoutManager.HORIZONTAL, false));

        rv_sub_cat.addOnItemTouchListener(new RecyclerItemClickListener(WallpaperByCatActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    call.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subCatId = adapterSubCategories.setSelected(position);
                arrayListWallpapers.clear();
                if (adapterWallpaper != null) {
                    adapterWallpaper.notifyDataSetChanged();
                }
                page = 1;
                isOver = false;
                getWallpaperData();
            }
        }));

        rv_wallpapers.addOnItemTouchListener(new RecyclerItemClickListener(WallpaperByCatActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInter(position, "");
            }
        }));

        rv_wallpapers.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver && !isLoading) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getWallpaperData();
                        }
                    }, 0);
                }
            }
        });

        getSubCategories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }if (menuItem.getItemId() == R.id.menu_filter) {
            openFilterDialog();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s;
            Intent intent = new Intent(WallpaperByCatActivity.this, SearchWallActivity.class);
            startActivity(intent);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void getSubCategories() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);

            Call<ItemSubCatList> call = apiInterface.getSubCategories(methods.getAPIRequest(Constant.URL_SUB_CATEGORIES, 0, "", "", catID, "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<ItemSubCatList>() {
                @Override
                public void onResponse(@NonNull Call<ItemSubCatList> call, @NonNull Response<ItemSubCatList> response) {
                    if (response.body() != null && response.body().getArrayListSubCat() != null) {
                        arrayListSubCat.addAll(response.body().getArrayListSubCat());
                        setAdapterSubCat();
                        for (int i = 0; i < response.body().getArrayListSubCat().size(); i++) {
                            dbHelper.addToSubCatList(response.body().getArrayListSubCat().get(i), catID);
                        }
                    } else {
                        setEmptySubCat();
                    }
                    getWallpaperData();
                }

                @Override
                public void onFailure(@NonNull Call<ItemSubCatList> call, @NonNull Throwable t) {
                    call.cancel();
                    setEmptySubCat();
                    getWallpaperData();
                }
            });
        } else {
            arrayListSubCat = dbHelper.getSubCat(catID);
            setAdapterSubCat();

            getWallpaperData();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getWallpaperData() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);
            isLoading = true;

            if (subCatId.equals("")) {
                databaseTable = "cat";
                databaseID = catID;
                call = APIClient.getClient().create(APIInterface.class).getWallpapersByCat(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_CAT, page, color_ids, wallType, catID, "", "", "", "", "", "", "", new SharedPref(WallpaperByCatActivity.this).getUserId(), ""), String.valueOf(page));
            } else {
                databaseTable = "subcat";
                databaseID = subCatId;
                call = APIClient.getClient().create(APIInterface.class).getWallpapersBySubCat(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_SUB_CAT, page, color_ids, wallType, subCatId, "", "", "", "", "", "", "", new SharedPref(WallpaperByCatActivity.this).getUserId(), ""), String.valueOf(page));
            }
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
                                dbHelper.addWallpaper(response.body().getArrayListWallpaper().get(i), databaseTable, databaseID);

                                arrayListWallpapers.add(response.body().getArrayListWallpaper().get(i));

                                if (Constant.isNativeAd) {
                                    int abc = arrayListWallpapers.lastIndexOf(null);
                                    if (((arrayListWallpapers.size() - (abc + 1)) % Constant.nativeAdShow == 0) && (response.body().getArrayListWallpaper().size() - 1 != i || totalRecord != response.body().getTotalRecords())) {
                                        arrayListWallpapers.add(null);
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
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            if (subCatId.equals("")) {
                arrayListWallpapers = dbHelper.getWallByCat(catID, wallType);
            } else {
                arrayListWallpapers = dbHelper.getWallBySubCat(subCatId, wallType);
            }
            setAdapter();
            isOver = true;
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setAdapter() {
        if (!isScroll) {
            adapterWallpaper = new AdapterWallpaper(WallpaperByCatActivity.this, arrayListWallpapers, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, "");
                }
            });
            AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapterWallpaper);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.5f));
            rv_wallpapers.setAdapter(adapterAnim);
        } else {
            adapterWallpaper.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (arrayListWallpapers.size() == 0) {
            textView_empty.setText(getString(R.string.no_data_found));
            textView_empty.setVisibility(View.VISIBLE);
            rv_wallpapers.setVisibility(View.GONE);
        } else {
            rv_wallpapers.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }

    public void setAdapterSubCat() {
        adapterSubCategories = new AdapterSubCategories(WallpaperByCatActivity.this, arrayListSubCat);
        rv_sub_cat.setAdapter(adapterSubCategories);
        setEmptySubCat();
    }

    private void setEmptySubCat() {
        if (arrayListSubCat.size() == 0) {
            rv_sub_cat.setVisibility(View.GONE);
        } else {
            rv_sub_cat.setVisibility(View.VISIBLE);
        }
    }

    private void openFilterDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_filter_wallpaper, null);

        BottomSheetDialog dialog_filter = new BottomSheetDialog(WallpaperByCatActivity.this);
        dialog_filter.setContentView(view);
        dialog_filter.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_filter.show();

        final RecyclerView rv_colors = dialog_filter.findViewById(R.id.rv_filter_colors);
        final TextView button_portrait = dialog_filter.findViewById(R.id.tv_filter_portrait);
        final TextView button_landscape = dialog_filter.findViewById(R.id.tv_filter_landscape);
        final TextView button_square = dialog_filter.findViewById(R.id.tv_filter_square);
        final MaterialButton button_filter = dialog_filter.findViewById(R.id.button_filter);
        final MaterialButton button_clear = dialog_filter.findViewById(R.id.button_filter_clear);


        if (Constant.isColorOn && Constant.arrayListColors.size() > 0) {
            LinearLayoutManager llm = new LinearLayoutManager(WallpaperByCatActivity.this, LinearLayoutManager.HORIZONTAL, false);
            rv_colors.setLayoutManager(llm);

            adapterColors = new AdapterColors(WallpaperByCatActivity.this, Constant.arrayListColors);
            adapterColors.setMultipleSelected(color_ids);
            rv_colors.setAdapter(adapterColors);

            rv_colors.addOnItemTouchListener(new RecyclerItemClickListener(WallpaperByCatActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    adapterColors.setSelected(position);
                }
            }));

        } else {
            dialog_filter.findViewById(R.id.tv3).setVisibility(View.GONE);
        }
        wallTempType = wallType;

        if (wallTempType.equals(getString(R.string.portrait))) {
            button_portrait.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if (wallTempType.equals(getString(R.string.landscape))) {
            button_landscape.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if (wallTempType.equals(getString(R.string.square))) {
            button_square.setBackgroundResource(R.drawable.bg_gradient_round);
        }

        button_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wallTempType.equals(getString(R.string.portrait))) {
                    wallTempType = "";
                    button_portrait.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.portrait);
                    button_portrait.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_landscape.setBackgroundResource(R.drawable.bg_button_filter);
                    button_square.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_landscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wallTempType.equals(getString(R.string.landscape))) {
                    wallTempType = "";
                    button_landscape.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.landscape);
                    button_landscape.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_portrait.setBackgroundResource(R.drawable.bg_button_filter);
                    button_square.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wallTempType.equals(getString(R.string.square))) {
                    wallTempType = "";
                    button_square.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.square);
                    button_square.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_portrait.setBackgroundResource(R.drawable.bg_button_filter);
                    button_landscape.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_clear.setOnClickListener(v -> {
            button_portrait.setBackgroundResource(R.drawable.bg_button_filter);
            button_landscape.setBackgroundResource(R.drawable.bg_button_filter);
            button_square.setBackgroundResource(R.drawable.bg_button_filter);
            wallTempType = "";
            wallType = "";
            color_ids = "";
            if (adapterColors != null) {
                adapterColors.clearSelected();
            }
        });

        button_filter.setOnClickListener(v -> {
            if (adapterColors != null) {
                color_ids = adapterColors.getSelected();
            }
            wallType = wallTempType;

            page = 1;
            totalRecord = 0;
            isOver = false;
            arrayListWallpapers.clear();
            if (adapterWallpaper != null) {
                adapterWallpaper.notifyDataSetChanged();
            }
            getWallpaperData();
            dialog_filter.dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        if (from.equals("noti")) {
            Intent intent = new Intent(WallpaperByCatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        if (adapterWallpaper != null) {
            adapterWallpaper.destroyNativeAds();
        }
        super.onDestroy();
    }
}