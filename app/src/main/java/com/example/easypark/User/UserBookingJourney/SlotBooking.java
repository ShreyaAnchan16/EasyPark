package com.example.easypark.User.UserBookingJourney;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.easypark.R;
import com.example.easypark.User.ParkingLocationModel.ParkingLocation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class SlotBooking extends AppCompatActivity {

    private int totalSlots;
    String locationName, locationtype, locationdetails;
    private Button[] slotButtons;
    private int selectedSlot = -1; // No slot selected by default
    private EditText dateInput, startTimeInput, endTimeInput, vehicleInput;

    // Calendar instances to store selected date and time
    private Calendar selectedDateCalendar = Calendar.getInstance();
    private Calendar startTimeCalendar = Calendar.getInstance();
    private Calendar endTimeCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_booking);
        ImageButton backButton = findViewById(R.id.ic_back_button);
        backButton.setOnClickListener(v -> onBackPressed());


        // Retrieve data from Intent
        Intent intent = getIntent();
        ParkingLocation location = (ParkingLocation) intent.getSerializableExtra("selectedParkingLocation");
        if (location == null) {
            Toast.makeText(this, "Parking location data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalSlots = Integer.parseInt(location.getNumberOfSlots());
        slotButtons = new Button[totalSlots]; // Initialize the slotButtons array
        locationName = location.getLocationName();
        locationtype = location.getLocationType();
        locationdetails = location.getLocationDetails();

        GridLayout slotGrid = findViewById(R.id.slot_grid);
        dateInput = findViewById(R.id.date_picker_input);
        startTimeInput = findViewById(R.id.start_time_input);
        endTimeInput = findViewById(R.id.end_time_input);
        vehicleInput = findViewById(R.id.vehicle); // Initialize vehicle input field

        // Setup slot buttons dynamically
        setupSlotButtons(slotGrid);

        // Setup date picker
        dateInput.setOnClickListener(v -> openDatePicker());

        // Setup time pickers
        startTimeInput.setOnClickListener(v -> openTimePicker(startTimeCalendar, startTimeInput));
        endTimeInput.setOnClickListener(v -> openTimePicker(endTimeCalendar, endTimeInput));

        // Setup vehicle number to uppercase
        vehicleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    vehicleInput.setText(input.toUpperCase());
                    vehicleInput.setSelection(vehicleInput.getText().length()); // Move cursor to the end
                }
            }
        });

        // Proceed button logic
        Button proceedButton = findViewById(R.id.proceed_button);
        proceedButton.setOnClickListener(v -> handleProceedButton());
    }

    // Setup dynamic slot buttons
    private void setupSlotButtons(GridLayout slotGrid) {
        for (int i = 0; i < totalSlots; i++) {
            Button slotButton = new Button(this);
            slotButton.setText(String.valueOf(i + 1));
            slotButton.setTag(i);
            slotButton.setTextColor(Color.BLACK);
            slotButton.setOnClickListener(v -> selectSlot((int) v.getTag()));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 150;
            params.height = 150;
            params.setMargins(10, 10, 10, 10);
            slotButton.setLayoutParams(params);
            slotGrid.addView(slotButton);

            slotButtons[i] = slotButton;
        }
    }

    // Open DatePickerDialog and restrict to today or future dates
    private void openDatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(SlotBooking.this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateCalendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateInput.setText(sdf.format(selectedDateCalendar.getTime()));
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent past dates
        datePickerDialog.show();
    }

    // Open TimePickerDialog
    private void openTimePicker(final Calendar calendar, final EditText inputField) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(SlotBooking.this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    inputField.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    // Handle Proceed button click
    private void handleProceedButton() {
        String vehicleNo = vehicleInput.getText().toString(); // Use vehicleInput
        String startTime = startTimeInput.getText().toString();
        String endTime = endTimeInput.getText().toString();
        String selectedDate = dateInput.getText().toString();

        if (selectedSlot == -1) {
            Toast.makeText(SlotBooking.this, "Please select a slot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate form inputs
        if (vehicleNo.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(SlotBooking.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!vehicleNo.matches("^[A-Z]{2}[ -][0-9]{1,2}(?: [A-Z])?(?: [A-Z]*)? [0-9]{4}$")) {
            Toast.makeText(SlotBooking.this, "Invalid vehicle number format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure the selected date is today or a future date
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        selectedDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedDateCalendar.set(Calendar.MINUTE, 0);
        selectedDateCalendar.set(Calendar.SECOND, 0);
        selectedDateCalendar.set(Calendar.MILLISECOND, 0);

        if (selectedDateCalendar.before(currentDate)) {
            Toast.makeText(SlotBooking.this, "Please choose today or a future date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure end time is after start time
        if (endTimeCalendar.before(startTimeCalendar)) {
            Toast.makeText(SlotBooking.this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Now check for existing bookings before proceeding
        checkExistingBookings(selectedDate, startTime, endTime, selectedSlot, vehicleNo, locationName);
    }

    // Select a slot and disable others
    private void selectSlot(int slotNumber) {
        if (selectedSlot != -1) {
            slotButtons[selectedSlot].setEnabled(true);
            slotButtons[selectedSlot].setBackgroundColor(Color.WHITE);
        }
        selectedSlot = slotNumber;
        slotButtons[selectedSlot].setEnabled(false);
        slotButtons[selectedSlot].setBackgroundColor(Color.GREEN);
    }

    // Check for existing bookings in Firebase
    private void checkExistingBookings(String selectedDate, String startTime, String endTime, int slotNumber, String vehicleNo, String locationName) {
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("BookingInformation");

        bookingsRef.orderByChild("date").equalTo(selectedDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isSlotBooked = false;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String existingSlotNumber = snapshot.child("slotNumber").getValue(String.class);
                            String existingStartTime = snapshot.child("startTime").getValue(String.class);
                            String existingEndTime = snapshot.child("endTime").getValue(String.class);
                            String bookingStatus = snapshot.child("status").getValue(String.class);
                            String existingLocationName = snapshot.child("LocationName").getValue(String.class);

                            // Check if the booking status is not "Completed" and the locations match
                            if (!"Completed".equals(bookingStatus) && locationName.equals(existingLocationName)) {
                                if (existingSlotNumber.equals(String.valueOf(slotNumber + 1))) {
                                    // Check if the time overlaps
                                    if (isTimeOverlap(startTime, endTime, existingStartTime, existingEndTime)) {
                                        isSlotBooked = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (isSlotBooked) {
                            Toast.makeText(SlotBooking.this, "Slot already booked for the selected time at the same location.", Toast.LENGTH_SHORT).show();
                        } else {
                            proceedWithBooking(vehicleNo, selectedDate, startTime, endTime, locationName, locationtype, locationdetails);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SlotBooking.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check if the booking times overlap
    private boolean isTimeOverlap(String startTime1, String endTime1, String startTime2, String endTime2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Calendar start1 = Calendar.getInstance();
            start1.setTime(sdf.parse(startTime1));

            Calendar end1 = Calendar.getInstance();
            end1.setTime(sdf.parse(endTime1));

            Calendar start2 = Calendar.getInstance();
            start2.setTime(sdf.parse(startTime2));

            Calendar end2 = Calendar.getInstance();
            end2.setTime(sdf.parse(endTime2));

            return start1.before(end2) && start2.before(end1);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Proceed with booking
    private void proceedWithBooking(String vehicleNo, String selectedDate, String startTime, String endTime, String LocationName, String LocationType, String LocationDetails) {
        HashMap<String, String> bookingDetails = new HashMap<>();
        bookingDetails.put("slotNumber", String.valueOf(selectedSlot + 1));
        bookingDetails.put("vehicleNumber", vehicleNo);
        bookingDetails.put("date", selectedDate);
        bookingDetails.put("startTime", startTime);
        bookingDetails.put("endTime", endTime);
        bookingDetails.put("LocationName", LocationName);
        bookingDetails.put("LocationType", LocationType);
        bookingDetails.put("LocationDetails", LocationDetails);

        Intent intent = new Intent(SlotBooking.this, UserPaymentPage.class);
        intent.putExtra("bookingDetails", bookingDetails);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Navigate back to the previous activity
        super.onBackPressed();
        finish(); // This ensures that the current activity is finished
    }
}
