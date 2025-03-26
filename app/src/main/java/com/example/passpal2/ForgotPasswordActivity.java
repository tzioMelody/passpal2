package com.example.passpal2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText inputEmail, newPassword, confirmNewPassword;
    private Button resetPassBtn, cancelBtnForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ρύθμιση Action Bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.dark_blue)));
        getSupportActionBar().setTitle("Reset your password!");

        resetPassBtn = findViewById(R.id.resetpassbtn);
        cancelBtnForgot = findViewById(R.id.CancelbtnForgot);

        inputEmail = findViewById(R.id.inputEmail);
        newPassword = findViewById(R.id.ResetpasswordEditText);
        confirmNewPassword = findViewById(R.id.ConfirmpasswordEditText);

        cancelBtnForgot.setOnClickListener(v -> finish());

        resetPassBtn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String newPasswordText = newPassword.getText().toString().trim();
            String confirmNewPasswordText = confirmNewPassword.getText().toString().trim();
            DataBaseHelper dbHelper = new DataBaseHelper(ForgotPasswordActivity.this);

            // Ανάκτηση του αποθηκευμένου κωδικού του χρήστη
            String existingPass = dbHelper.getStoredPassword(email);
            if (existingPass == null) {
                showToast("No password found for this email.");
                return;
            }

            // Εδώ διαχωρίζουμε το stored password και το salt
            String[] parts = existingPass.split(":");
            if (parts.length != 2) {
                showToast("Error retrieving password.");
                return;
            }
            String storedHash = parts[0];
            String storedSaltStr = parts[1];
            byte[] storedSalt = PasswordUtil.decodeSalt(storedSaltStr);

            // Έλεγχος αν ο νέος κωδικός είναι ίδιος με τον αποθηκευμένο
            String hashedNewPassword = PasswordUtil.hashPassword(newPasswordText, storedSalt);
            if (hashedNewPassword.equals(storedHash)) {
                showToast("The new password must be different from the current one.");
                return;
            }

            // Έλεγχοι για το email και τα passwords
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Please enter a valid email address.");
            } else if (!dbHelper.isEmailTaken(email)) {
                showToast("Email not found in database.");
            } else if (TextUtils.isEmpty(newPasswordText) || newPasswordText.length() < 8) {
                showToast("Password should be at least 8 characters.");
            } else if (!newPasswordText.equals(confirmNewPasswordText)) {
                showToast("Passwords do not match.");
            } else {
                // Εκτέλεση του Task για ενημέρωση του κωδικού
                new UpdatePasswordTask(email, newPasswordText, ForgotPasswordActivity.this).execute();
            }
        });

    }

    private class UpdatePasswordTask extends AsyncTask<Void, Void, Boolean> {
        private String email;
        private String newPassword;
        private Context context;

        UpdatePasswordTask(String email, String newPassword, Context context) {
            this.email = email;
            this.newPassword = newPassword;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            try {
                // Δημιουργία salt για τον νέο κωδικό
                byte[] salt = PasswordUtil.generateSalt();

                // Δημιουργία του hash του νέου κωδικού
                String hashedPassword = PasswordUtil.hashPassword(newPassword, salt);

                // Κωδικοποίηση του salt για αποθήκευση στη βάση δεδομένων
                String saltStr = PasswordUtil.encodeSalt(salt);

                // Συνδυασμός του hash και του salt για αποθήκευση
                String passwordToStore = hashedPassword + ":" + saltStr;

                // Ενημέρωση του password στη βάση δεδομένων
                dbHelper.updatePasswordByEmail(email, passwordToStore);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                showToast("Password reset successful. You can now log in with your new password.");
                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).finish();
            } else {
                showToast("Password reset failed. Please try again.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
