package fr.upjv;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.upjv.Adapters.CoordinatesAdapter;
import fr.upjv.Model.Coordinate;
import fr.upjv.Model.Picture;
import fr.upjv.Model.Trip;
import fr.upjv.Utils.SerializableGeoPoint;
import fr.upjv.Utils.SerializableTimestamp;
import fr.upjv.miage_2023_android_projet.R;

public class TripActivity extends AppCompatActivity {
    private Trip trip;

    private RecyclerView recyclerView;


    // FIRESTORE VARS
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private TextView tripNameTextView;
    private TextView startDateTextView;

    private ActivityResultLauncher<Intent> activityLauncher;

    // FLOATING ACTION BUTTONS
    private FloatingActionButton fabMain;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabMail;
    private FloatingActionButton fabPosition;

    private Boolean clicked;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;

    private CoordinatesAdapter coordinatesAdapter;

    private ListenerRegistration coordinatesListenerRegistration;
    private ListenerRegistration picturesListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        // Init floating action buttons
        this.initFloatingActionButton();
        this.clicked = false;

        // Init UI elements
        this.tripNameTextView = findViewById(R.id.id_trip_text_name);
        this.startDateTextView = findViewById(R.id.id_trip_text_debut);

        // Init firestore
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();

        this.trip = (Trip) getIntent().getSerializableExtra("current_trip");

        this.recyclerView = findViewById(R.id.id_trip_recyclerView);

        // Rename text view
        this.tripNameTextView.setText(this.trip.getName());

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        this.startDateTextView.setText(dateFormatter.format(new Date(this.trip.getStart())));


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Load coordinates of a trip
        this.initCoordinates();
        this.loadPictures();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (coordinatesListenerRegistration != null) {
            coordinatesListenerRegistration.remove();
        }

