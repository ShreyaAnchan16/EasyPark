package com.example.easypark.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easypark.R;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.example.easypark.User.ParkingLocationModel.ParkingLocation;
import com.example.easypark.User.ParkingLocationModel.ParkingLocationAdapter;
import com.example.easypark.User.Settings.SettingsMainPage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private RecyclerView recyclerView;
    private ParkingLocationAdapter adapter;
    private List<ParkingLocation> parkingLocationList;
    private List<ParkingLocation> filteredList;
    private DatabaseReference databaseReference;
    private EditText searchBar;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize views
        searchBar = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        parkingLocationList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ParkingLocationAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("ParkingLocations");


        loadAllParkingLocations();


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.length() > 4) {
                        searchParkingLocations(query);
                    } else {
                        loadAllParkingLocations();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home, no action needed
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(HomePage.this, MyBookingsListing.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(HomePage.this, SettingsMainPage.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }


    private void loadAllParkingLocations() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parkingLocationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ParkingLocation location = snapshot.getValue(ParkingLocation.class);
                    parkingLocationList.add(location);
                }
                filteredList.clear();
                filteredList.addAll(parkingLocationList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Method to search parking locations by name in Firebase
    private void searchParkingLocations(final String query) {
        // Retrieve all locations from Firebase
        databaseReference.orderByChild("locationName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filteredList.clear();
                        String lowerCaseQuery = query.toLowerCase();  // Convert the search query to lowercase
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ParkingLocation location = snapshot.getValue(ParkingLocation.class);

                            // Perform a case-insensitive comparison on the client side
                            if (location.getLocationName() != null && location.getLocationName().toLowerCase().contains(lowerCaseQuery)) {
                                filteredList.add(location);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (filteredList.isEmpty()) {
                            // Display "Not Found" or a message when no results match
                            Log.d("SearchResult", "No matching parking locations found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
                    }
                });
    }

}
