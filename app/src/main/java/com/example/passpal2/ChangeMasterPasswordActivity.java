package com.example.passpal2;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class ChangeMasterPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmNewPasswordEditText;
    private Button submitButton, cancelButton, generatePasswordButton;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_master_password);

        dbHelper = new DataBaseHelper(this);

        newPasswordEditText = findViewById(R.id.newMasterPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewMasterPassword);
        submitButton = findViewById(R.id.submitNewMasterPassword);
        cancelButton = findViewById(R.id.cancelChangeMasterPassword);
        generatePasswordButton = findViewById(R.id.generatePasswordButton);

        // Retrieve user ID from Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            showToast("Invalid user ID");
            finish();
            return;
        }

        setupButtons();
    }

    private void setupButtons() {
        submitButton.setOnClickListener(v -> changeMasterPassword());
        cancelButton.setOnClickListener(v -> finish());
        generatePasswordButton.setOnClickListener(v -> generateNewPassword());
    }

    private void changeMasterPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            showToast("Both fields are required");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            showToast("Passwords do not match");
            return;
        }

        if (newPassword.length() != 4) {
            showToast("Password must be exactly 4 characters");
            return;
        }

        // Update the master password in the database
        boolean success = dbHelper.updateMasterPassword(userId, newPassword);
        if (success) {
            showToast("Master Password updated successfully");
            finish();
        } else {
            showToast("Failed to update Master Password. Try again.");
        }
    }

    private void generateNewPassword() {
        // Generate a 4-character random password
        String generatedPassword = generateRandomPassword(4);
        newPasswordEditText.setText(generatedPassword);
        confirmNewPasswordEditText.setText(generatedPassword);
    }

    private String generateRandomPassword(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder passwordBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            passwordBuilder.append(allowedChars.charAt(randomIndex));
        }

        return passwordBuilder.toString();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
