package com.example.passpal2;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private CheckBox rememberMeCheckBox;
    SharedPreferences preferences;
    private TextInputEditText usernameEditText, passwordEditText;
    private ProgressBar progressBar;
    private Button logInBtn, forgotPasswordBtn, donthaveaccountBtn;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ρύθμιση Action Bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.dark_blue)));
        getSupportActionBar().setTitle("Sign in!");
        initializeViews();

        dbHelper = new DataBaseHelper(this);
        dbHelper.getWritableDatabase();

        // Χρησιμοποίησε το ίδιο όνομα αρχείου για τα SharedPreferences
        preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Αν το remembered_user_id υπάρχει, πήγαινε κατευθείαν στο MainActivity
        int rememberedUserId = preferences.getInt("remembered_user_id", -1);

        if (rememberedUserId != -1) {
            // Ο χρήστης είναι ήδη συνδεδεμένος, πήγαινε κατευθείαν στο κύριο Activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_id", rememberedUserId);
            startActivity(intent);
            finish();
        }

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
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

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
            int userId = dbHelper.getUserIdByUsername(username);

            // Αποθήκευση user_id στα Shared Preferences με το σωστό όνομα
            preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);  // Χρησιμοποίησε το ίδιο όνομα αρχείου
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("userId", userId);
            if (rememberMeCheckBox.isChecked()) {
                // Αν είναι τσεκαρισμένο, αποθήκευσε το remembered_user_id
                editor.putInt("remembered_user_id", userId);
            } else {
                editor.remove("remembered_user_id");  // Αλλιώς αφαίρεσέ το
            }
            editor.apply();

            // Προώθηση στην κύρια δραστηριότητα
            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            // Εμφάνιση μηνύματος για λάθος credentials
            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
