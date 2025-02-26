package com.example.barbershop.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.example.barbershop.R;

public class Service implements Parcelable {
    private String id;
    private String name;
    private String description;
    private double price;
    private int duration;

    // Required empty constructor for Firestore
    public Service() {}

    public Service(String name, String description, double price, int duration) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
    }

    protected Service(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        duration = in.readInt();
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getImageResource() {
        String serviceName = name.toLowerCase();
        Log.d("Service", "Getting image resource for: " + serviceName);
        
        if (serviceName.contains("haircut")) {
            return R.drawable.service_haircut;
        } else if (serviceName.contains("beard") && serviceName.contains("hair")) {
            return R.drawable.service_hair_beard;
        } else if (serviceName.contains("beard")) {
            return R.drawable.service_beard_trim;
        } else if (serviceName.contains("color")) {
            return R.drawable.service_coloring;
        } else if (serviceName.contains("shave")) {
            return R.drawable.service_shave;
        } else {
            return R.drawable.service_default;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeInt(duration);
    }
} 