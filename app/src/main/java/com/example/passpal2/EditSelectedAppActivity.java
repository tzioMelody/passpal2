package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class EditSelectedAppActivity extends AppCompatActivity {

    private ImageView appIconImageView;
    private TextView appNameTextView;
    private EditText appLinkEditText;
    private ImageView generatePsw;
    private Button saveSelectedAppData;
    private EditText selectedAppPassword;

    // Μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);

        // Λήψη των πληροφοριών από το Intent
        Intent intent = getIntent();
        if (intent != null) {
            AppsObj selectedApp = intent.getParcelableExtra("selectedApp");

            // Εύρεση των views στο layout
            appIconImageView = findViewById(R.id.appIconImageView);
            appNameTextView = findViewById(R.id.appNameTextView);
            appLinkEditText = findViewById(R.id.appLinkEditText);

            // Ορισμός της εικόνας
            appIconImageView.setImageResource(selectedApp.getAppImages());
            // Ορισμός του ονόματος
            appNameTextView.setText(selectedApp.getAppNames());
            // Ορισμός του link
            appLinkEditText.setText(selectedApp.getAppLinks());
        }

        generatePsw = findViewById(R.id.GeneratePsw);
        saveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        selectedAppPassword = findViewById(R.id.SelectedAppPassword);

        generatePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeneratePasswordTask().execute();
            }
        });

        selectedAppPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                togglePasswordVisibility();
                return false;
            }
        });
    }

    // Background Thread for generate new password
    private class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            int len = 12;
            return generatePswd(len);
        }

        @Override
        protected void onPostExecute(String result) {
            selectedAppPassword.setText(result);
        }
    }

    private String generatePswd(int len) {
        String charsCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String chars = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String symbols = "!@#$%^&*_=+-/€.?<>)";

        String passSymbols = charsCaps + chars + nums + symbols;
        Random rnd = new Random();

        StringBuilder password = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            password.append(passSymbols.charAt(rnd.nextInt(passSymbols.length())));
        }
        return password.toString();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            selectedAppPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            selectedAppPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        // Keep the cursor at the end
        selectedAppPassword.setSelection(selectedAppPassword.getText().length());
    }
}
