package fr.upjv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import fr.upjv.miage_2023_android_projet.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleClickGoToLoginPage(View view) {
        Intent intentLogin = new Intent(this, LoginActivity.class);

        startActivity(intentLogin);
    }

    public void handleClickGoToRegisterPage(View view) {
        Intent intentRegister = new Intent(this, RegisterActivity.class);

        startActivity(intentRegister);
    }
}