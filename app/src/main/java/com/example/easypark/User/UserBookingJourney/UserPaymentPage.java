package com.example.easypark.User.UserBookingJourney;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.easypark.R;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserPaymentPage extends AppCompatActivity {

    private TextView slotNumberTextView;
    private TextView vehicleNumberTextView;
    private TextView dateTextView;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private TextView subtotalTextView;
    private TextView taxTextView;
    private TextView totalAmountTextView;
    private CardView paymentCard;
    private Button payNowBtn;
    private EditText upiIdInput;
    private RadioGroup paymentMethods;

    // Firebase references
    private FirebaseDatabase database;
    private DatabaseReference bookingRef;

    // Constants
    private final String PREFS_NAME = "MyPrefs";
    private final String KEY_EMAIL = "email";
    private final String KEY_USERNAME = "username";

    // Brevo API constants
    private final String BREVO_API_KEY = "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";
    private final String BREVO_API_URL = "https://api.sendinblue.com/v3/smtp/email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_payment_page);
        ImageButton backButton = findViewById(R.id.ic_back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize views
        slotNumberTextView = findViewById(R.id.slot_number);
        vehicleNumberTextView = findViewById(R.id.vehicle_number);
        dateTextView = findViewById(R.id.booking_date);
        startTimeTextView = findViewById(R.id.start_time);
        endTimeTextView = findViewById(R.id.end_time);
        subtotalTextView = findViewById(R.id.total_amount);
        taxTextView = findViewById(R.id.tax_amount);
        totalAmountTextView = findViewById(R.id.tot_amt);
        payNowBtn = findViewById(R.id.pay_now_button);
        upiIdInput = findViewById(R.id.upi_id_input);
        paymentMethods = findViewById(R.id.payment_methods);

        // Firebase initialization
        database = FirebaseDatabase.getInstance();
        bookingRef = database.getReference("BookingInformation");

        // Retrieve booking details
        HashMap<String, String> bookingDetails = (HashMap<String, String>) getIntent().getSerializableExtra("bookingDetails");

        // Retrieve user information from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "N/A");
        String username = sharedPreferences.getString(KEY_USERNAME, "N/A");

        // Display booking details
        if (bookingDetails != null) {
            slotNumberTextView.setText(bookingDetails.get("slotNumber"));
            vehicleNumberTextView.setText(bookingDetails.get("vehicleNumber"));
            dateTextView.setText(bookingDetails.get("date"));
            startTimeTextView.setText(bookingDetails.get("startTime"));
            endTimeTextView.setText(bookingDetails.get("endTime"));

            // Calculate subtotal, tax, and total amount
            int subtotal = calculateSubtotal(bookingDetails.get("startTime"), bookingDetails.get("endTime"));
            double tax = subtotal * 0.18; // Assuming 18% tax rate
            int totalAmount = (int) Math.round(subtotal + tax);

            // Display the amounts
            subtotalTextView.setText("₹" + subtotal);
            taxTextView.setText("₹" + tax);
            totalAmountTextView.setText("₹" + totalAmount);

            // Set a listener for the payment methods RadioGroup
            paymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedRadioButton = findViewById(checkedId);
                if (selectedRadioButton.getId() == R.id.rb_upi) {
                    upiIdInput.setVisibility(View.VISIBLE); // Show UPI ID input
                } else {
                    upiIdInput.setVisibility(View.GONE); // Hide UPI ID input for other methods
                }
            });

            // Store booking details in Firebase when "Pay Now" is clicked
            payNowBtn.setOnClickListener(view -> {
                String status = "In-Progress";
                storeBookingInFirebase(bookingDetails, email, username, totalAmount, status);
                sendConfirmationEmail(email, bookingDetails, totalAmount);

                // Navigate to PaymentDoneActivity
                Intent intent = new Intent(UserPaymentPage.this, com.example.easypark.User.UserBookingJourney.PaymentDoneActivity.class);
                startActivity(intent);
            });
        }
    }

    // Example method to calculate subtotal based on start and end time
    private int calculateSubtotal(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        long hours = ChronoUnit.HOURS.between(start, end);
        return (int) hours * 100; // ₹100 per hour rate
    }

    // Method to store booking information in Firebase
    private void storeBookingInFirebase(HashMap<String, String> bookingDetails, String email, String username, int totalAmount, String status) {
        HashMap<String, Object> bookingData = new HashMap<>();
        bookingData.put("slotNumber", bookingDetails.get("slotNumber"));
        bookingData.put("vehicleNumber", bookingDetails.get("vehicleNumber"));
        bookingData.put("date", bookingDetails.get("date"));
        bookingData.put("startTime", bookingDetails.get("startTime"));
        bookingData.put("endTime", bookingDetails.get("endTime"));
        bookingData.put("totalAmount", totalAmount);
        bookingData.put("email", email);
        bookingData.put("username", username);
        bookingData.put("status", status);
        bookingData.put("LocationName", bookingDetails.get("LocationName"));

        bookingRef.push().setValue(bookingData)
                .addOnSuccessListener(aVoid -> Toast.makeText(UserPaymentPage.this, "Booking saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(UserPaymentPage.this, "Failed to save booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to send confirmation email using Brevo API
    private void sendConfirmationEmail(String toEmail, HashMap<String, String> bookingDetails, int totalAmount) {
        OkHttpClient client = new OkHttpClient();

        // Create the email content
        String emailBody = "{"
                + "\"sender\": {\"name\": \"EasyPark\", \"email\": \"skylords124@gmail.com\"},"
                + "\"to\": [{\"email\": \"" + toEmail + "\"}],"
                + "\"subject\": \"Booking Confirmation\","
                + "\"htmlContent\": \"<h1>Booking Details</h1>"
                + "<p><strong>Location Name:</strong> " + bookingDetails.get("LocationName") + "</p>"
                + "<p><strong>Slot Number:</strong> " + bookingDetails.get("slotNumber") + "</p>"
                + "<p><strong>Vehicle Number:</strong> " + bookingDetails.get("vehicleNumber") + "</p>"
                + "<p><strong>Date:</strong> " + bookingDetails.get("date") + "</p>"
                + "<p><strong>Start Time:</strong> " + bookingDetails.get("startTime") + "</p>"
                + "<p><strong>End Time:</strong> " + bookingDetails.get("endTime") + "</p>"
                + "<p><strong>Total Amount:</strong> ₹" + totalAmount + "</p>"
                + "\"}";

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(emailBody, mediaType);

        Request request = new Request.Builder()
                .url(BREVO_API_URL)
                .addHeader("api-key", BREVO_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(UserPaymentPage.this, "Failed to send confirmation email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(UserPaymentPage.this, "Confirmation email sent!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
