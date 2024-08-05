package com.example.screentimemonitor.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.screentimemonitor.R;
import com.example.screentimemonitor.SharedPreferencesManager;
import com.example.screentimemonitor.Module.DataDay;
import com.example.screentimemonitor.databinding.FragmentHomeBinding;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Fetch and display screen time data
        displayScreenTimeData(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayScreenTimeData(binding.getRoot()); // Refresh data when the fragment is resumed
    }

    private void displayScreenTimeData(View root) {
        TextView screenTimeTextView = root.findViewById(R.id.text_daily);

        // Retrieve screen time data from SharedPreferencesManager
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();

        if (spManager == null) {
            screenTimeTextView.setText("No data available");
            return;
        }

        List<DataDay> dataDays = spManager.getDataDayList();

        if (dataDays.isEmpty()) {
            screenTimeTextView.setText("No data available");
            return;
        }

        DataDay today = dataDays.get(dataDays.size() - 1); // Get the latest day
        Map<String, Long> appUsageByNames = today.getAppUsageByNames();

        StringBuilder screenTimeData = new StringBuilder();
        PackageManager pm = getActivity().getPackageManager();

        for (Map.Entry<String, Long> entry : appUsageByNames.entrySet()) {
            String packageName = entry.getKey();
            long usageTime = entry.getValue();

            if (usageTime >= TimeUnit.MINUTES.toMillis(1)) { // Only show usage time of 1 minute or more
                String appName = getAppNameFromPackage(packageName, pm);
                screenTimeData.append(appName).append(": ").append(formatUsageTime(usageTime)).append("\n");
            }
        }

        // Display the data
        if (screenTimeData.length() > 0) {
            screenTimeTextView.setText(screenTimeData.toString());
        } else {
            screenTimeTextView.setText("No data available");
        }
    }

    private String getAppNameFromPackage(String packageName, PackageManager pm) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return packageName;
        }
    }

    private String formatUsageTime(long usageTime) {
        long hours = TimeUnit.MILLISECONDS.toHours(usageTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(usageTime) % 60;

        if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + (minutes > 0 ? " and " + minutes + " minute" + (minutes > 1 ? "s" : "") : "");
        } else {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
