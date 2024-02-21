package com.example.passpal2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

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
            // Διαχωρισμος του αποθηκευμένου κωδικου και το salt
            String storedPassword = user.getPassword();
            String[] parts = storedPassword.split(":");
            if (parts.length == 2) {
                // Ανακτήστε το salt
                String salt = parts[1];
                // Γινεται hash ο κωδικός με το salt
                String hashedInputPassword = DataBaseHelper.hashPassword(password, DataBaseHelper.decodeSalt(salt));

                // Συγκριση των κωδικων
                if (hashedInputPassword.equals(parts[0])) {

                    // Αν η σύνδεση είναι επιτυχής, ανακτήστε το ID του χρήστη και το όνομά του
                    int userId = user.getId();
                    String loggedInUsername = dbHelper.getUsernameByUserId(userId);

                    // Αποθηκεύστε το ID του χρήστη για μελλοντική χρήση, εάν είναι απαραίτητο
                    saveUserId(userId);

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

    // Εσωτερική κλάση για την αντιπροσωπεία του AsyncTask
    private class RetrieveUserIdTask extends AsyncTask<String, Void, Integer> {
        private WeakReference<LoginActivity> activityReference;

        RetrieveUserIdTask(LoginActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Integer doInBackground(String... params) {
            String username = params[0];
            DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
            DataBaseHelper.User user = dbHelper.getUserByUsername(username);
            if (user != null) {
                // Επιστροφή του userId
                return user.getId();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer userId) {
            super.onPostExecute(userId);
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (userId != null) {

                activity.saveUserId(userId);
                // Ενημέρωση του χρήστη για επιτυχή σύνδεση
                Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show();
                activity.proceedToMainActivity();
            } else {
                // Εμφάνιση μηνύματος σφάλματος αν δεν βρέθηκε το userId
                Toast.makeText(activity, "Login failed. Please check your username and password.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Μέθοδος για την αποθήκευση του userId
    private void saveUserId(int userId) {
        SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("userId", userId);
        editor.apply();
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
