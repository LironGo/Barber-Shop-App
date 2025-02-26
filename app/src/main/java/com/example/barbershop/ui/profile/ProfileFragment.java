package com.example.barbershop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.barbershop.auth.LoginActivity;
import com.example.barbershop.databinding.FragmentProfileBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is admin
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                boolean isAdmin = Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"));
                
                // Show shop name field for admins
                binding.shopNameLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                
                // Load user data
                loadUserData();
            });

        // Setup buttons
        binding.saveButton.setOnClickListener(v -> saveProfile());
        binding.logoutButton.setOnClickListener(v -> logout());
        binding.deleteAccountButton.setOnClickListener(v -> showDeleteConfirmation());

        return root;
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    binding.nameEditText.setText(documentSnapshot.getString("name"));
                    binding.emailEditText.setText(documentSnapshot.getString("email"));
                    binding.phoneEditText.setText(documentSnapshot.getString("phone"));
                    
                    // Load shop name for admin users
                    if (documentSnapshot.getBoolean("isAdmin") != null && 
                        documentSnapshot.getBoolean("isAdmin")) {
                        binding.shopNameEditText.setText(documentSnapshot.getString("shopName"));
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), 
                    "Error loading profile: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }

    private void saveProfile() {
        String name = binding.nameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            binding.nameEditText.setError("Name is required");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        
        // Check if user is admin and save shop name
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && 
                    Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                    
                    String shopName = binding.shopNameEditText.getText().toString().trim();
                    if (shopName.isEmpty()) {
                        binding.shopNameEditText.setError("Shop name is required");
                        return;
                    }
                    
                    updates.put("shopName", shopName);
                    
                    // Also update the barbershop document
                    Map<String, Object> shopUpdates = new HashMap<>();
                    shopUpdates.put("name", shopName);
                    
                    db.collection("barbershops").document(userId)
                        .set(shopUpdates, SetOptions.merge());
                }
                
                // Save user profile updates
                db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> 
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(getContext(), 
                            "Error updating profile: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show());
            });
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                showPasswordConfirmationDialog();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showPasswordConfirmationDialog() {
        EditText passwordInput = new EditText(requireContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");

        new AlertDialog.Builder(requireContext())
            .setTitle("Confirm Password")
            .setMessage("Please enter your password to confirm account deletion")
            .setView(passwordInput)
            .setPositiveButton("Confirm", (dialog, which) -> {
                String password = passwordInput.getText().toString();
                if (!password.isEmpty()) {
                    deleteAccount(password);
                } else {
                    Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), password);
            user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    // Delete user data from Firestore
                    String userId = user.getUid();
                    db.collection("users").document(userId).delete()
                        .addOnSuccessListener(aVoid -> {
                            // Delete user appointments
                            db.collection("appointments")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                        doc.getReference().delete();
                                    }
                                    
                                    // Finally delete the Firebase Auth account
                                    user.delete()
                                        .addOnSuccessListener(result -> {
                                            Toast.makeText(getContext(),
                                                "Account deleted successfully",
                                                Toast.LENGTH_SHORT).show();
                                            
                                            // Only navigate after everything is deleted
                                            auth.signOut();
                                            Intent loginIntent = new Intent(requireContext(), LoginActivity.class);
                                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(loginIntent);
                                            requireActivity().finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(),
                                            "Error deleting account: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                                });
                        });
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                    "Authentication failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 