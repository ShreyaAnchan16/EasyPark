package com.example.easypark.User.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.SignIn.SignIn;
import com.example.easypark.User.HomePage;
import com.example.easypark.SignIn.SignIn;
import com.example.easypark.User.MyBookings.MyBookingsListing;
import com.example.easypark.User.Settings.Feedback.FeedbackActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsMainPage extends AppCompatActivity {

    private static final String KEY_SESSION_ACTIVE = "session_active";
    private static final String PREF_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private TextView name;
    private TextView emails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_main_page);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = sharedPreferences.getString(KEY_EMAIL, "N/A");
        String username = sharedPreferences.getString(KEY_USERNAME, "N/A");
        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        name=findViewById(R.id.settingnameTxt);
        emails=findViewById(R.id.settingnameEditText);

        name.setText(username);
        emails.setText(email);



        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(SettingsMainPage.this, HomePage.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (item.getItemId() == R.id.nav_bookings) {
                    startActivity(new Intent(SettingsMainPage.this, MyBookingsListing.class));
                    overridePendingTransition(0, 0);
                } else if (item.getItemId() == R.id.nav_settings) {
                    return true;
                }
                return false;
            }
        });

        // Logout button functionality
        Button logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();


                Intent intent = new Intent(SettingsMainPage.this, SignIn.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        Button HelpSuppButton=findViewById(R.id.HelpSuppButton);
        HelpSuppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  Intent intent = new Intent(SettingsMainPage.this, SignIn.class);


              //  startActivity(intent);
              //  finish();
                Log.d("SettingsMainPage", "Logout button clicked");
                Intent intent = new Intent(SettingsMainPage.this, FeedbackActivity.class);
                startActivity(intent);
            }
        });

    }
}
