package com.example.passpal2;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText;
    private ProgressBar progressBar;
    private Button logInBtn, forgotPasswordBtn, donthaveaccountBtn;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        initializeViews();

        dbHelper = new DataBaseHelper(this);
        dbHelper.getWritableDatabase();

        logInBtn.setOnClickListener(v -> attemptLogin());
        donthaveaccountBtn.setOnClickListener(v -> navigateToRegister());
        forgotPasswordBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        logInBtn = findViewById(R.id.logInBtn);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);
        donthaveaccountBtn = findViewById(R.id.donthaveaccountBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void navigateToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d("LoginDebug", "Username: " + username);
        Log.d("LoginDebug", "Password: " + password);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToast("All fields are required");
            return;
        }

        // Έλεγχος αν υπάρχει ο χρήστης με αυτά τα στοιχεία
        boolean isUserValid = dbHelper.checkUser(username, password);
        Log.d("LoginDebug", "User valid: " + isUserValid);

        if (isUserValid) {
            // Λήψη του user ID
            int userId = dbHelper.getUserIdByUsername(username);
            Log.d("LoginDebug", "UserID: " + userId);

            // Προώθηση στην κύρια δραστηριότητα
            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Please check your username and password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
