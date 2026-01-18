package kr.ac.uc.albago.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kr.ac.uc.albago.BuildConfig;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * RetrofitClient
 * - 역할(user/employer)에 따라 싱글턴으로 구분된 Retrofit 인스턴스를 제공
 * - JWT Access Token 자동 첨부 및 Refresh Token으로 자동 갱신 처리
 */
public class RetrofitClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL;

    // 역할별 싱글턴 인스턴스
    private static RetrofitClient userInstance;
    private static RetrofitClient employerInstance;

    // Retrofit으로 생성된 API 인터페이스
    private final ApiService api;

    // SharedPreferences에서 토큰을 관리
    private final SharedPreferences prefs;

    // context 저장 (refresh 전용 Retrofit 생성에 사용)
    private final Context context;

    /**
     * 생성자 - 내부에서 OkHttpClient 및 Retrofit 인스턴스를 구성
     */
    private RetrofitClient(Context context, String role) {

        android.util.Log.d("API_DEBUG",
                "BASE_URL = " + BuildConfig.API_BASE_URL + ", role = " + role);

        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        // 로그 출력을 위한 HTTP 로깅 인터셉터
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Retrofit과 연결될 OkHttpClient 구성
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)

                //  인터셉터: 모든 요청에 accessToken 자동 추가
                .addInterceptor(new AuthInterceptor(context))


                //  로그 출력
                .addInterceptor(loggingInterceptor)

                //  Authenticator: accessToken이 만료되어 401이 오면 자동으로 refresh 요청 수행
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        // 재시도 횟수 2번 이상이면 중단 (무한 루프 방지)
                        if (responseCount(response) >= 2) return null;

                        String refreshToken = prefs.getString("REFRESH_TOKEN", null);
                        if (refreshToken == null) return null;

                        //  Interceptor/Authenticator 없는 전용 인스턴스로 refresh API 호출
                        RefreshResponse refreshResp = createRefreshApi().refresh(Map.of("refreshToken", refreshToken)).execute().body();
                        if (refreshResp == null || refreshResp.accessToken == null) {
                            // refresh 실패 → 토큰 정리
                            prefs.edit().clear().apply();
                            return null; // 여기서 끝
                        }

                        // 새 accessToken 저장
                        prefs.edit()
                        .putString("ACCESS_TOKEN", refreshResp.accessToken)
                        .putString("REFRESH_TOKEN", refreshResp.refreshToken) // ⭐ 추가
                        .apply();

                        // 실패했던 원래 요청을 accessToken 붙여서 재요청
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + refreshResp.accessToken)
                                .build();
                    }

                    // 요청 재시도 횟수 계산 (최대 2번까지만)
                    private int responseCount(Response response) {
                        int count = 1;
                        while ((response = response.priorResponse()) != null) count++;
                        return count;
                    }
                })

                .build();

        // Retrofit 인스턴스 구성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.api = retrofit.create(ApiService.class);
    }

    /**
     * Refresh 전용 Retrofit 인스턴스
     * - Interceptor/Authenticator 없이 동작하여 무한루프 방지
     */
    private ApiService createRefreshApi() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }

    /**
     * role에 따라 Retrofit 인스턴스를 초기화 (앱 시작 시 1회 호출)
     */
    public static synchronized void init(Context context, String role) {
        init(context, role, false);
    }

    /**
     * force=true일 경우 기존 인스턴스를 무조건 새로 생성
     */
    public static synchronized void init(Context context, String role, boolean force) {
        if ("EMPLOYER".equals(role)) {
            if (employerInstance == null || force) {
                employerInstance = new RetrofitClient(context, "EMPLOYER");
            }
        } else {
            if (userInstance == null || force) {
                userInstance = new RetrofitClient(context, "user");
            }
        }
    }

    /**
     * 역할별 RetrofitClient 인스턴스 가져오기
     */
    public static RetrofitClient getInstance(String role) {
        if ("EMPLOYER".equals(role)) {
            if (employerInstance == null)
                throw new IllegalStateException("RetrofitClient(EMPLOYER) not initialized. Call init(context, \"EMPLOYER\") first.");
            return employerInstance;
        } else {
            if (userInstance == null)
                throw new IllegalStateException("RetrofitClient(user) not initialized. Call init(context, \"user\") first.");
            return userInstance;
        }
    }

    /**
     * 실제 API 인터페이스 반환
     */
    public ApiService getApi() {
        return api;
    }
}
