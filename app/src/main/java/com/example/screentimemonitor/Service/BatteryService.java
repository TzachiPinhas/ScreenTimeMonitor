package com.example.screentimemonitor.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.screentimemonitor.R;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;

import java.util.Map;
import java.util.TreeMap;

public class BatteryService extends Service {

    private static final String TAG = "BatteryService";
    private static final String CHANNEL_ID = "BatteryServiceChannel";
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() { // Start the service and collect battery data every 10 minutes
        super.onCreate();
        createNotificationChannel();
        startForeground(2, getNotification());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                collectBatteryData();
                handler.postDelayed(this, 60000); // Run every 10 minutes
                Log.d(TAG, "Battery data collected");
            }
        };
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() { // Restart the service when it is destroyed
        super.onDestroy();
        Log.d("BatteryService", "Service destroyed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestartServiceReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }



    private void collectBatteryData() { // Collect battery data and save it in SharedPreferences
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE); // Get the battery manager
        if (batteryManager == null) {
            return;
        }

        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); // Get the battery level
        long currentTime = System.currentTimeMillis();

        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        Map<Long, Integer> batteryDataMap = new TreeMap<>(spManager.getBatteryDataMap()); // Get the battery data map

        // Keep only the data for the last 24 hours
        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000);
        batteryDataMap.entrySet().removeIf(entry -> entry.getKey() < oneDayAgo); // Remove the data points older than 24 hours

        batteryDataMap.put(currentTime, batteryLevel); //add the new point to the map
        spManager.saveBatteryDataMap(batteryDataMap); //save the map
    }

    private void createNotificationChannel() { // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Battery Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification getNotification() { // Create the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.battery_service_title))
                .setContentText(getString(R.string.battery_service_text))
                .setSmallIcon(R.drawable.icon)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
