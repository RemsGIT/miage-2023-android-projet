package fr.upjv.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Objects;

import fr.upjv.Model.Coordinate;
import fr.upjv.miage_2023_android_projet.R;

public class CoordinatesViewHolder extends RecyclerView.ViewHolder {

    private TextView latitude;
    private TextView longitude;
    private TextView createdAt;


    public CoordinatesViewHolder(@NonNull View itemView) {
        super(itemView);

        latitude = itemView.findViewById(R.id.list_item_coordinates_lat);
        longitude = itemView.findViewById(R.id.list_item_coordinates_long);
        createdAt = itemView.findViewById(R.id.list_item_coordinates_createdAt);
    }

    public void vizualizeCoordinates(Coordinate coordinate) {
        if(Objects.nonNull(coordinate)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy H:mm");

            latitude.setText(String.valueOf(coordinate.getCoords().getLatitude()));
            longitude.setText(String.valueOf(coordinate.getCoords().getLongitude()));
            createdAt.setText(dateFormatter.format(coordinate.getCreatedAt().toDate()));
        }
    }
}