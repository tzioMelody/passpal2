package com.example.passpal2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Button  deleteAccountButton, logOutButton, viewCredentialsButton, changeMasterPasswordButton, editProfileButton;
    private int userId;
    private DataBaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Ενεργοποίηση του back arrow
            getSupportActionBar().setTitle("Profile settings");
        }

        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID
        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);

        Log.d("ProfileActivity", "User ID received: " + userId);

        if (userId == -1) {
            Log.e("ProfileActivity", "User ID is invalid.");
            Toast.makeText(this, "User ID is invalid", Toast.LENGTH_SHORT).show();
            finish(); // Τερματισμός αν το user ID δεν είναι έγκυρο
            return;
        }

        // Συνδέουμε τα στοιχεία από το XML
        deleteAccountButton = findViewById(R.id.delete_account_button);
        logOutButton = findViewById(R.id.log_out_button);
        viewCredentialsButton = findViewById(R.id.loginPasswordButton);
        editProfileButton = findViewById(R.id.edit_profile_button);
        changeMasterPasswordButton = findViewById(R.id.change_master_password_button);


        // Ρύθμιση λειτουργικότητας κουμπιών
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Edit Profile
        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });


        // Change Master Password
        changeMasterPasswordButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChangeMasterPasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // View Credentials Button
        viewCredentialsButton.setOnClickListener(v -> openLoginPasswordActivity());

        // Log Out
        logOutButton.setOnClickListener(view -> performLogout());

        // Delete Account
        deleteAccountButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone. Your credentials will be deleted for your safety.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount(userId))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back arrow
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void openLoginPasswordActivity() {
        Intent intent = new Intent(this, EnterMasterPasswordActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void deleteUserAccount(int userId) {
        boolean isDeleted = dbHelper.deleteUserData(userId);

        if (isDeleted) {
            // Διαγραφή των δεδομένων "Remember Me" από τις SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("userId");  // Διαγραφή του userId
            editor.remove("rememberMe");  // Διαγραφή του "Remember Me" (αν υπάρχει)
            editor.apply();  // Εφαρμογή των αλλαγών

            // Εμφάνιση μηνύματος για επιτυχή διαγραφή
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

            // Μετάβαση στην οθόνη σύνδεσης (LoginActivity)
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // Κλείσιμο της τρέχουσας οθόνης
        } else {
            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
        }
    }



    private void performLogout() {
        // Καθαρίζουμε τα SharedPreferences
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Toast.makeText(this, "Logout successful!" , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
