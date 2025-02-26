package com.example.barbershop.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.barbershop.MainActivity;
import com.example.barbershop.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String name = binding.nameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("name", name);
                    userProfile.put("phone", phone);
                    userProfile.put("email", email);
                    userProfile.put("isAdmin", false);

                    db.collection("users").document(user.getUid())
                        .set(userProfile)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("RegisterActivity", "User profile saved successfully");
                            binding.progressBar.setVisibility(View.GONE);
                            
                            // Show success message
                            Toast.makeText(RegisterActivity.this, 
                                "Registration successful!", 
                                Toast.LENGTH_SHORT).show();

                            // Go directly to MainActivity since user is already logged in
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, 
                                "Error creating profile: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                } else {
                    Log.e("RegisterActivity", "createUserWithEmail:failure", task.getException());
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, 
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
} 