package com.example.screentimemonitor;

import android.content.Intent;
import android.os.Bundle;

import com.example.screentimemonitor.Service.BatteryService;
import com.example.screentimemonitor.Service.RestartServiceReceiver;
import com.example.screentimemonitor.Service.ScreenTimeService;
import com.example.screentimemonitor.Utilities.PermissionUtils;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.screentimemonitor.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferencesManager.init(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_battery, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        if (!PermissionUtils.hasUsageStatsPermission(this)) {
            PermissionUtils.requestUsageStatsPermission(this);
        }


        // Start the ScreenTimeService
        Intent screenTimeServiceIntent = new Intent(this, ScreenTimeService.class);
        ContextCompat.startForegroundService(this, screenTimeServiceIntent);

        // Start the BatteryService
        Intent batteryServiceIntent = new Intent(this, BatteryService.class);
        ContextCompat.startForegroundService(this, batteryServiceIntent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestartServiceReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }

}
