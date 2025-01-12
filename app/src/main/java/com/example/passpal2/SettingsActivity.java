package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private TextView shareButton, updateDataButton, viewCredentialsButton, settingsPlaceholderButton;
    private DataBaseHelper dbHelper;
    private int userId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DataBaseHelper(this);
        firestore = FirebaseFirestore.getInstance();

        // Λήψη του user ID από το Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Αρχικοποίηση κουμπιών
        shareButton = findViewById(R.id.shareButton);
        updateDataButton = findViewById(R.id.updateButton);
        viewCredentialsButton = findViewById(R.id.loginPasswordButton);
/*        settingsPlaceholderButton = findViewById(R.id.settingsPlaceholderButton);*/

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Share Button
        shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Replace with your Google Drive link
            String downloadLink = "https://drive.google.com/file/d/<your-file-id>/view?usp=sharing";
            String shareMessage = "Check out PassPal, the ultimate password manager! Download it here: " + downloadLink;

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });


        // Update Data Button
        updateDataButton.setOnClickListener(v -> syncDataToFirestore());

        // View Credentials Button
        viewCredentialsButton.setOnClickListener(v -> openLoginPasswordActivity());

        // Placeholder for future functionality
        settingsPlaceholderButton.setOnClickListener(v -> showPlaceholderMessage());
    }

    private void shareAppInfo() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareMessage = "Check out PassPal, the ultimate password manager!";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void syncDataToFirestore() {
        String userEmail = dbHelper.getUserEmailByUserId(userId);
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found. Cannot sync data.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .document(userEmail)
                .collection("apps")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    firestore.runBatch(batch -> {
                        for (AppsObj app : dbHelper.getAllSelectedApps(userId)) {
                            batch.set(
                                    firestore.collection("users")
                                            .document(userEmail)
                                            .collection("apps")
                                            .document(app.getAppNames()),
                                    app.toMap()
                            );
                        }
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(updateDataButton, "Data synced successfully.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error syncing data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to connect to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openLoginPasswordActivity() {
        Intent intent = new Intent(this, EnterMasterPasswordActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void showPlaceholderMessage() {
        Toast.makeText(this, "Settings functionality coming soon!", Toast.LENGTH_SHORT).show();
    }
}
