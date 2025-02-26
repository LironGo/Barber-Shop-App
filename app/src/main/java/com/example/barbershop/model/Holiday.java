package com.example.barbershop.model;

import java.util.Date;

public class Holiday {
    private Date date;
    private String name;
    private boolean fullDay;

    public Holiday() {
        // Required empty constructor for Firestore
    }

    public Holiday(Date date, String name, boolean fullDay) {
        this.date = date;
        this.name = name;
        this.fullDay = fullDay;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFullDay() {
        return fullDay;
    }

    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
    }
} 