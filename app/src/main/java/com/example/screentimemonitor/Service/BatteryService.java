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
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(2, getNotification());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                collectBatteryData();
                Log.d(TAG, "Battery data collected");
                handler.postDelayed(this, 20000); // Run every 10 minutes
            }
        };
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BatteryService", "Service destroyed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestartServiceReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }



    private void collectBatteryData() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager == null) {
            return;
        }

        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        long currentTime = System.currentTimeMillis();

        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        Map<Long, Integer> batteryDataMap = new TreeMap<>(spManager.getBatteryDataMap());

        // Keep only the data for the last 24 hours
        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000);
        batteryDataMap.entrySet().removeIf(entry -> entry.getKey() < oneDayAgo);

        batteryDataMap.put(currentTime, batteryLevel); //add the new point to the map
        spManager.saveBatteryDataMap(batteryDataMap); //save the map
    }

    private void createNotificationChannel() {
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

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Battery Service")
                .setContentText("Monitoring battery usage...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
