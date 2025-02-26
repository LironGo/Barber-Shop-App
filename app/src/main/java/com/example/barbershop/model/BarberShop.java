package com.example.barbershop.model;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;

public class BarberShop {
    private String id;
    private String name;
    private String ownerName;
    private String ownerEmail;
    private String description;
    private Map<String, WorkingHours> workingHours;
    private List<Holiday> holidays;
    private String address;
    private String phone;
    private String imageUrl;

    public BarberShop() {
        // Required empty constructor for Firestore
        workingHours = new HashMap<>();
        holidays = new ArrayList<>();
    }

    public BarberShop(String id, String name, String ownerName, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.workingHours = new HashMap<>();
        this.holidays = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Map<String, WorkingHours> getWorkingHours() { return workingHours; }
    public void setWorkingHours(Map<String, WorkingHours> workingHours) { this.workingHours = workingHours; }

    public List<Holiday> getHolidays() { return holidays; }
    public void setHolidays(List<Holiday> holidays) { this.holidays = holidays; }

    public boolean isOpenAt(Date date) {
        // Check if it's a holiday
        for (Holiday holiday : holidays) {
            if (holiday.getDate().equals(date)) {
                return false;
            }
        }

        // Check working hours
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dayOfWeek = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        WorkingHours hours = workingHours.get(dayOfWeek);
        
        return hours != null && hours.isOpen();
    }

    private String getDayOfWeek(int day) {
        switch (day) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "";
        }
    }

    @Override
    public String toString() {
        // This is what will show in the Spinner
        return name;
    }
} 