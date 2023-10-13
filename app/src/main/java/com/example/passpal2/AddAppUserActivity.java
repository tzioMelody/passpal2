package com.example.passpal2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddAppUserActivity extends AppCompatActivity {

    private EditText newAppname, newAppLink, newAppUsername, newAppEmail, newAppPassword;
    private ImageButton showHideBtn,passGnrt;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appadduser);

        newAppname = findViewById(R.id.newAppname);
        newAppLink = findViewById(R.id.newAppLink);
        newAppUsername = findViewById(R.id.newAppUsername);
        newAppEmail = findViewById(R.id.newAppEmail);
        newAppPassword = findViewById(R.id.newAppPassword);
        showHideBtn = findViewById(R.id.showHideBtn);

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePasswordVisibility();
            }
        });

        Button saveNewApp = findViewById(R.id.saveNewApp);
        saveNewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveApp();
            }
        });

        // Κουμπί για δημιουργία κωδικού
        ImageButton generatePasswordButton = findViewById(R.id.passGnrt);
        generatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePasswordInBackground();
            }
        });

    }
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            newAppPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            showHideBtn.setImageResource(R.drawable.hidepswd);
        } else {
            newAppPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showHideBtn.setImageResource(R.drawable.showpswdbtn);
        }
    }

    private void saveApp() {
        String appName = newAppname.getText().toString();
        String appLink = newAppLink.getText().toString();
        String appUsername = newAppUsername.getText().toString();
        String appEmail = newAppEmail.getText().toString();
        String appPassword = newAppPassword.getText().toString();

        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(appLink) || TextUtils.isEmpty(appUsername) || TextUtils.isEmpty(appEmail) || TextUtils.isEmpty(appPassword)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Εδώ μπορείτε να χρησιμοποιήσετε τα δεδομένα που λάβατε για να αποθηκεύσετε την εφαρμογή στη βάση δεδομένων σας

        Toast.makeText(this, "App saved!", Toast.LENGTH_SHORT).show();
    }

    private void generatePasswordInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                // Καλέστε τη μέθοδο generatePassword για να δημιουργήσετε τον κωδικό
                return generatePassword(8); // Μήκος 8 για παράδειγμα
            }

            @Override
            protected void onPostExecute(String generatedPassword) {
                // Ενημερώστε το UI με τον δημιουργημένο κωδικό
                newAppPassword.setText(generatedPassword);
            }
        }.execute();
    }
}
