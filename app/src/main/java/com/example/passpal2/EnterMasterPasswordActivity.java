package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnterMasterPasswordActivity extends AppCompatActivity {

    private EditText masterPasswordEditText;
    private Button submitMasterPasswordButton;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_masterpassword);
        getSupportActionBar().setTitle("Enter Master Password");

        dbHelper = new DataBaseHelper(this);

        masterPasswordEditText = findViewById(R.id.masterPasswordInput);
        submitMasterPasswordButton = findViewById(R.id.submitMasterPassword);

        // Λήψη του user ID από το intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);

        // Έλεγχος αν το userId είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            Log.d("EnterMasterPasswordActivity", "User ID is invalid");
            finish();
            return;
        }

        Log.d("EnterMasterPasswordActivity", "Received UserID: " + userId);

        submitMasterPasswordButton.setOnClickListener(v -> submitMasterPassword());
    }

    private void submitMasterPassword() {
        String masterPassword = masterPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(masterPassword)) {
            showToast("Password field is required");
            return;
        }

        if (masterPassword.length() != 4) {
            showToast("Password must be exactly 4 characters long");
            return;
        }

        // Έλεγχος του Master Password
        try {
            if (dbHelper.checkMasterPassword(userId, masterPassword)) {
                showToast("Password correct");

                Log.d("EnterMasterPasswordActivity", "Correct Master Password for UserID: " + userId);

                // Μετάβαση στο PasswordsTableActivity
                Intent intent = new Intent(EnterMasterPasswordActivity.this, PasswordsTableActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                showToast("Password incorrect");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to verify Master Password due to error");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
