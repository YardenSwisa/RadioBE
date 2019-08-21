package com.example.radiobe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.radiobe.R;
import com.example.radiobe.fragments.MainScreen;
import com.example.radiobe.generalScreens.ActivityRadio;
import com.example.radiobe.registrations.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button btnLive;
    private Button btnLogin;
    FirebaseUser firebaseUser;

    //this is a new change for github


    // asdas
    // yarden change


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {                         //TODO : try to understand why it didn't work from the splash screen itself.
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
        }

        setupViews();

        //Gif of the MainActivity
        ImageView gifRadio = findViewById(R.id.ivRadioGif);
        Glide.with(this).asGif().load(R.drawable.loading_radio).into(gifRadio);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);

        });

        btnLive.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityRadio.class);
            startActivity(intent);
        });


    }

    private void setupViews() {
        btnLive = findViewById(R.id.btnLive);
        btnLogin = findViewById(R.id.btnLogin);
    }
}
