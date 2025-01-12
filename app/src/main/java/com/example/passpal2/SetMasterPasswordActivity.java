package com.example.passpal2;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

public class SetMasterPasswordActivity extends AppCompatActivity {

    private EditText masterPasswordEditText, confirmMasterPasswordEditText;
    private Button submitMasterPasswordButton;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_password);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.dark_blue)));
        getSupportActionBar().setTitle("Set master password");

        dbHelper = new DataBaseHelper(this);
        Button notNow = findViewById(R.id.notNow);
        masterPasswordEditText = findViewById(R.id.masterPassword);
        confirmMasterPasswordEditText = findViewById(R.id.confirmMasterPassword);
        submitMasterPasswordButton = findViewById(R.id.submitMasterPassword);

        // Λήψη του user ID από το intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);

        // Έλεγχος αν το userId είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            Log.d("MasterPasswordActivity", "User ID is invalid");
            finish();
            return;
        }

        Log.d("MasterPasswordActivity", "Received UserID: " + userId);

        submitMasterPasswordButton.setOnClickListener(v -> submitMasterPassword());

        notNow.setOnClickListener(v -> {
            // Show a message to the user
            Toast.makeText(SetMasterPasswordActivity.this,
                    "You will need to set a master password in the future.",
                    Toast.LENGTH_LONG).show();

            // Navigate back to MainActivity
            Intent navigateIntent = new Intent(SetMasterPasswordActivity.this, MainActivity.class);
            navigateIntent.putExtra("user_id", userId); // Pass the user ID if needed
            startActivity(navigateIntent);
            finish(); // Close the current activity
        });

    }

    private void submitMasterPassword() {
        String masterPassword = masterPasswordEditText.getText().toString().trim();
        String confirmMasterPassword = confirmMasterPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(masterPassword) || TextUtils.isEmpty(confirmMasterPassword)) {
            showToast("Both fields are required");
            return;
        }

        if (masterPassword.length() != 4 || confirmMasterPassword.length() != 4) {
            showToast("Password must be exactly 4 characters long");
            return;
        }

        if (isAllCharactersSame(masterPassword) || isCommonPassword(masterPassword)) {
            showToast("Password is too simple");
            return;
        }

        if (!masterPassword.equals(confirmMasterPassword)) {
            showToast("Passwords do not match");
            return;
        }

        try {
            // Directly use the insertMasterPassword() method
            dbHelper.insertMasterPassword(userId, masterPassword);
            showToast("Master Password set successfully");

            // Μετάβαση στο MainActivity
            Intent intent = new Intent(SetMasterPasswordActivity.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to set Master Password due to an error");
        }
    }


    private boolean isAllCharactersSame(String input) {
        char firstChar = input.charAt(0);
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }

    private boolean isCommonPassword(String password) {
        return password.equals("1234") || password.equals("abcd");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
