package com.example.easypark.admin.addParkingLocation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.admin.History.TransactionHistory;
import com.example.easypark.admin.ManageLocations.ParkLocationListing;
import com.example.easypark.admin.adminHomePage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class addParkLocation extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etParkingLocationName, etLocationDetails, etNumberOfSlots, etLatitude, etLongitude;
    private Spinner spinnerLocationType;
    private Button btnUploadImage, btnAddLocation;
    private Uri imageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_park_location);
        //Below is the code for navbar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(addParkLocation.this, adminHomePage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                //in this page only
                return true;
            } else if (itemId == R.id.manage_loc) {
                startActivity(new Intent(addParkLocation.this, ParkLocationListing.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId==R.id.nav_history) {
                startActivity(new Intent(addParkLocation.this, TransactionHistory.class));
                overridePendingTransition(0, 0);
                return  true;
            }

            return false;
        });

        // Initialize UI components
        etParkingLocationName = findViewById(R.id.etParkingLocationName);
        etLocationDetails = findViewById(R.id.etLocationDetails);
        spinnerLocationType = findViewById(R.id.spinnerLocationType);
        etNumberOfSlots = findViewById(R.id.etNumberOfSlots);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnAddLocation = findViewById(R.id.btnAddLocation);

        // Initialize Firebase
        storageReference = FirebaseStorage.getInstance().getReference("ParkingLocationImages");
        databaseReference = FirebaseDatabase.getInstance().getReference("ParkingLocations");

        // Initialize ActivityResultLauncher for image picker
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                imageUri = result;
            }
        });

        btnUploadImage.setOnClickListener(v -> openFileChooser());

        btnAddLocation.setOnClickListener(v -> addParkingLocation());
    }

    private void openFileChooser() {
        imagePickerLauncher.launch("image/*");
    }

    private void addParkingLocation() {
        String locationName = etParkingLocationName.getText().toString().trim();
        String locationDetails = etLocationDetails.getText().toString().trim();
        String locationType = spinnerLocationType.getSelectedItem().toString();
        String numberOfSlots = etNumberOfSlots.getText().toString().trim();
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                Map<String, Object> parkingLocation = new HashMap<>();
                                parkingLocation.put("locationName", locationName);
                                parkingLocation.put("locationDetails", locationDetails);
                                parkingLocation.put("locationType", locationType);
                                parkingLocation.put("numberOfSlots", numberOfSlots);
                                parkingLocation.put("latitude", latitude);
                                parkingLocation.put("longitude", longitude);
                                parkingLocation.put("imageUrl", imageUrl);

                                databaseReference.push().setValue(parkingLocation)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(addParkLocation.this, "Parking Location added successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(addParkLocation.this, "Failed to add Parking Location", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }))
                    .addOnFailureListener(e -> Toast.makeText(addParkLocation.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
        }
    }
}
