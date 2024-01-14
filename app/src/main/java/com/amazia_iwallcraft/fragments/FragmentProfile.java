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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazia_iwallcraft.adapter.AdapterLiveWallpaper;
import com.amazia_iwallcraft.adapter.AdapterWallpaper;
import com.amazia_iwallcraft.adapter.SpinAdapter;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemFavList;
import com.amazia_iwallcraft.apiservices.ItemUserList;
import com.amazia_iwallcraft.wallpaper.LiveWallpapersDetailsActivity;
import com.amazia_iwallcraft.wallpaper.ProfileEditActivity;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.wallpaper.WallPaperDetailsActivity;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.interfaces.RecyclerViewClickListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
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

public class FragmentProfile extends Fragment {

    Methods methods;
    SharedPref sharedPref;
    RecyclerView recyclerView;
    AdapterWallpaper adapter;
    AdapterLiveWallpaper adapterLiveWallpaper;
    ArrayList<ItemWallpaper> arrayList;
    NestedScrollView nestedScrollView;
    Spinner sp_fav_type;
    ImageView iv_profile, iv_profile_edit;
    TextView tv_name, tv_email;
    CircularProgressBar progressBar;
    TextView tv_empty, tv_empty_list;
    int totalRecord = 0;
    StaggeredGridLayoutManager grid;
    String errorString = "";
    FloatingActionButton fab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        grid = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        grid.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        InterAdListener interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent;
                if (type.equals(getString(R.string.wallpapers))) {

                    Constant.arrayList.clear();
                    Constant.arrayList.addAll(arrayList);
                    Constant.arrayList.removeAll(Collections.singleton(null));

                    int real_pos = Constant.arrayList.indexOf(arrayList.get(position));

                    intent = new Intent(getActivity(), WallPaperDetailsActivity.class);
                    intent.putExtra("pos", real_pos);
                    intent.putExtra("list_type", getString(R.string.favourite));
                    intent.putExtra("page", 1);
                    intent.putExtra("wallType", "");
                    intent.putExtra("color_ids", "");
                } else {

                    Constant.arrayListLiveWallpapers.clear();
                    Constant.arrayListLiveWallpapers.addAll(arrayList);
                    Constant.arrayListLiveWallpapers.removeAll(Collections.singleton(null));

                    int real_pos = Constant.arrayListLiveWallpapers.indexOf(arrayList.get(position));

                    intent = new Intent(getActivity(), LiveWallpapersDetailsActivity.class);
                    intent.putExtra("pos", real_pos);
                }
                startActivity(intent);
            }
        };

        methods = new Methods(getActivity(), interAdListener);
        sharedPref = new SharedPref(getActivity());

        arrayList = new ArrayList<>();

        nestedScrollView = rootView.findViewById(R.id.nsv_profile);

        sp_fav_type = rootView.findViewById(R.id.sp_prof_fav);
        iv_profile = rootView.findViewById(R.id.iv_profile);
        iv_profile_edit = rootView.findViewById(R.id.iv_profile_edit);
        tv_name = rootView.findViewById(R.id.tv_profile_name);
        tv_email = rootView.findViewById(R.id.tv_profile_email);
        progressBar = rootView.findViewById(R.id.pb_wallcat);
        tv_empty = rootView.findViewById(R.id.tv_empty_wallcat);
        tv_empty_list = rootView.findViewById(R.id.tv_empty_list);

        ArrayList<String> arrayListType = new ArrayList<>();
        arrayListType.add(getString(R.string.wallpapers));
        arrayListType.add(getString(R.string.live_wallpapers));
        SpinAdapter adapterFavType = new SpinAdapter(getActivity(), android.R.layout.simple_spinner_item, arrayListType);
        sp_fav_type.setAdapter(adapterFavType);

        sp_fav_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                arrayList.clear();

                if (position == 0) {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (adapterLiveWallpaper != null) {
                        adapterLiveWallpaper.notifyDataSetChanged();
                    }
                }
                getWallpaperData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (sharedPref.isLogged() && !sharedPref.getUserId().equals("")) {
            loadUserProfile();

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

            iv_profile_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                    startActivity(intent);
                }
            });

        } else {
            progressBar.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.GONE);
            tv_empty.setText(getString(R.string.not_log));
            tv_empty.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        super.onViewCreated(view, savedInstanceState);
    }

    private void loadUserProfile() {
        if (methods.isNetworkAvailable()) {

            progressBar.setVisibility(View.VISIBLE);

            Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getProfile(methods.getAPIRequest(Constant.URL_PROFILE, 0, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        if (response.body().getArrayListUser().get(0).getSuccess().equals("1")) {

                            sharedPref.setUserName(response.body().getArrayListUser().get(0).getName());
                            sharedPref.setEmail(response.body().getArrayListUser().get(0).getEmail());
                            sharedPref.setUserMobile(response.body().getArrayListUser().get(0).getMobile());
                            sharedPref.setUserImage(response.body().getArrayListUser().get(0).getImage());

                            tv_name.setText(response.body().getArrayListUser().get(0).getName());
                            tv_email.setText(response.body().getArrayListUser().get(0).getEmail());
                            if (!response.body().getArrayListUser().get(0).getImage().equals("")) {
                                Picasso.get()
                                        .load(response.body().getArrayListUser().get(0).getImage())
                                        .placeholder(R.drawable.user)
                                        .into(iv_profile);
                            }
                        } else {
//                            setEmpty(false, getString(R.string.invalid_user));
                            methods.logout(getActivity(), sharedPref);
                        }
                    } else {
                        errorString = getString(R.string.server_error);
                        setEmpty();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    errorString = getString(R.string.server_error);
                    setEmpty();
                }
            });
        } else {
            errorString = getString(R.string.internet_not_connected);
            setEmpty();
        }
    }

    private void getWallpaperData() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);
            Call<ItemFavList> call;
            if (sp_fav_type.getSelectedItemPosition() == 0) {
                call = APIClient.getClient().create(APIInterface.class).getWallpapersByFav(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_FAV, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), "Wallpaper"));
            } else {
                call = APIClient.getClient().create(APIInterface.class).getWallpapersByFav(methods.getAPIRequest(Constant.URL_WALLPAPER_BY_FAV, 0, "", "", "", "", "", "", "", "", "", "", new SharedPref(getActivity()).getUserId(), "LiveWallpaper"));
            }

            call.enqueue(new Callback<ItemFavList>() {
                @Override
                public void onResponse(@NonNull Call<ItemFavList> call, @NonNull Response<ItemFavList> response) {
                    if (getActivity() != null) {
                        if (response.body() != null && response.body().getItemFavPost() != null) {
                            if (response.body().getItemFavPost().getArrayListWallpapers().size() == 0 &&
                                    response.body().getItemFavPost().getArrayListLiveWallpapers().size() == 0) {
                                setEmptyList();
                            } else {
                                if (sp_fav_type.getSelectedItemPosition() == 0) {
                                    totalRecord = response.body().getItemFavPost().getArrayListWallpapers().size();
                                    for (int i = 0; i < response.body().getItemFavPost().getArrayListWallpapers().size(); i++) {

                                        arrayList.add(response.body().getItemFavPost().getArrayListWallpapers().get(i));

                                        if (Constant.isNativeAd) {
                                            int abc = arrayList.lastIndexOf(null);
                                            if (((arrayList.size() - (abc + 1)) % Constant.nativeAdShow == 0) && (response.body().getItemFavPost().getArrayListWallpapers().size() - 1 != i || totalRecord != response.body().getItemFavPost().getArrayListWallpapers().size())) {
                                                arrayList.add(null);
                                            }
                                        }
                                    }
                                } else {
                                    totalRecord = response.body().getItemFavPost().getArrayListLiveWallpapers().size();
                                    for (int i = 0; i < response.body().getItemFavPost().getArrayListLiveWallpapers().size(); i++) {

                                        arrayList.add(response.body().getItemFavPost().getArrayListLiveWallpapers().get(i));

                                        if (Constant.isNativeAd) {
                                            int abc = arrayList.lastIndexOf(null);
                                            if (((arrayList.size() - (abc + 1)) % Constant.nativeAdShow == 0) && (response.body().getItemFavPost().getArrayListLiveWallpapers().size() - 1 != i || totalRecord != response.body().getItemFavPost().getArrayListLiveWallpapers().size())) {
                                                arrayList.add(null);
                                            }
                                        }
                                    }
                                }

                                setAdapter();
                            }
                        } else {
                            setEmptyList();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemFavList> call, @NonNull Throwable t) {
                    call.cancel();
                    setEmptyList();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            setAdapter();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        if (sp_fav_type.getSelectedItemPosition() == 0) {
            adapterLiveWallpaper = null;
            adapter = new AdapterWallpaper(getActivity(), arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, getString(R.string.wallpapers));
                }
            });
            AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapter);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
            recyclerView.setAdapter(adapterAnim);
        } else {
            adapter = null;
            adapterLiveWallpaper = new AdapterLiveWallpaper(getActivity(), arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, getString(R.string.live_wallpapers));
                }
            });
            AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapterLiveWallpaper);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
            recyclerView.setAdapter(adapterAnim);
        }
        setEmptyList();
    }

    private void setEmpty() {
        progressBar.setVisibility(View.GONE);
        if (arrayList.size() == 0) {
            tv_empty.setText(errorString);
            tv_empty.setVisibility(View.VISIBLE);
            nestedScrollView.setVisibility(View.GONE);
        } else {
            nestedScrollView.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        }
    }

    private void setEmptyList() {
        progressBar.setVisibility(View.GONE);
        if (arrayList.size() == 0) {
            tv_empty_list.setText(getString(R.string.no_data_found));
            tv_empty_list.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tv_empty_list.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        if (tv_name != null && Constant.isUpdate) {
            Constant.isUpdate = false;
            tv_name.setText(sharedPref.getUserName());
            tv_email.setText(sharedPref.getEmail());
            if (!sharedPref.getUserImage().equals("")) {
                Picasso.get()
                        .load(sharedPref.getUserImage())
                        .placeholder(R.drawable.user)
                        .into(iv_profile);
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroyNativeAds();
        }
        super.onDestroy();
    }
}