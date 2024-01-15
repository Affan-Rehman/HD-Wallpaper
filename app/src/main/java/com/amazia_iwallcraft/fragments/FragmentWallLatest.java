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
import android.widget.Toast;

import com.amazia_iwallcraft.adapter.AdapterCCategories;
import com.amazia_iwallcraft.adapter.AdapterCategories;
import com.amazia_iwallcraft.adapter.AdapterSubCategories;
import com.amazia_iwallcraft.apiservices.ItemCatList;
import com.amazia_iwallcraft.items.ItemCat;
import com.amazia_iwallcraft.wallpaper.MainActivity;
import com.amazia_iwallcraft.wallpaper.WallpaperByCatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.amazia_iwallcraft.utils.EndlessRecyclerViewScrollListener;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.RecyclerItemClickListener;
import com.amazia_iwallcraft.utils.SharedPref;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentWallLatest extends Fragment {

    private DBHelper dbHelper;
    APIInterface apiInterface;
    private AdapterCCategories adapterCCategories;
    private ArrayList<ItemCat> arrayCList;
    Call<ItemWallpaperList> call;
    private RecyclerView recyclerView,rv_sub_cat;
    private AdapterWallpaper adapter;
    private ArrayList<ItemWallpaper> arrayList;
    private CircularProgressBar progressBar;
    private Methods methods;
    private Boolean isOver = false, isScroll = false, isLoading = false;
    private TextView textView_empty;
    private int page = 1, totalRecord = 0;
    private GridLayoutManager grid;
    private String wallType = "", wallTempType = "", color_ids = "",subCatId = "", databaseTable = "",
    databaseID = "", from = "", catID = "", catName = "";
    private FloatingActionButton fab;
    private ArrayList<ItemWallpaper> arrayListWallpapers;
    private AdapterWallpaper adapterWallpaper;
    private AdapterSubCategories adapterSubCategories;
    AdapterColors adapterColors = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        apiInterface = APIClient.getClient().create(APIInterface.class);
        View rootView = inflater.inflate(R.layout.fragment_wall_by_cat, container, false);

        grid = new GridLayoutManager(getActivity(), 3);
        arrayCList = new ArrayList<>();


        dbHelper = new DBHelper(getActivity());

        dbHelper.getAbout();

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_wallcat);
        textView_empty = rootView.findViewById(R.id.tv_empty_wallcat);
        rv_sub_cat = rootView.findViewById(R.id.rv_sub_cat);
        rv_sub_cat.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_sub_cat.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> methods.showInter(position, "")));


        InterAdListener interAdListener = (pos, type) -> {
            int position = getPosition(adapterCCategories.getID(pos));

            FragmentSubCategories frag = new FragmentSubCategories();
            Bundle bundle = new Bundle();
            bundle.putString("cid", arrayCList.get(position).getId());
            bundle.putString("from", "");
            frag.setArguments(bundle);
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.add(R.id.frame_layout, frag, arrayCList.get(position).getName());

            ft.commitAllowingStateLoss();
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(arrayCList.get(position).getName());
        };

        methods = new Methods(getContext(), interAdListener);
        getCategories();
        fab = rootView.findViewById(R.id.fab);
        recyclerView = rootView.findViewById(R.id.rv_wall_by_cat);
        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(grid);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = grid.findFirstVisibleItemPosition();

                if (firstVisibleItem > 2) {
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

    private final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
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
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            Call<ItemWallpaperList> call = APIClient.getClient().create(APIInterface.class).getWallpapersByLatest(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_LATEST, page, color_ids, wallType, "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), ""), String.valueOf(page));
            call.enqueue(new Callback<ItemWallpaperList>() {
                @Override
                public void onResponse(@NonNull Call<ItemWallpaperList> call, @NonNull Response<ItemWallpaperList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null && response.body().getArrayListWallpaper() != null) {
                            if (response.body().getArrayListWallpaper().size() == 0) {
                                isOver = true;
                                setCEmpty();
                            } else {
                                totalRecord = totalRecord + response.body().getArrayListWallpaper().size();
                                for (int i = 0; i < response.body().getArrayListWallpaper().size(); i++) {
                                    dbHelper.addWallpaper(response.body().getArrayListWallpaper().get(i), "latest", "");

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
            arrayList = dbHelper.getWallpapers("id", wallType);
            setAdapter();
            isOver = true;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setCAdapter() {
        adapterCCategories = new AdapterCCategories(getActivity(), arrayCList);
        AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapterCCategories);
        adapterAnim.setFirstOnly(true);
        adapterAnim.setDuration(500);
        adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
        rv_sub_cat.setAdapter(adapterAnim);
        if(arrayCList.size()!=0){
            rv_sub_cat.setVisibility(View.VISIBLE);
        }
        setCEmpty();
    }

    private void setCEmpty() {
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }
    public void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterWallpaper(getActivity(), arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {

                    Constant.arrayList.clear();
                    Constant.arrayList.addAll(arrayList);
                    Constant.arrayList.removeAll(Collections.singleton(null));

                    int real_pos = Constant.arrayList.indexOf(arrayList.get(position));

                    Intent intent = new Intent(getActivity(), WallPaperDetailsActivity.class);
                    intent.putExtra("pos", real_pos);
                    intent.putExtra("list_type", getString(R.string.latest));
                    intent.putExtra("page", page);
                    intent.putExtra("wallType", wallType);
                    intent.putExtra("color_ids", color_ids);

                    startActivity(intent);

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

        if(wallTempType.equals(getString(R.string.portrait))) {
            button_portrait.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if(wallTempType.equals(getString(R.string.landscape))) {
            button_landscape.setBackgroundResource(R.drawable.bg_gradient_round);
        } else if(wallTempType.equals(getString(R.string.square))) {
            button_square.setBackgroundResource(R.drawable.bg_gradient_round);
        }

        button_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wallTempType.equals(getString(R.string.portrait))) {
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
                if(wallTempType.equals(getString(R.string.landscape))) {
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
                if(wallTempType.equals(getString(R.string.square))) {
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
            if(adapterColors != null) {
                adapterColors.clearSelected();
            }
        });

        button_filter.setOnClickListener(v -> {
            if(adapterColors != null) {
                color_ids = adapterColors.getSelected();
            }
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
    private void getCategories() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);

            Call<ItemCatList> call = apiInterface.getCategories(methods.getAPIRequest(Constant.URL_CATEGORIES, 0, "", "", "", "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<ItemCatList>() {
                @Override
                public void onResponse(@NonNull Call<ItemCatList> call, @NonNull Response<ItemCatList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null && response.body().getArrayListCat() != null) {
                            arrayCList.addAll(response.body().getArrayListCat());
                            setCAdapter();
                            for (int i = 0; i < response.body().getArrayListCat().size(); i++) {
                                dbHelper.addToCatList(response.body().getArrayListCat().get(i));
                            }
                        } else {
                            setCEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemCatList> call, @NonNull Throwable t) {
                    call.cancel();
                    setCEmpty();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            arrayCList = dbHelper.getCat();
            if (arrayList != null) {
                setCAdapter();
            }
            progressBar.setVisibility(View.GONE);
        }
    }
    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < arrayCList.size(); i++) {
            if (id.equals(arrayCList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }
}