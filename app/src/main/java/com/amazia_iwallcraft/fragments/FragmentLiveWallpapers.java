package com.amazia_iwallcraft.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.amazia_iwallcraft.adapter.AdapterLiveWallpaper;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemWallpaperList;
import com.amazia_iwallcraft.wallpaper.LiveWallpapersDetailsActivity;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.wallpaper.SearchLiveWallpaperActivity;
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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentLiveWallpapers extends Fragment {

    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private AdapterLiveWallpaper adapter;
    private ArrayList<ItemWallpaper> arrayList;
    private CircularProgressBar progressBar;
    private Methods methods;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    private TextView textView_empty;
    private int page = 1, totalRecord = 0;
    private StaggeredGridLayoutManager grid;
    private String wallType = "", wallTempType = "";
    private FloatingActionButton fab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wall_by_cat, container, false);

        grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        grid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        InterAdListener interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {

                Constant.arrayListLiveWallpapers.clear();
                Constant.arrayListLiveWallpapers.addAll(arrayList);
                Constant.arrayListLiveWallpapers.removeAll(Collections.singleton(null));

                int real_pos = Constant.arrayListLiveWallpapers.indexOf(arrayList.get(position));

                Intent intent = new Intent(getActivity(), LiveWallpapersDetailsActivity.class);
                intent.putExtra("pos", real_pos);
                startActivity(intent);
            }
        };

        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), interAdListener);

        dbHelper.getAbout();

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_wallcat);
        textView_empty = rootView.findViewById(R.id.tv_empty_wallcat);

        fab = rootView.findViewById(R.id.fab);
        recyclerView = rootView.findViewById(R.id.rv_wall_by_cat);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(grid);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver && !isLoading && wallType.equals("")) {
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int[] firstVisibleItem = grid.findFirstVisibleItemPositions(null);

                if (firstVisibleItem[0] > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        getWallpaperData();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_search, menu);
                MenuItem item = menu.findItem(R.id.menu_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
                SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
                searchView.setOnQueryTextListener(queryTextListener);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_filter) {
                    openFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        super.onViewCreated(view, savedInstanceState);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s;
            Intent intent = new Intent(getActivity(), SearchLiveWallpaperActivity.class);
            startActivity(intent);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void getWallpaperData() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);

            Call<ItemWallpaperList> call;
            if(wallType.equals(getString(R.string.popular))) {
                call = APIClient.getClient().create(APIInterface.class).getLiveWallByPopular(methods.getAPIRequest(Constant.URL_LIVE_WALL_POPULAR, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""), String.valueOf(page));
            } else if(wallType.equals(getString(R.string.download))) {
                call = APIClient.getClient().create(APIInterface.class).getLiveWallByDownloads(methods.getAPIRequest(Constant.URL_LIVE_WALL_DOWNLOADS, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""), String.valueOf(page));
            } else if(wallType.equals(getString(R.string.rated))) {
                call = APIClient.getClient().create(APIInterface.class).getLiveWallByRated(methods.getAPIRequest(Constant.URL_LIVE_WALL_RATED, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""), String.valueOf(page));
            } else {
                isLoading = true;
                call = APIClient.getClient().create(APIInterface.class).getLiveWallByLatest(methods.getAPIRequest(Constant.URL_LIVE_WALL_LATEST, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""), String.valueOf(page));
            }
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null && response.body().getArrayListWallpaper() != null) {
                            if (response.body().getArrayListWallpaper().size() == 0) {
                                isOver = true;
                                setEmpty();
                            } else {
                                totalRecord = totalRecord + response.body().getArrayListWallpaper().size();

                                for (int i = 0; i < response.body().getArrayListWallpaper().size(); i++) {
                                    dbHelper.addWallpaper(response.body().getArrayListWallpaper().get(i), "live_wall", "");

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
            arrayList.addAll(dbHelper.getLiveWallpapers("1"));
            setAdapter();
            isOver = true;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterLiveWallpaper(getActivity(), arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, "");
                }
            });

            AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapter);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
            recyclerView.setAdapter(adapterAnim);
        } else {
            adapter.notifyDataSetChanged();
        }
        setEmpty();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (arrayList.size() == 0) {
            textView_empty.setText(getString(R.string.no_data_found));
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }

    private void openFilterDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_filter_live_wallpaper, null);

        BottomSheetDialog dialog_filter = new BottomSheetDialog(getActivity());
        dialog_filter.setContentView(view);
        dialog_filter.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_filter.show();

        final TextView button_popular = dialog_filter.findViewById(R.id.tv_filter_popular);
        final TextView button_most_download = dialog_filter.findViewById(R.id.tv_filter_most_download);
        final TextView button_rated = dialog_filter.findViewById(R.id.tv_filter_rated);
        final MaterialButton button_filter = dialog_filter.findViewById(R.id.button_filter);
        final MaterialButton button_clear = dialog_filter.findViewById(R.id.button_filter_clear);

        wallTempType = wallType;

        if(wallTempType.equals(getString(R.string.popular))) {
            button_popular.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if(wallTempType.equals(getString(R.string.download))) {
            button_most_download.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if(wallTempType.equals(getString(R.string.rated))) {
            button_rated.setBackgroundResource(R.drawable.bg_gradient_round);
        }

        button_popular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wallTempType.equals(getString(R.string.popular))) {
                    wallTempType = "";
                    button_popular.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.popular);
                    button_popular.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_most_download.setBackgroundResource(R.drawable.bg_button_filter);
                    button_rated.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_most_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wallTempType.equals(getString(R.string.download))) {
                    wallTempType = "";
                    button_most_download.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.download);
                    button_most_download.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_popular.setBackgroundResource(R.drawable.bg_button_filter);
                    button_rated.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_rated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wallTempType.equals(getString(R.string.rated))) {
                    wallTempType = "";
                    button_rated.setBackgroundResource(R.drawable.bg_button_filter);
                } else {
                    wallTempType = getString(R.string.rated);
                    button_rated.setBackgroundResource(R.drawable.bg_gradient_round);
                    button_popular.setBackgroundResource(R.drawable.bg_button_filter);
                    button_most_download.setBackgroundResource(R.drawable.bg_button_filter);
                }
            }
        });

        button_clear.setOnClickListener(v -> {
            button_popular.setBackgroundResource(R.drawable.bg_button_filter);
            button_most_download.setBackgroundResource(R.drawable.bg_button_filter);
            button_rated.setBackgroundResource(R.drawable.bg_button_filter);
            wallTempType = "";
            wallType = "";
        });

        button_filter.setOnClickListener(v -> {
            wallType = wallTempType;

            page = 1;
            totalRecord = 0;
            isOver = false;
            arrayList.clear();
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
            getWallpaperData();
            dialog_filter.dismiss();
        });
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroyNativeAds();
        }
        super.onDestroy();
    }
}