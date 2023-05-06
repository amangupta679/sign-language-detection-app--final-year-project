package com.andreaziqing.signlanguagedetectionapp.OnBoarding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.andreaziqing.signlanguagedetectionapp.Authentication.LoginActivity;
import com.andreaziqing.signlanguagedetectionapp.R;


/**
 * The Splash activity class.
 * Handles the splash screen logic and animation of the start UI.
 * Varies its behavior depending whether is the first time for the user in the app or not, showing
 * the onboarding UI accordingly.
 */
public class SplashActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    private static int SPLASH_TIMER = 5000;

    SharedPreferences onBoardingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        lottieAnimationView = findViewById(R.id.lottie);
        lottieAnimationView.animate().translationY(1400).setDuration(600).setStartDelay(4000);

        new Handler().postDelayed(()-> {

            // Check if it's the first time the user enter the app
            onBoardingScreen = getSharedPreferences("onBoardingScreen", MODE_PRIVATE);
            boolean isFirstTime = onBoardingScreen.getBoolean("firstTime", true);

            if (isFirstTime) {
                SharedPreferences.Editor editor = onBoardingScreen.edit();
                editor.putBoolean("firstTime", false);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), OnBoarding.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

        }, SPLASH_TIMER);
    }
}