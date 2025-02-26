package com.example.barbershop.util;

import android.util.Log;
import com.example.barbershop.model.Service;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseInitializer {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void initializeServices(OnServicesInitializedListener listener) {
        // First check if services already exist
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("services").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    // Only initialize if the collection is empty
                    List<Service> services = Arrays.asList(
                        new Service("Haircut", 
                            "Classic haircut with modern styling", 
                            30.00, 
                            30),
                        new Service("Beard Trim", 
                            "Professional beard grooming and shaping", 
                            20.00, 
                            20),
                        new Service("Hair Coloring", 
                            "Full hair coloring service", 
                            60.00, 
                            90),
                        new Service("Hair & Beard", 
                            "Complete hair and beard styling", 
                            45.00, 
                            45),
                        new Service("Shave", 
                            "Traditional straight razor shave", 
                            25.00, 
                            25)
                    );
                    
                    WriteBatch batch = db.batch();
                    
                    for (Service service : services) {
                        DocumentReference docRef = db.collection("services").document();
                        batch.set(docRef, service);
                    }
                    
                    batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("DatabaseInitializer", "Services initialized successfully");
                            if (listener != null) {
                                listener.onServicesInitialized(true);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DatabaseInitializer", "Error initializing services", e);
                            if (listener != null) {
                                listener.onServicesInitialized(false);
                            }
                        });
                } else {
                    Log.d("DatabaseInitializer", "Services collection already has data");
                    if (listener != null) {
                        listener.onServicesInitialized(true);
                    }
                }
            } else {
                Log.e("DatabaseInitializer", "Error checking services collection", task.getException());
                if (listener != null) {
                    listener.onServicesInitialized(false);
                }
            }
        });
    }

    public static void initializeBarberShops() {
        List<Map<String, Object>> shops = Arrays.asList(
            new HashMap<String, Object>() {{
                put("name", "Downtown Cuts");
                put("address", "123 Main St, City");
                put("phone", "555-0123");
                put("rating", 4.5);
                put("openingHours", new HashMap<String, String>() {{
                    put("monday", "9:00-18:00");
                    put("tuesday", "9:00-18:00");
                    put("wednesday", "9:00-18:00");
                    put("thursday", "9:00-18:00");
                    put("friday", "9:00-18:00");
                    put("saturday", "10:00-16:00");
                    put("sunday", "Closed");
                }});
            }},
            new HashMap<String, Object>() {{
                put("name", "Classic Barbers");
                put("address", "456 Oak Ave, City");
                put("phone", "555-0456");
                put("rating", 4.8);
                put("openingHours", new HashMap<String, String>() {{
                    put("monday", "10:00-19:00");
                    put("tuesday", "10:00-19:00");
                    put("wednesday", "10:00-19:00");
                    put("thursday", "10:00-19:00");
                    put("friday", "10:00-19:00");
                    put("saturday", "9:00-17:00");
                    put("sunday", "Closed");
                }});
            }}
        );

        WriteBatch batch = db.batch();
        
        for (Map<String, Object> shop : shops) {
            DocumentReference docRef = db.collection("barbershops").document();
            batch.set(docRef, shop);
        }

        batch.commit()
            .addOnSuccessListener(aVoid -> Log.d("DatabaseInitializer", "Barber shops initialized successfully"))
            .addOnFailureListener(e -> Log.e("DatabaseInitializer", "Error initializing barber shops", e));
    }

    public static void forceReinitializeServices(OnServicesInitializedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // First delete all existing services
        db.collection("services")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                WriteBatch batch = db.batch();
                
                // Add all documents to delete batch
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    batch.delete(doc.getReference());
                }
                
                // Execute the batch delete
                batch.commit().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Log.d("DatabaseInitializer", "Successfully deleted all services");
                        
                        // Now add all services again
                        List<Service> services = Arrays.asList(
                            new Service("Haircut", 
                                "Classic haircut with modern styling", 
                                30.00, 
                                30),
                            new Service("Beard Trim", 
                                "Professional beard grooming and shaping", 
                                20.00, 
                                20),
                            new Service("Hair Coloring", 
                                "Full hair coloring service", 
                                60.00, 
                                90),
                            new Service("Hair & Beard", 
                                "Complete hair and beard styling", 
                                45.00, 
                                45),
                            new Service("Shave", 
                                "Traditional straight razor shave", 
                                25.00, 
                                25)
                        );
                        
                        WriteBatch newBatch = db.batch();
                        
                        for (Service service : services) {
                            DocumentReference docRef = db.collection("services").document();
                            newBatch.set(docRef, service);
                        }
                        
                        newBatch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("DatabaseInitializer", "Services reinitialized successfully");
                                if (listener != null) {
                                    listener.onServicesInitialized(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DatabaseInitializer", "Error reinitializing services", e);
                                if (listener != null) {
                                    listener.onServicesInitialized(false);
                                }
                            });
                    } else {
                        Log.e("DatabaseInitializer", "Error deleting services", deleteTask.getException());
                        if (listener != null) {
                            listener.onServicesInitialized(false);
                        }
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e("DatabaseInitializer", "Error getting services to delete", e);
                if (listener != null) {
                    listener.onServicesInitialized(false);
                }
            });
    }

    // Add this interface for callback
    public interface OnServicesInitializedListener {
        void onServicesInitialized(boolean success);
    }
} 