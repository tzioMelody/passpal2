package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnterMasterPasswordActivity extends AppCompatActivity {

    private EditText masterPasswordEditText;
    private Button submitMasterPasswordButton;
    private ProgressBar progressBar;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_masterpassword);
        getSupportActionBar().setTitle("Enter Master Password");

        initializeViews();

        dbHelper = new DataBaseHelper(this);

        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);

        if (userId == -1) {
            showToast("User ID is invalid");
            Log.d("EnterMasterPasswordActivity", "User ID is invalid");
            finish();
            return;
        }

        Log.d("EnterMasterPasswordActivity", "Received UserID: " + userId);

        submitMasterPasswordButton.setOnClickListener(v -> attemptMasterPasswordVerification());
    }

    private void initializeViews() {
        masterPasswordEditText = findViewById(R.id.masterPasswordInput);
        submitMasterPasswordButton = findViewById(R.id.submitMasterPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void attemptMasterPasswordVerification() {
        progressBar.setVisibility(View.VISIBLE);

        String masterPassword = masterPasswordEditText.getText().toString().trim();

        Log.d("MasterPasswordDebug", "Master Password entered: [Hidden for security]");

        if (TextUtils.isEmpty(masterPassword)) {
            progressBar.setVisibility(View.GONE);
            showToast("Password field is required");
            return;
        }

        if (masterPassword.length() != 4) {
            progressBar.setVisibility(View.GONE);
            showToast("Password must be exactly 4 characters long");
            return;
        }

        boolean isMasterPasswordValid = dbHelper.checkMasterPassword(userId, masterPassword);

        progressBar.setVisibility(View.GONE);
        Log.d("MasterPasswordDebug", "Master Password valid: " + isMasterPasswordValid);

        if (isMasterPasswordValid) {
            showToast("Password correct");
            Log.d("MasterPasswordDebug", "Correct Master Password for UserID: " + userId);

            Intent intent = new Intent(EnterMasterPasswordActivity.this, PasswordsTableActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } else {
            showToast("Password incorrect");
            Log.e("MasterPasswordDebug", "Incorrect Master Password for UserID: " + userId);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
