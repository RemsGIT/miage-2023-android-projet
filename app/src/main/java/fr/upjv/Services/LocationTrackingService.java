package fr.upjv.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

import fr.upjv.Model.Coordinate;
import fr.upjv.Utils.SerializableGeoPoint;
import fr.upjv.Utils.SerializableTimestamp;

public class LocationTrackingService extends Service {
    private static final long INTERVAL = 10000; // Interval in milliseconds
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler handler;
    private Runnable runnable;
    private long lastUpdateTime = 0;

    private String tripDocID;
    private Integer period;

    private FirebaseFirestore firebaseFirestore;


    @Override
    public void onCreate() {
        super.onCreate();

        // Init firestore
        firebaseFirestore = FirebaseFirestore.getInstance();

        handler = new Handler();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime >= period) {
                    lastUpdateTime = currentTime;

                    // Enregistrer la nouvelle localisation dans Firebase ici
                    System.out.println("enregistrement vers firebase");

                    Timestamp now = new Timestamp(new Date());
                    GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Coordinate newCoordinate = new Coordinate(new SerializableTimestamp(now), new SerializableGeoPoint(point));

                    firebaseFirestore
                            .collection("voyages")
                            .document(tripDocID) // trip id
                            .collection("coordinates")
                            .add(newCoordinate)
                            .addOnCompleteListener(task -> {
                                System.out.println(task.isSuccessful() ? "nouvelle pos firebase" : "erreur firebase new pos");
                            });
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Mise à jour toutes les X minutes

        this.period = intent.getIntExtra("period", 5) * 60 * 1000;
        this.tripDocID = intent.getStringExtra("tripdocid");

        handler.post(new Runnable() {
            @Override
            public void run() {
                System.out.println("runnable");
                requestLocationUpdates();
                handler.postDelayed(this, period); // Utilisez simplement la période en millisecondes
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        stopLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, locationListener);
        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}