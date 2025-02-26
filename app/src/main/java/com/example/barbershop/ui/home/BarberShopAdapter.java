package com.example.barbershop.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.barbershop.databinding.ItemBarberShopBinding;
import com.example.barbershop.model.BarberShop;
import com.example.barbershop.model.WorkingHours;

import java.util.List;
import java.util.Map;

public class BarberShopAdapter extends RecyclerView.Adapter<BarberShopAdapter.ViewHolder> {
    private final List<BarberShop> barberShops;
    private final OnBarberShopClickListener listener;

    public interface OnBarberShopClickListener {
        void onBarberShopClick(BarberShop barberShop);
    }

    public BarberShopAdapter(List<BarberShop> barberShops, OnBarberShopClickListener listener) {
        this.barberShops = barberShops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBarberShopBinding binding = ItemBarberShopBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(barberShops.get(position));
    }

    @Override
    public int getItemCount() {
        return barberShops.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBarberShopBinding binding;

        ViewHolder(ItemBarberShopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBarberShopClick(barberShops.get(position));
                }
            });
        }

        void bind(BarberShop barberShop) {
            binding.shopName.setText(barberShop.getName());
            binding.ownerName.setText("Owner: " + barberShop.getOwnerName());
            binding.workingHours.setText(formatWorkingHours(barberShop.getWorkingHours()));
        }
    }

    private String formatWorkingHours(Map<String, WorkingHours> workingHours) {
        if (workingHours == null || workingHours.isEmpty()) {
            return "Working hours not set";
        }

        StringBuilder sb = new StringBuilder();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        for (String day : days) {
            WorkingHours hours = workingHours.get(day);
            if (hours != null && hours.isOpen()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(day).append(": ")
                  .append(hours.getOpenTime())
                  .append(" - ")
                  .append(hours.getCloseTime());
            }
        }

        return sb.length() > 0 ? sb.toString() : "Closed";
    }
} 