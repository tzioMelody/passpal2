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

import com.google.android.material.textfield.TextInputEditText;

import javax.crypto.SecretKey;

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

        // Αρχικοποίηση της DataBaseHelper
        dbHelper = new DataBaseHelper(this);

        // Αρχικοποίηση των UI στοιχείων
        initializeViews();

        buttonRegister.setOnClickListener(v -> attemptRegistration());
        getSupportActionBar().setTitle("Sign up");

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

        // Καταγραφή των τιμών για debugging
        Log.d("RegisterActivity", "Attempting registration for email: " + email);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showToast("All fields are required");
            return;
        }

        if (password.length() < 8) {
            showToast("Password must be at least 8 characters long");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return;
        }

        if (dbHelper.isUsernameExists(username)) {
            showToast("Username already taken");
            return;
        }

        if (dbHelper.isEmailTaken(email)) {
            showToast("Email already taken");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);

        // Εκκίνηση του EmailVerificationTask
        Log.d("RegisterActivity", "Starting EmailVerificationTask for email: " + email);
        new EmailVerificationTask(this, this).execute(email);
    }

    @Override
    public void onEmailVerified(boolean isEmailValid) {
        if (isEmailValid) {
            String username = inputUsername.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            Log.d("RegisterActivity", "Email verified. Proceeding with registration for user: " + username);
            registerUser(username, email, password);
        } else {
            showToast("Invalid email address");

            // Καταγραφή αν η επαλήθευση email απέτυχε
            Log.d("RegisterActivity", "Email verification failed for email: " + inputEmail.getText().toString().trim());

            progressBar.setVisibility(View.GONE);
            overlayView.setVisibility(View.GONE);
        }
    }

    private void registerUser(String username, String email, String password) {
        try {
            // Δημιουργία του AES κλειδιού
            SecretKey aesKey = DataBaseHelper.generateAESKey();

            // Κρυπτογράφηση του email και του password
            String encryptedEmail = DataBaseHelper.encryptAES(email, aesKey);
            String encryptedPassword = DataBaseHelper.encryptAES(password, aesKey);

            // Αποθήκευση του χρήστη στη βάση δεδομένων
            long userId = dbHelper.insertUser(username, encryptedEmail, encryptedPassword);
            if (userId != -1) {
                // Αποθήκευση του κλειδιού AES (χρησιμοποιούμε το userId)
                dbHelper.saveAESKey(aesKey, (int) userId);

                showToast("User registered successfully");
                Log.d("RegisterActivity", "User registered successfully with ID: " + userId);

                Intent intent = new Intent(RegisterActivity.this, SetMasterPasswordActivity.class);
                intent.putExtra("user_id", (int) userId);
                startActivity(intent);
                finish();
            } else {
                showToast("Failed to register user");
                Log.e("RegisterActivity", "Failed to register user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to register user due to error");
            Log.e("RegisterActivity", "Error during registration: " + e.getMessage());
        } finally {
            progressBar.setVisibility(View.GONE);
            overlayView.setVisibility(View.GONE);
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
