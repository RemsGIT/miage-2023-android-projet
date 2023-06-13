package fr.upjv.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class TripViewHolder extends RecyclerView.ViewHolder {

    private TextView titleTextView;
    private TextView dateTextView;

    public TripViewHolder(@NonNull View itemView) {
        super(itemView);

        titleTextView = itemView.findViewById(R.id.list_item_trip_text_title);
        dateTextView = itemView.findViewById(R.id.list_item_trip_text_date);

    }

    public void visualizeTrip(Trip trip) {
        if(Objects.nonNull(trip)) {
            titleTextView.setText(trip.getName());
            dateTextView.setText(trip.getStart());
        }
    }
}
