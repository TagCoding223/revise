package com.revise;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {
    // A flag to track if your app has finished its initial heavy lifting
    private boolean isAppReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force a smooth crossfade animation when the Activity is recreated for theme changes
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Load the saved theme preference BEFORE installing the splash screen or layout
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        // 1. Install the splash screen BEFORE super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Keep the splash screen visible until your data is ready
        splashScreen.setKeepOnScreenCondition(() -> !isAppReady);

        // 3. Simulate loading initial data (e.g., checking JWT token, checking Room DB)
        loadInitialData();
    }

    private void loadInitialData() {
        // Here you would check for stored tokens or offline sync states.
        // We use a simple thread to simulate a 1.5-second load.
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Once data is loaded, set flag to true to dismiss the splash screen
            isAppReady = true;

            // You can then trigger your Navigation logic here (e.g., go to Log in or Dashboard)
        }).start();
    }
}