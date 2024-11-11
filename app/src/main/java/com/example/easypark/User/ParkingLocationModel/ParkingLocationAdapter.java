package com.example.easypark.User.ParkingLocationModel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easypark.R;
import com.example.easypark.User.UserBookingJourney.SlotBooking;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ParkingLocationAdapter extends RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder> {

    private List<ParkingLocation> parkingLocationList;
    private Context context;

    public ParkingLocationAdapter(Context context, List<ParkingLocation> parkingLocationList) {
        this.context = context;
        this.parkingLocationList = parkingLocationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_parking_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingLocation location = parkingLocationList.get(position);
        holder.tvLocationName.setText(location.getLocationName());
        holder.tvLocationDetails.setText(location.getLocationDetails());
        holder.tvSlots.setText("Slots: " + location.getNumberOfSlots());
        holder.tvSlotType.setText("Slot type: " + location.getLocationType());
        Picasso.get().load(location.getImageUrl()).into(holder.ivParkingImage);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve data from SharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String email = sharedPreferences.getString("email", "N/A");
                String username = sharedPreferences.getString("username", "N/A");

                // Create an Intent to redirect to SlotBooking activity
                Intent intent = new Intent(context, SlotBooking.class);
                intent.putExtra("email", email);
                intent.putExtra("username", username);

                // Pass the specific parking location data
                intent.putExtra("selectedParkingLocation", location);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingLocationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivParkingImage;
        TextView tvLocationName;
        TextView tvLocationDetails;
        TextView tvSlots;
        TextView tvSlotType;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivParkingImage = itemView.findViewById(R.id.ivParkingImage);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvLocationDetails = itemView.findViewById(R.id.tvLocationDetails);
            tvSlots = itemView.findViewById(R.id.tvSlots);
            tvSlotType = itemView.findViewById(R.id.tvSlotType);
            cardView = itemView.findViewById(R.id.first);
        }
    }
}
