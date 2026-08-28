package com.revise;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import com.revise.network.TokenManager;

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

        // 3. Setup Navigation safely on the Main UI Thread
        setupNavigation();

        // 4. Start the background delay for the splash screen
        loadInitialData();
    }

    private void setupNavigation() {
        // 1. Find the Navigation Host
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 2. Inflate the graph manually so we can change the start destination
            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);

            // 3. Check for existing session
            TokenManager tokenManager = new TokenManager(this);
            String token = tokenManager.getAccessToken();
            if (token != null && !token.isEmpty()) {
                // User is logged in -> Go straight to Dashboard
                navGraph.setStartDestination(R.id.dashboardFragment);
            } else {
                // User is NOT logged in -> Show Login Screen
                navGraph.setStartDestination(R.id.loginFragment);
            }

            // 4. Apply the modified graph to the controller
            navController.setGraph(navGraph);
        }
    }

    private void loadInitialData() {
        // We use a simple thread ONLY to simulate a 1.5-second load.
        // Once time passes, we flip the flag to dismiss the splash screen.
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Once data is loaded, set flag to true to dismiss the splash screen
            isAppReady = true;
        }).start();
    }
}