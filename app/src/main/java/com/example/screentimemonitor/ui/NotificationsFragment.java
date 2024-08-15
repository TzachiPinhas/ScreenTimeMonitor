package com.example.screentimemonitor.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.screentimemonitor.R;
import com.example.screentimemonitor.Utilities.SharedPreferencesManager;
import com.example.screentimemonitor.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private EditText editTextHours;
    private EditText editTextMinutes;
    private Button buttonSetTimeLimit;
    private TextView textCurrentLimit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        findViews(root);
        SharedPreferencesManager spManager = SharedPreferencesManager.getInstance();
        displayCurrentLimit(spManager);

        buttonSetTimeLimit.setOnClickListener(v -> { // Set the time limit
            if (editTextHours.getText().toString().isEmpty() || editTextMinutes.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), R.string.enter_hours_and_minutes, Toast.LENGTH_SHORT).show();
                return;
            }

            int hours = Integer.parseInt(editTextHours.getText().toString());
            int minutes = Integer.parseInt(editTextMinutes.getText().toString());
            int totalTimeLimit = (hours * 60) + minutes;

            if (hours > 24 || minutes > 59) { // Check if the time limit is valid
                Toast.makeText(getContext(), R.string.enter_valid_time, Toast.LENGTH_SHORT).show();
                return;
            }

            spManager.saveUsageTimeLimit(totalTimeLimit);// Save the time limit
            spManager.setNotificationSent(false); // Reset notification flag

            Toast.makeText(getContext(), R.string.time_limit_updated, Toast.LENGTH_SHORT).show();
            displayCurrentLimit(spManager);

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        return root;
    }

    private void displayCurrentLimit(SharedPreferencesManager spManager) { // Display the current time limit
        int totalTimeLimit = spManager.getUsageTimeLimit(); // Get the usage time limit
        int hours = totalTimeLimit / 60;
        int minutes = totalTimeLimit % 60;
        textCurrentLimit.setText(getString(R.string.current_limit, hours, minutes));
        editTextHours.setText(String.valueOf(hours));
        editTextMinutes.setText(String.valueOf(minutes));
    }

    private void findViews(View root) {
        editTextHours = binding.editTextHours;
        editTextMinutes = binding.editTextMinutes;
        buttonSetTimeLimit = binding.buttonSetTimeLimit;
        textCurrentLimit = binding.textCurrentLimit;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
