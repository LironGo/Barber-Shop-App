package com.example.barbershop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.barbershop.databinding.FragmentBusinessSettingsBinding;
import com.example.barbershop.model.WorkingHours;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessSettingsFragment extends Fragment {
    private FragmentBusinessSettingsBinding binding;
    private FirebaseFirestore db;
    private Map<String, WorkingHours> workingHours;
    private Map<String, Boolean> holidays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentBusinessSettingsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        
        setupWorkingHours();
        setupHolidays();
        loadSettings();
        
        return binding.getRoot();
    }

    private void setupWorkingHours() {
        // Implementation for working hours setup
    }

    private void setupHolidays() {
        // Implementation for holidays setup
    }

    private void loadSettings() {
        db.collection("businessSettings").document("schedule")
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    // Update UI with loaded settings
                }
            });
    }

    private void saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("workingHours", workingHours);
        settings.put("holidays", holidays);

        db.collection("businessSettings").document("schedule")
            .set(settings)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(getContext(), "Settings saved", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Error saving settings: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }
} 