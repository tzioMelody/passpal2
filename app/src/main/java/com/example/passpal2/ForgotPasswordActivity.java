package com.example.passpal2;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText inputEmail, newPassword, ConfirmNewPassword;
    private Button resetPassBtn, CancelbtnForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

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
                String Newpassword = newPassword.getText().toString();
                String confirmNewPassword = ConfirmNewPassword.getText().toString();


                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
                    //Checking how valid the email address is
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please re-enter email!", Toast.LENGTH_SHORT).show();
                } else if(TextUtils.isEmpty(Newpassword)){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                } else if(Newpassword.length() < 8){
                    Toast.makeText(ForgotPasswordActivity.this, "Password should be at least 8 digits", Toast.LENGTH_SHORT).show();
                }else if(Newpassword != confirmNewPassword){
                    Toast.makeText(ForgotPasswordActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}