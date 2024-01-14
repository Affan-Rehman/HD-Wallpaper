package com.amazia_iwallcraft.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.amazia_iwallcraft.adapter.AdapterColors;
import com.amazia_iwallcraft.adapter.AdapterWallpaper;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemWallpaperList;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.wallpaper.SearchWallActivity;
import com.amazia_iwallcraft.wallpaper.WallPaperDetailsActivity;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.interfaces.RecyclerViewClickListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.RecyclerItemClickListener;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentWallRated extends Fragment {

    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private AdapterWallpaper adapter;
    private ArrayList<ItemWallpaper> arrayList;
    private CircularProgressBar progressBar;
    private Methods methods;
    private TextView textView_empty;
    private StaggeredGridLayoutManager grid;
    private String wallType = "", wallTempType = "", color_ids = "";
    private FloatingActionButton fab;
    AdapterColors adapterColors = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wall_by_cat, container, false);

        grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        grid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        InterAdListener interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayList);
                Constant.arrayList.removeAll(Collections.singleton(null));

                int real_pos = Constant.arrayList.indexOf(arrayList.get(position));

                Intent intent = new Intent(getActivity(), WallPaperDetailsActivity.class);
                intent.putExtra("pos", real_pos);
                intent.putExtra("list_type", getString(R.string.rated));
                intent.putExtra("page", 1);
                intent.putExtra("wallType", wallType);
                intent.putExtra("color_ids", color_ids);
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
                menuInflater.inflate(R.menu.menu_search_fragment, menu);
                MenuItem item = menu.findItem(R.id.menu_search_frag);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(queryTextListener);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_filter_frag) {
                    openFilterDialog();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        super.onViewCreated(view, savedInstanceState);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s;
            Intent intent = new Intent(getActivity(), SearchWallActivity.class);
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

            Call<ItemWallpaperList> call = APIClient.getClient().create(APIInterface.class).getWallpapersByRated(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_RATED, 0, color_ids, wallType, "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""));
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null && response.body().getArrayListWallpaper() != null) {
                            for (int i = 0; i < response.body().getArrayListWallpaper().size(); i++) {
                                dbHelper.addWallpaper(response.body().getArrayListWallpaper().get(i), "latest", "");
                                arrayList.add(response.body().getArrayListWallpaper().get(i));

                                if (Constant.isNativeAd) {
                                    int abc = arrayList.lastIndexOf(null);
                                    if (((arrayList.size() - (abc + 1)) % Constant.nativeAdShow == 0) && (response.body().getArrayListWallpaper().size() - 1 != i || response.body().getArrayListWallpaper().size() != response.body().getTotalRecords())) {
                                        arrayList.add(null);
                                    }
                                }
                            }
                            setAdapter();
                        } else {
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemWallpaperList> call, @NonNull Throwable t) {
                    call.cancel();
                    setEmpty();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            arrayList = dbHelper.getWallpapers("rate", wallType);
            setAdapter();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        adapter = new AdapterWallpaper(getActivity(), arrayList, new RecyclerViewClickListener() {
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
        View view = getLayoutInflater().inflate(R.layout.layout_filter_wallpaper, null);

        BottomSheetDialog dialog_filter = new BottomSheetDialog(getActivity());
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
            LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            rv_colors.setLayoutManager(llm);

            adapterColors = new AdapterColors(getActivity(), Constant.arrayListColors);
            adapterColors.setMultipleSelected(color_ids);
            rv_colors.setAdapter(adapterColors);

            rv_colors.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
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

            arrayList.clear();
            if (adapter != null) {
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