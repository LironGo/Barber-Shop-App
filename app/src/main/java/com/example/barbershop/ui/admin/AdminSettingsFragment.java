package com.example.barbershop.ui.admin;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.barbershop.R;
import com.example.barbershop.databinding.FragmentAdminSettingsBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminSettingsFragment extends Fragment {
    private FragmentAdminSettingsBinding binding;
    private FirebaseFirestore db;
    private Map<String, WorkingHours> workingHours;
    private String breakStart = "";
    private String breakEnd = "";

    private static class WorkingHours {
        String start;
        String end;
        boolean isOpen;

        WorkingHours() {
            this.start = "09:00";
            this.end = "17:00";
            this.isOpen = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminSettingsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        workingHours = new HashMap<>();
        
        initializeWorkingHours();
        setupWorkingHoursViews();
        setupBreakTimeButtons();
        setupSaveButton();

        loadSettings();

        return binding.getRoot();
    }

    private void initializeWorkingHours() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            workingHours.put(day, new WorkingHours());
        }
    }

    private void setupWorkingHoursViews() {
        LinearLayout container = binding.workingHoursContainer;
        for (Map.Entry<String, WorkingHours> entry : workingHours.entrySet()) {
            View dayView = getLayoutInflater().inflate(R.layout.item_working_hours, container, false);
            
            TextView dayName = dayView.findViewById(R.id.dayName);
            Button startTime = dayView.findViewById(R.id.startTimeButton);
            Button endTime = dayView.findViewById(R.id.endTimeButton);
            
            dayName.setText(entry.getKey());
            startTime.setText(entry.getValue().start);
            endTime.setText(entry.getValue().end);

            setupTimeButton(startTime, time -> entry.getValue().start = time);
            setupTimeButton(endTime, time -> entry.getValue().end = time);

            container.addView(dayView);
        }
    }

    private void setupTimeButton(Button button, TimeSelectedCallback callback) {
        button.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, selectedMinute);
                    button.setText(time);
                    callback.onTimeSelected(time);
                },
                hour,
                minute,
                true
            );
            timePickerDialog.show();
        });
    }

    private void setupBreakTimeButtons() {
        setupTimeButton(binding.breakStartButton, time -> breakStart = time);
        setupTimeButton(binding.breakEndButton, time -> breakEnd = time);
    }

    private void setupSaveButton() {
        binding.saveSettingsButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        db.collection("settings").document("workingHours")
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    if (data != null) {
                        // Update UI with loaded settings
                        // Implementation details...
                    }
                }
            });
    }

    private void saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("workingHours", workingHours);
        settings.put("breakTime", new HashMap<String, String>() {{
            put("start", breakStart);
            put("end", breakEnd);
        }});

        db.collection("settings").document("workingHours")
            .set(settings)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(requireContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(requireContext(), "Error saving settings: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }

    private interface TimeSelectedCallback {
        void onTimeSelected(String time);
    }
} 