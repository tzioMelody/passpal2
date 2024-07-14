package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passpal2.R;
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

        boolean isUserValid = dbHelper.checkUser(username, password);
        Log.d("LoginDebug", "User valid: " + isUserValid);

        if (isUserValid) {
            dbHelper.updateLastLogin(username);

            long userId = dbHelper.getUserIdByUsername(username);
            Log.d("LoginDebug", "UserID: " + userId);

            Intent intent;
            if (!dbHelper.hasMasterPassword(Math.toIntExact(userId))) {
                intent = new Intent(LoginActivity.this, MasterPasswordActivity.class);
                intent.putExtra("user_id", userId);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("user_id", userId);
            }
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
