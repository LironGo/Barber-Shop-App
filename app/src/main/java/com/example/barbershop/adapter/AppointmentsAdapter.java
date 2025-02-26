package com.example.barbershop.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.databinding.ItemAppointmentBinding;
import com.example.barbershop.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder> {
    private List<Appointment> appointments = new ArrayList<>();
    private final AppointmentCallback callback;
    private final boolean isAdmin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface AppointmentCallback {
        void onDeleteAppointment(Appointment appointment);
    }

    public AppointmentsAdapter(AppointmentCallback callback, boolean isAdmin) {
        this.callback = callback;
        this.isAdmin = isAdmin;
    }

    public void submitList(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentBinding binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        
        // Set appointment details
        holder.binding.serviceNameTextView.setText(appointment.getServiceName());
        holder.binding.shopNameTextView.setText(appointment.getShopName());
        
        // Format date
        holder.binding.dateTimeTextView.setText(dateFormat.format(appointment.getAppointmentDate()));
        
        // Set status with appropriate color
        String status = appointment.getStatus();
        holder.binding.statusTextView.setText(status);
        
        // Set status color
        int statusColor;
        switch (status.toLowerCase()) {
            case "confirmed":
                statusColor = Color.GREEN;
                break;
            case "rejected":
                statusColor = Color.RED;
                break;
            case "pending":
                statusColor = Color.BLUE;
                break;
            default:
                statusColor = Color.BLACK;
        }
        holder.binding.statusTextView.setTextColor(statusColor);
        
        // Hide accept/reject buttons for everyone
        holder.binding.acceptButton.setVisibility(View.GONE);
        holder.binding.rejectButton.setVisibility(View.GONE);
        
        // Show delete button for both admin and user
        holder.binding.cancelButton.setText("Delete");
        holder.binding.cancelButton.setVisibility(View.VISIBLE);
        
        // Set delete button click listener
        holder.binding.cancelButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onDeleteAppointment(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppointmentBinding binding;

        AppointmentViewHolder(ItemAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 