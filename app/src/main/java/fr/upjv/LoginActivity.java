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
        this.signIn(this.inputEmail.getText().toString(), this.inputPassword.getText().toString());
    }

    private void signIn(String email, String password ) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Login:success : " + user.getEmail());
                        Toast.makeText(LoginActivity.this, "Connexion", Toast.LENGTH_SHORT).show();

                        this.redirectToHomePage();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void handleGoToRegister(View view) {
        Intent intentRegister = new Intent(this, RegisterActivity.class);

        startActivity(intentRegister);
    }

    private void redirectToHomePage(){
        Intent intentHome = new Intent(this, MainActivity.class);

        startActivity(intentHome);
    }
}