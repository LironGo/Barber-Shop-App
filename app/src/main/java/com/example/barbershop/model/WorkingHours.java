package com.example.barbershop.model;

public class WorkingHours {
    private String openTime;
    private String closeTime;
    private boolean isClosed;

    public WorkingHours() {
        // Empty constructor needed for Firestore
    }

    public WorkingHours(String day) {
        this.openTime = "09:00";
        this.closeTime = "17:00";
        this.isClosed = false;
    }

    public WorkingHours(String openTime, String closeTime, boolean isClosed) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
    }

    public boolean isOpen() {
        return !isClosed;
    }

    public void setOpen(boolean open) {
        this.isClosed = !open;
    }

    // Getters and setters
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }
} 