package com.example.easypark.SignIn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.easypark.ForgotPass.ForgotPass;
import com.example.easypark.R;
import com.example.easypark.SignUp.SignUp;
import com.example.easypark.User.HomePage;
import com.example.easypark.admin.adminHomePage;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_SESSION_ACTIVE = "session_active";
    private static final String KEY_LOGIN_TIME = "login_time";
    private static final long SESSION_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        // Check if session exists and is still valid
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean sessionActive = sharedPreferences.getBoolean(KEY_SESSION_ACTIVE, false);
        long loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        if (sessionActive && !isSessionExpired(loginTime, currentTime)) {
            // Redirect to HomePage if session is active and not expired
            Intent intent = new Intent(SignIn.this, HomePage.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.textView2);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignIn.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                if(email.equals("admin@gmail.com")&&password.equals("admin")){
                    Toast.makeText(SignIn.this,"Admin dahsboard loading",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignIn.this, adminHomePage.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String dbPassword = userSnapshot.child("password").getValue(String.class);
                                String dbUsername = userSnapshot.child("username").getValue(String.class);
                                String dbEmailID = userSnapshot.child("email").getValue(String.class);
                                if (dbPassword != null && dbPassword.equals(password)) {
                                    Snackbar.make(findViewById(android.R.id.content), "Login Successful", Snackbar.LENGTH_LONG)
                                            .setBackgroundTint(Color.parseColor("#4CAF50")) // Set background color to green
                                            .setActionTextColor(Color.parseColor("#FFFFFF")) // Set action text color to white (if needed)
                                            .show();


                                    //Toast.makeText(SignIn.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                    // Store the user information and session
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(KEY_SESSION_ACTIVE, true);
                                    editor.putString("email", dbEmailID);
                                    editor.putString("username", dbUsername);
                                    editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
                                    editor.apply();

                                    Intent intent = new Intent(SignIn.this, HomePage.class);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                            //Toast.makeText(SignIn.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            Snackbar.make(findViewById(android.R.id.content), "Incorrect Password", Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(Color.parseColor("#FF0000"))
                                    .setActionTextColor(Color.parseColor("#FFFFFF"))
                                    .show();

                        } else {
                            Toast.makeText(SignIn.this, "Email not registered", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SignIn.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, ForgotPass.class);
            startActivity(intent);
        });
    }

    private boolean isSessionExpired(long loginTime, long currentTime) {
        return (currentTime - loginTime) > SESSION_DURATION_MS;
    }
}
