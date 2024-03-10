package com.example.passpal2;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class EditSelectedAppActivity extends AppCompatActivity {

    private ImageView appIconImageView;
    private TextView appNameTextView;
    private EditText appLinkEditText;
    private EditText inputEmailEditedApp,inputUsernameEditedApp;
    private Button generatePsw;
    private Button saveSelectedAppData;
    private Button OpenAppWebsiteBtn;
    private ProgressBar progressBar;
    private EditText selectedAppPassword;
    private View overlayView;
    // Μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;
    private DataBaseHelper dbHelper = new DataBaseHelper(this);
    int appId;
    int userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_app);

        // Λήψη των πληροφοριών από το Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("APP_DATA")) {
            AppsObj selectedApp = intent.getParcelableExtra("APP_DATA");
            appId = intent.getIntExtra("APP_ID", -1);
           userid = intent.getIntExtra("USER_ID",-1);

            // Εύρεση των views στο layout
            appIconImageView = findViewById(R.id.appIconImageView);
            appNameTextView = findViewById(R.id.appNameTextView);
            appLinkEditText = findViewById(R.id.inputLinkEditedApp);
            inputUsernameEditedApp = findViewById(R.id.inputUsernameEditedApp);

            // Ορισμός της εικόνας
            appIconImageView.setImageResource(selectedApp.getAppImages());
            // Ορισμός του ονόματος
            appNameTextView.setText(selectedApp.getAppNames());
            // Ορισμός του link
            appLinkEditText.setText(selectedApp.getAppLinks());
        }
        inputEmailEditedApp = findViewById(R.id.inputEmailEditedApp);
        generatePsw = findViewById(R.id.GeneratePsw);
        saveSelectedAppData = findViewById(R.id.SaveSelectedAppData);
        OpenAppWebsiteBtn = findViewById(R.id.OpenAppWebsite);
        selectedAppPassword = findViewById(R.id.passwordEditText);

        generatePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GeneratePasswordTask().execute();
            }
        });
        saveSelectedAppData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmailEditedApp.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(EditSelectedAppActivity.this, "Please fill in the email field", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                overlayView.setVisibility(View.VISIBLE);

                EmailVerificationTask verificationTask = new EmailVerificationTask(new EmailVerificationTask.EmailVerificationListener() {
                    @Override
                    public void onEmailVerified(boolean isEmailValid) {
                        progressBar.setVisibility(View.GONE);
                        overlayView.setVisibility(View.GONE);
                        if (isEmailValid) {
                            saveChanges();
                        } else {
                            Toast.makeText(EditSelectedAppActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                verificationTask.execute(email);
            }
        });

        OpenAppWebsiteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = appLinkEditText.getText().toString();

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url; // Προσθήκη του http αν δεν υπάρχει για να διασφαλιστεί ότι το URL είναι έγκυρο
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });



        selectedAppPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                togglePasswordVisibility();
                return false;
            }
        });
    }




    private void saveChanges() {
        String username = inputUsernameEditedApp.getText().toString();
        String email = inputEmailEditedApp.getText().toString();
        String password = selectedAppPassword.getText().toString();

        int userId = userid;
        int appId = this.appId;

        boolean success = dbHelper.updateAppCredentials(appId, userId, username, email, password);

        if (success) {
            Toast.makeText(this, "Credentials saved successfully", Toast.LENGTH_SHORT).show();
            // Επιστροφή στην MainActivity
            Intent intent = new Intent(EditSelectedAppActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Failed to save credentials", Toast.LENGTH_SHORT).show();
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("POSITION", getIntent().getIntExtra("POSITION", -1));
        setResult(RESULT_OK, returnIntent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Αποθήκευση Αλλαγών") // Ορισμός του τίτλου του παραθύρου
                .setMessage("Είστε σίγουροι ότι θέλετε να φύγετε; Όλες οι αλλαγές που δεν έχουν αποθηκευτεί θα χαθούν.")
                .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditSelectedAppActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Όχι", null)
                .show();
    }

    // generate new password
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
        // Keep the cursor at the end
        selectedAppPassword.setSelection(selectedAppPassword.getText().length());
    }
}
