package com.example.passpal2;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private int userId;
    private TableLayout passwordsTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_table);
        getSupportActionBar().setTitle("Passwords vault");

        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID από το intent
        userId = getIntent().getIntExtra("user_id", -1);

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Αρχικοποίηση του TableLayout
        passwordsTableLayout = findViewById(R.id.passwordsTableLayout);

        // Φόρτωση δεδομένων
        loadCredentials();
    }

    // Φόρτωση credentials από τη βάση δεδομένων και προσθήκη δυναμικών γραμμών στο TableLayout
    private void loadCredentials() {
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);
        if (credentialsList != null && !credentialsList.isEmpty()) {
            for (DataBaseHelper.AppCredentials credential : credentialsList) {
                // Δημιουργία νέου TableRow
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                // Δημιουργία και ρύθμιση TextView για το όνομα της εφαρμογής
                TextView appNameTextView = new TextView(this);
                appNameTextView.setText(credential.getAppName());
                row.addView(appNameTextView);

                // Δημιουργία και ρύθμιση TextView για το όνομα χρήστη
                TextView usernameTextView = new TextView(this);
                usernameTextView.setText(credential.getUsername());
                row.addView(usernameTextView);

                // Δημιουργία και ρύθμιση TextView για τον κωδικό
                TextView passwordTextView = new TextView(this);
                passwordTextView.setText("••••••••");  // Κρυμμένος κωδικός
                row.addView(passwordTextView);

                // Δημιουργία και ρύθμιση TextView για το κουμπί Show/Hide
                TextView showHideTextView = new TextView(this);
                showHideTextView.setText("Show");
                showHideTextView.setTextColor(getResources().getColor(R.color.purple_500));

                // Προσθήκη λειτουργικότητας εμφάνισης/απόκρυψης
                showHideTextView.setOnClickListener(view -> {
                    if (showHideTextView.getText().equals("Show")) {
                        passwordTextView.setText(credential.getPassword()); // Εμφάνιση κωδικού
                        showHideTextView.setText("Hide");
                    } else {
                        passwordTextView.setText("••••••••");  // Απόκρυψη κωδικού
                        showHideTextView.setText("Show");
                    }
                });
                row.addView(showHideTextView);

                // Προσθήκη της γραμμής στο TableLayout
                passwordsTableLayout.addView(row);
            }
        } else {
            showToast("No saved credentials found.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
