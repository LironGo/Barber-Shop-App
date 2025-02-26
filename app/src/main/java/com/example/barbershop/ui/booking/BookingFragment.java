package com.example.barbershop.ui.booking;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.barbershop.R;
import com.example.barbershop.databinding.FragmentBookingBinding;
import com.example.barbershop.model.Appointment;
import com.example.barbershop.model.BarberShop;
import com.example.barbershop.model.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class BookingFragment extends Fragment {
    private FragmentBookingBinding binding;
    private FirebaseFirestore db;
    private Service selectedService;
    private static final String[] BARBERS = {"John", "Mike", "Sarah", "Emma"};
    private Calendar selectedDateTime;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentBookingBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        selectedDateTime = Calendar.getInstance();

        // Get selected service from arguments
        if (getArguments() != null) {
            selectedService = (Service) getArguments().getSerializable("service");
            binding.serviceNameText.setText(selectedService.getName());
            binding.servicePriceText.setText(String.format("$%.2f", selectedService.getPrice()));
        }

        setupBarberSpinner();
        setupDateTimePickers();
        setupBookButton();

        return binding.getRoot();
    }

    private void setupBarberSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            BARBERS
        );
        binding.barberSpinner.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        binding.datePicker.setMinDate(System.currentTimeMillis());

        binding.datePicker.setOnDateChangedListener((view, year, month, day) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
        });

        binding.timePicker.setOnTimeChangedListener((view, hour, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedDateTime.set(Calendar.MINUTE, minute);
        });
    }

    private void setupBookButton() {
        binding.bookButton.setOnClickListener(v -> bookAppointment());
    }

    private void bookAppointment() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String barberName = (String) binding.barberSpinner.getSelectedItem();
        Date appointmentDateTime = selectedDateTime.getTime();

        // Get selected shop info
        BarberShop selectedShop = (BarberShop) binding.barberSpinner.getSelectedItem();
        if (selectedShop == null) {
            Toast.makeText(requireContext(), "Please select a shop", Toast.LENGTH_SHORT).show();
            return;
        }

        Appointment appointment = new Appointment(
            userId,
            selectedService.getId(),
            selectedService.getName(),
            appointmentDateTime,
            "scheduled",
            selectedShop.getId(),
            selectedShop.getName()
        );

        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(requireContext(), "Appointment booked successfully!", 
                    Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_booking_to_appointments);
            })
            .addOnFailureListener(e -> Toast.makeText(requireContext(),
                "Error booking appointment: " + e.getMessage(),
                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 