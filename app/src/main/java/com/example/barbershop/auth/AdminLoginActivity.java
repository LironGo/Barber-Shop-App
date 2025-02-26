package com.example.barbershop.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barbershop.MainActivity;
import com.example.barbershop.databinding.ActivityAdminLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {
    private ActivityAdminLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int ADMIN_REGISTER_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Add back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.adminLoginButton.setOnClickListener(v -> loginAdmin());
        binding.registerAsOwnerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, AdminRegisterActivity.class);
            startActivityForResult(intent, ADMIN_REGISTER_REQUEST_CODE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADMIN_REGISTER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && 
                data.getBooleanExtra("REGISTRATION_SUCCESS", false)) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            }
            // The registration activity has finished, regardless of result
            binding.adminEmailEditText.setText("");
            binding.adminPasswordEditText.setText("");
        }
    }

    private void loginAdmin() {
        String email = binding.adminEmailEditText.getText().toString().trim();
        String password = binding.adminPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.adminLoginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Verify if user is admin
                    String userId = task.getResult().getUser().getUid();
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"))) {
                                // User is admin, proceed to MainActivity
                                Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Not an admin user
                                mAuth.signOut();
                                binding.progressBar.setVisibility(View.GONE);
                                binding.adminLoginButton.setEnabled(true);
                                Toast.makeText(AdminLoginActivity.this, 
                                    "Access denied: Not a shop owner account", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            mAuth.signOut();
                            binding.progressBar.setVisibility(View.GONE);
                            binding.adminLoginButton.setEnabled(true);
                            Toast.makeText(AdminLoginActivity.this, 
                                "Error verifying admin status: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.adminLoginButton.setEnabled(true);
                    Toast.makeText(AdminLoginActivity.this, 
                        "Login failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
} 