package com.example.barbershop.ui.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.barbershop.R;
import com.example.barbershop.adapter.BarberShopAdapter;
import com.example.barbershop.databinding.FragmentServiceDetailsBinding;
import com.example.barbershop.model.BarberShop;
import com.example.barbershop.model.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceDetailsFragment extends Fragment implements BarberShopAdapter.OnBarberShopSelectedListener {
    private FragmentServiceDetailsBinding binding;
    private FirebaseFirestore db;
    private BarberShopAdapter adapter;
    private List<BarberShop> barberShops;
    private Service currentService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false);
        
        db = FirebaseFirestore.getInstance();
        barberShops = new ArrayList<>();
        adapter = new BarberShopAdapter(barberShops, this);
        
        binding.barberShopsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.barberShopsRecyclerView.setAdapter(adapter);

        String serviceId = getArguments().getString("serviceId");
        loadServiceDetails(serviceId);
        loadBarberShops();

        return binding.getRoot();
    }

    private void loadServiceDetails(String serviceId) {
        db.collection("services").document(serviceId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    currentService = document.toObject(Service.class);
                    if (currentService != null) {
                        binding.serviceName.setText(currentService.getName());
                        binding.serviceDescription.setText(currentService.getDescription());
                        binding.servicePrice.setText(String.format("$%.2f", currentService.getPrice()));
                    }
                }
            });
    }

    private void loadBarberShops() {
        db.collection("users")
            .whereEqualTo("isAdmin", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                barberShops.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    BarberShop shop = document.toObject(BarberShop.class);
                    if (shop != null) {
                        shop.setId(document.getId());
                        barberShops.add(shop);
                    }
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                    "Error loading barber shops: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onBarberShopSelected(BarberShop barberShop) {
        Bundle args = new Bundle();
        args.putString("shopId", barberShop.getId());
        args.putString("serviceId", currentService.getId());
        args.putString("serviceName", currentService.getName());
        args.putDouble("servicePrice", currentService.getPrice());
        
        Navigation.findNavController(requireView())
            .navigate(R.id.action_service_details_to_booking, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
 