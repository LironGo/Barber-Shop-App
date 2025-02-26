package com.example.barbershop.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.databinding.ItemHolidayBinding;
import com.example.barbershop.model.Holiday;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HolidaysAdapter extends RecyclerView.Adapter<HolidaysAdapter.HolidayViewHolder> {
    private List<Holiday> holidays = new ArrayList<>();
    private final HolidayCallback callback;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface HolidayCallback {
        void onDeleteHoliday(Holiday holiday);
    }

    public HolidaysAdapter(HolidayCallback callback) {
        this.callback = callback;
    }

    public void submitList(List<Holiday> newHolidays) {
        this.holidays = newHolidays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolidayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHolidayBinding binding = ItemHolidayBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new HolidayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolidayViewHolder holder, int position) {
        holder.bind(holidays.get(position));
    }

    @Override
    public int getItemCount() {
        return holidays.size();
    }

    class HolidayViewHolder extends RecyclerView.ViewHolder {
        private final ItemHolidayBinding binding;

        HolidayViewHolder(ItemHolidayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Holiday holiday) {
            binding.holidayDateTextView.setText(dateFormat.format(holiday.getDate()));
            binding.holidayNoteTextView.setText(holiday.getName());
            
            binding.deleteButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onDeleteHoliday(holiday);
                }
            });
        }
    }
} 