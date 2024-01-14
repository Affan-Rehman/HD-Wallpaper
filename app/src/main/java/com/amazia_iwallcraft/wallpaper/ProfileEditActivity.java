package com.amazia_iwallcraft.wallpaper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.amazia_iwallcraft.apiservices.APIClient;
import com.amazia_iwallcraft.apiservices.APIInterface;
import com.amazia_iwallcraft.apiservices.ItemUserList;
import com.amazia_iwallcraft.utils.Constant;
import com.amazia_iwallcraft.utils.Methods;
import com.amazia_iwallcraft.utils.SharedPref;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    ImageView iv_profile;
    EditText editText_name, editText_email, editText_phone, editText_pass, editText_cpass;
    String imagePath = "";
    ProgressDialog progressDialog;
    int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        toolbar = findViewById(R.id.toolbar_proedit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppCompatButton button_update = findViewById(R.id.button_prof_update);
        iv_profile = findViewById(R.id.iv_profile);
        editText_name = findViewById(R.id.editText_profedit_name);
        editText_email = findViewById(R.id.editText_profedit_email);
        editText_phone = findViewById(R.id.editText_profedit_phone);
        editText_pass = findViewById(R.id.editText_profedit_password);
        editText_cpass = findViewById(R.id.editText_profedit_cpassword);

        if(sharedPref.getLoginType().equals(Constant.LOGIN_TYPE_NORMAL)) {
            editText_cpass.setEnabled(true);
            editText_pass.setEnabled(true);
        } else {
            if(!sharedPref.getEmail().equals("")) {
                editText_email.setEnabled(false);
            }
            editText_cpass.setEnabled(false);
            editText_pass.setEnabled(false);
        }

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        setProfileVar();

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    loadUpdateProfile();
                }
            }
        });

        iv_profile.setOnClickListener(v -> {
            if (methods.checkPer()) {
                pickImage();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private Boolean validate() {
        editText_name.setError(null);
        editText_email.setError(null);
        editText_cpass.setError(null);
        if (editText_name.getText().toString().trim().isEmpty()) {
            editText_name.setError(getString(R.string.cannot_empty));
            editText_name.requestFocus();
            return false;
        } else if (editText_email.getText().toString().trim().isEmpty()) {
            editText_email.setError(getString(R.string.email_empty));
            editText_email.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().endsWith(" ")) {
            editText_pass.setError(getString(R.string.pass_end_space));
            editText_pass.requestFocus();
            return false;
        } else if (!editText_pass.getText().toString().trim().equals(editText_cpass.getText().toString().trim())) {
            editText_cpass.setError(getString(R.string.pass_nomatch));
            editText_cpass.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void updateArray(String image) {
        sharedPref.setUserName(editText_name.getText().toString());
        sharedPref.setEmail(editText_email.getText().toString());
        sharedPref.setUserMobile(editText_phone.getText().toString());
        sharedPref.setUserImage(image);

        if (!editText_pass.getText().toString().equals("")) {
            sharedPref.setRemeber(false);
        }
    }

    private void loadUpdateProfile() {
        if (methods.isNetworkAvailable()) {

            progressDialog.show();

            File file = null;


            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("data", methods.getAPIRequest(Constant.URL_PROFILE_UPDATE,0,"","","","","","",editText_name.getText().toString(),editText_email.getText().toString(),editText_pass.getText().toString(),editText_phone.getText().toString(), sharedPref.getUserId(), ""));
            if (imagePath != null && !imagePath.equals("")) {
                file = new File(imagePath);
                builder.addFormDataPart("user_image",  file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }

            RequestBody requestBody = builder.build();
            Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getProfileUpdate(requestBody);
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        if (response.body().getArrayListUser().get(0).getSuccess().equals("1")) {
                            updateArray(response.body().getArrayListUser().get(0).getImage());
                            imagePath = "";
                            Constant.isUpdate = true;
                            finish();
                            Toast.makeText(ProfileEditActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            if (response.body().getArrayListUser().get(0).getMessage().contains("Email address already used")) {
                                editText_email.setError(response.body().getArrayListUser().get(0).getMessage());
                                editText_email.requestFocus();
                            }
                        }
                    } else {
                        Toast.makeText(ProfileEditActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    Toast.makeText(ProfileEditActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileEditActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    public void setProfileVar() {
        editText_name.setText(sharedPref.getUserName());
        editText_phone.setText(sharedPref.getUserMobile());
        editText_email.setText(sharedPref.getEmail());

        if(!sharedPref.getUserImage().equals("")) {
            Picasso.get()
                    .load(sharedPref.getUserImage())
                    .into(iv_profile);
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = methods.getPathImage(uri);
            iv_profile.setImageURI(uri);
        }
    }
}
