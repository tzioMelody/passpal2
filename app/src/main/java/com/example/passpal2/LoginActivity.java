package com.example.passpal2;

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
    private Button login, donthaveaccount, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        inputPassword = findViewById(R.id.passWord);
        inputUserName = findViewById(R.id.userName);

        login = findViewById(R.id.login);
        donthaveaccount = findViewById(R.id.donthaveaccount);
        forgotPassword = findViewById(R.id.forgotpassword);

        // Έλεγχος αποθηκευμένων διαπιστευτηρίων για autofill
        checkSavedCredentials();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailEditText = findViewById(R.id.userName);
                String username = inputUserName.getText().toString();

                EditText passwordEditText = findViewById(R.id.passWord);
                String userPassword = passwordEditText.getText().toString();

                // Έλεγχος κενού
                if (username.trim().isEmpty() || userPassword.trim().isEmpty()) {
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

                User user = dbHelper.getUserByUsername(username);

                if (user != null && user.getPassword().equals(userPassword)) {
                    new UpdateLoginDateTimeTask(username).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Log.d("LoginActivity", "Όλα εντάξει;");

                    // Αποθήκευση διαπιστευτηρίων για αυτόματη σύνδεση
                    saveCredentials(username, userPassword);

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

        donthaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
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
        private String loginDate;
        private String loginTime;

        UpdateLoginDateTimeTask(String username) {
            this.username = username;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loginDate = getCurrentDate();
            loginTime = getCurrentTime();

            DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(DataBaseHelper.COLUMN_LOGINDATE, loginDate);
            cv.put(DataBaseHelper.COLUMN_LOGINTIME, loginTime);
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

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
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

        User user = dbHelper.getUserByUsername(email);

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
