package com.example.easypark.admin;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.User.HomePage;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.example.easypark.User.Settings.SettingsMainPage;
import com.example.easypark.admin.History.TransactionHistory;
import com.example.easypark.admin.ManageLocations.ParkLocationListing;
import com.example.easypark.admin.addParkingLocation.addParkLocation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class adminHomePage extends AppCompatActivity {

    private TextView progressText1, progressText2, progressText3, progressText4;
    private Button addParkLoc;
    private int totalAmountSum = 0;
    private int bookingCount = 0;
    private int parkingLocationCount = 0;
    private int usersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        //Below is the code for Navbar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home, no action needed
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(adminHomePage.this, addParkLocation.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.manage_loc) {
                startActivity(new Intent(adminHomePage.this, ParkLocationListing.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId==R.id.nav_history) {
                startActivity(new Intent(adminHomePage.this, TransactionHistory.class));
                overridePendingTransition(0, 0);
                return  true;
            }

            return false;
        });



    // Find the TextViews by their ID
        progressText1 = findViewById(R.id.progress_text1);
        progressText2 = findViewById(R.id.progress_text2);
        progressText3 = findViewById(R.id.progress_text3);
        progressText4 = findViewById(R.id.progress_text4);

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Fetch data from "BookingInformation" collection
        DatabaseReference bookingRef = database.getReference("BookingInformation");
        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                totalAmountSum = 0;
                bookingCount = 0;
                for (DataSnapshot booking : snapshot.getChildren()) {
                    String status = booking.child("status").getValue(String.class);
                    Integer amount = booking.child("totalAmount").getValue(Integer.class);

                    if (status != null && status.equals("Completed") && amount != null) {
                        totalAmountSum += amount;
                    }
                    bookingCount++;
                }


                animateText(progressText3, totalAmountSum);
                animateText(progressText2, bookingCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle possible errors
            }
        });

        // Fetch data from "ParkingLocations" collection
        DatabaseReference parkingRef = database.getReference("ParkingLocations");
        parkingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                parkingLocationCount = (int) snapshot.getChildrenCount(); // Count of parking locations
                animateText(progressText1, parkingLocationCount); // Update TextView for parking locations count
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle possible errors
            }
        });

        // Fetch data from "Users" collection
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersCount = (int) snapshot.getChildrenCount(); // Count of users
                animateText(progressText4, usersCount); // Update TextView for users count
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    // Method to animate the TextView count
    private void animateText(final TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(6000); // 6 seconds for the count animation
        animator.addUpdateListener(valueAnimator -> textView.setText(valueAnimator.getAnimatedValue().toString()));
        animator.start();
    }
}
