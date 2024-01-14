package com.amazia_iwallcraft.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.amazia_iwallcraft.adapter.AdapterCategories;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.wallpaper.MainActivity;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.interfaces.InterAdListener;
import com.amazia_iwallcraft.items.ItemCat;
import com.amazia_iwallcraft.apiservices.ItemCatList;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.DBHelper;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.RecyclerItemClickListener;
import com.amazia_iwallcraft.utils.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategories extends Fragment {

    private DBHelper dbHelper;
    private Methods methods;
    private RecyclerView recyclerView;
    private AdapterCategories adapterCategories;
    private ArrayList<ItemCat> arrayList;
    private CircularProgressBar progressBar;
    private TextView textView_empty;
    private SearchView searchView;
    private SharedPref sharedPref;
    APIInterface apiInterface;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        InterAdListener interAdListener = (pos, type) -> {
            int position = getPosition(adapterCategories.getID(pos));

            FragmentSubCategories frag = new FragmentSubCategories();
            Bundle bundle = new Bundle();
            bundle.putString("cid", arrayList.get(position).getId());
            bundle.putString("from", "");
            frag.setArguments(bundle);
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.hide(getParentFragmentManager().findFragmentByTag(getString(R.string.categories)));
            ft.add(R.id.frame_layout, frag, arrayList.get(position).getName());
            ft.addToBackStack(arrayList.get(position).getName());
            ft.commitAllowingStateLoss();
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(arrayList.get(position).getName());
        };

        sharedPref = new SharedPref(getActivity());
        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), interAdListener);

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_cat);
        textView_empty = rootView.findViewById(R.id.tv_empty_cat);
        recyclerView = rootView.findViewById(R.id.rv_cat);
        GridLayoutManager grid = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(grid);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> methods.showInter(position, "")));

        getCategories();
 
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_search, menu);
                menu.findItem(R.id.menu_filter).setVisible(false);
                MenuItem item = menu.findItem(R.id.menu_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
                searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
                searchView.setOnQueryTextListener(queryTextListener);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        super.onViewCreated(view, savedInstanceState);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (!searchView.isIconified() && adapterCategories != null) {
                adapterCategories.getFilter().filter(s);
                adapterCategories.notifyDataSetChanged();
            }
            return false;
        }
    };

    private void getCategories() {
        if (methods.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);

            Call<ItemCatList> call = apiInterface.getCategories(methods.getAPIRequest(Constant.URL_CATEGORIES, 0, "", "", "", "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<ItemCatList>() {
                @Override
                public void onResponse(@NonNull Call<ItemCatList> call, @NonNull Response<ItemCatList> response) {
                    if(getActivity() != null) {
                        if (response.body() != null && response.body().getArrayListCat() != null) {
                            arrayList.addAll(response.body().getArrayListCat());
                            setAdapter();
                            for (int i = 0; i < response.body().getArrayListCat().size(); i++) {
                                dbHelper.addToCatList(response.body().getArrayListCat().get(i));
                            }
                        } else {
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ItemCatList> call, @NonNull Throwable t) {
                    call.cancel();
                    setEmpty();
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            arrayList = dbHelper.getCat();
            if (arrayList != null) {
                setAdapter();
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setAdapter() {
        adapterCategories = new AdapterCategories(getActivity(), arrayList);
        AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapterCategories);
        adapterAnim.setFirstOnly(true);
        adapterAnim.setDuration(500);
        adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
        recyclerView.setAdapter(adapterAnim);
        setEmpty();
    }

    private void setEmpty() {
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            if (id.equals(arrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }
}