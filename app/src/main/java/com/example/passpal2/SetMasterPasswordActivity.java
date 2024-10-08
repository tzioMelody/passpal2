package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        getSupportActionBar().setTitle("Set master password");

        dbHelper = new DataBaseHelper(this);

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
            // Δημιουργία AES κλειδιού
            SecretKey secretKey = DataBaseHelper.generateAESKey();

            // Κρυπτογράφηση του master password
            String encryptedMasterPassword = DataBaseHelper.encryptAES(masterPassword, secretKey);
            dbHelper.insertMasterPassword(userId, encryptedMasterPassword);

            // Αποθήκευση του κλειδιού σε ασφαλές σημείο (π.χ. SharedPreferences)
            dbHelper.saveAESKey(secretKey, userId);

            showToast("Master Password set successfully");

            // Μετάβαση στο MainActivity
            Intent intent = new Intent(SetMasterPasswordActivity.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to set Master Password due to error");
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
