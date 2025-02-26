package com.example.barbershop.model;

public class TimeSlot {
    private int hour;
    private int minute;
    private boolean available;

    public TimeSlot(int hour, int minute, boolean available) {
        this.hour = hour;
        this.minute = minute;
        this.available = available;
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public boolean isAvailable() { return available; }
    
    public String getFormattedTime() {
        return String.format("%02d:%02d", hour, minute);
    }
} 