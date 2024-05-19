package com.example.passpal2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddAppUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText newAppName;
    private TextInputEditText newAppLink;
    private TextInputEditText newAppUsername;
    private TextInputEditText newAppEmail;
    private TextInputEditText newAppPassword;
    private ImageButton addAppPhoto;
    private Uri appImageUri;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appadduser);

        newAppName = findViewById(R.id.newAppname);
        newAppLink = findViewById(R.id.newAppLink);
        newAppUsername = findViewById(R.id.newAppUsername);
        newAppEmail = findViewById(R.id.newAppEmail);
        newAppPassword = findViewById(R.id.newAppPassword);
        addAppPhoto = findViewById(R.id.addAppPhoto);
        dbHelper = new DataBaseHelper(this);

        // Αποθήκευση του userId από το Intent
        userId = getIntent().getIntExtra("USER_ID", -1);

        addAppPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        findViewById(R.id.saveNewApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewApp();
            }
        });

        findViewById(R.id.generatePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewPassword();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            appImageUri = data.getData();
            addAppPhoto.setImageURI(appImageUri);
        }
    }

    private void generateNewPassword() {
        new GeneratePasswordTask(this, newPassword -> newAppPassword.setText(newPassword)).execute();
    }

    private void saveNewApp() {
        String appName = newAppName.getText().toString().trim();
        String appLink = newAppLink.getText().toString().trim();
        String username = newAppUsername.getText().toString().trim();
        String email = newAppEmail.getText().toString().trim();
        String password = newAppPassword.getText().toString().trim();

        if (appName.isEmpty() || appLink.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.isAppSelected(appName, userId)) {
            Toast.makeText(this, "Application name already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Αν το appImageUri είναι null, χρησιμοποιούμε το default_app_icon
        int appImageResId = appImageUri != null ? 0 : R.drawable.default_app_icon;

        // Insert the new app into the database
        boolean result = dbHelper.saveSelectedAppToDatabase(new AppsObj(appName, appLink, appImageResId, username, email, password), userId);

        if (result) {
            Toast.makeText(this, "Application added successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add application.", Toast.LENGTH_SHORT).show();
        }
    }
}
