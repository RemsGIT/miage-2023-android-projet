package fr.upjv;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Picture;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    private Trip trip;

    // Init firebase
    private FirebaseFirestore firebaseFirestore;
    private ListenerRegistration coordinatesListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        this.firebaseFirestore = FirebaseFirestore.getInstance();

        // Init location manager
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        this.trip = (Trip) getIntent().getSerializableExtra("current_trip");

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        IMapController mapController = map.getController();
        mapController.setCenter(new GeoPoint(49.8941, 2.2958));
        mapController.setZoom(18);

        // Display coordinates on the map
        this.showAllCoordinates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDetach();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // L'autorisation de localisation a été accordée
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        this.createMarkerAtUserPosition(location);

        // Arrêter les mises à jour de la localisation après l'obtention de la première position
        locationManager.removeUpdates(this);
    }

    /**
     * Create a user marker at position parameter
     * @param location
     */
    private void createMarkerAtUserPosition(Location location) {
        // Récupérer la nouvelle localisation de l'utilisateur
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Centrer la carte sur la position de l'utilisateur
        map.getController().setCenter(new GeoPoint(latitude, longitude));

        // Add marker at user's position
        Marker userMarker = new Marker(map);
        userMarker.setPosition(new GeoPoint(latitude, longitude));
        userMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.person));
        userMarker.setTitle(getString(R.string.info_user_position));
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userMarker);
    }

    /**
     * Back to the trip's page
     * @param view
     */
    public void onClickReturn(View view) {
        finish();
    }

    /**
     * Display the coordinates, pictures, user location
     */
    private void showAllCoordinates() {
        Query coordinatesQuery = firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .orderBy("createdAt", Query.Direction.ASCENDING);

        this.coordinatesListenerRegistration = coordinatesQuery.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                return;
            }

            this.trip.setCoordinates(snapshot.toObjects(Coordinate.class));

            // Clear the map and create markers
            map.getOverlays().clear();

            List<GeoPoint> points = new ArrayList<>();
            for (Coordinate coordinate : trip.getCoordinates()) {
                Marker pointMarker = new Marker(map);

                Double latitude = coordinate.getCoords().getLatitude();
                Double longitude = coordinate.getCoords().getLongitude();


                GeoPoint point = new GeoPoint(latitude, longitude);

                pointMarker.setPosition(point);
                pointMarker.setTitle(getString(R.string.info_coordinate_saved));
                pointMarker.setIcon(getResources().getDrawable(R.drawable.red_circle_svgrepo_com));
                pointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                map.getOverlays().add(pointMarker);

                points.add(point);
            }
            this.displayLineBetweenCoordinates(points);

            // Display all pictures
            this.showAllPictures();

            // Display user's position
            this.getUserLocation();

            // Refresh map
            map.invalidate();
        });
    }

    /**
     * Set firebase listener to pictures
     */
    private void showAllPictures() {
        for (Picture picture : trip.getPictures()) {
            Marker pictureMarker = new Marker(map);

            Double latitude = picture.getCoords().getLatitude();
            Double longitude = picture.getCoords().getLongitude();

            GeoPoint point = new GeoPoint(latitude, longitude);

            pictureMarker.setPosition(point);

            pictureMarker.setIcon(getDrawable(R.drawable.ic_camera));

            pictureMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            // Open picture when clicking on the icon
            pictureMarker.setOnMarkerClickListener((marker, mapView) -> {
                showImagePopup(picture.getImageUrl());
                return true;
            });

            map.getOverlays().add(pictureMarker);
        }
    }

    /**
     * Display red line between the coordinates
     * @param points
     */
    private void displayLineBetweenCoordinates(List<GeoPoint> points) {
        Polyline line = new Polyline();
        line.setPoints(points);
        line.setColor(Color.RED);
        line.setWidth(5f);

        map.getOverlayManager().add(line);
    }

    /**
     * Open the image in popup when clicking on the camera icon, using "picasso"
     * @param imageUrl
     */
    private void showImagePopup(String imageUrl) {
        Dialog dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.popup_image);

        ImageView imageView = dialog.findViewById(R.id.id_picture_popup_image);

        // Get screen's dimension
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Get 80% of screen's width
        int targetWidth = (int) (screenWidth * 0.8);

        // Update imageView's width
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = targetWidth;

        imageView.setLayoutParams(layoutParams);

        // Load the image in the imageView
        Picasso.get().load(imageUrl).into(imageView);

        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (coordinatesListenerRegistration != null) {
            coordinatesListenerRegistration.remove();
        }
    }

    /**
     * Get the last known user's location or update if no exists and create marker
     */
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            // Permissions OK
            Location userLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if(Objects.nonNull(userLoc)) {
                this.createMarkerAtUserPosition(userLoc);
            }
        } else {
            // Ask permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}