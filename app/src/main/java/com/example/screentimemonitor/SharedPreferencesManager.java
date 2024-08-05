package com.example.screentimemonitor;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.screentimemonitor.Module.DataDay;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {

    private static volatile SharedPreferencesManager instance = null;
    public static final String DATA_DAY_LIST = "DATA_DAY_LIST";
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
        Type listType = new TypeToken<ArrayList<DataDay>>() {
        }.getType();

        if (json.equals("")) {
            return new ArrayList<>();
        }
        return new Gson().fromJson(json, listType);
    }
}