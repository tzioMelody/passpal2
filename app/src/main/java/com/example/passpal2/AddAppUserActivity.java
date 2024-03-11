package com.example.passpal2;

import static com.example.passpal2.DataBaseHelper.getUserId;

import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    int userId;
    String username;
    String email;
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appadduser);

        // Ορισμός των στοιχείων UI από το layout
        passGenButton = findViewById(R.id.passGnrt);
        saveNewApp = findViewById(R.id.saveNewApp);
        newAppPassword = findViewById(R.id.newAppPassword);
        ImageButton showHideButton = findViewById(R.id.showHideBtn);
       MaterialButton generatePasswordButton = findViewById(R.id.generatePasswordButton);

        userId = getIntent().getIntExtra("USER_ID", -1);
        // Ορισμός listener για το κουμπί εμφάνισης/απόκρυψης κωδικού
        showHideButton.setOnClickListener(v -> togglePasswordVisibility());

        generatePasswordButton.setOnClickListener(v -> {
            new GeneratePasswordTask(this, password -> newAppPassword.setText(password)).execute();
        });


        // Ορισμός listener για το κουμπί αποθήκευσης νέας εφαρμογής
        saveNewApp.setOnClickListener(v -> saveNewApp());

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


    private void saveNewApp() {
        EditText newAppUsername = findViewById(R.id.newAppUsername);
        EditText newAppEmail = findViewById(R.id.newAppEmail);
        EditText newAppname = findViewById(R.id.newAppname);
        EditText newAppLink = findViewById(R.id.newAppLink);
        EditText newAppPassword = findViewById(R.id.newAppPassword);

        String appName = newAppname.getText().toString();
        String appLink = newAppLink.getText().toString();
         username = newAppUsername.getText().toString();
         email = newAppEmail.getText().toString();
         password = newAppPassword.getText().toString();

        // Έλεγχος των πεδίων
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(appLink) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(password)) {
            Toast.makeText(AddAppUserActivity.this, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ελέγχουμε την εγκυρότητα του email
        new EmailVerificationTask(isEmailValid -> {
            if (isEmailValid) {
                // Ελέγχουμε την εγκυρότητα του link
                new CheckAppLinkValidityTask().execute(appLink, appName);
            } else {
                Toast.makeText(AddAppUserActivity.this, "Email is not valid.", Toast.LENGTH_SHORT).show();
            }
        }).execute(email);
    }


    private class CheckAppLinkValidityTask extends AsyncTask<String, Void, Integer> {
        private String appName;
        private String appLink;

        @Override
        protected Integer doInBackground(String... params) {
            appLink = params[0];
            appName = params[1];
            try {
                URL url = new URL(appLink);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                return connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Αποθήκευση της εφαρμογής στη βάση
                AppsObj newApp = new AppsObj(appName, appLink, R.drawable.default_app_icon);
                newApp.setUsername(username);
                newApp.setEmail(email);
                newApp.setPassword(password);
                boolean success = dbHelper.addNewAppWithDetails(userId, appName, appLink, username, email, password);

                if (success) {
                    Toast.makeText(AddAppUserActivity.this, "App added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Επιστροφή στην προηγούμενη δραστηριότητα
                } else {
                    Toast.makeText(AddAppUserActivity.this, "Failed to add the app", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddAppUserActivity.this, "The app link is not valid.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Αποθήκευση Αλλαγών") // Ορισμός του τίτλου του παραθύρου
                .setMessage("Είστε σίγουροι ότι θέλετε να φύγετε; Όλες οι αλλαγές που δεν έχουν αποθηκευτεί θα χαθούν.")
                .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddAppUserActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Όχι", null)
                .show();
    }
}






