package com.amazia_iwallcraft.wallpaper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazia_iwallcraft.adapter.AdapterWallpaperWelcome;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemUserList;
import com.amazia_iwallcraft.interfaces.WallpaperRetrieveListener;
import com.amazia_iwallcraft.items.ItemWallpaper;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.MarqueeRecyclerView;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    Methods methods;
    EditText editText_name, editText_email, editText_pass, editText_cpass, editText_phone;
    Button button_register;
    ProgressDialog progressDialog;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);

        button_register = findViewById(R.id.button_register);
        editText_name = findViewById(R.id.et_regis_name);
        editText_email = findViewById(R.id.et_regis_email);
        editText_pass = findViewById(R.id.et_regis_password);
        editText_cpass = findViewById(R.id.et_regis_cpassword);
        editText_phone = findViewById(R.id.et_regis_phone);

        button_register.setOnClickListener(view -> {
            if (validate()) {
                loadRegister();
            }
        });

        methods.getWallpapers(new WallpaperRetrieveListener() {
            @Override
            public void onSuccess(ArrayList<ItemWallpaper> arrayListWallpaper) {
                MarqueeRecyclerView rv_latest = findViewById(R.id.rv_welcome);
                CircularProgressBar progressBar = findViewById(R.id.pb_welcome);

                rv_latest.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                rv_latest.setScrollSpeed(1, 10);
                rv_latest.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        return true;
                    }
                });

                AdapterWallpaperWelcome adapterWallpaper = new AdapterWallpaperWelcome(RegisterActivity.this, arrayListWallpaper);

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

    private Boolean validate() {
        if (editText_name.getText().toString().trim().isEmpty()) {
            editText_name.setError(getResources().getString(R.string.enter_name));
            editText_name.requestFocus();
            return false;
        } else if (editText_email.getText().toString().trim().isEmpty()) {
            editText_email.setError(getResources().getString(R.string.enter_email));
            editText_email.requestFocus();
            return false;
        } else if (!isEmailValid(editText_email.getText().toString())) {
            editText_email.setError(getString(R.string.error_invalid_email));
            editText_email.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().isEmpty()) {
            editText_pass.setError(getResources().getString(R.string.enter_password));
            editText_pass.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().endsWith(" ")) {
            editText_pass.setError(getResources().getString(R.string.pass_end_space));
            editText_pass.requestFocus();
            return false;
        } else if (editText_cpass.getText().toString().isEmpty()) {
            editText_cpass.setError(getResources().getString(R.string.enter_cpassword));
            editText_cpass.requestFocus();
            return false;
        } else if (!editText_pass.getText().toString().equals(editText_cpass.getText().toString())) {
            editText_cpass.setError(getResources().getString(R.string.pass_nomatch));
            editText_cpass.requestFocus();
            return false;
        } else if (editText_phone.getText().toString().trim().isEmpty()) {
            editText_phone.setError(getResources().getString(R.string.enter_phone));
            editText_phone.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadRegister() {
        if (methods.isNetworkAvailable()) {
            progressDialog.show();

            Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getRegistration(methods.getAPIRequest(Constant.URL_REGISTRATION, 0, "", "", "", "", "", "", editText_name.getText().toString(), editText_email.getText().toString(), editText_pass.getText().toString(), editText_phone.getText().toString(), "", ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        switch (response.body().getArrayListUser().get(0).getSuccess()) {
                            case "1":
                                Toast.makeText(RegisterActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("from", "");
                                startActivity(intent);
                                finish();
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), response.body().getArrayListUser().get(0).getMessage());
                                break;
                            default:
                                if (response.body().getArrayListUser().get(0).getMessage().contains("already") || response.body().getArrayListUser().get(0).getMessage().contains("Invalid email format")) {
                                    editText_email.setError(response.body().getArrayListUser().get(0).getMessage());
                                    editText_email.requestFocus();
                                } else {
                                    Toast.makeText(RegisterActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    Toast.makeText(RegisterActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }
}