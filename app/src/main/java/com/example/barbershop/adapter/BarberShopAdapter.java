package com.example.barbershop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.R;
import com.example.barbershop.model.BarberShop;
import com.example.barbershop.model.WorkingHours;

import java.util.List;
import java.util.Map;

public class BarberShopAdapter extends RecyclerView.Adapter<BarberShopAdapter.ViewHolder> {
    private List<BarberShop> barberShops;
    private OnBarberShopSelectedListener listener;

    public interface OnBarberShopSelectedListener {
        void onBarberShopSelected(BarberShop barberShop);
    }

    public BarberShopAdapter(List<BarberShop> barberShops, OnBarberShopSelectedListener listener) {
        this.barberShops = barberShops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_barber_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarberShop shop = barberShops.get(position);
        holder.bind(shop);
    }

    @Override
    public int getItemCount() {
        return barberShops.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView shopName;
        private TextView ownerName;
        private TextView workingHours;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            ownerName = itemView.findViewById(R.id.ownerName);
            workingHours = itemView.findViewById(R.id.workingHours);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBarberShopSelected(barberShops.get(position));
                }
            });
        }

        void bind(BarberShop shop) {
            shopName.setText(shop.getName());
            ownerName.setText("Owner: " + shop.getOwnerName());
            // Format working hours summary
            String hours = formatWorkingHours(shop.getWorkingHours());
            workingHours.setText(hours);
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