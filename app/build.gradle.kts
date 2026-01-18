plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "kr.ac.uc.albago"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.ac.uc.albago"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //  Retrofit BASE URL (로컬 개발용)
//        buildConfigField(
//            "String",
//            "API_BASE_URL",
//            "\"http://192.168.35.202:8080/\""
//        )

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"xxxxxxxxxxxx.apps.googleusercontent.com\""
        )

    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // 에뮬레이터용 로컬 서버
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://10.0.2.2:8080\""
            )
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            //  나중에 EC2 올릴 때 여기만 변경
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // 네트워크 통신 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("androidx.cardview:cardview:1.0.0") // 카드뷰 라이브러리



    // AndroidX 안정화된 버전
    implementation(libs.appcompat)           // 1.6.1
    implementation(libs.material)           // 1.9.0
    implementation(libs.activity)           // 1.8.0
    implementation(libs.constraintlayout)   // 2.1.4

    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.21.0")

    //https://navermaps.github.io/android-map-sdk/guide-ko/1.html 여기서 최신버전 가져왔습니다

    // 테스트 라이브러리
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Navigation 라이브러리 추가
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")
    implementation ("com.google.android.gms:play-services-location:21.0.1")  // 네이버지도sdk도 구글서비스조합 해서씀

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Add Picasso dependency > 프사 때문에 추가됨
    implementation ("com.squareup.picasso:picasso:2.8")

    // Google Login & Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")


}