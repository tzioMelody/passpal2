package com.example.passpal2;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.passpal2.R;

import java.util.Random;

public class EditSelectedAppActivity extends AppCompatActivity {
    private ImageView appIconImageView;
    private TextView appNameTextView;
    private EditText appLinkEditText;
    private EditText inputEmailEditedApp, inputUsernameEditedApp;
    private Button generatePsw;
    private Button saveSelectedAppData;
    private Button openAppWebsiteBtn;
    private ProgressBar progressBar;
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
        generatePsw = findViewById(R.id.GeneratePsw);
        saveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        openAppWebsiteBtn = findViewById(R.id.OpenAppWebsite);
        selectedAppPassword = findViewById(R.id.passwordEditText);

        generatePsw.setOnClickListener(view -> new GeneratePasswordTask().execute());
        saveSelectedAppData.setOnClickListener(v -> {
            String email = inputEmailEditedApp.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(EditSelectedAppActivity.this, "Please fill in the email field", Toast.LENGTH_SHORT).show();
                return;
            }

            EmailVerificationTask verificationTask = new EmailVerificationTask(isEmailValid -> {
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

    private void saveChanges() {
        String username = inputUsernameEditedApp.getText().toString();
        String email = inputEmailEditedApp.getText().toString();
        String password = selectedAppPassword.getText().toString();
        String link = appLinkEditText.getText().toString();
        String appName = appNameTextView.getText().toString();

        boolean success = dbHelper.saveAppCredentials(appId, userId, appName, username, email, password, link);

        if (success) {
            Toast.makeText(this, "Credentials saved successfully", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("APP_ID", appId);
            returnIntent.putExtra("UPDATED_APP_NAME", appNameTextView.getText().toString());
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save credentials", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Αποθήκευση Αλλαγών")
                .setMessage("Είστε σίγουροι ότι θέλετε να φύγετε; Όλες οι αλλαγές που δεν έχουν αποθηκευτεί θα χαθούν.")
                .setPositiveButton("Ναι", (dialog, which) -> EditSelectedAppActivity.super.onBackPressed())
                .setNegativeButton("Όχι", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            int len = 12;
            return generatePswd(len);
        }

        @Override
        protected void onPostExecute(String result) {
            selectedAppPassword.setText(result);
        }
    }

    private String generatePswd(int len) {
        String charsCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String chars = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String symbols = "!@#$%^&*_=+-/€.?<>)";

        String passSymbols = charsCaps + chars + nums + symbols;
        Random rnd = new Random();

        StringBuilder password = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            password.append(passSymbols.charAt(rnd.nextInt(passSymbols.length())));
        }
        return password.toString();
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
