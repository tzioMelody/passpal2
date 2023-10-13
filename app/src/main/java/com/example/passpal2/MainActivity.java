package com.example.passpal2;



import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FloatingActionButton με βάση το ID του
        FloatingActionButton appsBtn = findViewById(R.id.appsBtn);

        // OnClickListener για τον FloatingActionButton
        appsBtn.setOnClickListener(view -> {
            // Μετάβαση στην σελίδα με λίστα εφαρμογών
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });
    }
}