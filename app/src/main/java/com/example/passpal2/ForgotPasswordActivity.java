package com.example.passpal2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText inputEmail, newPassword, confirmNewPassword;
    private Button resetPassBtn, cancelBtnForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setTitle("Reset Password");

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

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Please enter a valid email address.");
            } else if (!dbHelper.isEmailTaken(email)) {
                showToast("Email not found in database.");
            } else if (TextUtils.isEmpty(newPasswordText) || newPasswordText.length() < 8) {
                showToast("Password should be at least 8 characters.");
            } else if (!newPasswordText.equals(confirmNewPasswordText)) {
                showToast("Passwords do not match.");
            } else {
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
                byte[] salt = DataBaseHelper.generateSalt();
                String hashedPassword = DataBaseHelper.hashPassword(newPassword, salt);
                String saltStr = DataBaseHelper.encodeSalt(salt);
                String passwordToStore = hashedPassword + ":" + saltStr;

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
