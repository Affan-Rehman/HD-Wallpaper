package com.amazia_iwallcraft.wallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.amazia_iwallcraft.adapter.AdapterWallpaperWelcome;
import com.amazia_iwallcraft.interfaces.WallpaperRetrieveListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.MarqueeRecyclerView;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;

public class WelcomeActivity extends AppCompatActivity {

    SharedPref sharedPref;
    Methods methods;
    MaterialButton btn_signup, btn_login, button_skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        methods = new Methods(this);
        sharedPref = new SharedPref(this);

        button_skip = findViewById(R.id.button_skip);
        btn_signup = findViewById(R.id.btn_welcome_signup);
        btn_login = findViewById(R.id.btn_welcome_login);

        btn_login.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("from", "");
            startActivity(intent);
        });

        btn_signup.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        button_skip.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        methods.getWallpapers(new WallpaperRetrieveListener() {
            @Override
            public void onSuccess(ArrayList<ItemWallpaper> arrayListWallpaper) {
                MarqueeRecyclerView rv_latest = findViewById(R.id.rv_welcome);
                CircularProgressBar progressBar = findViewById(R.id.pb_welcome);

                rv_latest.suppressLayout(true);
                rv_latest.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                rv_latest.setScrollSpeed(1, 10);
                rv_latest.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        return true;
                    }
                });

                AdapterWallpaperWelcome adapterWallpaper = new AdapterWallpaperWelcome(WelcomeActivity.this, arrayListWallpaper);

                AnimationAdapter adapterAnim = new AlphaInAnimationAdapter(adapterWallpaper);
                adapterAnim.setFirstOnly(true);
                adapterAnim.setDuration(500);
                adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
                rv_latest.setAdapter(adapterAnim);

                progressBar.setVisibility(View.INVISIBLE);
                if (arrayListWallpaper.size() == 0) {
                    rv_latest.setVisibility(View.GONE);
                } else {
                    rv_latest.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}