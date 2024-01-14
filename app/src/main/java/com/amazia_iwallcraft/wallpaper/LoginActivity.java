package com.amazia_iwallcraft.wallpaper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazia_iwallcraft.wallpaper.R;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.onesignal.OneSignal;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cn.refactor.library.SmoothCheckBox;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private String from = "";

    SharedPref sharedPref;
    EditText editText_email, editText_password;
    Button button_login, button_skip;
    TextView textView_forgotpass;
    Methods methods;
    ProgressDialog progressDialog;
    LinearLayout ll_checkbox;
    SmoothCheckBox cb_rememberme;
    private MaterialButton btn_login_google, btn_login_fb;
    private FirebaseAuth mAuth;

    /*Facebook Login*/
    LoginButton loginButtonFB;
    CallbackManager callbackManager;

    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        mAuth = FirebaseAuth.getInstance();

        from = getIntent().getStringExtra("from");

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        btn_login_google = findViewById(R.id.btn_login_google);
        btn_login_fb = findViewById(R.id.btn_login_fb);
        loginButtonFB = findViewById(R.id.login_button);
        loginButtonFB.setReadPermissions(Arrays.asList("email"));
        callbackManager = CallbackManager.Factory.create();

        ll_checkbox = findViewById(R.id.ll_checkbox);
        cb_rememberme = findViewById(R.id.cb_rememberme);
        editText_email = findViewById(R.id.et_login_email);
        editText_password = findViewById(R.id.et_login_password);
        button_login = findViewById(R.id.button_login);
        button_skip = findViewById(R.id.button_skip);
        textView_forgotpass = findViewById(R.id.tv_forgotpass);

        if (sharedPref.getIsRemember()) {
            editText_email.setText(sharedPref.getEmail());
            editText_password.setText(sharedPref.getPassword());
        }

        ll_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_rememberme.setChecked(!cb_rememberme.isChecked());
            }
        });

        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

        textView_forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportDialog();
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        btn_login_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButtonFB.performClick();
            }
        });

        btn_login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (methods.isNetworkAvailable()) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 112);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButtonFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
                if (loggedIn) {
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
//                Log.e("aaa", exception.getMessage());
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

                AdapterWallpaperWelcome adapterWallpaper = new AdapterWallpaperWelcome(LoginActivity.this, arrayListWallpaper);

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

    private void attemptLogin() {
        editText_email.setError(null);
        editText_password.setError(null);

        // Store values at the time of the login attempt.
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            editText_password.setError(getString(R.string.error_password_sort));
            focusView = editText_password;
            cancel = true;
        }
        if (editText_password.getText().toString().endsWith(" ")) {
            editText_password.setError(getString(R.string.pass_end_space));
            focusView = editText_password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            editText_email.setError(getString(R.string.cannot_empty));
            focusView = editText_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            editText_email.setError(getString(R.string.error_invalid_email));
            focusView = editText_email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loadLogin();
        }
    }

    private void loadLogin() {
        if (methods.isNetworkAvailable()) {

            progressDialog.show();

            Call<ItemUserList> call = apiInterface.getLogin(methods.getAPIRequest(Constant.URL_LOGIN, 0, "", "", "", "", "", "", "", editText_email.getText().toString(), editText_password.getText().toString(), "", "", ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                        if (response.body().getArrayListUser().get(0).getSuccess().equals("1")) {
                            sharedPref.setLoginDetails(response.body().getArrayListUser().get(0).getId(), response.body().getArrayListUser().get(0).getName(), response.body().getArrayListUser().get(0).getMobile(), editText_email.getText().toString(), response.body().getArrayListUser().get(0).getImage(), "", cb_rememberme.isChecked(), editText_password.getText().toString(), Constant.LOGIN_TYPE_NORMAL);
                            sharedPref.setIsLogged(true);
                            sharedPref.setIsAutoLogin(true);

//                            if (from.equals("app")) {
//                                finish();
//                            } else {
                            openMainActivity();
//                            }
                        }

                        Toast.makeText(LoginActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadLoginSocial(final String loginType, final String name, String email, final String authId) {
        if (methods.isNetworkAvailable()) {

            progressDialog.show();

            Call<ItemUserList> call = apiInterface.getSocialLogin(methods.getAPIRequest(Constant.URL_SOCIAL_LOGIN, 0, authId, loginType, "", "", "", "", name, email, "", "", "", ""));
            call.enqueue(new Callback<ItemUserList>() {
                @Override
                public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                    if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {

                        switch (response.body().getArrayListUser().get(0).getSuccess()) {
                            case "1":
                                sharedPref.setLoginDetails(response.body().getArrayListUser().get(0).getId(), response.body().getArrayListUser().get(0).getName(), "", email, "", authId, cb_rememberme.isChecked(), "", loginType);
                                sharedPref.setIsLogged(true);
                                sharedPref.setIsAutoLogin(true);

                                OneSignal.sendTag("user_id", response.body().getArrayListUser().get(0).getId());

                                Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

//                                if (from.equals("app")) {
//                                    finish();
//                                } else {
                                openMainActivity();
//                                }
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), response.body().getArrayListUser().get(0).getMessage());
                                break;
                            default:
                                if (response.body().getArrayListUser().get(0).getMessage().contains("already") || response.body().getArrayListUser().get(0).getMessage().contains("Invalid email format")) {
                                    editText_email.setError(response.body().getArrayListUser().get(0).getMessage());
                                    editText_email.requestFocus();
                                } else {
                                    Toast.makeText(LoginActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                try {
                                    if (loginType.equals(Constant.LOGIN_TYPE_FB)) {
                                        LoginManager.getInstance().logOut();
                                    } else if (loginType.equals(Constant.LOGIN_TYPE_GOOGLE)) {
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String first_name = "", email = "", last_name = "";

                            if (object.has("first_name")) {
                                first_name = object.getString("first_name");
                            }
                            if (object.has("last_name")) {
                                last_name = object.getString("last_name");
                            }
                            if (object.has("email")) {
                                email = object.getString("email");
                            }
                            String id = object.getString("id");
                            loadLoginSocial(Constant.LOGIN_TYPE_FB, first_name + " " + last_name, email, id);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            loadLoginSocial(Constant.LOGIN_TYPE_GOOGLE, user.getDisplayName(), user.getEmail(), user.getUid());
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to Sign IN", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showReportDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.layout_forgot_password, null);

        BottomSheetDialog dialog_forgot_pass = new BottomSheetDialog(LoginActivity.this);
        dialog_forgot_pass.setContentView(view);
        dialog_forgot_pass.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_forgot_pass.show();

        final EditText et_email = dialog_forgot_pass.findViewById(R.id.et_forgot_email);
        MaterialButton button_submit = dialog_forgot_pass.findViewById(R.id.button_forgot_send);

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (methods.isNetworkAvailable()) {
                    if (!et_email.getText().toString().trim().isEmpty()) {
                        dialog_forgot_pass.dismiss();
                        loadForgotPass(et_email.getText().toString());
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadForgotPass(String email) {
        progressDialog.show();

        Call<ItemUserList> call = APIClient.getClient().create(APIInterface.class).getForgotPassword(methods.getAPIRequest(Constant.URL_FORGOT_PASSWORD, 0, "", "", "", "", "", "", "", email, "", "", "", ""));
        call.enqueue(new Callback<ItemUserList>() {
            @Override
            public void onResponse(@NonNull Call<ItemUserList> call, @NonNull Response<ItemUserList> response) {
                if (response.body() != null && response.body().getArrayListUser() != null && response.body().getArrayListUser().size() > 0) {
                    Toast.makeText(LoginActivity.this, response.body().getArrayListUser().get(0).getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ItemUserList> call, @NonNull Throwable t) {
                call.cancel();
                Toast.makeText(LoginActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 112) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                if (resultCode != 0) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    firebaseAuthWithGoogle(task.getResult().getIdToken());
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_login_goole), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_goole), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}