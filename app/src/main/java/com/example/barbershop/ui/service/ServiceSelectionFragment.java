package com.example.barbershop.ui.service;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barbershop.R;
import com.example.barbershop.adapter.ServicesAdapter;
import com.example.barbershop.databinding.FragmentServiceSelectionBinding;
import com.example.barbershop.model.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.example.barbershop.model.TimeSlot;
import com.example.barbershop.model.Appointment;
import com.example.barbershop.adapter.TimeSlotAdapter;
import com.example.barbershop.model.BarberShop;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.barbershop.util.DatabaseInitializer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.text.SimpleDateFormat;

public class ServiceSelectionFragment extends Fragment {
    private FragmentServiceSelectionBinding binding;
    private FirebaseFirestore db;
    private ServicesAdapter adapter;

    // Define the callback interfaces
    private interface TimeSlotAvailabilityCallback {
        void onTimeSlotAvailabilityChecked(List<TimeSlot> availableSlots);
    }

    private interface DateSelectedCallback {
        void onDateSelected(Date date);
    }

    private interface TimeSelectedCallback {
        void onTimeSelected(String time);
    }

    private interface TimeSlotSelectedCallback {
        void onTimeSlotSelected(int hour, int minute);
    }

    private interface DateAvailabilityCallback {
        void onDateAvailabilityChecked(boolean isAvailable);
    }

