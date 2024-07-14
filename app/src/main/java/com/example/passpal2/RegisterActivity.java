package com.example.passpal2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class RegisterActivity extends AppCompatActivity implements EmailVerificationTask.EmailVerificationListener {

    private TextInputEditText inputUsername, inputEmail, inputPassword, inputConfirmPassword;
    private ProgressBar progressBar;
    private Button buttonRegister, alreadyAccount;
    private View overlayView;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();

        buttonRegister.setOnClickListener(v -> attemptRegistration());
        getSupportActionBar().setTitle("Sign up");

        dbHelper = new DataBaseHelper(this);
        dbHelper.getWritableDatabase();

        Log.d("RegisterActivity", "Η βάση δεδομένων έχει δημιουργηθεί και είναι έτοιμη.");

        alreadyAccount.setOnClickListener(v -> navigateToLogin());
    }

    private void initializeViews() {
        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        alreadyAccount = findViewById(R.id.alreadyAccount);
        progressBar = findViewById(R.id.progressBar);
        overlayView = findViewById(R.id.overlayView);
    }

    private void navigateToLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    private void attemptRegistration() {
        String username = inputUsername.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            showToast("Password must be at least 8 characters long");
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isUsernameTaken(username)) {
            Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isEmailTaken(email)) {
            Toast.makeText(this, "Email already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        new EmailVerificationTask(this).execute(email);
    }

    private void registerUser(String username, String email, String password) {
        if (dbHelper.isUserExists(email) || dbHelper.isUsernameTaken(username)) {
            showToast("User already exists");
            return;
        }

        try {
            byte[] salt = DataBaseHelper.generateSalt();
            String hashedPassword = DataBaseHelper.hashPassword(password, salt);
            String saltStr = DataBaseHelper.encodeSalt(salt);
            String passwordToStore = hashedPassword + ":" + saltStr;

            DataBaseHelper.User newUser = new DataBaseHelper.User(0, username, email, passwordToStore, "");

            long userId = dbHelper.insertUser(username, email, passwordToStore);
            if (userId != -1) {
                showToast("User registered successfully");

                Intent intent = new Intent(RegisterActivity.this, MasterPasswordActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                showToast("Failed to register user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to register user due to error");
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmailVerified(boolean isEmailValid) {
        if (isEmailValid) {
            String username = inputUsername.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            registerUser(username, email, password);
        } else {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);
    }
}
