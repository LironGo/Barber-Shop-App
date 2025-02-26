package com.example.barbershop.ui.appointments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.barbershop.R;
import com.example.barbershop.adapter.AppointmentsAdapter;
import com.example.barbershop.databinding.FragmentAppointmentsBinding;
import com.example.barbershop.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment implements AppointmentsAdapter.AppointmentCallback {
    private FragmentAppointmentsBinding binding;
    private FirebaseFirestore db;
    private AppointmentsAdapter adapter;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadAppointments();

        binding.bookAppointmentButton.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(
                AppointmentsFragmentDirections.actionAppointmentsToServices()
            )
        );

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AppointmentsAdapter(this, false);
        binding.appointmentsRecyclerView.setAdapter(adapter);
        binding.appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadAppointments() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Appointment> appointments = new ArrayList<>();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Appointment appointment = document.toObject(Appointment.class);
                    appointment.setId(document.getId());

                    // Calculate appointment end time
                    long appointmentStartTime = appointment.getAppointmentDate().getTime();
                    long appointmentEndTime = appointmentStartTime + (appointment.getServiceDuration() * 60 * 1000);
                    long currentTimeMillis = System.currentTimeMillis(); // Get current time

                    // Check if the appointment has ended but is not yet marked as "completed"
                    if (currentTimeMillis >= appointmentEndTime && !"completed".equalsIgnoreCase(appointment.getStatus())) {
                        db.collection("appointments")
                                .document(appointment.getId())
                                .update("status", "completed")
                                .addOnSuccessListener(aVoid -> {
                                    appointment.setStatus("completed"); // Update locally
                                    adapter.notifyDataSetChanged(); // Refresh UI
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure (optional logging)
                                });
                    }

                    appointments.add(appointment);
                }

                adapter.submitList(appointments);

                // Show empty state if no appointments
                if (appointments.isEmpty()) {
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                    binding.emptyStateText.setText("No appointments found");
                } else {
                    binding.emptyStateText.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                // Only show empty state, no toast
                if (binding != null) {
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                    binding.emptyStateText.setText("No appointments found");
                }
            });
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