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
import java.util.Observable;
import java.util.Observer;

import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Picture;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;


// TODO LIRE CI DESSOUS
// FONCTION APPELÉ QUAND REFRESH COORDONNÉES OU PICTURES
// POUR APRES LE CLEAR DE LA MAP: TOUT REMETTRE
// REMETTRE AUSSI L'ICONE DU USER POSITION QD REFRESH
public class MapActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    private Trip trip;

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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get user's location
            Location l = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            // Start point
            try {
                GeoPoint geoPoint = new GeoPoint(l.getLatitude(), l.getLongitude());

                // Center the map to user
                mapController.setCenter(geoPoint);

                this.createMarkerAtUserPosition(geoPoint);
            } catch (Exception e) {
                // Position unknowned
                System.out.println(e.getMessage());
            }

        }

        // Affichage des coordonnées enregistrées dans l'app
        this.showAllCoordinates();
        this.showAllPictures();
    }

    protected void onResume() {
        super.onResume();
        map.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // Demander les autorisations de localisation à l'utilisateur
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
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
        // Récupérer la nouvelle localisation de l'utilisateur
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Centrer la carte sur la position de l'utilisateur
        map.getController().setCenter(new GeoPoint(latitude, longitude));

        this.createMarkerAtUserPosition(new GeoPoint(latitude, longitude));

        // Arrêter les mises à jour de la localisation après l'obtention de la première position
        locationManager.removeUpdates(this);
    }

    private void createMarkerAtUserPosition(GeoPoint geoPoint) {
        // Add marker at user's position
        Marker userMarker = new Marker(map);
        userMarker.setPosition(geoPoint);
        userMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.person));
        userMarker.setTitle("Votre position");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userMarker);
    }

    public void onClickReturn(View view) {
        System.out.println("return");
        finish();
    }

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
                pointMarker.setTitle("Position enregistrée \npendant votre voyage");
                pointMarker.setIcon(getResources().getDrawable(R.drawable.red_circle_svgrepo_com));
                pointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                map.getOverlays().add(pointMarker);

                points.add(point);

                pointMarker.setOnMarkerClickListener(((marker, mapView) -> {
                    System.out.println("clear");
                    map.getOverlays().clear();
                    return true;
                }));
            }
            this.displayLineBetweenCoordinates(points);

            // Refresh map
            map.invalidate();
        });


    }

    private void showAllPictures() {
        for (Picture picture : trip.getPictures()) {
            Marker pictureMarker = new Marker(map);

            Double latitude = picture.getCoords().getLatitude();
            Double longitude = picture.getCoords().getLongitude();

            GeoPoint point = new GeoPoint(latitude, longitude);

            pictureMarker.setPosition(point);

            pictureMarker.setIcon(getDrawable(R.drawable.ic_camera));

            pictureMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            // Open picture on click on the icon
            pictureMarker.setOnMarkerClickListener((marker, mapView) -> {
                // Appeler votre fonction ici
                showImagePopup(picture.getImageUrl());
                return true;
            });

            map.getOverlays().add(pictureMarker);
        }
    }

    private void displayLineBetweenCoordinates(List<GeoPoint> points) {
        Polyline line = new Polyline();
        line.setPoints(points);
        line.setColor(Color.RED);
        line.setWidth(5f);

        map.getOverlayManager().add(line);
    }

    private void showImagePopup(String imageUrl) {
        Dialog dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.popup_image);

        ImageView imageView = dialog.findViewById(R.id.id_picture_popup_image);

        // Obtenir les dimensions de l'écran
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Calculer la taille souhaitée (90% de la taille de l'écran)
        int targetWidth = (int) (screenWidth * 0.8);

        // Appliquer les nouvelles dimensions à l'ImageView
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = targetWidth;

        imageView.setLayoutParams(layoutParams);

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
}