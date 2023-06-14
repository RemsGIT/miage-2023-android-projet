package fr.upjv.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.upjv.Model.Coordinate;
import fr.upjv.ViewHolders.CoordinatesViewHolder;
import fr.upjv.miage_2023_android_projet.R;

public class CoordinatesAdapter extends RecyclerView.Adapter<CoordinatesViewHolder> {

    private List<Coordinate> coordinates;

    public CoordinatesAdapter(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @NonNull
    @Override
    public CoordinatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View v = layoutInflater.inflate(R.layout.list_item_coordinate, parent, false);

        return new CoordinatesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CoordinatesViewHolder holder, int position) {
        holder.vizualizeCoordinates(coordinates.get(position));
    }

    @Override
    public int getItemCount() {
        return coordinates != null ? coordinates.size() : 0;

    }
}
