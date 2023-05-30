package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.upjv.miage_2023_android_projet.R;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "Register";

    private FirebaseAuth mAuth;

    // Components
    private EditText inputEmail;
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Connect to firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // Init components
        this.inputEmail = findViewById(R.id.editText_email);
        this.inputPassword = findViewById(R.id.editText_password);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            // Redirect the user to home page ?
            reload();
        }
    }


    public void handleClickRegister(View view) {
        Log.d(TAG, "sendForm : " + this.inputEmail.getText().toString());

        this.createAccount(this.inputEmail.getText().toString(), this.inputPassword.getText().toString());
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Register success -> sign in the user
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "createAccount:success : " + user.toString());

                        Toast.makeText(RegisterActivity.this, "New account created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reload() { }

}