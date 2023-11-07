package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class AddAppUserActivity extends AppCompatActivity {

    private ImageView passGenButton;
    private FloatingActionButton saveNewApp;
    private EditText newAppPassword;

    // μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appadduser);

        passGenButton = findViewById(R.id.passGnrt);
        saveNewApp = findViewById(R.id.saveNewApp);
        newAppPassword = findViewById(R.id.newAppPassword);

        ImageButton showHideButton = findViewById(R.id.showHideBtn);
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility();
            }
        });

        passGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeneratePasswordTask().execute();
            }
        });

        saveNewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newAppUsername = findViewById(R.id.newAppUsername);
                EditText newAppEmail = findViewById(R.id.newAppEmail);
                EditText newAppname = findViewById(R.id.newAppname);
                EditText newAppLink = findViewById(R.id.newAppLink);

                String appName = newAppname.getText().toString();
                String appLink = newAppLink.getText().toString();
                String username = newAppUsername.getText().toString();
                String email = newAppEmail.getText().toString();
                String password = newAppPassword.getText().toString();

                if (TextUtils.isEmpty(appName)) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter the name of the app", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(appLink)) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter the link of the app", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(username)) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(AddAppUserActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                } else {
                    new CheckAppLinkValidityTask().execute(appLink, appName);
                }
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            newAppPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            newAppPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        // Keep the cursor at the end
        newAppPassword.setSelection(newAppPassword.getText().length());
    }

    private class CheckAppLinkValidityTask extends AsyncTask<String, Void, Integer> {
        private String appName;
        private String appLink;

        @Override
        protected Integer doInBackground(String... params) {
            appName = params[1];
            appLink = params[0];
            try {
                URL url = new URL(appLink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                return httpURLConnection.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
                // Ήπια προστασία για τυχόν σφάλματα
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                AppsObj.UserApp newUserApp = new AppsObj.UserApp(appName, appLink);
                AppsObj.USER_APPS.add(newUserApp);
                // Καλούμε μέθοδο για αποθήκευση
                saveNewUserAppToDatabase(newUserApp);
                Toast.makeText(AddAppUserActivity.this, "App added successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddAppUserActivity.this, AppSelectionActivity.class);
                startActivity(intent);
            } else {
                if (responseCode == -1) {
                    Toast.makeText(AddAppUserActivity.this, "An error occurred while checking the app link", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddAppUserActivity.this, "The entered app link is not reachable", Toast.LENGTH_SHORT).show();
                }
            }
        }
        private void saveNewUserAppToDatabase(AppsObj.UserApp newUserApp) {
            // Παράδειγμα υλοποίησης σώζοντας τα δεδομένα στη λίστα `USER_APPS`:
            AppsObj.USER_APPS.add(newUserApp);

            // Το στέλνουμε στην AppSelectionActivity να εμφανιστεί η νέα εφαρμογή
            Intent intent = new Intent(AddAppUserActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        }
    }


    private class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            int len = 12;
            return generatePswd(len);
        }

        @Override
        protected void onPostExecute(String result) {
            newAppPassword.setText(result);
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
}
