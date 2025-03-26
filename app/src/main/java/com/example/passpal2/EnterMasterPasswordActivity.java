package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EnterMasterPasswordActivity extends AppCompatActivity {

    private EditText masterPasswordEditText;
    private Button submitMasterPasswordButton,forgotMasterPassword;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_masterpassword);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Password Vault");
        }

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
        forgotMasterPassword.setOnClickListener(v -> forgotMasterPasswordToChabgePass());



    }

    private void initializeViews() {
        masterPasswordEditText = findViewById(R.id.masterPasswordInput);
        submitMasterPasswordButton = findViewById(R.id.submitMasterPassword);
        forgotMasterPassword = findViewById(R.id.forgotMasterPassword);
    }

    private void attemptMasterPasswordVerification() {
        try {
            String masterPassword = masterPasswordEditText.getText().toString().trim();

            Log.d("MasterPasswordDebug", "Master Password entered: [Hidden for security]");

            if (TextUtils.isEmpty(masterPassword)) {
                showToast("Password field is required");
                return;
            }

            if (masterPassword.length() != 4) {
                showToast("Password must be exactly 4 characters long");
                return;
            }

            boolean isMasterPasswordValid = dbHelper.checkMasterPassword(userId, masterPassword);

            Log.d("MasterPasswordDebug", "Master Password valid: " + isMasterPasswordValid);

            if (isMasterPasswordValid) {
                showToast("Password is correct");
                Log.d("MasterPasswordDebug", "Correct Master Password for UserID: " + userId);

                Intent intent = new Intent(EnterMasterPasswordActivity.this, PasswordsTableActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                showToast("Password incorrect");
                Log.e("MasterPasswordDebug", "Incorrect Master Password for UserID: " + userId);
            }
        } catch (Exception e) {
            Log.e("MasterPasswordDebug", "Error during password verification: ", e);
            showToast("An error occurred. Please try again.");
        }
    }

    public void forgotMasterPasswordToChabgePass() {
        Intent intent = new Intent(EnterMasterPasswordActivity.this, ChangeMasterPasswordActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // back arrow
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
