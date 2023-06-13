package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class MapActivity extends AppCompatActivity {

    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        this.trip =  (Trip) getIntent().getSerializableExtra("current_trip");


    }
}