package com.example.barbershop.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.databinding.ItemSpecialDayBinding;
import com.example.barbershop.model.Holiday;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpecialDaysAdapter extends RecyclerView.Adapter<SpecialDaysAdapter.SpecialDayViewHolder> {
    private List<Holiday> specialDays = new ArrayList<>();
    private final SpecialDayCallback callback;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface SpecialDayCallback {
        void onDeleteSpecialDay(Holiday specialDay);
    }

    public SpecialDaysAdapter(SpecialDayCallback callback) {
        this.callback = callback;
    }

    public void submitList(List<Holiday> newSpecialDays) {
        this.specialDays = newSpecialDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpecialDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpecialDayBinding binding = ItemSpecialDayBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new SpecialDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialDayViewHolder holder, int position) {
        holder.bind(specialDays.get(position));
    }

    @Override
    public int getItemCount() {
        return specialDays.size();
    }

    class SpecialDayViewHolder extends RecyclerView.ViewHolder {
        private final ItemSpecialDayBinding binding;

        SpecialDayViewHolder(ItemSpecialDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Holiday holiday) {
            binding.dateTextView.setText(dateFormat.format(holiday.getDate()));
            binding.noteTextView.setText(holiday.getName());
            binding.fullDayTextView.setText("Day Off");
            binding.deleteButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onDeleteSpecialDay(holiday);
                }
            });
        }
    }
} 