package com.example.passpal2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        inputPassword = findViewById(R.id.passWord);
        inputUserName = findViewById(R.id.userName);

        login = findViewById(R.id.login);
        donthaveaccount = findViewById(R.id.donthaveaccount);
        forgotPassword = findViewById(R.id.forgotpassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUserName.getText().toString(); // Αποθηκεύστε το όνομα χρήστη
                String password = inputPassword.getText().toString();

                // Ελέγξτε τη βάση δεδομένων για την ύπαρξη του χρήστη
                DataBaseHelper dbHelper = new DataBaseHelper(LoginActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Αλλάξτε τη μέθοδο για να πάρετε τον χρήστη με βάση το όνομα χρήστη
                User user = dbHelper.getUserByUsername(username);

                if (user != null && user.getPassword().equals(password)) {
                    // Επιτυχής σύνδεση. Ενημέρωση της βάσης δεδομένων με την ημερομηνία και την ώρα.
                    new UpdateLoginDateTimeTask(username).execute();
                } else {
                    // Αν ο κωδικός δεν είναι σωστός, εμφάνισε ένα μήνυμα
                    Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }

                // Μην ξεχάσετε να κλείσετε τη βάση δεδομένων μετά τη χρήση
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
            cv.put(UserDB.COLUMN_LOGIN_DATE, loginDate);
            cv.put(UserDB.COLUMN_LOGIN_TIME, loginTime);
            db.update(UserDB.TABLE_USERS, cv, UserDB.COLUMN_USERNAME + " = ?", new String[]{username});

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
}
