package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.upjv.Adapters.CoordinatesAdapter;
import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class TripActivity extends AppCompatActivity {

    private Trip trip;

    private RecyclerView recyclerView;

    private FirebaseFirestore firebaseFirestore;

    private TextView tripNameTextView;
    private TextView startDateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        // Init UI elements
        this.tripNameTextView = findViewById(R.id.id_trip_text_name);
        this.startDateTextView = findViewById(R.id.id_trip_text_debut);

        // Init firestore
        this.firebaseFirestore = FirebaseFirestore.getInstance();

        this.trip =  (Trip) getIntent().getSerializableExtra("current_trip");

        this.recyclerView = findViewById(R.id.id_trip_recyclerView);

        // Load coordinates of a trip
        this.initCoordinates();

        // Rename text view
        this.tripNameTextView.setText(this.trip.getName());

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        this.startDateTextView.setText(dateFormatter.format(new Date(this.trip.getStart())));


    }

    public void onClickRedirectToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);

        intent.putExtra("current_trip", this.trip);

        startActivity(intent);;
    }

    private void initCoordinates(){
        // Search coordinates on Firebase
        this.firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        this.trip.setCoordinates(task.getResult().toObjects(Coordinate.class));
                    }

                    CoordinatesAdapter ca = new CoordinatesAdapter(this.trip.getCoordinates());
                    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    this.recyclerView.setAdapter(ca);
                });
    }

    public void onClickStopTrip(View view) {

        trip.setIsActive(false);

        this.firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .set(trip)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, "Le voyage est terminé", Toast.LENGTH_SHORT).show();

                        // Redirect user to home page
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                });
    }
}