        if(picturesListenerRegistration != null) {
            picturesListenerRegistration.remove();
        }
    }

    public void onClickRedirectToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);

        intent.putExtra("current_trip", this.trip);
        startActivity(intent);
    }

    public void onClickStopTrip(View view) {

        trip.setIsActive(false);

        this.firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .set(trip)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Le voyage est terminé", Toast.LENGTH_SHORT).show();

                        // Redirect user to home page
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        startActivity(mainIntent);

                        // Stop the location listener
                        Intent stopIntent = new Intent("STOP_LOCATION_SERVICE");
                        sendBroadcast(stopIntent);
                    }
                });
    }

    public void onClickFabMain(View view) {
        setVisibility(clicked);
        setAnimation(clicked);
        setClickabled(clicked);

        clicked = !clicked;
    }

    public void onClickFabCamera(View view) {
        // Vérifier si la permission de la caméra est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // La permission de la caméra n'est pas accordée, demander à l'utilisateur de l'accorder
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            // La permission de la caméra est déjà accordée, ouvrir la caméra
            openCamera();
        }
    }

    public void onClickFabMail(View view) {
        Toast.makeText(this, "Envoyer un mail", Toast.LENGTH_SHORT).show();
        this.exportCoordinatesToGPXAndKML();
    }

    public void onClickFabPosition(View view) {
        // Save user's position manually
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER; // Utilisation du fournisseur GPS

        if (locationManager != null && locationManager.isProviderEnabled(provider)) {
            // Vérifier les autorisations de localisation ici si nécessaire

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Location lastKnownLocation = locationManager.getLastKnownLocation(provider);


                try {
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();

                    Timestamp now = new Timestamp(new Date());
                    GeoPoint point = new GeoPoint(latitude, longitude);
                    Coordinate newCoordinate = new Coordinate(new SerializableTimestamp(now), new SerializableGeoPoint(point));

                    firebaseFirestore
                            .collection("voyages")
                            .document(this.trip.getDocID()) // trip id
                            .collection("coordinates")
                            .add(newCoordinate)
                            .addOnCompleteListener(task -> {
                                String message = task.isSuccessful() ? "Position enregistrée manuellement" : "Erreur lors de l'enregistrement de la position";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            });
                }
                catch (Exception e) {
                    Toast.makeText(this, "Votre localisation ne peut pas être récupérée. Veuillez réessayer plus tard", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Vous n'avez pas autorisé l'accès votre localisation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // Vérifier si la permission de la caméra a été accordée
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // La permission de la caméra a été accordée, ouvrir la caméra
                openCamera();
            } else {
                // La permission de la caméra a été refusée, afficher un message d'erreur ou prendre une autre action
                Toast.makeText(this, "Permission de la caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initCoordinates() {

        Query coordinatesQuery = firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .orderBy("createdAt", Query.Direction.ASCENDING);


        coordinatesAdapter = new CoordinatesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(coordinatesAdapter);

        // Listener for coordinates
        this.coordinatesListenerRegistration = coordinatesQuery.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                return;
            }

            List<Coordinate> coordinates = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                Coordinate coordinate = document.toObject(Coordinate.class);
                coordinates.add(coordinate);
            }

            System.out.println("maj");

            trip.setCoordinates(coordinates);
            coordinatesAdapter.setCoordinates(coordinates);
            coordinatesAdapter.notifyDataSetChanged();
        });

    }
    private void loadPictures() {
        Query picturesQuery = firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("pictures");

        this.picturesListenerRegistration = picturesQuery.addSnapshotListener((snapshot, error) -> {
            List<Picture> picturesList = snapshot.toObjects(Picture.class);

            trip.setPictures(picturesList);
        });


    }

    private void initFloatingActionButton() {
        this.fabMain = findViewById(R.id.id_trip_fab_main);
        this.fabCamera = findViewById(R.id.id_trip_fab_camera);
        this.fabMail = findViewById(R.id.id_trip_fab_mail);
        this.fabPosition = findViewById(R.id.id_trip_fab_position);

        // Init animations
        this.rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        this.rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        this.fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        this.toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        // Activity launcher for camera
        this.initActivityCameraLauncher();
    }

    private void setVisibility(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.setVisibility(View.VISIBLE);
            this.fabMail.setVisibility(View.VISIBLE);
            this.fabPosition.setVisibility(View.VISIBLE);
        } else {
            this.fabCamera.setVisibility(View.INVISIBLE);
            this.fabMail.setVisibility(View.INVISIBLE);
            this.fabPosition.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.startAnimation(fromBottom);
            this.fabMail.startAnimation(fromBottom);
            this.fabPosition.startAnimation(fromBottom);

            this.fabMain.startAnimation(rotateOpen);
        } else {
            this.fabCamera.startAnimation(toBottom);
            this.fabMail.startAnimation(toBottom);
            this.fabPosition.startAnimation(toBottom);

            this.fabMain.startAnimation(rotateClose);
        }
    }

    private void setClickabled(Boolean clicked) {
        if (!clicked) {
            this.fabCamera.setClickable(true);
            this.fabMail.setClickable(true);
            this.fabPosition.setClickable(true);
        } else {
            this.fabCamera.setClickable(false);
            this.fabMail.setClickable(false);
            this.fabPosition.setClickable(false);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.activityLauncher.launch(intent);
    }

    private void saveImageToCloudStorage(Uri imageUri) {
        if(imageUri != null) {

            // Create a ref of the file in Firebase Storage
            StorageReference imageRef = storageReference.child("images/"+imageUri.getLastPathSegment());

            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnCompleteListener(taskSnapshot -> {
                if(taskSnapshot.isSuccessful()) {
                    imageRef.getDownloadUrl().addOnCompleteListener(uri -> {
                        if(uri.isSuccessful()) {

                            // get user's location
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            String provider = LocationManager.GPS_PROVIDER;
                            if (locationManager != null && locationManager.isProviderEnabled(provider)) {
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                    Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                                    try {
                                        double latitude = lastKnownLocation.getLatitude();
                                        double longitude = lastKnownLocation.getLongitude();

                                        GeoPoint point = new GeoPoint(latitude, longitude);
                                        String imageURL = uri.getResult().toString();

                                        Picture picture = new Picture(imageURL, new SerializableGeoPoint(point));

                                        // Store image url in firestore
                                        this.firebaseFirestore
                                                .collection("voyages")
                                                .document(trip.getDocID())
                                                .collection("pictures")
                                                .add(picture)
                                                .addOnCompleteListener(task -> {
                                                    System.out.println(task.isSuccessful() ? "photo ok" : "photo pas ok");
                                                });
                                    }
                                    catch (Exception e) { }

                                }
                            }
                        }
                    });
                }
            }).addOnFailureListener(error -> {
                error.printStackTrace();
            });
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        File file = null;
        try {
            file = File.createTempFile("image", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void initActivityCameraLauncher() {
        this.activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult()
                ,
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Convert picture to bitmap
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");

                        // Bitmap -> temporary file
                        File tempFile = saveBitmapToFile(photo);

                        // Temporary file -> uri : picture's url on firebase
                        Uri uri = Uri.fromFile(tempFile);

                        this.saveImageToCloudStorage(uri);
                    }
                }
        );
    }

    // *** GENERATE FILES AND SEND IT *** //
    private void exportCoordinatesToGPXAndKML() {
        firebaseFirestore
                .collection("voyages")
                .document(trip.getDocID())
                .collection("coordinates")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // Get coordinates

                        List<Coordinate> coordinates = task.getResult().toObjects(Coordinate.class);

                        this.generateGPXFile(coordinates);
                        this.generateKMLFile(coordinates);

                        this.sendEmailWithGpxKMLFiles();
                    }
                });
    }

    private void generateGPXFile(List<Coordinate> coordinates) {
        File gpxFile = new File(getExternalFilesDir(null), trip.getDocID()+".gpx");

        try {
            FileWriter writer = new FileWriter(gpxFile);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            // Format = latitude, longitude
            writer.write("<gpx version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
            for (Coordinate coordinate : coordinates) {
                writer.write("<wpt lat=\"" + coordinate.getCoords().getLatitude() + "\" lon=\"" + coordinate.getCoords().getLongitude() + "\"></wpt>\n");
            }
            writer.write("</gpx>");

            writer.close();
        }
        catch (IOException e) {
            Toast.makeText(this, "Erreur lors de la génération du fichier GPX", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateKMLFile(List<Coordinate> coordinates) {
        File kmlFile = new File(getExternalFilesDir(null), trip.getDocID()+".kml");

        try {
            FileWriter writer = new FileWriter(kmlFile);

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");

            // Format = longtitude, latitude
            for (Coordinate coordinate : coordinates) {
                writer.write("<Placemark>\n");
                writer.write("<Point><coordinates>" + coordinate.getCoords().getLongitude() + "," + coordinate.getCoords().getLatitude() + "</coordinates></Point>\n");
                writer.write("</Placemark>\n");
            }

            writer.write("</kml>");

            writer.close();
        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors de la génération du fichier KML", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailWithGpxKMLFiles(){
        // Get the two files
        File gpxFile = new File(getExternalFilesDir(null), trip.getDocID()+".gpx");
        File kmlFile = new File(getExternalFilesDir(null), trip.getDocID()+".kml");

        Uri gpxUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", gpxFile);
        Uri kmlUri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", kmlFile);

        // Create the email intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Coordonnées GPS de mon voyage " + trip.getName());

        // Add attachments
        ArrayList<Uri> attachmentUris = new ArrayList<>();
        attachmentUris.add(gpxUri);
        attachmentUris.add(kmlUri);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris);

        // Accordez les autorisations d'accès aux URI des fichiers pour le destinataire de l'e-mail
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Grant permission to the receiving app
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, gpxUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            grantUriPermission(packageName, kmlUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(Intent.createChooser(emailIntent, "Envoyer des fichiers GPX/KML"));
    }

}