package com.example.easypark.User.UserBookingJourney;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.easypark.R;
import com.example.easypark.User.HomePage;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.example.easypark.User.Settings.SettingsMainPage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PaymentDoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_done);

        ImageView imageView = findViewById(R.id.payment_gif);
        Glide.with(this)
                .load("https://media.tenor.com/images/f92741349d817846a95a0b52b7ae2055/tenor.gif") // Replace with your image URL
                .into(imageView);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the selected item for the bottom navigation view
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set default selected item

        // Set item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null; // Declare intent variable

                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(PaymentDoneActivity.this, HomePage.class);
                } else if (item.getItemId() == R.id.nav_bookings) {
                    intent = new Intent(PaymentDoneActivity.this, MyBookingsListing.class);
                } else if (item.getItemId() == R.id.nav_settings) {
                    intent = new Intent(PaymentDoneActivity.this, SettingsMainPage.class);
                }

                if (intent != null) {
                    startActivity(intent); // Start the activity if intent is not null
                }
                return true; // Return true to indicate the item click was handled
            }
        });
    }
}
