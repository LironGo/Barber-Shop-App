package com.example.barbershop.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barbershop.MainActivity;
import com.example.barbershop.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.barbershop.util.UIHelper;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private static final int REGISTER_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        if (mAuth.getCurrentUser() != null) {
            startMainActivity();
        }

        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST_CODE);
        });
        binding.adminLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && 
                data.getBooleanExtra("REGISTRATION_SUCCESS", false)) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            }
            // The registration activity has finished, regardless of result
            binding.emailEditText.setText("");
            binding.passwordEditText.setText("");
        }
    }

    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Check if this user is an admin
                        String userId = mAuth.getCurrentUser().getUid();
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists() && 
                                    Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                                    // This is an admin user trying to login through customer login
                                    mAuth.signOut();
                                    UIHelper.showToast(LoginActivity.this, 
                                        "Shop owners must use the Shop Owner Login page");
                                } else {
                                    // Regular customer, proceed to main activity
                                    startMainActivity();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // If we can't verify, assume regular user
                                startMainActivity();
                            });
                    } else {
                        UIHelper.showToast(LoginActivity.this, 
                            "Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 