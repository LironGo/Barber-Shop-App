package com.example.barbershop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.barbershop.databinding.ActivityMainBinding;
import com.example.barbershop.util.DatabaseInitializer;
import com.example.barbershop.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Setup navigation controller
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        
        // Check if user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, go to login screen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Check if current user is admin
        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
            .addOnSuccessListener(document -> {
                if (document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"))) {
                    isAdmin = true;
                    setupAdminNavigation();
                    
                    // Navigate to admin appointments by default
                    navController.navigate(R.id.navigation_appointments_admin);
                } else {
                    setupCustomerNavigation();
                    
                    // Navigate to services by default
                    navController.navigate(R.id.navigation_services);
                }
                
                // Initialize database if needed
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                boolean isInitialized = prefs.getBoolean("database_initialized", false);
                
                if (!isInitialized) {
                    DatabaseInitializer.initializeServices(success -> {
                        if (success) {
                            Log.d("MainActivity", "Services initialized successfully");
                        } else {
                            Log.e("MainActivity", "Failed to initialize services");
                        }
                    });
                    DatabaseInitializer.initializeBarberShops();
                    prefs.edit().putBoolean("database_initialized", true).apply();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                setupCustomerNavigation(); // Default to customer navigation
            });
    }

    private void setupAdminNavigation() {
        binding.bottomNavigation.getMenu().clear();
        binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu_admin);
        
        // Set up navigation controller with bottom navigation
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        
        // Add manual handling for the navigation items
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_appointments_admin) {
                navController.navigate(R.id.navigation_appointments_admin);
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                navController.navigate(R.id.navigation_schedule);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile);
                return true;
            }
            
            return false;
        });
    }

    private void setupCustomerNavigation() {
        binding.bottomNavigation.getMenu().clear();
        binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_menu);
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
            || super.onSupportNavigateUp();
    }
}