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

public class CreateTripActivity extends AppCompatActivity {

    private TextView textPeriod;
    private SeekBar periodSeekBar;

    private EditText editTextName;
    private EditText editTextDateDebut;
    private EditText editTextDateFin;

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
        this.editTextDateFin = findViewById(R.id.id_createtrip_edit_fin);

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

        // Set default date
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        this.editTextDateDebut.setText(dateFormatter.format(date));
        this.disableEditText(this.editTextDateDebut);

    }

    public void onClickReturn(View view) {
        finish();
    }

    public void onClickSubmitForm(View view) {
        String name = this.editTextName.getText().toString();
        String debut = this.editTextDateDebut.getText().toString();
        String fin = this.editTextDateFin.getText().toString();
        Integer period = this.periodSeekBar.getProgress();

        // get mAuth et add uuid

        Trip newTrip = new Trip(name, debut,mAuth.getCurrentUser().getUid(), fin, period, true);

        Toast.makeText(this, "Soumission du formulaire", Toast.LENGTH_SHORT).show();
        System.out.println(name);
        System.out.println(debut);
        System.out.println(fin);
        System.out.println(period);

        this.saveTrip(newTrip);
    }

    private void saveTrip(Trip data){
        this.firebaseFirestore
                .collection("voyages")
                .document()
                .set(data)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Voyage créé", Toast.LENGTH_SHORT).show();

                    // Redirect the user to home page
                    Intent intentHome = new Intent(this, MainActivity.class);
                    startActivity(intentHome);
                });
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
    }

}