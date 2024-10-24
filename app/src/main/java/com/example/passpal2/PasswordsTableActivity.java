package com.example.passpal2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private int userId;
    private RecyclerView passwordsRecyclerView;
    private PasswordsAdapter passwordsAdapter;

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

        // Αρχικοποίηση του RecyclerView
        passwordsRecyclerView = findViewById(R.id.passwordsRecyclerView);
        passwordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Φόρτωση δεδομένων
        loadCredentials();
    }

    // Φόρτωση credentials από τη βάση δεδομένων και αρχικοποίηση του adapter
    private void loadCredentials() {
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);
        if (credentialsList != null && !credentialsList.isEmpty()) {
            passwordsAdapter = new PasswordsAdapter(credentialsList);
            passwordsRecyclerView.setAdapter(passwordsAdapter);
        } else {
            showToast("No saved credentials found.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
