package com.example.screentimemonitor.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.screentimemonitor.Module.DataDay;
import com.example.screentimemonitor.R;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

public class ScreenTimeService extends Service {

    private static final String TAG = "ScreenTimeService";
    private static final String CHANNEL_ID = "ScreenTimeServiceChannel";
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                collectAndSaveUsageData();
                handler.postDelayed(this, 600000); // Run every 10 minute
                Log.d(TAG, "Data collected and saved");
            }
        };
        handler.post(runnable);
        SharedPreferencesManager.init(getApplicationContext()); // Initialize SharedPreferencesManager
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (flags == START_FLAG_REDELIVERY) {
            handler.post(runnable); // Ensure the handler is running
        }
        return START_STICKY; // Ensure the service restarts if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, RestartServiceReceiver.class);
        this.sendBroadcast(broadcastIntent);
    }


    private void collectAndSaveUsageData() {
        String currentDate = getCurrentDate();
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();

        if (!currentDate.equals(spManager.getLastCheckedDate())) {
            spManager.setNotificationSent(false);
            spManager.setLastCheckedDate(currentDate);
        }


        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long startTime = getStartOfDayInMillis(); // Collect data from the start of the day

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
        Map<String, Long> usageMap = new HashMap<>();
        UsageEvents.Event event = new UsageEvents.Event();
        Map<String, Long> lastUsedMap = new HashMap<>();

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            String packageName = event.getPackageName();
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastUsedMap.put(packageName, event.getTimeStamp());
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                Long lastUsedTime = lastUsedMap.get(packageName);
                if (lastUsedTime != null) {
                    long totalTime = usageMap.getOrDefault(packageName, 0L);
                    usageMap.put(packageName, totalTime + (event.getTimeStamp() - lastUsedTime));
                }
            }
        }
        long totalUsageTime = 0;
        for (long usageTime : usageMap.values()) {
            totalUsageTime += usageTime;
        }

        // Filter out apps with zero usage time
        usageMap.entrySet().removeIf(entry -> entry.getValue() <= 0);

        checkUsageTimeLimit(totalUsageTime);

        updateDataDays(currentDate, usageMap);
    }

    private long getStartOfDayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void updateDataDays(String currentDate, Map<String, Long> usageMap) {
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        List<DataDay> dataDays = spManager.getDataDayList();

        if (!dataDays.isEmpty() && dataDays.get(dataDays.size() - 1).getDate().equals(currentDate)) {
            DataDay today = dataDays.get(dataDays.size() - 1);
            Map<String, Long> appUsageByNames = today.getAppUsageByNames();

            for (Map.Entry<String, Long> entry : usageMap.entrySet()) {
                appUsageByNames.put(entry.getKey(), entry.getValue()); // Update with the most recent value
            }

            today.setAppUsageByNames(appUsageByNames);
        } else {
            if (dataDays.size() == 30) {
                dataDays.remove(0); // Remove the oldest entry
            }
            dataDays.add(new DataDay(currentDate, usageMap));
        }

        spManager.putDataDayList(new ArrayList<>(dataDays)); // Save the updated list back to SharedPreferences
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Time Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.icon)
                .build();
    }


    private void checkUsageTimeLimit(long totalUsageTime) {
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        int usageTimeLimit = spManager.getUsageTimeLimit(); // Assuming you have a method to get the usage time limit
        if (totalUsageTime > usageTimeLimit * 60 * 1000) { // Convert limit from minutes to milliseconds
            sendTimeLimitExceededNotification();
        }
    }

    private void sendTimeLimitExceededNotification() {
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        if (!spManager.isNotificationSent()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(getString(R.string.time_limit_exceeded_title))
                    .setContentText(getString(R.string.time_limit_exceeded_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(3, builder.build());
                spManager.setNotificationSent(true);
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
