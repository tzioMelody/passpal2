package com.example.passpal2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private DataBaseHelper dbHelper = new DataBaseHelper(this);
    private TextInputEditText inputUsername, inputEmail, inputPassword, inputConfirmPassword;
    private Button buttonRegister, alreadyAccount;
    private ProgressBar progressBar;
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Sign up");

        // για την σωστή εκκίνηση της βάσης τοπικά στο κινητό
        dbHelper.getWritableDatabase();


        Log.d("RegisterActivity", "Η βάση δεδομένων έχει δημιουργηθεί και είναι έτοιμη.");


        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        alreadyAccount = findViewById(R.id.alreadyAccount);
        progressBar = findViewById(R.id.progressBar);
        overlayView = findViewById(R.id.overlayView);

        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });
    }




    private void attemptRegistration() {
        String username = inputUsername.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Εμφάνιση του ProgressBar και της αναμονής
        progressBar.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);

        // Καλέστε τον AsyncTask για την επαλήθευση του email
        EmailVerificationTask verificationTask = new EmailVerificationTask(new EmailVerificationTask.EmailVerificationListener() {
            @Override
            public void onEmailVerified(boolean isEmailValid) {
                progressBar.setVisibility(View.GONE);
                overlayView.setVisibility(View.GONE);
                if (isEmailValid) {
                    // Προχωρήστε με την εγγραφή αν το email είναι έγκυρο
                    registerUser(username, email, password);
                } else {
                    // Εμφάνιση μηνύματος σφάλματος αν το email δεν είναι έγκυρο
                    Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
        verificationTask.execute(email);
    }

    private void registerUser(String username, String email, String password) {
        if (dbHelper.isUserExists(email) || dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Δημιουργία salt και hashed password
            byte[] salt = DataBaseHelper.generateSalt();
            String hashedPassword = DataBaseHelper.hashPassword(password, salt);

            // Κωδικοποίηση το salt για αποθήκευση
            String saltStr = DataBaseHelper.encodeSalt(salt);

            //Αποθήκευση
            String passwordToStore = hashedPassword + ":" + saltStr;

            DataBaseHelper.User newUser = new DataBaseHelper.User(0, username, email, passwordToStore);

            // εισαγωγή του νέου χρήστη στη βάση
            if (dbHelper.addOne(newUser)) {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();

                // Παιρνω το ID του
                int userId = dbHelper.getUserIdByUsername(username);

                // Προσθέστε το όνομα χρήστη στο Intent
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to register user due to an error", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isPasswordStrong(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }


}
