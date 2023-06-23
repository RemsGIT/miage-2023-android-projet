package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.upjv.miage_2023_android_projet.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";

    private FirebaseAuth mAuth;

    private EditText inputEmail;
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect to firebase authentication
        mAuth = FirebaseAuth.getInstance();

        this.inputEmail = findViewById(R.id.editTextLogin_email);
        this.inputPassword = findViewById(R.id.editTextLogin_password);
    }


    public void handleClickLogin(View view) {

        String email = this.inputEmail.getText().toString().trim();
        String password = this.inputPassword.getText().toString().trim();

        if(email.equals("") || password.equals("")) {
            Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        this.signIn(email, password);
    }

    /**
     * Verify the user with firebase auth
     * @param email
     * @param password
     */
    private void signIn(String email, String password ) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Login:success : " + user.getEmail());
                        Toast.makeText(LoginActivity.this, "Connexion", Toast.LENGTH_SHORT).show();

                        this.redirectToHomePage();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erreur de connexion.\nVérifiez vos identifiants",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * OnClick "s'inscrire"
     * @param view
     */
    public void handleGoToRegister(View view) {
        Intent intentRegister = new Intent(this, RegisterActivity.class);

        startActivity(intentRegister);
    }

    /**
     * Redirect the user to home page
     */
    private void redirectToHomePage(){
        Intent intentHome = new Intent(this, MainActivity.class);

        startActivity(intentHome);
    }
}