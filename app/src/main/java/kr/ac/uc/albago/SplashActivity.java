package kr.ac.uc.albago;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.uc.albago.employer.EmployerMainActivity;

public class SplashActivity extends AppCompatActivity {
    private boolean isMoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView slogan = findViewById(R.id.textViewSlogan);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        slogan.startAnimation(fadeIn);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        // 앱 완전 재시작 & 로그인 유지 안 하면 토큰 삭제
        if (isTaskRoot() && !prefs.getBoolean("keep_logged_in", false)) {
            prefs.edit()
                    .remove("ACCESS_TOKEN")
                    .remove("REFRESH_TOKEN")
                    .remove("user_role")
                    .apply();
        }

        // 토큰 삭제 후 최신값 읽기
        String accessToken = prefs.getString("ACCESS_TOKEN", null);
        String role = prefs.getString("user_role", "user");

        new Handler().postDelayed(() -> {
            if (!isMoved) {
                isMoved = true;
                Intent intent;
                // ACCESS_TOKEN 없으면 무조건 MainActivity로
                if (accessToken == null) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else if ("employer".equals(role)) {
                    intent = new Intent(SplashActivity.this, EmployerMainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 900);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}