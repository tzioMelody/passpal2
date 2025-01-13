package com.example.passpal2;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity implements EmailVerificationTask.EmailVerificationListener {

    private TextInputEditText usernameEditText, emailEditText;
    private Button saveButton, cancelButton;
    private DataBaseHelper dbHelper;
    private int userId;
    private String newUsername, newEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Database Helper
        dbHelper = new DataBaseHelper(this);

        // Ρύθμιση Action Bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.dark_blue)));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit profile");
        }

        // Retrieve Views
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Get the logged-in user's ID
        userId = DataBaseHelper.getUserId(this);

        // Pre-fill fields with current user data
        String currentUsername = dbHelper.getUsernameByUserId(userId);
        String currentEmail = dbHelper.getUserEmailByUserId(userId);
        usernameEditText.setText(currentUsername);
        emailEditText.setText(currentEmail);

        // Handle Cancel Button Click
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            newUsername = usernameEditText.getText().toString().trim();
            newEmail = emailEditText.getText().toString().trim();

            // Επικύρωση εισαγωγής
            if (validateInputs(newUsername, newEmail)) {
                // Επαλήθευση του email μέσω του EmailVerificationTask
                new EmailVerificationTask(this, this).execute(newEmail);
            }
        });

    }

    @Override
    public void onEmailVerified(boolean isEmailValid) {
        if (isEmailValid) {
            // Αν το email είναι έγκυρο, προχωράμε στην ενημέρωση του προφίλ
            boolean updateSuccess = dbHelper.updateUsernameAndEmailWithCheck(userId, newUsername, newEmail);

            if (updateSuccess) {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                // Μετάβαση στη LoginActivity για να γίνει επανεκκίνηση της συνεδρίας
                Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile. Username or email may already exist.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid email address. Please provide a valid email.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String username, String email) {
        if (TextUtils.isEmpty(username)) {
            showToast("Username cannot be empty");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return false;
        }
        if (dbHelper.isUsernameExists(username) && !username.equals(dbHelper.getUsernameByUserId(userId))) {
            showToast("Username is already taken");
            return false;
        }
        if (dbHelper.isEmailTaken(email) && !email.equals(dbHelper.getUserEmailByUserId(userId))) {
            showToast("Email is already in use");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Αν πατηθεί το back arrow, επιστρέφουμε στην προηγούμενη οθόνη
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to leave? The changes you've made will not be saved!")
                .setPositiveButton("YES", (dialog, which) -> {
                    setResult(RESULT_OK);
                    super.onBackPressed();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
