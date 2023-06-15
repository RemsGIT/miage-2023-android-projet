package fr.upjv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.Map;
import java.util.Objects;

import fr.upjv.BroadcastReceiver.LocationBroadcastReceiver;
import fr.upjv.Forms.CreateTripActivity;
import fr.upjv.Model.Trip;
import fr.upjv.Services.LocationTrackingService;
import fr.upjv.miage_2023_android_projet.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseFirestore firebaseFirestore;

    // Ui components
    private TextView textNbTrip;
    private TextView btnActionTrip;
    private ImageView imageView;

    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textNbTrip = findViewById(R.id.main_text_nbtrip);
        this.btnActionTrip = findViewById(R.id.main_btn_actiontrip);
        this.imageView = findViewById(R.id.id_main_imageView);

        mAuth = FirebaseAuth.getInstance();
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the user
        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null) {
            // Redirect the user to login page if not connected
            System.out.println("pas connecté");
            redirectToLogin();
        }
        else {
            System.out.println(user.getEmail());

            // Check if there is an active trip
            this.firebaseFirestore
                    .collection("voyages")
                    .whereEqualTo("isActive", true)
                    .get()
                    .addOnCompleteListener(dataSnapshot -> {
                        if(dataSnapshot.isSuccessful()) {
                            if(!dataSnapshot.getResult().isEmpty()) {
                                this.currentTrip = dataSnapshot.getResult().toObjects(Trip.class).get(0);

                                // If there's an active trip : check if location service is running if period is periodic
                                if(!this.isLocationTrackingServiceRunning() && this.currentTrip.getPeriod() > 0) {
                                    // check permissions
                                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        // Permissions already accepted
                                        startLocationListenerOnPeriod(this.currentTrip.getPeriod(), this.currentTrip.getDocID());
                                    } else {
                                        // Ask permissions to user
                                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                    }
                                }

                            }
                        }

                        this.updateUI();
                    });
        }
    }

    private void redirectToLogin() {
        Intent intentLogin = new Intent(this, LoginActivity.class);

        startActivity(intentLogin);
    }

    private void redirectToTrip() {
        Intent intentTrip = new Intent(this, TripActivity.class);

        intentTrip.putExtra("current_trip", this.currentTrip);

        startActivity(intentTrip);
    }

    public void redirectToCreateTripForm(View view) {
        Intent intentCreateTrip = new Intent(this, CreateTripActivity.class);

        startActivity(intentCreateTrip);
    }

    private void updateUI(){
        if(Objects.nonNull(this.currentTrip)) {
            this.textNbTrip.setText("Vous avez un voyage en cours");
            this.btnActionTrip.setText("Mon voyage");

            this.imageView.setImageResource(R.drawable.background_trip);

            this.btnActionTrip.setOnClickListener(v -> redirectToTrip());
        }
    }

    /**
     * Able the app to know if the location service is already running
     * @return true if location tracking service is running
     */
    private Boolean isLocationTrackingServiceRunning () {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationTrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startLocationListenerOnPeriod(Integer period, String tripDocID) {
        LocationBroadcastReceiver broadcastReceiver = new LocationBroadcastReceiver();
        broadcastReceiver.setLocationService(new LocationTrackingService());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("STOP_LOCATION_SERVICE");
        registerReceiver(broadcastReceiver, intentFilter);

        //startService(new Intent(this, LocationTrackingService.class).putExtra("period", period).putExtra("tripdocid", tripDocID));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions accepted : start the service
                startLocationListenerOnPeriod(this.currentTrip.getPeriod(), this.currentTrip.getDocID());
            }
        }
    }
}