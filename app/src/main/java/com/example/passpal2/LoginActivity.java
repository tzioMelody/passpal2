package com.example.passpal2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText inputPassword, inputUserName;
    private Button loginButton, donthaveaccountButton, forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        inputUserName = findViewById(R.id.usernameEditText);
        inputPassword = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.logInBtn);
        donthaveaccountButton = findViewById(R.id.donthaveaccountBtn);
        forgotPasswordButton = findViewById(R.id.forgotPasswordBtn);

        // Έλεγχος για αυτόματη σύνδεση
        checkSavedCredentials();

        loginButton.setOnClickListener(v -> loginUser());
        donthaveaccountButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        forgotPasswordButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String username = inputUserName.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
        DataBaseHelper.User user = dbHelper.getUserByUsername(username);

        if (user != null) {
            // Διαχωρίστε τον αποθηκευμένο κωδικό και το salt
            String storedPassword = user.getPassword();
            String[] parts = storedPassword.split(":");
            if (parts.length == 2) {
                String salt = parts[1]; // Ανακτήστε το salt
                String hashedInputPassword = DataBaseHelper.hashPassword(password, DataBaseHelper.decodeSalt(salt)); // Κάντε hash τον κωδικό με το salt

                if (hashedInputPassword.equals(parts[0])) { // Συγκρίνετε τον κωδικό
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    proceedToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your username and password.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Please check your username and password.", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveCredentials(String username, String password) {
        SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    private void successfulLogin(String username, String password) {
        DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
        saveCredentials(username, password);
        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
        proceedToMainActivity();
    }

    private void checkSavedCredentials() {
        SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if (username != null && password != null) {
            loginUserWithSharedPreferences(username, password);
        }
    }

    private void loginUserWithSharedPreferences(String username, String password) {
        DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
        DataBaseHelper.User user = dbHelper.getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            Toast.makeText(LoginActivity.this, "Automatic login successful", Toast.LENGTH_SHORT).show();
            proceedToMainActivity();
        }
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
