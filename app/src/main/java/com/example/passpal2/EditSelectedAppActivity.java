package com.example.passpal2;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.security.NoSuchAlgorithmException;

public class EditSelectedAppActivity extends AppCompatActivity {
    private ImageView appIconImageView;
    private TextView appNameTextView;
    private EditText appLinkEditText;
    private EditText inputEmailEditedApp, inputUsernameEditedApp;
    private Button saveSelectedAppData;
    private Button openAppWebsiteBtn;
    private EditText selectedAppPassword;
    private boolean isPasswordVisible = false;
    private DataBaseHelper dbHelper;
    int appId;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);



        dbHelper = new DataBaseHelper(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("APP_DATA")) {
            AppsObj selectedApp = intent.getParcelableExtra("APP_DATA");
            String appName = selectedApp.getAppNames();

           // να φανει το ονομα της εφαρμογης πανω στην μπαρα
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(appName);
            }


            appId = intent.getIntExtra("APP_ID", -1);
            userId = intent.getIntExtra("USER_ID", -1);

            appIconImageView = findViewById(R.id.appIconImageView);
            appNameTextView = findViewById(R.id.appNameTextView);
            appLinkEditText = findViewById(R.id.inputLinkEditedApp);
            inputUsernameEditedApp = findViewById(R.id.inputUsernameEditedApp);

            appIconImageView.setImageResource(selectedApp.getAppImages());
            appNameTextView.setText(selectedApp.getAppNames());
            appLinkEditText.setText(selectedApp.getAppLinks());
        }
        inputEmailEditedApp = findViewById(R.id.inputEmailEditedApp);
        saveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        openAppWebsiteBtn = findViewById(R.id.OpenAppWebsite);
        selectedAppPassword = findViewById(R.id.passwordEditText);

        saveSelectedAppData.setOnClickListener(v -> {
            String email = inputEmailEditedApp.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(EditSelectedAppActivity.this, "Please fill in the email field", Toast.LENGTH_SHORT).show();
                return;
            }

            EmailVerificationTask verificationTask = new EmailVerificationTask(EditSelectedAppActivity.this, isEmailValid -> {
                if (isEmailValid) {
                    saveChanges();
                } else {
                    Toast.makeText(EditSelectedAppActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                }
            });
            verificationTask.execute(email);

        });

        openAppWebsiteBtn.setOnClickListener(view -> {
            String url = appLinkEditText.getText().toString();

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        selectedAppPassword.setOnTouchListener((v, event) -> {
            togglePasswordVisibility();
            return false;
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Αν πατηθεί το back arrow, επιστρέφουμε στην προηγούμενη οθόνη
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        String username = inputUsernameEditedApp.getText().toString();
        String email = inputEmailEditedApp.getText().toString();
        String password = selectedAppPassword.getText().toString();
        String link = appLinkEditText.getText().toString();
        String appName = appNameTextView.getText().toString();

        // Add hashing for the password before saving
        try {
            byte[] salt = DataBaseHelper.generateSalt();
            String hashedPassword = DataBaseHelper.hashPassword(password, salt);
            String saltStr = DataBaseHelper.encodeSalt(salt);
            String passwordToStore = hashedPassword + ":" + saltStr;

            // Insert new credentials into the database (not updating existing ones)
            boolean success = dbHelper.saveAppCredentials(userId, appName, username, email, passwordToStore, link);

            if (success) {
                Toast.makeText(this, "Credentials saved successfully", Toast.LENGTH_SHORT).show();

                // Hide save button and show the "Open app/website" button
                saveSelectedAppData.setVisibility(View.GONE);
                openAppWebsiteBtn.setVisibility(View.VISIBLE);

                // Keep the entered data visible and show a result message
                Intent returnIntent = new Intent();
                returnIntent.putExtra("APP_ID", appId);
                returnIntent.putExtra("UPDATED_APP_NAME", appNameTextView.getText().toString());
                setResult(RESULT_OK, returnIntent);

            } else {
                Toast.makeText(this, "Failed to save credentials", Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to hash password", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Αποθήκευση Αλλαγών")
                .setMessage("Είστε σίγουροι ότι θέλετε να φύγετε; Όλες οι αλλαγές που δεν έχουν αποθηκευτεί θα χαθούν.")
                .setPositiveButton("Ναι", (dialog, which) -> {
                    setResult(RESULT_OK);
                    super.onBackPressed();
                })
                .setNegativeButton("Όχι", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            selectedAppPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            selectedAppPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        selectedAppPassword.setSelection(selectedAppPassword.getText().length());
    }
}
