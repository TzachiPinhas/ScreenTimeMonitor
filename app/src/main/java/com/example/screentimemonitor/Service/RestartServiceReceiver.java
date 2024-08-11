package com.example.screentimemonitor.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RestartServiceReceiver", "Service is being restarted");
        context.startService(new Intent(context, ScreenTimeService.class));
        context.startService(new Intent(context, BatteryService.class));
    }
}
