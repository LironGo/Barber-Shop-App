package com.example.barbershop.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.barbershop.adapter.AppointmentsAdapter;
import com.example.barbershop.databinding.FragmentAdminAppointmentsBinding;
import com.example.barbershop.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAppointmentsFragment extends Fragment implements AppointmentsAdapter.AppointmentCallback {
    private FragmentAdminAppointmentsBinding binding;
    private FirebaseFirestore db;
    private AppointmentsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentAdminAppointmentsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        
        // Setup RecyclerView
        adapter = new AppointmentsAdapter(this, true);
        binding.appointmentsRecyclerView.setAdapter(adapter);
        binding.appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Load appointments
        loadAppointments();
        
        return binding.getRoot();
    }

    private void loadAppointments() {
        String shopId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        db.collection("appointments")
            .whereEqualTo("shopId", shopId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Appointment> appointments = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Appointment appointment = document.toObject(Appointment.class);
                    appointment.setId(document.getId());
                    appointments.add(appointment);
                }
                
                adapter.submitList(appointments);
                
                // Show empty state if needed
                if (appointments.isEmpty()) {
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyStateText.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), 
                    "Error loading appointments", 
                    Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteAppointment(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Appointment")
            .setMessage("Are you sure you want to delete this appointment?")
            .setPositiveButton("Yes", (dialog, which) -> {
                db.collection("appointments").document(appointment.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(getContext(), "Error deleting appointment", Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 