package com.example.easypark.User.MyBookings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.easypark.R;
import com.example.easypark.User.HomePage;
import com.example.easypark.User.Settings.SettingsMainPage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyBookingsListing extends AppCompatActivity {

    private LinearLayout bookingsContainer;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings_listing);
        // BottomNavigationView setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyBookingsListing.this, HomePage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                // Already on MyBookingsListing, no action needed
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MyBookingsListing.this, SettingsMainPage.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        // Find the LinearLayout where cards will be added
        bookingsContainer = findViewById(R.id.bookings_container);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "N/A");

        // Reference to Firebase Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("BookingInformation");

        // Query bookings by specific email
        databaseRef.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookingsContainer.removeAllViews(); // Clear the container first

                // Create a list to store all booking data
                List<Map<String, Object>> bookingsList = new ArrayList<>();

                // Loop through each booking data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> bookingData = (Map<String, Object>) snapshot.getValue();
                    if (bookingData != null) {
                        String uniqueId = snapshot.getKey();
                        bookingData.put("uniqueId", uniqueId);
                        bookingsList.add(bookingData);
                    }
                }

                // Sort bookings based on the status ('In-Progress' first, then 'Pending', then 'Completed')
                Collections.sort(bookingsList, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> booking1, Map<String, Object> booking2) {
                        String status1 = booking1.get("status").toString();
                        String status2 = booking2.get("status").toString();
                        return getStatusPriority(status1) - getStatusPriority(status2);
                    }

                    private int getStatusPriority(String status) {
                        switch (status) {
                            case "In-Progress":
                                return 1;
                            case "Pending":
                                return 2;
                            case "Completed":
                                return 3;
                            default:
                                return 4;
                        }
                    }
                });
                List<Map<String, Object>> inProgressBookings = new ArrayList<>();
                List<Map<String, Object>> pendingBookings = new ArrayList<>();
                List<Map<String, Object>> completedBookings = new ArrayList<>();
                List<Map<String, Object>> cancelledBookings = new ArrayList<>();
                for (Map<String, Object> bookingData : bookingsList) {
                    String status = bookingData.get("status").toString();
                    if (status.equals("In-Progress")) {
                        inProgressBookings.add(bookingData);
                    } else if (status.equals("Pending")) {
                        pendingBookings.add(bookingData);
                    } else if (status.equals("Completed")) {
                        completedBookings.add(bookingData);
                    }else
                    {   cancelledBookings.add(bookingData);

                    }
                }
                Collections.reverse(inProgressBookings);
                Collections.reverse(pendingBookings);
                Collections.reverse(completedBookings);
                Collections.reverse(cancelledBookings);

                // Display sorted bookings
                for (Map<String, Object> bookingData : inProgressBookings) {
                    addBookingCard(bookingData);
                }
                for (Map<String, Object> bookingData : pendingBookings) {
                    addBookingCard(bookingData);
                }

                for (Map<String, Object> bookingData : completedBookings) {
                    addBookingCard(bookingData);
                }
                for (Map<String, Object> bookingData : cancelledBookings) {
                    addBookingCard(bookingData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    // Function to add a booking card dynamically to the container
    private void addBookingCard(Map<String, Object> bookingData) {
        // Create a new CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200// Set to WRAP_CONTENT for flexible height
        );
        cardParams.setMargins(16, 16, 16, 16); // Set margins for the CardView
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(12f);
        cardView.setCardElevation(8f);

        // Create a LinearLayout to hold card content
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL); // Keep orientation as horizontal
        linearLayout.setPadding(22, 50, 16, 16);

        // Create TextView for location name
        TextView locationNameView = new TextView(this);
        locationNameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        locationNameView.setText(bookingData.get("LocationName").toString());
        locationNameView.setTextSize(15f);
        locationNameView.setTextColor(getResources().getColor(android.R.color.black));
        locationNameView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); // Align to start and center vertically

        // Create TextView for status
        TextView statusView = new TextView(this);
        statusView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // Set weight to 1 for equal spacing
        statusView.setText(bookingData.get("status").toString());
        statusView.setTextSize(16f);
        statusView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL); // Align to end and center vertically
        if (statusView.getText().toString().equalsIgnoreCase("In-Progress")) {
            statusView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (statusView.getText().toString().equalsIgnoreCase("Completed")) {
            statusView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Add both TextViews to the LinearLayout
        linearLayout.addView(locationNameView);
        linearLayout.addView(statusView);

        // Add the LinearLayout to the CardView
        cardView.addView(linearLayout);

        // Set an OnClickListener on the CardView to pass data to another activity
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyBookingsListing.this, SpecificBookingActivity.class);
                Bundle bundle = new Bundle();
                for (Map.Entry<String, Object> entry : bookingData.entrySet()) {
                    bundle.putString(entry.getKey(), entry.getValue().toString());
                }
                //bundle.putString("uniqueId", bookingData.get("uniqueId").toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // Finally, add the CardView to the container
        bookingsContainer.addView(cardView);
    }


}