    private boolean isHoliday(Date date, List<Map<String, Object>> holidays) {
        if (holidays == null) return false;
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        clearTime(cal1);
        
        for (Map<String, Object> holiday : holidays) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(((Timestamp) holiday.get("date")).toDate());
            clearTime(cal2);
            
            if (cal1.equals(cal2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWorkingDay(Date date, Map<String, Object> workingHours) {
        if (workingHours == null) return false;
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String dayOfWeek = days[cal.get(Calendar.DAY_OF_WEEK) - 1];
        
        Map<String, Object> dayHours = (Map<String, Object>) workingHours.get(dayOfWeek);
        return dayHours != null && (boolean) dayHours.get("isOpen");
    }

    private void checkTimeSlotAvailability(Date date, int serviceDuration, 
                                         TimeSlotAvailabilityCallback callback) {
        // Get working hours for this day
        db.collection("schedule").document("workingHours")
            .get()
            .addOnSuccessListener(workingHoursDoc -> {
                Map<String, Object> workingHours = workingHoursDoc.getData();
                
                // Get appointments
                db.collection("appointments")
                    .whereGreaterThanOrEqualTo("appointmentDate", getStartOfDay(date))
                    .whereLessThan("appointmentDate", getEndOfDay(date))
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<TimeSlot> slots = calculateAvailableTimeSlots(date, serviceDuration,
                            workingHours, querySnapshot.getDocuments());
                        callback.onTimeSlotAvailabilityChecked(slots);
                    });
            });
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        clearTime(calendar);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        clearTime(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static final int WORKING_HOURS_START = 8; // 8 AM
    private static final int WORKING_HOURS_END = 20;  // 8 PM

    private List<TimeSlot> calculateAvailableTimeSlots(Date date, int serviceDuration,
                                                     Map<String, Object> workingHours,
                                                     List<DocumentSnapshot> existingAppointments) {
        List<TimeSlot> availableSlots = new ArrayList<>();
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(date);
        startTime.set(Calendar.HOUR_OF_DAY, WORKING_HOURS_START);
        startTime.set(Calendar.MINUTE, 0);
        
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(date);
        endTime.set(Calendar.HOUR_OF_DAY, WORKING_HOURS_END);
        endTime.set(Calendar.MINUTE, 0);

        // Get current time
        Calendar currentTime = Calendar.getInstance();
        
        // If the date is today, start from the next available slot after current time
        if (isSameDay(startTime, currentTime)) {
            // Round up to the next 30-minute slot
            int minutes = currentTime.get(Calendar.MINUTE);
            if (minutes % 30 != 0) {
                minutes = ((minutes / 30) + 1) * 30;
                currentTime.set(Calendar.MINUTE, minutes);
                if (minutes == 60) {
                    currentTime.add(Calendar.HOUR_OF_DAY, 1);
                    currentTime.set(Calendar.MINUTE, 0);
                }
            }
            
            // If current time is after working hours start, use it as start time
            if (currentTime.after(startTime)) {
                startTime = (Calendar) currentTime.clone();
            }
        }

        while (startTime.before(endTime)) {
            boolean isAvailable = true;
            
            // Check if this slot is in the past
            if (startTime.before(currentTime)) {
                isAvailable = false;
            } else {
                // Check existing appointments for overlap
                for (DocumentSnapshot doc : existingAppointments) {
                    Appointment appointment = doc.toObject(Appointment.class);
                    if (appointment != null) {  // Add null check
                        Calendar appointmentTime = Calendar.getInstance();
                        appointmentTime.setTime(appointment.getAppointmentDate());
                        
                        if (isTimeOverlap(startTime, serviceDuration, 
                            appointmentTime, appointment.getServiceDuration())) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
            }
            
            // Add the slot (available or not)
            availableSlots.add(new TimeSlot(
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                isAvailable
            ));
            
            startTime.add(Calendar.MINUTE, 30); // 30-minute intervals
        }
        
        return availableSlots;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private Calendar parseTime(String time) {
        Calendar cal = Calendar.getInstance();
        String[] parts = time.split(":");
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        return cal;
    }

    private boolean isTimeOverlap(Calendar time1, int duration1, Calendar time2, int duration2) {
        Calendar end1 = (Calendar) time1.clone();
        end1.add(Calendar.MINUTE, duration1);
        
        Calendar end2 = (Calendar) time2.clone();
        end2.add(Calendar.MINUTE, duration2);
        
        return !time1.after(end2) && !time2.after(end1);
    }

    private void showTimeSlotPickerDialog(List<TimeSlot> availableSlots, 
                                        TimeSlotSelectedCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_slots, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.timeSlotsRecyclerView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        AlertDialog dialog = builder.setView(dialogView)
            .setTitle("Select Time")
            .setNegativeButton("Cancel", null)
            .create();

        TimeSlotAdapter adapter = new TimeSlotAdapter(availableSlots, timeSlot -> {
            callback.onTimeSlotSelected(timeSlot.getHour(), timeSlot.getMinute());
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);
        
        dialog.show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentServiceSelectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        
        // Show hardcoded services by default instead of loading from Firebase
        showHardcodedServices();
        
        // Keep the reinitialize button for admin use
        binding.reinitializeButton.setOnClickListener(v -> {
            Log.d("ServiceSelection", "Reinitialize button clicked");
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
            
            DatabaseInitializer.forceReinitializeServices(success -> {
                if (success) {
                    loadServices(); // Try loading from Firebase after reinitialization
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to reinitialize services", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return root;
    }

    private void setupRecyclerView() {
        adapter = new ServicesAdapter(service -> showBookingDialog(service));
        binding.servicesRecyclerView.setAdapter(adapter);
        binding.servicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadServices() {
        Log.d("ServiceSelection", "Loading services...");
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateLayout.setVisibility(View.GONE);
        
        db.collection("services")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d("ServiceSelection", "Query successful, document count: " + queryDocumentSnapshots.size());
                binding.progressBar.setVisibility(View.GONE);
                List<Service> services = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Service service = document.toObject(Service.class);
                    service.setId(document.getId());
                    services.add(service);
                    Log.d("ServiceSelection", "Added service: " + service.getName() + ", ID: " + service.getId());
                }
                
                if (services.isEmpty()) {
                    Log.d("ServiceSelection", "No services found, showing empty state");
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    Log.d("ServiceSelection", "Found " + services.size() + " services");
                    binding.emptyStateLayout.setVisibility(View.GONE);
                }
                
                adapter.submitList(services);
            })
            .addOnFailureListener(e -> {
                Log.e("ServiceSelection", "Error loading services", e);
                binding.progressBar.setVisibility(View.GONE);
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Error loading services: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showBookingDialog(Service service) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking, null);
        builder.setView(dialogView);
        
        // Create the dialog
        AlertDialog dialog = builder.create();
        
        // Get references to dialog views
        TextView serviceNameTextView = dialogView.findViewById(R.id.serviceNameTextView);
        Spinner shopSpinner = dialogView.findViewById(R.id.shopSpinner);
        Button dateButton = dialogView.findViewById(R.id.dateButton);
        Button timeButton = dialogView.findViewById(R.id.timeButton);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);
        
        // Set service name
        serviceNameTextView.setText(service.getName());
        
        // Load barber shops
        loadBarberShops(shopSpinner);
        
        // Setup date picker
        AtomicReference<Date> selectedDate = new AtomicReference<>(null);
        dateButton.setOnClickListener(v -> {
            BarberShop selectedShop = (BarberShop) shopSpinner.getSelectedItem();
            if (selectedShop == null) {
                Toast.makeText(requireContext(), "Please select a shop first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showDatePickerForShop(selectedShop, date -> {
                selectedDate.set(date);
                dateButton.setText(formatDate(date));
                timeButton.setEnabled(true);
            });
        });
        
        // Setup time picker
        AtomicReference<String> selectedTime = new AtomicReference<>(null);
        timeButton.setEnabled(false);
        timeButton.setOnClickListener(v -> {
            if (selectedDate.get() == null) {
                Toast.makeText(requireContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            BarberShop selectedShop = (BarberShop) shopSpinner.getSelectedItem();
            if (selectedShop == null) {
                Toast.makeText(requireContext(), "Please select a shop", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showTimePickerForShop(selectedShop, selectedDate.get(), service.getDuration(), time -> {
                selectedTime.set(time);
                timeButton.setText(time);
                confirmButton.setEnabled(true);
            });
        });
        
        // Setup confirm button
        confirmButton.setEnabled(false);
        confirmButton.setOnClickListener(v -> {
            BarberShop selectedShop = (BarberShop) shopSpinner.getSelectedItem();
            if (selectedShop == null) {
                Toast.makeText(requireContext(), "Please select a shop", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedDate.get() == null) {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedTime.get() == null) {
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create appointment
            bookAppointment(service, selectedShop, selectedDate.get(), selectedTime.get());
            dialog.dismiss();
        });
        
        // Update date/time when shop changes
        shopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Reset date and time when shop changes
                selectedDate.set(null);
                selectedTime.set(null);
                dateButton.setText("Select Date");
                timeButton.setText("Select Time");
                timeButton.setEnabled(false);
                confirmButton.setEnabled(false);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Show the dialog
        dialog.show();
    }

    private void bookAppointment(Service service, BarberShop shop, Date date, String time) {
        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        appointment.setServiceId(service.getId());
        appointment.setServiceName(service.getName());
        appointment.setServicePrice(service.getPrice());
        appointment.setServiceDuration(service.getDuration());
        appointment.setShopId(shop.getId());
        appointment.setShopName(shop.getName());
        
        // Parse time and set appointment date
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date timeDate = timeFormat.parse(time);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(timeDate);
            
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            
            appointment.setAppointmentDate(calendar.getTime());
        } catch (Exception e) {
            Log.e("ServiceSelection", "Error parsing time", e);
            Toast.makeText(requireContext(), "Error setting appointment time", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Set status as confirmed by default
        appointment.setStatus("confirmed");
        
        // Save to Firestore
        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(requireContext(), "Appointment booked successfully", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).navigateUp();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error booking appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showDatePickerForShop(BarberShop shop, DateSelectedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth);
                
                // Check if selected date is a special day/holiday
                db.collection("barbershops").document(shop.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = documentSnapshot.getData();
                            if (data != null) {
                                // Check for special days
                                if (data.containsKey("specialDays")) {
                                    List<Map<String, Object>> specialDays = 
                                        (List<Map<String, Object>>) data.get("specialDays");
                                    
                                    for (Map<String, Object> specialDay : specialDays) {
                                        Date specialDate = ((Timestamp) specialDay.get("date")).toDate();
                                        Calendar specialCal = Calendar.getInstance();
                                        specialCal.setTime(specialDate);
                                        
                                        if (isSameDay(selectedCalendar, specialCal)) {
                                            String name = (String) specialDay.get("name");
                                            Toast.makeText(requireContext(), 
                                                "This day is marked as " + name + " and is not available", 
                                                Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                }
                                
                                // Check working hours for the selected day
                                String dayOfWeek = getDayOfWeek(selectedCalendar.get(Calendar.DAY_OF_WEEK));
                                
                                if (data.containsKey("workingHours")) {
                                    Map<String, Object> workingHours = 
                                        (Map<String, Object>) data.get("workingHours");
                                    
                                    if (workingHours.containsKey(dayOfWeek)) {
                                        Map<String, Object> dayHours = 
                                            (Map<String, Object>) workingHours.get(dayOfWeek);
                                        boolean isClosed = (boolean) dayHours.getOrDefault("closed", false);
                                        
                                        if (isClosed) {
                                            Toast.makeText(requireContext(), 
                                                "Shop is closed on " + dayOfWeek, Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        
                                        // Day is available
                                        callback.onDateSelected(selectedCalendar.getTime());
                                    } else {
                                        Toast.makeText(requireContext(), 
                                            "No schedule found for " + dayOfWeek, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(requireContext(), 
                                        "No schedule found for this shop", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(), 
                                    "Shop not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), 
                                "Shop not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), 
                            "Error loading shop schedule: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            },
            year, month, day
        );
        
        // Set min date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        // Set max date to 3 months from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "";
        }
    }

    private String formatDate(Date date) {
        // Format as dd/MM/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    private void loadBarberShops(Spinner spinner) {
        // First try to get hardcoded shops if Firebase fails
        List<BarberShop> hardcodedShops = getHardcodedBarberShops();
        
        // Create adapter with hardcoded shops initially
        ArrayAdapter<BarberShop> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            hardcodedShops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        // Now try to load from Firebase
        db.collection("barbershops")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<BarberShop> shops = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    BarberShop shop = new BarberShop();
                    shop.setId(document.getId());
                    shop.setName(document.getString("name"));
                    
                    // Get owner details
                    db.collection("users").document(document.getId())
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                shop.setOwnerName(userDoc.getString("name"));
                            }
                        });
                    
                    shops.add(shop);
                }
                
                // If we got shops from Firebase, update the adapter
                if (!shops.isEmpty()) {
                    adapter.clear();
                    adapter.addAll(shops);
                    adapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> {
                // If Firebase fails, we already have hardcoded shops
                Log.e("ServiceSelection", "Error loading shops: " + e.getMessage());
            });
    }

    private void checkDateAvailability(Date date, Service service, BarberShop shop, DateAvailabilityCallback callback) {
        // Check if the date is a holiday
        db.collection("barbershops").document(shop.getId())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        // Check for special days/holidays
                        if (data.containsKey("specialDays")) {
                            List<Map<String, Object>> specialDays = 
                                (List<Map<String, Object>>) data.get("specialDays");
                            
                            if (isHoliday(date, specialDays)) {
                                callback.onDateAvailabilityChecked(false);
                                return;
                            }
                        }
                        
                        // Check working hours
                        if (data.containsKey("workingHours")) {
                            Map<String, Object> workingHours = (Map<String, Object>) data.get("workingHours");
                            
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            String dayOfWeek = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
                            
                            if (workingHours.containsKey(dayOfWeek)) {
                                Map<String, Object> dayHours = (Map<String, Object>) workingHours.get(dayOfWeek);
                                boolean isClosed = (boolean) dayHours.getOrDefault("closed", false);
                                
                                if (isClosed) {
                                    callback.onDateAvailabilityChecked(false);
                                    return;
                                }
                                
                                // Now check time slot availability
                                int serviceDuration = service.getDuration();
                                checkTimeSlotAvailability(date, serviceDuration, slots -> {
                                    // Update this line to check if the slots list is not empty
                                    boolean hasAvailableSlots = !slots.isEmpty();
                                    callback.onDateAvailabilityChecked(hasAvailableSlots);
                                });
                            } else {
                                callback.onDateAvailabilityChecked(false);
                            }
                        } else {
                            callback.onDateAvailabilityChecked(false);
                        }
                    } else {
                        callback.onDateAvailabilityChecked(false);
                    }
                } else {
                    callback.onDateAvailabilityChecked(false);
                }
            })
            .addOnFailureListener(e -> callback.onDateAvailabilityChecked(false));
    }

    private void showAvailableTimeSlots(Date date, int serviceDuration, 
                                      TimeSlotSelectedCallback callback) {
        // Get working hours for this day
        db.collection("schedule").document("workingHours")
            .get()
            .addOnSuccessListener(document -> {
                Map<String, Object> workingHours = document.getData();
                // Get all appointments for this day
                db.collection("appointments")
                    .whereGreaterThanOrEqualTo("appointmentDate", getStartOfDay(date))
                    .whereLessThan("appointmentDate", getEndOfDay(date))
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<TimeSlot> availableSlots = 
                            calculateAvailableTimeSlots(date, serviceDuration, 
                                workingHours, querySnapshot.getDocuments());
                        
                        showTimeSlotPickerDialog(availableSlots, callback);
                    });
            });
    }

    private void testWithDummyService() {
        List<Service> testServices = new ArrayList<>();
        Service testService = new Service("Test Service", "This is a test service", 10.0, 15);
        testService.setId("test-id");
        testServices.add(testService);
        adapter.submitList(testServices);
        binding.emptyStateLayout.setVisibility(View.GONE);
    }

    private void showHardcodedServices() {
        List<Service> services = new ArrayList<>();
        
        services.add(new Service("Haircut", "Classic haircut with modern styling", 30.00, 30));
        services.add(new Service("Beard Trim", "Professional beard grooming and shaping", 20.00, 20));
        services.add(new Service("Hair Coloring", "Full hair coloring service", 60.00, 90));
        services.add(new Service("Hair & Beard", "Complete hair and beard styling", 45.00, 45));
        services.add(new Service("Shave", "Traditional straight razor shave", 25.00, 25));
        
        adapter.submitList(services);
    }

    private List<BarberShop> getHardcodedBarberShops() {
        List<BarberShop> shops = new ArrayList<>();
        
        BarberShop shop1 = new BarberShop();
        shop1.setId("shop1");
        shop1.setName("Downtown Barber");
        shop1.setAddress("123 Main St");
        shop1.setPhone("555-1234");
        shops.add(shop1);
        
        BarberShop shop2 = new BarberShop();
        shop2.setId("shop2");
        shop2.setName("Classic Cuts");
        shop2.setAddress("456 Oak Ave");
        shop2.setPhone("555-5678");
        shops.add(shop2);
        
        return shops;
    }

    private void showTimePickerForShop(BarberShop shop, Date date, int serviceDuration, TimeSelectedCallback callback) {
        // Load shop schedule for the selected day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dayOfWeek = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        
        // First, get the shop's working hours
        db.collection("barbershops").document(shop.getId())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null && data.containsKey("workingHours")) {
                        Map<String, Object> hoursData = (Map<String, Object>) data.get("workingHours");
                        
                        if (hoursData.containsKey(dayOfWeek)) {
                            Map<String, Object> dayHours = (Map<String, Object>) hoursData.get(dayOfWeek);
                            boolean isClosed = (boolean) dayHours.getOrDefault("closed", false);
                            
                            if (isClosed) {
                                Toast.makeText(requireContext(), 
                                    "Shop is closed on " + dayOfWeek, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            String openTime = (String) dayHours.get("openTime");
                            String closeTime = (String) dayHours.get("closeTime");
                            
                            if (openTime == null || closeTime == null) {
                                Toast.makeText(requireContext(), 
                                    "Invalid shop hours", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            // Get break time if any
                            int[] breakStartMinutes = {-1};
                            int[] breakEndMinutes = {-1};
                            
                            if (data.containsKey("breakTime")) {
                                Map<String, Object> breakTimeData = (Map<String, Object>) data.get("breakTime");
                                boolean isBreakEnabled = (boolean) breakTimeData.getOrDefault("enabled", false);
                                
                                if (isBreakEnabled) {
                                    String breakStartTime = (String) breakTimeData.get("startTime");
                                    String breakEndTime = (String) breakTimeData.get("endTime");
                                    
                                    if (breakStartTime != null && breakEndTime != null) {
                                        String[] breakStartParts = breakStartTime.split(":");
                                        String[] breakEndParts = breakEndTime.split(":");
                                        
                                        int breakStartHour = Integer.parseInt(breakStartParts[0]);
                                        int breakStartMinute = Integer.parseInt(breakStartParts[1]);
                                        
                                        int breakEndHour = Integer.parseInt(breakEndParts[0]);
                                        int breakEndMinute = Integer.parseInt(breakEndParts[1]);
                                        
                                        breakStartMinutes[0] = breakStartHour * 60 + breakStartMinute;
                                        breakEndMinutes[0] = breakEndHour * 60 + breakEndMinute;
                                    }
                                }
                            }
                            
                            // Generate time slots with break time consideration
                            List<String> timeSlots = generateTimeSlots(
                                openTime, closeTime, serviceDuration, date, breakStartMinutes[0], breakEndMinutes[0]);
                            
                            if (timeSlots.isEmpty()) {
                                Toast.makeText(requireContext(), 
                                    "No available time slots", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            // Show time picker dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Select Time");
                            
                            builder.setItems(timeSlots.toArray(new String[0]), (dialog, which) -> {
                                String selectedTime = timeSlots.get(which);
                                callback.onTimeSelected(selectedTime);
                            });
                            
                            builder.setNegativeButton("Cancel", null);
                            builder.show();
                        } else {
                            Toast.makeText(requireContext(), 
                                "No schedule found for " + dayOfWeek, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), 
                            "No schedule found for this shop", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), 
                        "Shop not found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), 
                    "Error loading shop schedule: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private List<String> generateTimeSlots(String openTime, String closeTime, 
                                           int serviceDuration, Date selectedDate, 
                                           int breakStartMinutes, int breakEndMinutes) {
        List<String> timeSlots = new ArrayList<>();
        
        try {
            // Parse open and close times
            String[] openParts = openTime.split(":");
            String[] closeParts = closeTime.split(":");
            
            int openHour = Integer.parseInt(openParts[0]);
            int openMinute = Integer.parseInt(openParts[1]);
            
            int closeHour = Integer.parseInt(closeParts[0]);
            int closeMinute = Integer.parseInt(closeParts[1]);
            
            // Convert to minutes since midnight
            int openMinutes = openHour * 60 + openMinute;
            int closeMinutes = closeHour * 60 + closeMinute;
            
            // Get current time
            Calendar now = Calendar.getInstance();
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            
            // If selected date is today, start from current time
            int startMinutes = openMinutes;
            if (isSameDay(now, selectedCal)) {
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);
                int currentMinutes = currentHour * 60 + currentMinute;
                
                // Round up to the next interval
                int interval = serviceDuration;
                currentMinutes = ((currentMinutes + interval - 1) / interval) * interval;
                
                if (currentMinutes > startMinutes) {
                    startMinutes = currentMinutes;
                }
            }
            
            // Generate time slots based on service duration
            for (int time = startMinutes; time <= closeMinutes - serviceDuration; time += serviceDuration) {
                // Skip slots that overlap with break time
                if (breakStartMinutes != -1 && breakEndMinutes != -1) {
                    if (time >= breakStartMinutes && time < breakEndMinutes) {
                        continue; // Skip this slot as it's during break time
                    }
                    
                    // Also skip if the service would extend into break time
                    if (time < breakStartMinutes && time + serviceDuration > breakStartMinutes) {
                        continue;
                    }
                }
                
                int hour = time / 60;
                int minute = time % 60;
                
                timeSlots.add(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        } catch (Exception e) {
            Log.e("ServiceSelection", "Error generating time slots", e);
        }
        
        return timeSlots;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 