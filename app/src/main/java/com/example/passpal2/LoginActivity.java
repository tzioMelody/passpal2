package com.example.passpal2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passpal2.Data.Entities.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private EditText inputPassword, inputUserName;
    private Button loginButton, donthaveaccountButton, forgotPasswordButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        // Ενημερώνουμε τις αναφορές στο UI σύμφωνα με το XML layout
        inputUserName = findViewById(R.id.usernameEditText);
        inputPassword = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.logInBtn);
        donthaveaccountButton = findViewById(R.id.donthaveaccountBtn);
        forgotPasswordButton = findViewById(R.id.forgotPasswordBtn);

        checkSavedCredentials();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUserName.getText().toString();
                String password = inputPassword.getText().toString();

                // Έλεγχος κενού
                if (username.trim().isEmpty() || password.trim().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Έλεγχος εγκυρότητας του login
                if (username.trim().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }

                DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                DataBaseHelper.User user = dbHelper.getUserByUsername(username);

                if (user != null && user.getPassword().equals(password)) {
                    new UpdateLoginDateTimeTask(username).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Log.d("LoginActivity", "Όλα εντάξει;");
                    Toast.makeText(LoginActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();

                    // Αποθήκευση διαπιστευτηρίων για αυτόματη σύνδεση
                    saveCredentials(username, password);

                    // Μετάβαση στην κύρια δραστηριότητα
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    // Αν ο κωδικός δεν είναι σωστός, εμφάνισε ένα μήνυμα
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }

                db.close();
            }
        });

        donthaveaccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    // Ενημερώνει τη βάση δεδομένων με την ημερομηνία και την ώρα σύνδεσης
    private class UpdateLoginDateTimeTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private String currentDateTime;

        UpdateLoginDateTimeTask(String username) {
            this.username = username;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            currentDateTime = getCurrentDateTime();

            DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(DataBaseHelper.COLUMN_LOGINDATETIME, currentDateTime);
            db.update(DataBaseHelper.USER_TABLE, cv, DataBaseHelper.COLUMN_USERNAME + " = ?", new String[]{username});

            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Μετάβαση στην κύρια δραστηριότητα
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    // Αποθήκευση διαπιστευτηρίων για αυτόματη σύνδεση
    private void saveCredentials(String email, String password) {
        SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    // Έλεγχος για αποθηκευμένα διαπιστευτήρια κατά την έναρξη της δραστηριότητας
    private void checkSavedCredentials() {
        Pair<String, String> credentials = getSavedCredentials();
        if (credentials.first != null && credentials.second != null) {
            // Υπάρχουν αποθηκευμένα διαπιστευτήρια, οπότε μπορείτε να πραγματοποιήσετε αυτόματη σύνδεση
            loginUser(credentials.first, credentials.second);
        }
    }

    // Σύνδεση του χρήστη
    private void loginUser(String email, String password) {
        DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        DataBaseHelper.User user = dbHelper.getUserByUsername(email);

        if (user != null && user.getPassword().equals(password)) {
            new UpdateLoginDateTimeTask(email).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Log.d("LoginActivity", "Αυτόματη σύνδεση επιτυχής");

            // Μετάβαση στην κύρια δραστηριότητα
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        db.close();
    }
    // Ανάκτηση των διαπιστευτηρίων από τα SharedPreferences
    private Pair<String, String> getSavedCredentials() {
        SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
        String email = preferences.getString("email", null);
        String password = preferences.getString("password", null);
        return new Pair<>(email, password);
    }
}
