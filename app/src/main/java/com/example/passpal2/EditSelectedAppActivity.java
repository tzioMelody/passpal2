package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class EditSelectedAppActivity extends AppCompatActivity {

    private ImageView appIconImageView;
    private TextView appNameTextView;
    private EditText appLinkEditText;
    private EditText inputEmailEditedApp;
    private Button generatePsw;
    private Button saveSelectedAppData;
    private Button OpenAppWebsiteBtn;
    private ProgressBar progressBar;
    private EditText selectedAppPassword;
    private View overlayView;
    // Μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;
    private DataBaseHelper db = new DataBaseHelper(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);

        // Λήψη των πληροφοριών από το Intent
        Intent intent = getIntent();
        if (intent != null) {
            AppsObj selectedApp = intent.getParcelableExtra("APP_DATA");

            // Εύρεση των views στο layout
            appIconImageView = findViewById(R.id.appIconImageView);
            appNameTextView = findViewById(R.id.appNameTextView);
            appLinkEditText = findViewById(R.id.inputLinkEditedApp);

            // Ορισμός της εικόνας
            appIconImageView.setImageResource(selectedApp.getAppImages());
            // Ορισμός του ονόματος
            appNameTextView.setText(selectedApp.getAppNames());
            // Ορισμός του link
            appLinkEditText.setText(selectedApp.getAppLinks());
        }
        inputEmailEditedApp = findViewById(R.id.inputEmailEditedApp);
        generatePsw = findViewById(R.id.GeneratePsw);
        saveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        OpenAppWebsiteBtn = findViewById(R.id.OpenAppWebsite);
        selectedAppPassword = findViewById(R.id.passwordEditText);

        generatePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeneratePasswordTask().execute();
            }
        });
        saveSelectedAppData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmailEditedApp.getText().toString(); // Υποθέτω ότι έχεις ένα input field με id inputEmailEditedApp

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(EditSelectedAppActivity.this, "Please fill in the email field", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Εμφάνιση του ProgressBar και της αναμονής (υποθέτω ότι έχεις προσθέσει ProgressBar και overlayView όπως στην RegisterActivity)
                progressBar.setVisibility(View.VISIBLE);
                overlayView.setVisibility(View.VISIBLE);

                // Καλέστε τον AsyncTask για την επαλήθευση του email
                EmailVerificationTask verificationTask = new EmailVerificationTask(new EmailVerificationTask.EmailVerificationListener() {
                    @Override
                    public void onEmailVerified(boolean isEmailValid) {
                        progressBar.setVisibility(View.GONE);
                        overlayView.setVisibility(View.GONE);
                        if (isEmailValid) {
                            // Προχωρήστε με την αποθήκευση των αλλαγών αν το email είναι έγκυρο
                            saveChanges(); // Μια μέθοδος που θα πρέπει να υλοποιήσεις για να αποθηκεύσεις τις αλλαγές
                        } else {
                            // Εμφάνιση μηνύματος σφάλματος αν το email δεν είναι έγκυρο
                            Toast.makeText(EditSelectedAppActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                verificationTask.execute(email);
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
    private void saveChanges() {
        // Παίρνουμε τις τρέχουσες τιμές από τα πεδία εισαγωγής
        String newName = appNameTextView.getText().toString();
        String newLink = appLinkEditText.getText().toString();
        String newEmail = inputEmailEditedApp.getText().toString();
        String newPassword = selectedAppPassword.getText().toString();



      /*  // Υποθέτωντας ότι έχεις ένα instance της DataBaseHelper με όνομα db
        boolean isUpdateSuccessful = db.updateAppInfo(updatedApp); // Υποθέτωντας ότι έχεις μια μέθοδο updateAppInfo στην DataBaseHelper

        if (db.updateAppInfo(updatedAppInfo)) {
            Toast.makeText(this, "App information updated successfully", Toast.LENGTH_SHORT).show();
            // Προαιρετικά, μετάβαση πίσω στην προηγούμενη δραστηριότητα ή ενημέρωση του UI
            finish(); // Κλείνει την τρέχουσα δραστηριότητα και επιστρέφει στην προηγούμενη
        } else {
            Toast.makeText(this, "Failed to update app information", Toast.LENGTH_SHORT).show();
        }*/
    }

    // generate new password
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
