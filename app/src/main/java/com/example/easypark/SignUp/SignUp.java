package com.example.easypark.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easypark.R;
import com.example.easypark.SignIn.SignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    // Declare the UI elements
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnSubmit;

    private DatabaseReference databaseReference;
    private TextView AlreadyUserBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the sign-up layout
        setContentView(R.layout.activity_sign_up);

        // Initialize UI elements
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword=findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        AlreadyUserBtn=findViewById(R.id.tvLogin);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        AlreadyUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword=etConfirmPassword.getText().toString().trim();
                if(!password.equals(confirmPassword)){
                    Toast.makeText(SignUp.this, "Password and confirm password should be same", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if the email already exists in the database
                    databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(SignUp.this, "Email already registered", Toast.LENGTH_SHORT).show();
                            } else {
                                //  registration
                                UserRegistration user = new UserRegistration(username, email, phoneNumber, password);
                                databaseReference.push().setValue(user).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Registration Successful,Please Login", Toast.LENGTH_SHORT).show();

                                        etUsername.setText("");
                                        etEmail.setText("");
                                        etPhoneNumber.setText("");
                                        etPassword.setText("");
                                        Intent intent = new Intent(SignUp.this, SignIn.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(SignUp.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(SignUp.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


    }
}
