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

import com.example.passpal2.MainActivity;
import com.example.passpal2.R;

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
        newPassword = findViewById(R.id.ResetpasswordEditText);
        ConfirmNewPassword = findViewById(R.id.ConfirmpasswordEditText);

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

                DataBaseHelper dbHelper = new DataBaseHelper(ForgotPasswordActivity.this);

                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                } else if (!dbHelper.isEmailExists(email)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Email not found in database.", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newPasswordText) || newPasswordText.length() < 8) {
                    Toast.makeText(ForgotPasswordActivity.this, "Password should be at least 8 characters.", Toast.LENGTH_SHORT).show();
                } else if (!newPasswordText.equals(confirmNewPasswordText)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    new UpdatePasswordTask(email, newPasswordText, ForgotPasswordActivity.this).execute();
                }
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
            dbHelper.updatePasswordByEmail(context, email, newPassword);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                // Password reset successful, navigate to MainActivity
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity) context).finish();
            } else {
                Toast.makeText(context, "Password reset failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
