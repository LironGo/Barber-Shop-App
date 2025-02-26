package com.example.barbershop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.barbershop.databinding.FragmentHomeBinding;
import com.example.barbershop.model.BarberShop;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements BarberShopAdapter.OnBarberShopClickListener {
    private FragmentHomeBinding binding;
    private BarberShopAdapter adapter;
    private FirebaseFirestore db;
    private List<BarberShop> barberShops;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        barberShops = new ArrayList<>();
        
        setupRecyclerView();
        loadBarberShops();

        return root;
    }

    private void setupRecyclerView() {
        adapter = new BarberShopAdapter(barberShops, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadBarberShops() {
        db.collection("users")
            .whereEqualTo("isAdmin", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                barberShops.clear();
                queryDocumentSnapshots.forEach(document -> {
                    BarberShop shop = document.toObject(BarberShop.class);
                    if (shop != null) {
                        shop.setId(document.getId());
                        barberShops.add(shop);
                    }
                });
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                    "Error loading barber shops: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onBarberShopClick(BarberShop barberShop) {
        // Handle barber shop selection
        Toast.makeText(getContext(), 
            "Selected: " + barberShop.getName(), 
            Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 