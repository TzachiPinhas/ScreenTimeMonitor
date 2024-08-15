package com.example.screentimemonitor.Module;

import java.util.Map;

public class DataDay { // DataDay class to store the data of a day

    String date;
    Map<String, Long> appUsageByNames;

    public DataDay() {
    }

    public DataDay(String date, Map<String, Long> appUsageByNames) {
        this.date = date;
        this.appUsageByNames = appUsageByNames;
    }

    public String getDate() {
        return date;
    }


    public Map<String, Long> getAppUsageByNames() {
        return appUsageByNames;
    }

    public DataDay setAppUsageByNames(Map<String, Long> appUsageByNames) {
        this.appUsageByNames = appUsageByNames;
        return this;
    }
}
