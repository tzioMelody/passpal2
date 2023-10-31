package com.example.passpal2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.passpal2.Data.Entities.User;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText inputEmail, newPassword, ConfirmNewPassword;
    private Button resetPassBtn, CancelbtnForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setTitle("Reset password");

        resetPassBtn = findViewById(R.id.resetpassbtn);
        CancelbtnForgot = findViewById(R.id.CancelbtnForgot);

        inputEmail = findViewById(R.id.inputEmail);
        newPassword = findViewById(R.id.newPassword);
        ConfirmNewPassword = findViewById(R.id.ConfirmNewPassword);

        CancelbtnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resetPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String newPasswordText = newPassword.getText().toString();
                String confirmNewPasswordText = ConfirmNewPassword.getText().toString();

                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newPasswordText) || newPasswordText.length() < 8) {
                    Toast.makeText(ForgotPasswordActivity.this, "Password should be at least 8 characters.", Toast.LENGTH_SHORT).show();
                } else if (!newPasswordText.equals(confirmNewPasswordText)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    // Εκκινήστε το AsyncTask για την ενημέρωση του κωδικού πρόσβασης
                    new UpdatePasswordTask(email, newPasswordText).execute();
                }
            }
        });
    }

    private class UpdatePasswordTask extends AsyncTask<Void, Void, Void> {
        private String email;
        private String newPassword;

        UpdatePasswordTask(String email, String newPassword) {
            this.email = email;
            this.newPassword = newPassword;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Εδώ μπορείτε να κάνετε την ενημέρωση του νέου κωδικού πρόσβασης στη βάση δεδομένων
            DataBaseHelper dbHelper = new DataBaseHelper(ForgotPasswordActivity.this);
            dbHelper.updatePasswordByEmail(email, newPassword);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(ForgotPasswordActivity.this, "Password reset successful.", Toast.LENGTH_SHORT).show();
        }
    }
}
