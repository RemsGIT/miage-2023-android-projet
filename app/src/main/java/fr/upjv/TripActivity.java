package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;

import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class TripActivity extends AppCompatActivity {

    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        this.trip =  (Trip) getIntent().getSerializableExtra("current_trip");

        System.out.println(trip.getName());
    }

    public void onClickRedirectToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);

        intent.putExtra("current_trip", this.trip);

        startActivity(intent);;
    }
}