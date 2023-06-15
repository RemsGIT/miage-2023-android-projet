package fr.upjv;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.squareup.picasso.Picasso;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;


// TODO LIRE CI DESSOUS
// DANS TRIP IL FAUT UNE LISTE DE <PICTURES> AUSSI COMME POUR COORDINATES: POUVOIR LES RÉCUPÉRER COMME COORDINATES
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
        System.out.println("return");
        finish();
    }

    private void showAllCoordinates() {
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
        }
        this.displayLineBetweenCoordinates(points);

    }

    private void showAllPictures() {

    }

    private void displayLineBetweenCoordinates(List<GeoPoint> points) {
        Polyline line = new Polyline();
        line.setPoints(points);
        line.setColor(Color.RED);
        line.setWidth(5f);

        map.getOverlayManager().add(line);
    }

    private void openPhoto(Uri imageUri) {
        // Search
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Vérifiez si l'application de galerie ou de visionneuse d'images est disponible
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Aucune application de galerie ou de visionneuse d'images n'est disponible
            Toast.makeText(this, "Aucune application de galerie trouvée", Toast.LENGTH_SHORT).show();
        }
    }


    private void testImageCloud() {
        String url = "https://firebasestorage.googleapis.com/v0/b/rgtravel-f8d2c.appspot.com/o/images%2Fimage5126787236995998348.jpg?alt=media&token=305fbb36-a2d8-49c6-bc19-447f83dd51cd";

        //Picasso.get().load(url).into(this.imageView);
    }
}