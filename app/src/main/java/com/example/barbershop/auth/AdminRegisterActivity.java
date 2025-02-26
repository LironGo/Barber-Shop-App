package com.example.barbershop.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.barbershop.databinding.ActivityAdminRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegisterActivity extends AppCompatActivity {
    private ActivityAdminRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Add back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.registerButton.setOnClickListener(v -> registerAdmin());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void registerAdmin() {
        String shopName = binding.shopNameEditText.getText().toString().trim();
        String ownerName = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        if (shopName.isEmpty() || ownerName.isEmpty() || email.isEmpty() || 
            password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Map<String, Object> adminProfile = new HashMap<>();
                    adminProfile.put("name", ownerName);
                    adminProfile.put("phone", phone);
                    adminProfile.put("email", email);
                    adminProfile.put("isAdmin", true);
                    adminProfile.put("shopName", shopName);

                    db.collection("users").document(user.getUid())
                        .set(adminProfile)
                        .addOnSuccessListener(aVoid -> {
                            mAuth.signOut();
                            showSuccessDialog();
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.registerButton.setEnabled(true);
                            Toast.makeText(AdminRegisterActivity.this, 
                                "Error creating profile: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(AdminRegisterActivity.this, 
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Success!")
            .setMessage("Shop owner registration successful. Please login to continue.")
            .setPositiveButton("Login Now", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }
} 