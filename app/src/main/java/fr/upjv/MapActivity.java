package fr.upjv;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    private Trip trip;

    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
            }
            catch (Exception e) {
                // Position unknowned
                System.out.println(e.getMessage());
            }

        }

        // Affichage des coordonnées enregistrées dans l'app
        this.showAllCoordinates();
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
            } else {
                // L'autorisation de localisation a été refusée
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

    private void createMarkerAtUserPosition(GeoPoint geoPoint){
        // Add marker at user's position
        Marker userMarker = new Marker(map);
        userMarker.setPosition(geoPoint);
        userMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.person));
        userMarker.setTitle("Votre position");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(userMarker);
    }

    public void onClickReturn(View view) {
        finish();
    }

    private void showAllCoordinates() {
        for (Coordinate coordinate : trip.getCoordinates()) {
            Marker pointMarker = new Marker(map);

            Double latitude = coordinate.getCoords().getLatitude();
            Double longitude = coordinate.getCoords().getLongitude();

            pointMarker.setPosition(new GeoPoint(latitude, longitude));
            pointMarker.setTitle("Position enregistrée \npendant votre voyage");
            pointMarker.setIcon(getResources().getDrawable(R.drawable.red_circle_svgrepo_com));
            pointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
            map.getOverlays().add(pointMarker);
        }
    }
}