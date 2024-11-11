package com.example.easypark.User.MyBookings;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.AlertDialog; // Add this import
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase; // Add this import

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.pdf.PdfDocument;
import android.widget.Toast;
import com.example.easypark.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SpecificBookingActivity extends AppCompatActivity {
    private ImageButton imgbtn;
    private TextView locationNameView, statusView, amtView, uniqIdView, usernameView, vehicleNumberView,
            dateView, emailView, slotNumberView, startTime, endTime;
    private Button DownloadCopy,CancelBooking;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String uniqueDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_booking);

        // Initialize views
        statusView = findViewById(R.id.tv_status);
        amtView = findViewById(R.id.tv_amount);
        uniqIdView = findViewById(R.id.tv_unique_id);
        usernameView = findViewById(R.id.tv_username);
        vehicleNumberView = findViewById(R.id.tv_vehicle_number);
        dateView = findViewById(R.id.tv_date);
        emailView = findViewById(R.id.tv_email);
        slotNumberView = findViewById(R.id.tv_slot_number);
        startTime = findViewById(R.id.tv_start_time);
        endTime = findViewById(R.id.tv_end_time);
        DownloadCopy = findViewById(R.id.btn_download_copy);
        imgbtn=findViewById(R.id.ic_back_button);
        CancelBooking=findViewById(R.id.btn_cancel_booking);
        DownloadCopy.setOnClickListener(v -> {
            if (checkPermission()) {
                createPdf();
            } else {
                requestPermission();
            }
        });
        imgbtn.setOnClickListener(v -> finish());
        CancelBooking.setOnClickListener(v -> showConfirmationDialog());
        // Get intent extras (assuming the data is passed correctly)
        Bundle extras = getIntent().getExtras();
        String statusData = extras.getString("status", "N/A");
        uniqueDocId=extras.getString("uniqueId", "N/A");

        if (statusData.equals("Completed") || statusData.equals("Cancelled")) {
            CancelBooking.setVisibility(View.GONE);  // Hide if booking is already completed or cancelled
        }
        if (extras != null) {
            statusView.setText(extras.getString("status", "N/A"));
            amtView.setText(extras.getString("totalAmount", "0"));
            uniqIdView.setText(extras.getString("uniqueId", "N/A"));
            usernameView.setText(extras.getString("username", "N/A"));
            vehicleNumberView.setText(extras.getString("vehicleNumber", "N/A"));
            dateView.setText(extras.getString("date", "N/A"));
            emailView.setText(extras.getString("email", "N/A"));
            slotNumberView.setText(extras.getString("slotNumber", "N/A"));
            startTime.setText(convertTo12HourFormat(extras.getString("startTime", "N/A")));
            endTime.setText(convertTo12HourFormat(extras.getString("endTime", "N/A")));
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (true || grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createPdf();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


    }
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Booking");
        builder.setMessage("Do you really want to cancel this booking?");
        builder.setPositiveButton("Yes", (dialog, which) -> cancelBooking());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void cancelBooking() {
        // Reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BookingInformation").child(uniqueDocId);

        // Update the status to "Cancelled"
        databaseReference.child("status").setValue("Cancelled").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                // Redirect to MyBookingsActivity (or your specific bookings page)
                startActivity(new Intent(SpecificBookingActivity.this, MyBookingsListing.class));
                finish(); // Close current activity
            } else {
                Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        // Create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        // Start a page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        int x = 10, y = 25;

        // Writing data to the PDF using the data from views
        paint.setTextSize(16);
        canvas.drawText("Booking Details", x, y, paint);
        y += 30;
        canvas.drawText("Booking ID: " + uniqIdView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Name: " + usernameView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Vehicle Number: " + vehicleNumberView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Start Time: " + startTime.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("End Time: " + endTime.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Date: " + dateView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Email: " + emailView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Slot Number: " + slotNumberView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Total Amount: " + amtView.getText().toString(), x, y, paint);
        y += 20;
        canvas.drawText("Status: " + statusView.getText().toString(), x, y, paint);

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF to the Downloads folder
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, "BookingDetails.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "PDF saved in Downloads: " + pdfFile.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Close the document
        pdfDocument.close();
    }

    private String convertTo12HourFormat(String time24) {
        if (time24.equals("N/A")) {
            return "N/A";
        }

        String[] parts = time24.split(":");
        if (parts.length != 2) {
            return "N/A";
        }

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        String amPm = (hour >= 12) ? "PM" : "AM";
        hour = hour % 12;
        hour = (hour == 0) ? 12 : hour;

        return String.format("%02d:%02d %s", hour, minute, amPm);
    }


}

