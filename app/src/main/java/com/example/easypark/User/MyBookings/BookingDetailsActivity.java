package com.example.easypark.User.MyBookings;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.easypark.R;

public class BookingDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        TextView locationNameView = findViewById(R.id.location_name);
        TextView statusView = findViewById(R.id.status);
        TextView amt = findViewById(R.id.amount);
        TextView uniqId=findViewById(R.id.unq);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            locationNameView.setText(extras.getString("LocationName"));
            statusView.setText(extras.getString("status"));
            amt.setText(extras.getString("totalAmount"));
            uniqId.setText(extras.getString("uniqueId"));
        }
    }
}
