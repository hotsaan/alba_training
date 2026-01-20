    package kr.ac.uc.albago.common;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.util.Log;
    import android.util.Patterns;
    import android.view.View;
    import android.view.animation.Animation;
    import android.view.animation.AnimationUtils;
    import android.widget.Button;
    import android.widget.CheckBox;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.navigation.NavController;
    import androidx.navigation.Navigation;
    import androidx.navigation.ui.NavigationUI;

    import com.google.android.gms.auth.api.signin.GoogleSignIn;
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
    import com.google.android.gms.auth.api.signin.GoogleSignInClient;
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
    import com.google.android.gms.common.api.ApiException;
    import com.google.android.gms.tasks.Task;
    import com.google.android.material.bottomnavigation.BottomNavigationView;

    import java.util.HashMap;
    import java.util.Map;

    import kr.ac.uc.albago.BuildConfig;
    import kr.ac.uc.albago.R;
    import kr.ac.uc.albago.api.LoginRequest;
    import kr.ac.uc.albago.api.LoginResponse;
    import kr.ac.uc.albago.api.RefreshResponse;
    import kr.ac.uc.albago.api.RetrofitClient;
    import kr.ac.uc.albago.employer.EmployerMainActivity;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class MainActivity extends AppCompatActivity {

        private static final String TAG = "MainActivity";

        private LinearLayout loginFormContainer;
        private EditText editTextEmail, editTextPassword;

        private Button buttonLogin;


        private TextView forgotPassword, createAccountText, buttonGuest;
        private CheckBox keepLoggedInCheckBox;

        private Button buttonUserLogin, buttonBossLogin;
        ;


        private BottomNavigationView bottomNavigationView;
        private NavController navController;

        private boolean isLoggedIn = false;
        private boolean isLoginScreen = true;

        private String currentRole = null;

        private GoogleSignInClient mGoogleSignInClient;
        private ActivityResultLauncher<Intent> mGoogleSignInLauncher;
        private ImageButton buttonGoogle;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Log.d("GOOGLE_CLIENT_ID", "WEB CLIENT ID = " + BuildConfig.GOOGLE_WEB_CLIENT_ID);

            super.onCreate(savedInstanceState);

            // Configure Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                  //  .requestIdToken("수정필요") // Use the Web Client ID that was already on your server
                    .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)

                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // Register the ActivityResultLauncher for the Google Sign-In result
            mGoogleSignInLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            handleGoogleSignInResult(task);
                        } else {
                            Toast.makeText(this, "Google Sign-In cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    });


            // RetrofitClient 두 역할 초기화
            RetrofitClient.init(getApplicationContext(), "user");
            RetrofitClient.init(getApplicationContext(), "employer");

            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);

            Log.d("DEBUG_SHARED", "Access Token: " + prefs.getString("ACCESS_TOKEN", "null"));


            // 앱 완전 재시작 & 로그인 유지 안 하면 토큰 삭제
            if (isTaskRoot() && !prefs.getBoolean("keep_logged_in", false)) {
                prefs.edit()
                        .remove("ACCESS_TOKEN")
                        .remove("REFRESH_TOKEN")
                        .remove("user_role")
                        .apply();

            }

            boolean keepLoggedIn = prefs.getBoolean("keep_logged_in", false);
            String refreshToken = keepLoggedIn ? prefs.getString("REFRESH_TOKEN", null) : null;

            if (refreshToken != null) {
                // 로그인 유지 중일 때 토큰 재발급 시도
                setContentView(R.layout.activity_login);
                loginFormContainer = findViewById(R.id.loginFormContainer);
                loginFormContainer.setVisibility(View.GONE);
                initializeLoginScreen();

                Map<String, String> body = new HashMap<>();
                body.put("refreshToken", refreshToken);

                String role = prefs.getString("user_role", "user");
                RetrofitClient.getInstance(role).getApi()
                        .refresh(body)
                        .enqueue(new Callback<RefreshResponse>() {
                            @Override
                            public void onResponse(Call<RefreshResponse> call, Response<RefreshResponse> resp) {
                                if (resp.isSuccessful() && resp.body() != null) {
                                    String newAccessToken = resp.body().accessToken;
                                    String newRole = resp.body().role;
                                    if (newRole == null || newRole.isEmpty()) {
                                        newRole = prefs.getString("user_role", "user");
                                    }
                                    prefs.edit()
                                            .putString("ACCESS_TOKEN", newAccessToken)
                                            .putString("user_role", newRole)
                                            .apply();

                                    RetrofitClient.init(getApplicationContext(), newRole, true);
                                    // apply() 이후에 화면 전환
                                    runOnUiThread(() -> {
                                        isLoggedIn = true;
                                        switchToNavigationScreen();
                                    });
                                } else {
                                    isLoggedIn = false;
                                    prefs.edit().clear().apply();
                                    setContentView(R.layout.activity_login);
                                    initializeLoginScreen();
                                }
                            }

                            @Override
                            public void onFailure(Call<RefreshResponse> call, Throwable t) {
                                isLoggedIn = false;
                                prefs.edit().clear().apply();
                                setContentView(R.layout.activity_login);
                                initializeLoginScreen();
                            }
                        });
            } else {
                // 비로그인 상태 또는 로그인 유지 안 하는 경우
                setContentView(R.layout.activity_login);
                initializeLoginScreen();
            }
        }

        private void initializeLoginScreen() {
            isLoginScreen = true;

            loginFormContainer = findViewById(R.id.loginFormContainer);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);

            buttonLogin = findViewById(R.id.buttonLogin);


            buttonGuest = findViewById(R.id.buttonGuest);
            forgotPassword = findViewById(R.id.textForgotPassword);
            createAccountText = findViewById(R.id.textCreateAccount);
            keepLoggedInCheckBox = findViewById(R.id.checkbox_keep_logged_in);

            buttonUserLogin = findViewById(R.id.buttonUserLogin);
            buttonBossLogin = findViewById(R.id.buttonBossLogin);

            loginFormContainer.setVisibility(View.GONE);

            buttonGoogle = findViewById(R.id.buttonGoogle);

            buttonGoogle.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                mGoogleSignInLauncher.launch(signInIntent);
            });

            buttonUserLogin.setOnClickListener(v -> {
                if (loginFormContainer.getVisibility() == View.GONE) {
                    loginFormContainer.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up_login_page);
                    loginFormContainer.startAnimation(anim);
                    currentRole = "user";
                    updateRoleUI(currentRole);
                } else if (!"user".equals(currentRole)) {
                    currentRole = "user";
                    updateRoleUI(currentRole);
                    playRoleSwitchAnimation();
                } else {
                        attemptLogin("user");
                }
            });

            buttonBossLogin.setOnClickListener(v -> {
                if (loginFormContainer.getVisibility() == View.GONE) {
                    loginFormContainer.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up_login_page);
                    loginFormContainer.startAnimation(anim);
                    currentRole = "employer";
                    updateRoleUI(currentRole);
                } else if (!"employer".equals(currentRole)) {
                    currentRole = "employer";
                    updateRoleUI(currentRole);
                    playRoleSwitchAnimation();
                } else {
                    attemptLogin("employer");
                }
            });



            buttonLogin.setOnClickListener(v -> {
                if (loginFormContainer.getVisibility() == View.GONE) {
                    loginFormContainer.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up_login_page);
                    loginFormContainer.startAnimation(anim);
                    buttonLogin.setVisibility(View.GONE);
                }
            });

            buttonGuest.setOnClickListener(v -> {
                // 게스트는 로그인 안 한 상태의 user 역할
                isLoggedIn = false;
                SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();
                editor.putString("user_role", "user")
                        .putBoolean("keep_logged_in", false)
                        .remove("ACCESS_TOKEN")
                        .remove("REFRESH_TOKEN")
                        .apply();

                RetrofitClient.init(getApplicationContext(), "user");  // user 역할로 초기화

                switchToNavigationScreen();
                Log.d(TAG, "Navigating to user main screen as guest (not logged in)");
            });

            forgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(this, ForgotChoiceActivity.class)));

            createAccountText.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        private void attemptLogin(String role) {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(email, password, role);
            RetrofitClient.getInstance(role).getApi().login(loginRequest)
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String roleFromResponse = response.body().role;
                                RetrofitClient.init(getApplicationContext(), roleFromResponse, true);

                                if (!role.equals(roleFromResponse))  {
                                    Toast.makeText(MainActivity.this, "해당하는 계정이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 로그인 성공 시 필요한 값 추출
                                String accessToken = response.body().accessToken;
                                String refreshToken = response.body().refreshToken;
                                String companyId = response.body().companyId;
                                String responseEmail = response.body().email;

                                SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();

                                if (keepLoggedInCheckBox.isChecked()) {
                                    editor.putString("ACCESS_TOKEN", accessToken);
                                    editor.putString("REFRESH_TOKEN", refreshToken);
                                    editor.putBoolean("keep_logged_in", true);
                                } else {
                                    editor.putString("ACCESS_TOKEN", accessToken);
                                    editor.putBoolean("keep_logged_in", false);
                                    editor.remove("REFRESH_TOKEN");
                                }

                                //  여기 핵심 저장
                                editor.putString("user_role", roleFromResponse != null ? roleFromResponse : "user");
                                editor.putString("email", responseEmail);
                                editor.putString("company_id", companyId);

                                editor.apply();

                                Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                switchToNavigationScreen();
                            } else {
                                String msg = role.equals("employer") ?
                                        "사장님 계정을 찾을 수 없습니다." :
                                        "이메일 또는 비밀번호가 올바르지 않습니다.";
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }

        private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
            try {
                GoogleSignInAccount account = completedTask.getResult(ApiException.class);

                // Signed in successfully, get the ID Token
                String idToken = account.getIdToken();
                if (idToken != null) {
                    Log.d(TAG, "Google ID Token: " + idToken);
                    // Send this token to your backend for verification and login
                    sendGoogleTokenToBackend(idToken);
                } else {
                    Toast.makeText(this, "Failed to get Google ID Token.", Toast.LENGTH_SHORT).show();
                }

            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                Toast.makeText(this, "Google Sign-In failed. Code: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }

        private void sendGoogleTokenToBackend(String idToken) {
            // We will assume Google login users are always the "user" role for the API call
            String roleForApiCall = "user";

            Map<String, String> body = new HashMap<>();
            body.put("idToken", idToken);

            RetrofitClient.getInstance(roleForApiCall).getApi().googleLogin(body)
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                // This logic is the same as your regular login success
                                LoginResponse loginResponse = response.body();

                                SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();
                                editor.putString("ACCESS_TOKEN", loginResponse.accessToken);
                                editor.putString("REFRESH_TOKEN", loginResponse.refreshToken);
                                editor.putString("user_role", loginResponse.role);
                                editor.putString("email", loginResponse.email);
                                editor.putString("company_id", loginResponse.companyId);
                                // For Google login, we can assume they want to stay logged in
                                editor.putBoolean("keep_logged_in", true);
                                editor.apply();

                                RetrofitClient.init(getApplicationContext(), loginResponse.role, true);

                                Toast.makeText(MainActivity.this, "Google 로그인 성공", Toast.LENGTH_SHORT).show();
                                switchToNavigationScreen(); // Navigate to the main app screen
                            } else {
                                // Handle login failure (e.g., account conflict, invalid token)
                                Toast.makeText(MainActivity.this, "Backend login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }



        private void updateRoleUI(String role) {
            if ("user".equals(role)) {
                buttonUserLogin.setBackgroundColor(getColor(R.color.activeButton));
                buttonBossLogin.setBackgroundColor(getColor(R.color.inactiveButton));
            } else {
                buttonBossLogin.setBackgroundColor(getColor(R.color.activeButton));
                buttonUserLogin.setBackgroundColor(getColor(R.color.inactiveButton));
            }
        }

        private void playRoleSwitchAnimation() {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_role_switch);
            loginFormContainer.startAnimation(anim);
        }


        private void switchToNavigationScreen() {
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            String role = prefs.getString("user_role", "user");
            String accessToken = prefs.getString("ACCESS_TOKEN", null);
            Log.d(TAG, "switchToNavigationScreen() accessToken: " + accessToken);

            isLoggedIn = (accessToken != null && !accessToken.isEmpty());

            if (role.equalsIgnoreCase("employer")) {
                Intent intent = new Intent( this, EmployerMainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            setContentView(R.layout.activity_main);
            isLoginScreen = false;

            bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);

            //  여기서 post()로 감싸서 NavController attach 타이밍 보장
            new Handler(Looper.getMainLooper()).post(() -> {
                navController = Navigation.findNavController(this, R.id.fragment_container);
                NavigationUI.setupWithNavController(bottomNavigationView, navController);

                // 홈 탭 이름 설정 (알바찾기/둘러보기)
                bottomNavigationView.getMenu().findItem(R.id.nav_home)
                        .setTitle(isLoggedIn ? "알바찾기" : "둘러보기");

                bottomNavigationView.setOnItemSelectedListener(item -> {
                                   int itemId = item.getItemId();
                    if (!isLoggedIn && (itemId == R.id.nav_activity || itemId == R.id.nav_profile)) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("로그인이 필요한 기능입니다.\n로그인 하시겠습니까?")
                                .setPositiveButton("로그인", (dialog, which) -> {
                                    setContentView(R.layout.activity_login);
                                    initializeLoginScreen();
                                    isLoginScreen = true;
                                })
                                .setNegativeButton("취소", null)
                                .show();
                        return false;
                    }
                    return NavigationUI.onNavDestinationSelected(item, navController);
                });
            });
        }

        @Override
        public void onBackPressed() {
            // If it's a login screen (which should not be part of the main nav graph)
            if (isLoginScreen) {
                super.onBackPressed(); // Let the system handle it (usually exits app)
            } else {
                // If it's not the login screen, we are in the main navigation graph.
                // Delegate back press to the NavController.
                // Get the NavController for your NavHostFragment (R.id.fragment_container based on your previous code)
                NavController currentNavController = Navigation.findNavController(this, R.id.fragment_container);

                // Attempt to navigate up (pop back stack) using the NavController
                // navigateUp() returns true if a back action was handled by the NavController.
                // If it returns false, it means the NavController has reached the root of its graph.
                if (!currentNavController.navigateUp()) {
                    // If the NavController couldn't navigate up (i.e., we are at the start destination of the graph),
                    // then let the default system back behavior happen (usually exits the app).
                    super.onBackPressed();
                }
            }
        }

    }