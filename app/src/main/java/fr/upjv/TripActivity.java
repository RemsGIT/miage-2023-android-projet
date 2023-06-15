package fr.upjv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.upjv.Adapters.CoordinatesAdapter;
import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class TripActivity extends AppCompatActivity {

    private boolean firstLoad = true;

    private Trip trip;

    private RecyclerView recyclerView;

    private FirebaseFirestore firebaseFirestore;

    private boolean isListenerActive = false;


    private TextView tripNameTextView;
    private TextView startDateTextView;

    // FLOATING ACTION BUTTONS
    private FloatingActionButton fabMain;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabMail;
    private FloatingActionButton fabPosition;

    private Boolean clicked;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;

    private CoordinatesAdapter coordinatesAdapter;

    private List<Coordinate> coordinatesForRecylerView;
    private ListenerRegistration coordinatesListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        coordinatesForRecylerView = new ArrayList<>();

        // Init floating action buttons
        this.initFloatingActionButton();
        this.clicked = false;

        // Init UI elements
        this.tripNameTextView = findViewById(R.id.id_trip_text_name);
        this.startDateTextView = findViewById(R.id.id_trip_text_debut);

        // Init firestore
        this.firebaseFirestore = FirebaseFirestore.getInstance();

        this.trip = (Trip) getIntent().getSerializableExtra("current_trip");

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

        startActivity(intent);
        ;
    }

    private void initCoordinates() {

        Query coordinatesQuery = firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .orderBy("createdAt", Query.Direction.ASCENDING);


        coordinatesAdapter = new CoordinatesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(coordinatesAdapter);


        coordinatesListener = coordinatesQuery.addSnapshotListener((snapshot, error) -> {
            if(error != null) {
                return;
            }

            List<Coordinate> coordinates = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                Coordinate coordinate = document.toObject(Coordinate.class);
                coordinates.add(coordinate);
            }

            System.out.println("maj");

            trip.setCoordinates(coordinates);
            coordinatesAdapter.setCoordinates(coordinates);
            coordinatesAdapter.notifyDataSetChanged();

        });
    }

    public void onClickStopTrip(View view) {

        trip.setIsActive(false);

        this.firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .set(trip)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Le voyage est terminé", Toast.LENGTH_SHORT).show();

                        // Redirect user to home page
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        startActivity(mainIntent);

                        // Stop the location listener
                        Intent stopIntent = new Intent("STOP_LOCATION_SERVICE");
                        sendBroadcast(stopIntent);
                    }
                });
    }

    private void initFloatingActionButton() {
        this.fabMain = findViewById(R.id.id_trip_fab_main);
        this.fabCamera = findViewById(R.id.id_trip_fab_camera);
        this.fabMail = findViewById(R.id.id_trip_fab_mail);
        this.fabPosition = findViewById(R.id.id_trip_fab_position);

        // Init animations
        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
    }

    public void onClickFabMain(View view) {
        setVisibility(clicked);
        setAnimation(clicked);
        setClickabled(clicked);

        clicked = !clicked;
    }

    public void onClickFabCamera(View view) {
        Toast.makeText(this, "Ouvrir la camera", Toast.LENGTH_SHORT).show();
    }

    public void onClickFabMail(View view) {
        Toast.makeText(this, "Envoyer un mail", Toast.LENGTH_SHORT).show();
    }

    public void onClickFabPosition(View view) {
        Toast.makeText(this, "Enregistrer la position", Toast.LENGTH_SHORT).show();
    }

    private void setVisibility(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.setVisibility(View.VISIBLE);
            this.fabMail.setVisibility(View.VISIBLE);
            this.fabPosition.setVisibility(View.VISIBLE);
        } else {
            this.fabCamera.setVisibility(View.INVISIBLE);
            this.fabMail.setVisibility(View.INVISIBLE);
            this.fabPosition.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.startAnimation(fromBottom);
            this.fabMail.startAnimation(fromBottom);
            this.fabPosition.startAnimation(fromBottom);

            this.fabMain.startAnimation(rotateOpen);
        } else {
            this.fabCamera.startAnimation(toBottom);
            this.fabMail.startAnimation(toBottom);
            this.fabPosition.startAnimation(toBottom);

            this.fabMain.startAnimation(rotateClose);
        }
    }

    private void setClickabled(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.setClickable(true);
            this.fabMail.setClickable(true);
            this.fabPosition.setClickable(true);
        } else {
            this.fabCamera.setClickable(false);
            this.fabMail.setClickable(false);
            this.fabPosition.setClickable(false);
        }
    }

    private void addFirebaseCoordinatesListener() {
        if (isListenerActive) {
            return; // Si le listener est déjà actif, évitez de l'ajouter à nouveau
        }
        isListenerActive = true;

        System.out.println("START LISTENER");
        this.firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    System.out.println("ok");

                    List<Coordinate> coordinatesUpdated = value.toObjects(Coordinate.class);
                    this.trip.addAllCoordinates(coordinatesUpdated);

                    // Notify changes
                    this.coordinatesAdapter.notifyDataSetChanged();
                });
    }
}