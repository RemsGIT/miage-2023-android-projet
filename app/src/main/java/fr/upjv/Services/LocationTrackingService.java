package fr.upjv.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.UUID;

import fr.upjv.Model.Coordinate;
import fr.upjv.Utils.SerializableGeoPoint;
import fr.upjv.Utils.SerializableTimestamp;
import fr.upjv.miage_2023_android_projet.R;

public class LocationTrackingService extends Service {
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

                    // Save the new position to firebase
                    Timestamp now = new Timestamp(new Date());
                    GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Coordinate newCoordinate = new Coordinate(new SerializableTimestamp(now), new SerializableGeoPoint(point));

                    firebaseFirestore
                            .collection("voyages")
                            .document(tripDocID)
                            .collection("coordinates")
                            .add(newCoordinate)
                            .addOnCompleteListener(task -> {});
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        createNotificationChannel();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Mise à jour toutes les X minutes
        this.period = intent.getIntExtra("period", 5) * 60 * 1000;
        this.tripDocID = intent.getStringExtra("tripdocid");

        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
                handler.postDelayed(this, period);
            }
        });

        // Service background
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "my_channel_id")
                .setContentTitle("Localisation")
                .setContentText("Enregistrement de votre position")
                .setSmallIcon(R.drawable.app_icon);
        Notification notification = notificationBuilder.build();
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        stopForeground(true);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Check if notification's channel does not already exist
            String channelId = "my_channel_id";
            if (notificationManager.getNotificationChannel(channelId) == null) {
                // Create the notification's channel
                CharSequence channelName = "My Channel";
                String channelDescription = "Description of my channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);

                channel.enableLights(false);
                channel.enableVibration(false);

                // Link the channel to notification manager
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}