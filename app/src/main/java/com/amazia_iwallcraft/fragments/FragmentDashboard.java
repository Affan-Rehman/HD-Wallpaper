package com.amazia_iwallcraft.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.amazia_iwallcraft.wallpaper.LoginActivity;
import com.amazia_iwallcraft.wallpaper.MainActivity;
import com.amazia_iwallcraft.wallpaper.R;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentDashboard extends Fragment {

    public static BottomNavigationView bottomNavigationMenu;
    private FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        fm = getParentFragmentManager();

        bottomNavigationMenu = rootView.findViewById(R.id.navigation_bottom);
        bottomNavigationMenu.setOnItemSelectedListener(onItemSelectedListener);

        bottomNavigationMenu.getMenu().findItem(R.id.nav_bottom_live_wallpapers).setVisible(Constant.isLiveWallpaperEnabled);

        loadFrag(new FragmentWallLatest(), getString(R.string.home));

        return rootView;
    }

    NavigationBarView.OnItemSelectedListener onItemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_bottom_latest) {
                loadFrag(new FragmentWallLatest(), getString(R.string.home));
                return true;
            } else if (itemId == R.id.nav_bottom_popular) {
                loadFrag(new FragmentWallPopular(), getString(R.string.popular));
                return true;
            } else if (itemId == R.id.nav_bottom_live_wallpapers) {
                loadFrag(new FragmentLiveWallpapers(), getString(R.string.live_wallpapers));
                return true;
            } else if (itemId == R.id.nav_bottom_cat) {
                loadFrag(new FragmentCategories(), getString(R.string.categories));
                return true;
            } else if (itemId == R.id.nav_bottom_profile) {
                if(new SharedPref(getActivity()).isLogged()) {
                    loadFrag(new FragmentProfile(), getString(R.string.profile));
                    return true;
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.putExtra("from", "app");
                    startActivity(intent);
                    return false;
                }
            }
            return false;
        }
    };

    public void loadFrag(Fragment f1, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (name.equals(getString(R.string.search))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment_dash, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment_dash, f1, name);
        }
        ft.commitAllowingStateLoss();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
    }
}