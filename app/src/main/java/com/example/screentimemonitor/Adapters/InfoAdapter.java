package com.example.screentimemonitor.Adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.screentimemonitor.R;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {

    private final Context context;
    private final List<Map.Entry<String, Long>> appUsageList;
    private final PackageManager packageManager;

    public InfoAdapter(Context context, List<Map.Entry<String, Long>> appUsageList) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.appUsageList = appUsageList.stream()
                .filter(entry -> entry.getValue() >= TimeUnit.MINUTES.toMillis(1))
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // Sorting in descending order
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horisontal_info_item, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        Map.Entry<String, Long> entry = appUsageList.get(position);
        String packageName = entry.getKey();
        long usageTime = entry.getValue();

        holder.nameTextView.setText(getAppNameFromPackage(packageName));
        holder.timeTextView.setText(formatUsageTime(usageTime));
        holder.iconImageView.setImageDrawable(getAppIconFromPackage(packageName));
    }

    @Override
    public int getItemCount() {
        return appUsageList.size();
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView timeTextView;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.item_IMG_icon);
            nameTextView = itemView.findViewById(R.id.item_LBL_name);
            timeTextView = itemView.findViewById(R.id.item_LBL_time);
        }
    }

    private String getAppNameFromPackage(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return packageName; // Fallback to package name if app name is not found
        }
    }

    private Drawable getAppIconFromPackage(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationIcon(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return context.getDrawable(R.drawable.ic_launcher_foreground);
        }
    }

    private String formatUsageTime(long usageTime) {
        long hours = TimeUnit.MILLISECONDS.toHours(usageTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(usageTime) % 60;

        String hoursText = hours > 0 ? context.getString(R.string.hours_format, hours) : "";
        String minutesText = minutes > 0 ? context.getString(R.string.minutes_format, minutes) : "";

        if (hours > 0 && minutes > 0) {
            return hoursText + " " + context.getString(R.string.and) + " " + minutesText;
        } else if (hours > 0) {
            return hoursText;
        } else {
            return minutesText;
        }
    }

}
