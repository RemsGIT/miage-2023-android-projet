package fr.upjv.Forms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.upjv.BroadcastReceiver.LocationBroadcastReceiver;
import fr.upjv.MainActivity;
import fr.upjv.Model.Trip;
import fr.upjv.Services.LocationTrackingService;
import fr.upjv.miage_2023_android_projet.R;

/**
 * Form to create new trip
 */
public class CreateTripActivity extends AppCompatActivity {

    private TextView textPeriod;
    private SeekBar periodSeekBar;

    private EditText editTextName;
    private EditText editTextDateDebut;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        this.textPeriod = findViewById(R.id.id_createtrip_text_sliderValue);
        this.periodSeekBar = findViewById(R.id.id_createtrip_seekBar_period);
        this.editTextName = findViewById(R.id.id_createtrip_edit_name);
        this.editTextDateDebut = findViewById(R.id.id_createtrip_edit_debut);

        // Init firebase
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        // On change slider
        this.periodSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0) {
                    textPeriod.setText("Manuellement");
                }
                else {
                    String textHour = progress > 1 ? "minutes" : "minute";
                    textPeriod.setText(progress + " " +  textHour);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set default start date to today : user can't modify it
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        this.editTextDateDebut.setText(dateFormatter.format(date));
        this.disableEditText(this.editTextDateDebut);

    }

    public void onClickReturn(View view) {
        finish();
    }

    /**
     * Submit the form : format data and save to firestore
     * @param view
     */
    public void onClickSubmitForm(View view) {
        String name = this.editTextName.getText().toString().trim();
        String debut = this.editTextDateDebut.getText().toString();
        Integer period = this.periodSeekBar.getProgress();

        if(name.equals("") || debut.equals("")) {
            Toast.makeText(this, R.string.error_fields_missing, Toast.LENGTH_SHORT).show();

            return;
        }

        Trip newTrip = new Trip(name, debut,mAuth.getCurrentUser().getUid(), period, true);

        Toast.makeText(this, R.string.info_saving, Toast.LENGTH_SHORT).show();

        this.saveTrip(newTrip);
    }

    /**
     * Save the new trip to firestore and redirect user if saved
     * @param data
     */
    private void saveTrip(Trip data){
        this.firebaseFirestore
                .collection("voyages")
                .document()
                .set(data)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, R.string.success_trip_create, Toast.LENGTH_SHORT).show();

                        // Redirect the user to home page
                        Intent intentHome = new Intent(this, MainActivity.class);
                        startActivity(intentHome);
                    }
                    else {
                        Toast.makeText(this, R.string.error_trip_create, Toast.LENGTH_SHORT).show();
                    }

                });
    }

    /**
     * Util to disable an edit text
     * @param editText
     */
    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }

}