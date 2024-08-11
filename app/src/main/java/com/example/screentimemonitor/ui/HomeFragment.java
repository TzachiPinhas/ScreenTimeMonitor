package com.example.screentimemonitor.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.screentimemonitor.Adapters.InfoAdapter;
import com.example.screentimemonitor.Module.DataDay;
import com.example.screentimemonitor.R;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;
import com.example.screentimemonitor.databinding.FragmentHomeBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MaterialButtonToggleGroup toggleButtonGroup;
    private MaterialTextView textAvgTime;
    private RecyclerView recyclerView;
    private InfoAdapter infoAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        findViews();
        setupAdapter();
        setupToggleButtons();
        return root;
    }

    private void findViews() {
        recyclerView = binding.recyclerViewAppUsage;
        toggleButtonGroup = binding.toggleButton;
        textAvgTime = binding.textAvgTime;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupAdapter();
        setupToggleButtons();
    }

    private void setupAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        displayScreenTimeData(); // Initial data display
    }

    private void displayScreenTimeData() {
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        if (spManager == null) {
            return;
        }

        List<DataDay> dataDays = spManager.getDataDayList();
        if (dataDays.isEmpty()) {
            return;
        }

        Map<String, Long> aggregatedAppUsage = new HashMap<>();
        long totalUsageTime = 0;
        int daysCount = 0;

        String interval = getCurrentInterval();
        int daysToConsider = interval.equals("daily") ? 1 : (interval.equals("weekly") ? 7 : 30);
        for (int i = dataDays.size() - 1; i >= 0 && daysCount < daysToConsider; i--) {
            DataDay dataDay = dataDays.get(i);
            for (Map.Entry<String, Long> entry : dataDay.getAppUsageByNames().entrySet()) {
                String packageName = entry.getKey();
                long usageTime = entry.getValue();
                aggregatedAppUsage.put(packageName, aggregatedAppUsage.getOrDefault(packageName, 0L) + usageTime);
            }
            totalUsageTime += dataDay.getAppUsageByNames().values().stream().mapToLong(Long::longValue).sum();
            daysCount++;
        }

        List<Map.Entry<String, Long>> appUsageList = new ArrayList<>(aggregatedAppUsage.entrySet());
        infoAdapter = new InfoAdapter(getContext(), appUsageList);
        recyclerView.setAdapter(infoAdapter);

        long avgUsageTime = totalUsageTime / daysCount;
        textAvgTime.setText("Average Screen Time: " + formatUsageTime(avgUsageTime));
    }


    private void setupToggleButtons() {
        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                displayScreenTimeData(); // Refresh data when toggled
            }
        });
        toggleButtonGroup.check(R.id.button1); // Ensure one button is always checked
    }

    private String getCurrentInterval() {
        int checkedId = toggleButtonGroup.getCheckedButtonId();
        if (checkedId == R.id.button2) {
            return "weekly";
        } else if (checkedId == R.id.button3) {
            return "monthly";
        } else {
            return "daily";
        }
    }

    private String formatUsageTime(long usageTime) {
        long hours = TimeUnit.MILLISECONDS.toHours(usageTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(usageTime) % 60;

        if (hours > 0) {
            return hours + " h" + (hours > 1 ? "s" : "") + (minutes > 0 ? " and " + minutes + " min" + (minutes > 1 ? "s" : "") : "");
        } else {
            return minutes + " min" + (minutes > 1 ? "s" : "");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
