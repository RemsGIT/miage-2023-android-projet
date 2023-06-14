package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.Map;
import java.util.Objects;

import fr.upjv.Forms.CreateTripActivity;
import fr.upjv.Model.Trip;
import fr.upjv.miage_2023_android_projet.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseFirestore firebaseFirestore;

    private TextView textNbTrip;
    private TextView btnActionTrip;

    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textNbTrip = findViewById(R.id.main_text_nbtrip);
        this.btnActionTrip = findViewById(R.id.main_btn_actiontrip);

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

            this.btnActionTrip.setOnClickListener(v -> redirectToTrip());
        }
    }
}