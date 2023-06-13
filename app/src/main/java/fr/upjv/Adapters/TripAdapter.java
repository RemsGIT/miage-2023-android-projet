package fr.upjv.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.upjv.Model.Trip;
import fr.upjv.ViewHolders.TripViewHolder;
import fr.upjv.miage_2023_android_projet.R;

public class TripAdapter extends RecyclerView.Adapter<TripViewHolder> {

    private List<Trip> trips;

    public TripAdapter(List<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View v = layoutInflater.inflate(R.layout.list_item_trip, parent, false);

        return new TripViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.visualizeTrip(trips.get(position));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }
}
