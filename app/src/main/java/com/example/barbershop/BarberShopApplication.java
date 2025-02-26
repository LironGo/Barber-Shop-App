package com.example.barbershop;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BarberShopApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
} 