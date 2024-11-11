package com.example.easypark;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.SignIn.SignIn;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            // Redirect to SignIn page after 3 seconds
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivityWithAnimation(intent);
            finish();
        }, 3000); // Adding 3 seconds loading time
    }

    private void startActivityWithAnimation(Intent intent) {
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                MainActivity.this,
                R.anim.fade_in,
                R.anim.fade_out);

        startActivity(intent, options.toBundle());
    }
}
