package kr.ac.uc.albago.employer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import kr.ac.uc.albago.R;

public class EmployerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences에서 로그인 토큰 확인
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        Log.d("DEBUG_SHARED", "Access Token: " + prefs.getString("ACCESS_TOKEN", "null"));
        String accessToken = prefs.getString("ACCESS_TOKEN", null);

        // 토큰이 없으면 로그인 화면으로 이동
        if (accessToken == null) {
            Intent intent = new Intent(this, kr.ac.uc.albago.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // RetrofitClient 초기화
        kr.ac.uc.albago.api.RetrofitClient.init(this, "employer");
        setContentView(R.layout.activity_company1_main);

        // BottomNavigationView 참조
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // NavHostFragment에서 NavController 가져오기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        NavController navController = navHostFragment.getNavController();
        // BottomNavigationView에서 탭 선택 시 항상 해당 프래그먼트로 이동 (popUpTo로 백스택 정리)
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, false)
                    .build();

            if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home, null, navOptions);
                return true;
            } else if (id == R.id.nav_post) {
                navController.navigate(R.id.nav_post, null, navOptions);
                return true;
            } else if (id == R.id.nav_applicants) {
                navController.navigate(R.id.nav_applicants, null, navOptions);
                return true;
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.nav_profile, null, navOptions);
                return true;
            }
            return false;
        });


        // NavigationUI.setupWithNavController(bottomNav, navController); // 사용하지 않음
    }
}