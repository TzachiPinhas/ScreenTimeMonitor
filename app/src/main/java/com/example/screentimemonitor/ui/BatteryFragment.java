package com.example.screentimemonitor.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.screentimemonitor.R;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;
import com.example.screentimemonitor.databinding.FragmentBatteryBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatteryFragment extends Fragment {

    private FragmentBatteryBinding binding;
    private BarChart barChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBatteryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        barChart = binding.idBarChart;

        setupBarChart();
        displayCurrentBatteryLevel();
        loadBatteryData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(10f);
        leftAxis.setGranularity(20f);
        leftAxis.setGranularity(30f);
        leftAxis.setGranularity(40f);
        leftAxis.setGranularity(50f);
        leftAxis.setGranularity(60f);
        leftAxis.setGranularity(70f);
        leftAxis.setGranularity(80f);
        leftAxis.setGranularity(90f);
        leftAxis.setLabelCount(11, true);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false); // Disable the legend
    }

    private void displayCurrentBatteryLevel() {
        Context context = getContext();
        if (context != null) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) (level / (float) scale * 100);
                binding.batteryLevel.setText("Battery Level: " + batteryPct + "%");
            }
        }
    }

    private void loadBatteryData() {
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        List<Map.Entry<Long, Integer>> batteryData = spManager.getBatteryData();

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<Long, Integer> entry : batteryData) {
            barEntries.add(new BarEntry(index, entry.getValue()));
            labels.add(formatTime(entry.getKey()));
            index++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Battery Level");
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : barEntries) {
            if (entry.getY() < 50) {
                colors.add(ContextCompat.getColor(getContext(), R.color.red));
            } else {
                colors.add(ContextCompat.getColor(getContext(), R.color.green));
            }
        }
        barDataSet.setColors(colors);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);
        barData.setDrawValues(false); // Disable values on top of bars

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.invalidate(); // refresh
    }

    private String formatTime(long timestamp) {
        // Format timestamp to a readable time, e.g., "14:00"
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
