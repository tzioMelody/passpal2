package com.example.passpal2;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_table);
        getSupportActionBar().setTitle("Passwords Table");

        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID από το intent
        userId = getIntent().getIntExtra("user_id", -1);

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Ανάκτηση δεδομένων και εμφάνιση στον πίνακα
        displayPasswordsTable();
    }

    private void displayPasswordsTable() {
        TableLayout tableLayout = findViewById(R.id.passwordsTableLayout);
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);

        for (DataBaseHelper.AppCredentials credentials : credentialsList) {
            TableRow tableRow = new TableRow(this);
            TextView appNameTextView = new TextView(this);
            TextView usernameTextView = new TextView(this);
            TextView passwordTextView = new TextView(this);

            appNameTextView.setText(credentials.getAppName());
            usernameTextView.setText(credentials.getUsername());
            passwordTextView.setText(credentials.getPassword());

            // Προσθήκη λειτουργικότητας εμφάνισης/απόκρυψης του κωδικού πρόσβασης
            passwordTextView.setOnClickListener(new View.OnClickListener() {
                boolean isPasswordVisible = false;

                @Override
                public void onClick(View v) {
                    if (isPasswordVisible) {
                        passwordTextView.setText(credentials.getPassword());
                    } else {
                        passwordTextView.setText("••••••••");
                    }
                    isPasswordVisible = !isPasswordVisible;
                }
            });

            tableRow.addView(appNameTextView);
            tableRow.addView(usernameTextView);
            tableRow.addView(passwordTextView);

            tableLayout.addView(tableRow);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
