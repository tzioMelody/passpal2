package com.example.passpal2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {
    private EditText password,username;
    private Button login, forgotpassword,donthaveaccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.passWord);
        username = findViewById(R.id.userName);

        Button login = findViewById(R.id.login);
        Button donthaveaccount = findViewById(R.id.donthaveaccount);
        Button forgotpassword = findViewById(R.id.forgotpassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        donthaveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);}
        });

        forgotpassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);}
        });
    }
    public void loginChecker (View View) {
        String name = username.getText().toString();
        String pass = password.getText().toString();
        if (username.equals(username) && password.equals(password)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "LogIn Successful", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText( this ,"password/username Incorrect",Toast.LENGTH_SHORT).show();
        }
    }
}
