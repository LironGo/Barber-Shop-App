package com.example.barbershop.model;

import java.util.Date;

public class Appointment {
    private String id;
    private String userId;
    private String serviceId;
    private String serviceName;
    private double servicePrice;
    private Date appointmentDate;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private Date createdAt;
    private int serviceDuration;
    private String shopId;
    private String shopName;

    public Appointment() {
        // Required empty constructor for Firestore
        this.createdAt = new Date();
    }

    public Appointment(String userId, String serviceId, String serviceName, 
                      Date appointmentDate, String status, String shopId, String shopName) {
        this.userId = userId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.shopId = shopId;
        this.shopName = shopName;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getServicePrice() { return servicePrice; }
    public void setServicePrice(double servicePrice) { this.servicePrice = servicePrice; }

    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(int serviceDuration) { this.serviceDuration = serviceDuration; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
} 