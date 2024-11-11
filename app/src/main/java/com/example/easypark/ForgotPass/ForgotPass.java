package com.example.easypark.ForgotPass;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPass extends AppCompatActivity {

    private EditText etEmail, etOTP, etNewPassword, etConfirmPassword;
    private Button btnSendOTP, btnVerifyOTP, btnUpdatePassword;
    private DatabaseReference databaseReference;
    private TextView OtpText,PasswordText,ConfirmPasswordText;
    private String generatedOTP;
    private String userEmail;

    // Brevo API Key (replace with your actual key)
    private final String BREVO_API_KEY = "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";
    private final String BREVO_API_URL = "https://api.sendinblue.com/v3/smtp/email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        // Initialize UI elements
        OtpText = findViewById(R.id.otptxt);
        PasswordText=findViewById(R.id.passwordtxt);
        ConfirmPasswordText=findViewById(R.id.confirmtxt);




        etEmail = findViewById(R.id.etEmail);
        etOTP = findViewById(R.id.etOTP);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = etEmail.getText().toString().trim();

                if (userEmail.isEmpty()) {
                    Toast.makeText(ForgotPass.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            generatedOTP = generateOTP();
                            sendOTPEmail(userEmail, generatedOTP);
                            Toast.makeText(ForgotPass.this, "OTP sent to " + userEmail, Toast.LENGTH_SHORT).show();
                            Toast.makeText(ForgotPass.this,"OTP is"+generatedOTP,Toast.LENGTH_LONG).show();
                            OtpText.setVisibility(View.VISIBLE);
                            etOTP.setVisibility(View.VISIBLE);
                            btnVerifyOTP.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(ForgotPass.this, "Email not registered", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ForgotPass.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = etOTP.getText().toString().trim();

                if (enteredOTP.isEmpty()) {
                    Toast.makeText(ForgotPass.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                } else if (enteredOTP.equals(generatedOTP)) {
                    Toast.makeText(ForgotPass.this, "OTP verified. Please enter your new password.", Toast.LENGTH_SHORT).show();
                    PasswordText.setVisibility(View.VISIBLE);
                    ConfirmPasswordText.setVisibility(View.VISIBLE);
                    etNewPassword.setVisibility(View.VISIBLE);

                    etConfirmPassword.setVisibility(View.VISIBLE);
                    btnUpdatePassword.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ForgotPass.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ForgotPass.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ForgotPass.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    userSnapshot.getRef().child("password").setValue(newPassword);
                                    Toast.makeText(ForgotPass.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                Toast.makeText(ForgotPass.this, "Error: Unable to update password", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ForgotPass.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Helper method to generate a 6-digit OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Method to send OTP email using Brevo
    private void sendOTPEmail(String toEmail, String otp) {
        OkHttpClient client = new OkHttpClient();

        // Create the JSON body for Brevo API
        String emailBody = "{"
                + "\"sender\": {\"name\": \"EasyPark\", \"email\": \"skylords124@gmail.com\"},"
                + "\"to\": [{\"email\": \"" + toEmail + "\"}],"
                + "\"subject\": \"Your OTP Code\","
                + "\"htmlContent\": \"Your OTP is: " + otp + "\","
                + "\"name\": \"OTP Campaign\""
                + "}";

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
                runOnUiThread(() -> Toast.makeText(ForgotPass.this, "Failed to send OTP email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    runOnUiThread(() -> Toast.makeText(ForgotPass.this, "Failed to send OTP email. Response code: " + response.code() + " Body: " + responseBody, Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ForgotPass.this, "OTP email sent successfully", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
