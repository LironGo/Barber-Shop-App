package com.example.barbershop.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.databinding.ItemServiceBinding;
import com.example.barbershop.model.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder> {
    private List<Service> services;
    private final OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServicesAdapter(OnServiceClickListener listener) {
        this.listener = listener;
        this.services = new ArrayList<>();
    }

    public void submitList(List<Service> newServices) {
        Log.d("ServicesAdapter", "Submitting list with " + (newServices != null ? newServices.size() : 0) + " services");
        if (newServices != null && !newServices.isEmpty()) {
            for (Service service : newServices) {
                Log.d("ServicesAdapter", "Service: " + service.getName() + ", ID: " + service.getId());
            }
            this.services = newServices;
        } else {
            this.services = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceBinding binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ServiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        
        holder.binding.serviceNameTextView.setText(service.getName());
        holder.binding.serviceDescriptionTextView.setText(service.getDescription());
        holder.binding.servicePriceTextView.setText(String.format(Locale.getDefault(), "$%.2f", service.getPrice()));
        holder.binding.serviceDurationTextView.setText(String.format(Locale.getDefault(), "%d min", service.getDuration()));
        
        // Load local drawable instead of URL
        holder.binding.serviceImage.setImageResource(service.getImageResource());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceBinding binding;

        ServiceViewHolder(ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 