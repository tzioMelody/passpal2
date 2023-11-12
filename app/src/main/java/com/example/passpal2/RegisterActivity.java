package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.passpal2.Data.Entities.User;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText inputUsername, inputEmail, inputPassword, inputConfirmPassword;
    private Button buttonRegister, alreadyAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Sign up");

        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        alreadyAccount = findViewById(R.id.alreadyAccount);

        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUsername.getText().toString();
                String password = inputPassword.getText().toString();
                String confirmPassword = inputConfirmPassword.getText().toString();

                String email = inputEmail.getText().toString();


                //Verify email
                VerifyEmailTask task = new VerifyEmailTask();
                task.execute(email);

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Δημιουργήστε ένα αντικείμενο DataBaseHelper
                    DataBaseHelper dbHelper = new DataBaseHelper(RegisterActivity.this);

                    if (dbHelper.isUserExists(email)) {
                        Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else if (dbHelper.isUsernameExists(username)) {
                        Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        User newUser = new User(username, email, password);
                        boolean isUserInserted = dbHelper.addOne(newUser);

                        if (isUserInserted) {
                            //Επιτυχημενη εγγραφη
/*
                            Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
*/
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(RegisterActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        });

    }

    private void getCurrentDateTimeInBackground(User user) {
        GetCurrentDateTimeTask task = new GetCurrentDateTimeTask(user);
        task.execute();
    }

    private class GetCurrentDateTimeTask extends AsyncTask<Void, Void, String[]> {
        private User user;

        GetCurrentDateTimeTask(User user) {
            this.user = user;
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            return getCurrentDateTime();
        }

        @Override
        protected void onPostExecute(String[] dateTime) {
            super.onPostExecute(dateTime);
            String loginDate = dateTime[0];
            String loginTime = dateTime[1];

            // Ενημέρωση της ημερομηνίας και της ώρας στον χρήστη
            user.setLoginDate(loginDate);
            user.setLoginTime(loginTime);

            // Εδώ πρέπει να προσθέσετε τον κώδικα για την αποθήκευση του χρήστη στη βάση δεδομένων
            // Προσαρμόστε ανάλογα με την βάση δεδομένων που χρησιμοποιείτε

            // Καλέστε τη μέθοδο getCurrentDateTimeInBackground για την ενημέρωση της ημερομηνίας και της ώρας στο υπόβαθρο
            getCurrentDateTimeInBackground(user);
        }
    }

    private String[] getCurrentDateTime() {
        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();
        return new String[]{currentDate, currentTime};
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


    private class VerifyEmailTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... emails) {
            String emailToVerify = emails[0];
            // Το API Key από το Hunter
            String apiKey = "9f387e4dfb8a839b9b246089137cc92244ad5562";

            try {
                URL url = new URL("https://api.hunter.io/v2/email-verifier?email=" + emailToVerify + "&api_key=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Παίρνετε το JSON από την απάντηση του Hunter API
                    String jsonString = result.toString();
                    try {
                        // Δημιουργείτε ένα αντικείμενο JSON από τη συμβολοσειρά JSON
                        JSONObject jsonObject = new JSONObject(jsonString);

                        // Παίρνετε το αντικείμενο data από το JSON
                        JSONObject data = jsonObject.getJSONObject("data");
                        // Παίρνετε την τιμή του πεδίου result
                        String emailResult = data.getString("result");

                        // Έλεγχος εάν το email είναι έγκυρο με βάση το πεδίο result
                        if (emailResult.equals("deliverable")) {
                            // Το email είναι έγκυρο
                            return true;
                        } else {
                            // Το email δεν είναι έγκυρο
                            return false;
                        }
                    } catch (JSONException e) {
                        // Αντιμετώπιση λαθών JSON
                        e.printStackTrace();
                        return false;
                    }

                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                // Αντιμετώπιση σφαλμάτων δικτύου
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isEmailValid) {
            super.onPostExecute(isEmailValid);
            if (isEmailValid) {
                // Το email είναι έγκυρο θα συνεχίσει την εγγραφή του χρήστη
                saveUserToDatabase();
            } else {
                //Το email δεν είναι έγκυρο βγαλε μήνυμα
                Toast.makeText(RegisterActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveUserToDatabase() {
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        String email = inputEmail.getText().toString();

        // Δημιουργείται ένα αντικείμενο User με τα δεδομένα που έχουν εισαχθεί
        User newUser = new User(username, email, password);

        // Προσθέτει τον νέο χρήστη στη βάση δεδομένων
        DataBaseHelper dbHelper = new DataBaseHelper(RegisterActivity.this);
        boolean isUserInserted = dbHelper.addOne(newUser);

        if (isUserInserted) {
            Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(RegisterActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
        }
    }


}
