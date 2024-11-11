package com.example.easypark.admin.ManageLocations;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.admin.History.TransactionHistory;
import com.example.easypark.admin.addParkingLocation.addParkLocation;
import com.example.easypark.admin.adminHomePage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParkLocationListing extends AppCompatActivity {

    private LinearLayout parkingLocationLayout;
    private DatabaseReference parkingRef;
    private DatabaseReference bookingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_park_location_listing);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.manage_loc);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ParkLocationListing.this, adminHomePage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(ParkLocationListing.this, addParkLocation.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.manage_loc) {
                // Already on ParkLocationListing, no action needed
                return true;
            }
            else if (itemId==R.id.nav_history) {
                startActivity(new Intent(ParkLocationListing.this, TransactionHistory.class));
                overridePendingTransition(0, 0);
                return  true;
            }

            return false;
        });

        parkingLocationLayout = findViewById(R.id.parking_location_layout);
        parkingRef = FirebaseDatabase.getInstance().getReference("ParkingLocations");
        bookingRef = FirebaseDatabase.getInstance().getReference("BookingInformation");

        // Load parking locations from Firebase
        loadParkingLocations();
    }

    private void loadParkingLocations() {
        parkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parkingLocationLayout.removeAllViews(); // Clear existing views
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ParkingLocation location = snapshot.getValue(ParkingLocation.class);
                    if (location != null) {
                        addLocationView(location);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ParkLocationListing.this, "Failed to load locations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLocationView(ParkingLocation location) {
        // Inflate a new view for the parking location
        View locationView = LayoutInflater.from(this).inflate(R.layout.item_parking_location, null);

        TextView locationName = locationView.findViewById(R.id.location_name);
        TextView locationType = locationView.findViewById(R.id.location_type);
        TextView locationDetails = locationView.findViewById(R.id.location_details);
        TextView numberOfSlots = locationView.findViewById(R.id.number_of_slots);
        TextView coordinates = locationView.findViewById(R.id.coordinates);
        Button deleteButton = locationView.findViewById(R.id.delete_button);

        // Set data to views
        locationName.setText(location.getLocationName());
        locationType.setText(location.getLocationType());
        locationDetails.setText(location.getLocationDetails());
        numberOfSlots.setText("Number of Slots: " + location.getNumberOfSlots());
        coordinates.setText("Lat: " + location.getLatitude() + ", Long: " + location.getLongitude());

        // Set the delete button click listener
        deleteButton.setOnClickListener(v -> {
            checkAndDelete(location.getLocationName(), locationView);
        });

        // Add the view to the layout
        parkingLocationLayout.addView(locationView);
    }

    private void checkAndDelete(String locationName, View locationView) {
        bookingRef.orderByChild("LocationName").equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean canDelete = true;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Directly access the 'status' field from the snapshot
                            String status = snapshot.child("status").getValue(String.class);
                            if ("In-Progress".equals(status) || "pending".equals(status)) {
                                canDelete = false;
                                break;
                            }
                        }

                        if (canDelete) {
                            showDeleteConfirmationDialog(locationName, locationView);
                        } else {
                            Toast.makeText(ParkLocationListing.this, "Cannot delete. Location has active bookings.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ParkLocationListing.this, "Failed to check bookings.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showDeleteConfirmationDialog(String locationName, View locationView) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Location")
                .setMessage("Are you sure you want to delete this location?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Delete the location from Firebase
                    parkingRef.orderByChild("locationName").equalTo(locationName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue();
                                parkingLocationLayout.removeView(locationView); // Remove the view from layout
                                Toast.makeText(ParkLocationListing.this, "Location deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ParkLocationListing.this, "Failed to delete location.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
