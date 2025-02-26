package com.example.barbershop.ui.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.barbershop.R;
import com.example.barbershop.adapter.SpecialDaysAdapter;
import com.example.barbershop.databinding.FragmentScheduleManagementBinding;
import com.example.barbershop.model.Holiday;
import com.example.barbershop.model.WorkingHours;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.text.SimpleDateFormat;

public class ScheduleManagementFragment extends Fragment {
    private FragmentScheduleManagementBinding binding;
    private FirebaseFirestore db;
    private Map<String, WorkingHours> workingHours;
    private List<Holiday> specialDays;
    private SpecialDaysAdapter specialDaysAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentScheduleManagementBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        
        // Initialize working hours
        workingHours = new HashMap<>();
        specialDays = new ArrayList<>();
        
        // First load existing schedule, then setup UI
        loadSchedule();
        
        // Setup save button
        binding.saveButton.setOnClickListener(v -> saveSchedule());
        
        // Setup add special day button
        binding.addSpecialDayButton.setOnClickListener(v -> showAddSpecialDayDialog());
        
        // Setup break time
        setupBreakTime();
        
        return binding.getRoot();
    }

    private void loadSchedule() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Set correct button text
        binding.saveButton.setText("Save Schedule");
        
        String shopId = getCurrentUserShopId();
        
        db.collection("barbershops").document(shopId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                binding.progressBar.setVisibility(View.GONE);
                
                if (documentSnapshot.exists()) {
                    // Get working hours
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null && data.containsKey("workingHours")) {
                        Map<String, Object> hoursData = (Map<String, Object>) data.get("workingHours");
                        
                        for (Map.Entry<String, Object> entry : hoursData.entrySet()) {
                            String day = entry.getKey();
                            Map<String, Object> hourData = (Map<String, Object>) entry.getValue();
                            
                            WorkingHours hours = new WorkingHours();
                            hours.setOpenTime((String) hourData.get("openTime"));
                            hours.setCloseTime((String) hourData.get("closeTime"));
                            hours.setClosed((Boolean) hourData.get("closed"));
                            
                            workingHours.put(day, hours);
                        }
                    }
                    
                    // Get break time
                    if (data != null && data.containsKey("breakTime")) {
                        Map<String, Object> breakTimeData = (Map<String, Object>) data.get("breakTime");
                        boolean isBreakEnabled = (boolean) breakTimeData.getOrDefault("enabled", false);
                        
                        binding.breakTimeSwitch.setChecked(isBreakEnabled);
                        binding.breakTimeContainer.setVisibility(isBreakEnabled ? View.VISIBLE : View.GONE);
                        
                        if (isBreakEnabled) {
                            String breakStartTime = (String) breakTimeData.get("startTime");
                            String breakEndTime = (String) breakTimeData.get("endTime");
                            
                            if (breakStartTime != null) {
                                binding.breakStartButton.setText(breakStartTime);
                            }
                            
                            if (breakEndTime != null) {
                                binding.breakEndButton.setText(breakEndTime);
                            }
                        }
                    }
                    
                    // Get special days
                    if (data != null && data.containsKey("specialDays")) {
                        List<Map<String, Object>> specialDaysData = 
                            (List<Map<String, Object>>) data.get("specialDays");
                        
                        specialDays.clear();
                        for (Map<String, Object> specialDayData : specialDaysData) {
                            Holiday holiday = new Holiday();
                            holiday.setName((String) specialDayData.get("name"));
                            
                            // Handle Timestamp or Date
                            Object dateObj = specialDayData.get("date");
                            if (dateObj instanceof com.google.firebase.Timestamp) {
                                holiday.setDate(((com.google.firebase.Timestamp) dateObj).toDate());
                            } else if (dateObj instanceof Date) {
                                holiday.setDate((Date) dateObj);
                            }
                            
                            holiday.setFullDay(true);  // Always full day
                            specialDays.add(holiday);
                        }
                    }
                    
                    // Now setup UI with loaded data
                    setupWorkingHours();
                    setupSpecialDays();
                } else {
                    // No existing schedule, create default
                    setupDefaultSchedule();
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), 
                    "Error loading schedule: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                
                // Setup default on failure
                setupDefaultSchedule();
            });
    }

    private void setupDefaultSchedule() {
        // Setup with default values
        setupWorkingHours();
        setupSpecialDays();
    }

    private void setupWorkingHours() {
        binding.workingHoursContainer.removeAllViews();
        
        // Put Sunday first in the array
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (String day : days) {
            // Get existing hours or create default
            WorkingHours hours = workingHours.get(day);
            if (hours == null) {
                hours = new WorkingHours();
                hours.setOpenTime("09:00");
                hours.setCloseTime("17:00");
                hours.setClosed(day.equals("Saturday")); // Closed on Saturday by default
                workingHours.put(day, hours);
            }
            
            // Add day to the container
            addDayToContainer(day, hours);
        }
    }

    private void setupBreakTime() {
        // Set default times if not set
        if (binding.breakStartButton.getText().toString().isEmpty()) {
            binding.breakStartButton.setText("12:00");
        }
        
        if (binding.breakEndButton.getText().toString().isEmpty()) {
            binding.breakEndButton.setText("13:00");
        }
        
        binding.breakTimeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.breakTimeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.breakStartButton.setOnClickListener(v -> {
            showTimePicker(binding.breakStartButton.getText().toString(), (hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                binding.breakStartButton.setText(time);
            });
        });
        
        binding.breakEndButton.setOnClickListener(v -> {
            showTimePicker(binding.breakEndButton.getText().toString(), (hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                binding.breakEndButton.setText(time);
            });
        });
    }

    private void showTimePicker(String currentTime, TimeSelectedCallback callback) {
        int hour = 9;
        int minute = 0;
        
        if (currentTime != null && !currentTime.isEmpty()) {
            String[] parts = currentTime.split(":");
            if (parts.length == 2) {
                try {
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    // Use defaults
                }
            }
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, selectedMinute) -> {
                if (callback != null) {
                    callback.onTimeSelected(hourOfDay, selectedMinute);
                }
            },
            hour,
            minute,
            true
        );
        
        timePickerDialog.show();
    }

    private class WorkingHoursViewHolder {
        private final TextView dayNameTextView;
        private final SwitchMaterial isOpenSwitch;
        private final MaterialButton openTimeButton;
        private final MaterialButton closeTimeButton;
        private final View timeContainer;

        WorkingHoursViewHolder(View itemView) {
            dayNameTextView = itemView.findViewById(R.id.dayNameTextView);
            isOpenSwitch = itemView.findViewById(R.id.isOpenSwitch);
            openTimeButton = itemView.findViewById(R.id.openTimeButton);
            closeTimeButton = itemView.findViewById(R.id.closeTimeButton);
            timeContainer = itemView.findViewById(R.id.timeContainer);

            isOpenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                timeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (workingHours.containsKey(dayNameTextView.getText().toString())) {
                    Objects.requireNonNull(workingHours.get(dayNameTextView.getText().toString())).setOpen(isChecked);
                }
            });

            openTimeButton.setOnClickListener(v -> showDayTimePickerDialog(true, dayNameTextView.getText().toString()));
            closeTimeButton.setOnClickListener(v -> showDayTimePickerDialog(false, dayNameTextView.getText().toString()));
        }

        void bind(String day) {
            dayNameTextView.setText(day);
            WorkingHours hours = workingHours.getOrDefault(day, new WorkingHours(day));
            workingHours.put(day, hours);
            
            isOpenSwitch.setChecked(hours.isOpen());
            timeContainer.setVisibility(hours.isOpen() ? View.VISIBLE : View.GONE);
            openTimeButton.setText(hours.getOpenTime());
            closeTimeButton.setText(hours.getCloseTime());
        }
    }

    private void showDayTimePickerDialog(boolean isOpenTime, String day) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                WorkingHours hours = workingHours.get(day);
                if (hours != null) {
                    if (isOpenTime) {
                        hours.setOpenTime(time);
                    } else {
                        hours.setCloseTime(time);
                    }
                }
                loadWorkingHours();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupSpecialDays() {
        // Initialize RecyclerView
        binding.specialDaysRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Create adapter with callback
        specialDaysAdapter = new SpecialDaysAdapter(new SpecialDaysAdapter.SpecialDayCallback() {
            @Override
            public void onDeleteSpecialDay(Holiday holiday) {
                // Remove from list
                specialDays.remove(holiday);
                specialDaysAdapter.submitList(specialDays);
                
                // Remove from database
                String shopId = getCurrentUserShopId();
                Map<String, Object> holidayData = new HashMap<>();
                holidayData.put("name", holiday.getName());
                holidayData.put("date", holiday.getDate());
                
                db.collection("barbershops").document(shopId)
                    .update("specialDays", FieldValue.arrayRemove(holidayData))
                    .addOnSuccessListener(aVoid -> 
                        Toast.makeText(requireContext(), "Special day removed", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(requireContext(), 
                            "Error removing special day: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show());
            }
        });
        
        // Set adapter and submit list
        binding.specialDaysRecyclerView.setAdapter(specialDaysAdapter);
        specialDaysAdapter.submitList(specialDays);
    }

    private void showAddSpecialDayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Special Day");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_special_day, null);
        builder.setView(dialogView);
        
        EditText nameEditText = dialogView.findViewById(R.id.specialDayNameEditText);
        Button dateButton = dialogView.findViewById(R.id.dateButton);
        
        // Set up date picker
        final Calendar[] selectedDate = {Calendar.getInstance()};
        dateButton.setText(formatDate(selectedDate[0].getTime()));
        
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate[0].set(year, month, dayOfMonth);
                    dateButton.setText(formatDate(selectedDate[0].getTime()));
                },
                selectedDate[0].get(Calendar.YEAR),
                selectedDate[0].get(Calendar.MONTH),
                selectedDate[0].get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Holiday holiday = new Holiday();
            holiday.setName(name);
            holiday.setDate(selectedDate[0].getTime());
            
            // Add to list and update UI
            specialDays.add(holiday);
            specialDaysAdapter.notifyDataSetChanged();
            
            // Save to database
            saveSpecialDay(holiday);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveSpecialDay(Holiday holiday) {
        String shopId = getCurrentUserShopId();
        
        Map<String, Object> holidayData = new HashMap<>();
        holidayData.put("name", holiday.getName());
        holidayData.put("date", holiday.getDate());
        
        db.collection("barbershops").document(shopId)
            .update("specialDays", FieldValue.arrayUnion(holidayData))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Special day added successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error adding special day: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadWorkingHours() {
        for (int i = 0; i < binding.workingHoursContainer.getChildCount(); i++) {
            View child = binding.workingHoursContainer.getChildAt(i);
            WorkingHoursViewHolder holder = new WorkingHoursViewHolder(child);
            String day = ((TextView) child.findViewById(R.id.dayNameTextView)).getText().toString();
            holder.bind(day);
        }
    }

    private void saveSchedule() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveButton.setEnabled(false);
        
        String shopId = getCurrentUserShopId();
        
        // Get shop name from user profile
        db.collection("users").document(shopId)
            .get()
            .addOnSuccessListener(userDoc -> {
                String shopName = userDoc.getString("shopName");
                if (shopName == null || shopName.isEmpty()) {
                    shopName = "My Barber Shop"; // Default name
                }
                
                // Get break time settings
                Map<String, Object> breakTime = new HashMap<>();
                if (binding.breakTimeSwitch.isChecked()) {
                    breakTime.put("enabled", true);
                    breakTime.put("startTime", binding.breakStartButton.getText().toString());
                    breakTime.put("endTime", binding.breakEndButton.getText().toString());
                } else {
                    breakTime.put("enabled", false);
                }
                
                // Create shop data
                Map<String, Object> shopData = new HashMap<>();
                shopData.put("id", shopId);
                shopData.put("name", shopName);
                shopData.put("workingHours", workingHours);
                shopData.put("breakTime", breakTime);
                shopData.put("specialDays", specialDays);
                
                db.collection("barbershops").document(shopId)
                    .set(shopData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.saveButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Schedule saved successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.saveButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Error saving schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.saveButton.setEnabled(true);
                Toast.makeText(requireContext(), "Error loading user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private String getCurrentUserShopId() {
        // For now, using userId as shopId. You might want to fetch the actual shopId from the user's profile
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void addDayToContainer(String day, WorkingHours hours) {
        View dayView = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_working_hours_admin, binding.workingHoursContainer, false);
        
        TextView dayNameTextView = dayView.findViewById(R.id.dayNameTextView);
        SwitchMaterial openSwitch = dayView.findViewById(R.id.isOpenSwitch);
        MaterialButton startTimeButton = dayView.findViewById(R.id.openTimeButton);
        MaterialButton endTimeButton = dayView.findViewById(R.id.closeTimeButton);
        
        dayNameTextView.setText(day);
        openSwitch.setChecked(!hours.isClosed());
        startTimeButton.setText(hours.getOpenTime());
        endTimeButton.setText(hours.getCloseTime());
        
        // Set up time pickers
        startTimeButton.setOnClickListener(v -> {
            showTimePicker(hours.getOpenTime(), new TimeSelectedCallback() {
                @Override
                public void onTimeSelected(int hourOfDay, int minute) {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    hours.setOpenTime(time);
                    startTimeButton.setText(time);
                }
            });
        });
        
        endTimeButton.setOnClickListener(v -> {
            showTimePicker(hours.getCloseTime(), new TimeSelectedCallback() {
                @Override
                public void onTimeSelected(int hourOfDay, int minute) {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    hours.setCloseTime(time);
                    endTimeButton.setText(time);
                }
            });
        });
        
        // Set up open/closed switch
        openSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hours.setClosed(!isChecked);
            startTimeButton.setEnabled(isChecked);
            endTimeButton.setEnabled(isChecked);
        });
        
        // Update UI based on current state
        boolean isOpen = !hours.isClosed();
        startTimeButton.setEnabled(isOpen);
        endTimeButton.setEnabled(isOpen);
        
        binding.workingHoursContainer.addView(dayView);
    }

    // Add this interface for the time picker callback
    private interface TimeSelectedCallback {
        void onTimeSelected(int hourOfDay, int minute);
    }

    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }
} 