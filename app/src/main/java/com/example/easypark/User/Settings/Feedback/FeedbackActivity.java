package com.example.easypark.User.Settings.Feedback;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.User.HomePage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText feedbackInput;
    private Button submitBtn;
    private ImageButton backButton;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private SharedPreferences sharedPreferences;

    private DatabaseReference feedbackRef; // Firebase database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "N/A");
        String username = sharedPreferences.getString(KEY_USERNAME, "N/A");

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        feedbackInput = findViewById(R.id.feedbackInput);
        submitBtn = findViewById(R.id.submitBtn);
        backButton = findViewById(R.id.ic_back_button);

        nameInput.setText(username);
        emailInput.setText(email);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        feedbackRef = database.getReference("Feedback");

        // Set up validation on submit button click
        submitBtn.setOnClickListener(v -> validateInputs());

        // Set up back button click listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Limit feedback input to 250 characters
        feedbackInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 250) {
                    feedbackInput.setText(s.subSequence(0, 250));
                    feedbackInput.setSelection(250);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateInputs() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String feedback = feedbackInput.getText().toString().trim();

        // Validate inputs
        if (feedback.isEmpty()) {
            Toast.makeText(FeedbackActivity.this, "Feedback field is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (feedback.length() > 250) {
            Toast.makeText(FeedbackActivity.this, "Feedback cannot exceed 250 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create feedback object
        FeedbackData feedbackData = new FeedbackData(name, email, feedback);

        // Push feedback data to Firebase
        feedbackRef.push().setValue(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    feedbackInput.setText(""); // Clear the input field after submission
                    Intent intent=new Intent(FeedbackActivity.this, HomePage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FeedbackActivity.this, "Failed to submit feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // This ensures that the current activity is finished
    }

    // FeedbackData class to represent feedback structure
    public static class FeedbackData {
        public String name;
        public String email;
        public String feedback;

        public FeedbackData() {
            // Default constructor required for calls to DataSnapshot.getValue(FeedbackData.class)
        }

        public FeedbackData(String name, String email, String feedback) {
            this.name = name;
            this.email = email;
            this.feedback = feedback;
        }
    }
}
