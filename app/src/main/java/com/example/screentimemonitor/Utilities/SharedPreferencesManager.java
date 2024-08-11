package com.example.screentimemonitor.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.screentimemonitor.Module.DataDay;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SharedPreferencesManager {

    private static volatile SharedPreferencesManager instance = null;
    private static final String DATA_DAY_LIST = "DATA_DAY_LIST";
    private static final String BATTERY_DATA = "BATTERY_DATA";
    private static final String USAGE_TIME_LIMIT = "time_limit";
    private static final String NOTIFICATION_SENT = "notification_sent";
    private static final String LAST_CHECKED_DATE = "last_checked_date";
    private SharedPreferences sharedPref;

    private SharedPreferencesManager(Context context) {
        this.sharedPref = context.getSharedPreferences(DATA_DAY_LIST, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                instance = new SharedPreferencesManager(context);
            }
        }
    }

    public static SharedPreferencesManager getInstance() {
        return instance;
    }

    public void putDataDayList(List<DataDay> dataDays) {
        Gson gson = new Gson();
        String dataDayListAsJson = gson.toJson(dataDays);
        sharedPref.edit().putString(DATA_DAY_LIST, dataDayListAsJson).apply();
    }

    public List<DataDay> getDataDayList() {
        String json = sharedPref.getString(DATA_DAY_LIST, "");
        Type listType = new TypeToken<ArrayList<DataDay>>() {}.getType();

        if (json.equals("")) {
            return new ArrayList<>();
        }
        return new Gson().fromJson(json, listType);
    }

    public void saveBatteryDataMap(Map<Long, Integer> batteryDataMap) {
        Gson gson = new Gson();
        String batteryDataAsJson = gson.toJson(batteryDataMap);
        sharedPref.edit().putString(BATTERY_DATA, batteryDataAsJson).apply();
    }

    public List<Map.Entry<Long, Integer>> getBatteryData() {
        Map<Long, Integer> batteryData = getBatteryDataMap();
        return new ArrayList<>(batteryData.entrySet());
    }

    public Map<Long, Integer> getBatteryDataMap() {
        String json = sharedPref.getString(BATTERY_DATA, "");
        Type type = new TypeToken<TreeMap<Long, Integer>>() {}.getType();

        if (json.equals("")) {
            return new TreeMap<>();
        }
        return new Gson().fromJson(json, type);
    }

    // Usage time limit and notification methods
    public void saveUsageTimeLimit(int timeLimit) {
        sharedPref.edit().putInt(USAGE_TIME_LIMIT, timeLimit).apply();
    }

    public int getUsageTimeLimit() {
        return sharedPref.getInt(USAGE_TIME_LIMIT, 180); // Default is 180 minutes (3 hours)
    }

    public void setNotificationSent(boolean sent) {
        sharedPref.edit().putBoolean(NOTIFICATION_SENT, sent).apply();
    }

    public boolean isNotificationSent() {
        return sharedPref.getBoolean(NOTIFICATION_SENT, false);
    }

    public void setLastCheckedDate(String date) {
        sharedPref.edit().putString(LAST_CHECKED_DATE, date).apply();
    }

    public String getLastCheckedDate() {
        return sharedPref.getString(LAST_CHECKED_DATE, "");
    }
}
