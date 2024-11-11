package com.example.easypark.admin.History;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.User.HomePage;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.example.easypark.User.Settings.SettingsMainPage;
import com.example.easypark.admin.ManageLocations.ParkLocationListing;
import com.example.easypark.admin.addParkingLocation.addParkLocation;
import com.example.easypark.admin.adminHomePage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistory extends AppCompatActivity {

    private LinearLayout transactionContainer;
    private DatabaseReference bookingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(TransactionHistory.this, adminHomePage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_history) {
                // Already on MyBookingsListing, no action needed
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(TransactionHistory.this, addParkLocation.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.manage_loc) {
                startActivity(new Intent(TransactionHistory.this, ParkLocationListing.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        // Initialize the LinearLayout where transactions will be added
        transactionContainer = findViewById(R.id.transaction_container);

        // Reference to Firebase Realtime Database 'BookingInformation' collection
        bookingRef = FirebaseDatabase.getInstance().getReference("BookingInformation");

        // Load transactions from Firebase
        loadTransactions();
    }

    private void loadTransactions() {
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the container before adding new data
                transactionContainer.removeAllViews();

                // Collect all transaction snapshots into a List
                List<DataSnapshot> transactionList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    transactionList.add(snapshot);
                }

                // Iterate through the list in reverse order
                for (int i = transactionList.size() - 1; i >= 0; i--) {
                    DataSnapshot snapshot = transactionList.get(i);

                    String date = snapshot.child("date").getValue(String.class);
                    String endTime = snapshot.child("endTime").getValue(String.class);
                    String slotNumber = snapshot.child("slotNumber").getValue(String.class);
                    Integer totalAmount = snapshot.child("totalAmount").getValue(Integer.class);
                    String vehicleNumber = snapshot.child("vehicleNumber").getValue(String.class);

                    // Debugging: Log the values received
                    if (date == null || endTime == null || slotNumber == null || totalAmount == null || vehicleNumber == null) {
                        // Log missing data for debugging purposes
                        System.out.println("Missing data in one or more fields for a transaction.");
                    }

                    // Add transaction view if all fields are non-null
                    if (date != null && endTime != null && slotNumber != null && totalAmount != null && vehicleNumber != null) {
                        addTransactionView(date, endTime, slotNumber, totalAmount, vehicleNumber);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransactionHistory.this, "Failed to load transactions: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                // Log the error for debugging
                System.out.println("Database Error: " + databaseError.getMessage());
            }
        });
    }


    private void addTransactionView(String date, String endTime, String slotNumber, Integer totalAmount, String vehicleNumber) {
        // Inflate the custom transaction item view
        View transactionView = LayoutInflater.from(this).inflate(R.layout.item_transaction, transactionContainer, false);

        // Find the TextViews in the inflated view
        TextView dateText = transactionView.findViewById(R.id.transaction_date);
        TextView endTimeText = transactionView.findViewById(R.id.transaction_end_time);
        TextView slotNumberText = transactionView.findViewById(R.id.transaction_slot_number);
        TextView totalAmountText = transactionView.findViewById(R.id.transaction_total_amount);
        TextView vehicleNumberText = transactionView.findViewById(R.id.transaction_vehicle_number);

        // Set the values to the TextViews
        dateText.setText("Date: " + date);
        endTimeText.setText("End Time: " + endTime);
        slotNumberText.setText("Slot Number: " + slotNumber);
        totalAmountText.setText("Total Amount: ₹" + totalAmount.toString()); // Convert Integer to String
        vehicleNumberText.setText("Vehicle Number: " + vehicleNumber);

        // Add the transaction view to the container
        transactionContainer.addView(transactionView);
    }
}
