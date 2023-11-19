package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class EditSelectedAppActivity extends MainActivity {

    private ImageView GeneratePsw;
    private Button SaveSelectedAppData;
    private EditText SelectedAppPassword;

    // μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);

        // Λήψη των πληροφοριών από το Intent
        Intent intent = getIntent();
        if (intent != null) {
            AppsObj.AppInfo selectedApp = intent.getParcelableExtra("selectedApp");

            // Εύρεση των views στο layout
            ImageView appIconImageView = findViewById(R.id.appIconImageView);
            TextView appNameTextView = findViewById(R.id.appNameTextView);
            EditText appLinkEditText = findViewById(R.id.appLinkEditText);

            // Ορισμός της εικόνας
            appIconImageView.setImageResource(selectedApp.getAppIconId());
            // Ορισμός του ονόματος
            appNameTextView.setText(selectedApp.getAppName());
            // Ορισμός του link
            appLinkEditText.setText(selectedApp.getAppLink());
        }

        GeneratePsw = findViewById(R.id.GeneratePsw);
        SaveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        SelectedAppPassword = findViewById(R.id.SelectedAppPassword);

        ImageButton showHideButton = findViewById(R.id.ShowHide);
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility();
            }
        });

        GeneratePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EditSelectedAppActivity.GeneratePasswordTask().execute();
            }
        });
        
}

//Background Thread for generate new password
    private class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            int len = 12;
            return generatePswd(len);
        }

        @Override
        protected void onPostExecute(String result) {
            SelectedAppPassword.setText(result);
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
            SelectedAppPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            SelectedAppPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        // Keep the cursor at the end
        SelectedAppPassword.setSelection(SelectedAppPassword.getText().length());
    }
}
