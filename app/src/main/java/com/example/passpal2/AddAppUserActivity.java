package com.example.passpal2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddAppUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;

    private TextInputEditText newAppName;
    private TextInputEditText newAppLink;
    private TextInputEditText newAppUsername;
    private TextInputEditText newAppEmail;
    private TextInputEditText newAppPassword;
    private ImageButton addAppPhoto;
    private Button cancelNewApp;
    private Uri appImageUri;
    private Bitmap appImageBitmap;
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
        cancelNewApp = findViewById(R.id.cancelNewApp);
        dbHelper = new DataBaseHelper(this);

        // Αποθήκευση του userId από το Intent
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add an app");
        }

        addAppPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        findViewById(R.id.saveNewApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewApp();
            }
        });

        cancelNewApp.setOnClickListener(view -> {
            // Έλεγχος αν όλα τα πεδία είναι κενά
            if (areAllFieldsEmpty()) {
                // Αν όλα τα πεδία είναι κενά, επιστροφή στην MainActivity χωρίς διάλογο
                Intent intent = new Intent(AddAppUserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Αν υπάρχει περιεχόμενο σε κάποιο πεδίο
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure?")
                        .setMessage("Are you sure you want to leave? The changes you've made will not be saved!")
                        .setPositiveButton("YES", (dialog, which) -> {
                            // Δημιουργία ενός νέου Intent για να επιστρέψει στην MainActivity
                            Intent intent = new Intent(AddAppUserActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    // Μέθοδος για έλεγχο αν όλα τα πεδία είναι κενά
    private boolean areAllFieldsEmpty() {
        return newAppName.getText().toString().trim().isEmpty() &&
                newAppLink.getText().toString().trim().isEmpty() &&
                newAppUsername.getText().toString().trim().isEmpty() &&
                newAppEmail.getText().toString().trim().isEmpty() &&
                newAppPassword.getText().toString().trim().isEmpty();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // back arrow
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageSourceDialog() {
        // Κάμερα ή Συλλογή
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        openImageChooser();
                    }
                })
                .show();
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
        }
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
            appImageBitmap = null; // Μηδενίζουμε το bitmap αν υπάρχει URI
            addAppPhoto.setImageURI(appImageUri);
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            appImageBitmap = (Bitmap) extras.get("data");
            appImageUri = null; // Μηδενίζουμε το URI αν υπάρχει bitmap
            addAppPhoto.setImageBitmap(appImageBitmap);
        }
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

        if (dbHelper.isLinkTaken(appLink, userId)) {
            Toast.makeText(this, "Application with the same link already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Επαλήθευση URL Format
        if (!Patterns.WEB_URL.matcher(appLink).matches()) {
            Toast.makeText(this, "Please enter a valid URL format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Επαλήθευση Αντικειμενικής Υπαρξης URL
        new UrlValidationTask(isValid -> {
            if (!isValid) {
                Toast.makeText(this, "The URL is not reachable. Please enter a valid URL.", Toast.LENGTH_SHORT).show();
            } else {
                proceedWithAppSave(appName, appLink, username, email, password);
            }
        }).execute(appLink);
    }

    private void proceedWithAppSave(String appName, String appLink, String username, String email, String password) {
        byte[] appImageBytes = null;
        if (appImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            appImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            appImageBytes = baos.toByteArray();
        } else if (appImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), appImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                appImageBytes = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Αν δεν έχει επιλεγεί εικόνα, προσθέτουμε μια default
            appImageBytes = getDefaultImageBytes();
        }

        boolean result = dbHelper.saveSelectedAppToDatabase(
                new AppsObj(appName, appLink, 0, username, email, password, appImageBytes),
                userId
        ) && dbHelper.saveAppCredentials(userId, appName, username, email, password, appLink);

        if (result) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("AppName", appName);
            returnIntent.putExtra("AppLink", appLink);
            returnIntent.putExtra("AppImageUri", appImageUri != null ? appImageUri.toString() : null);

            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to add application.", Toast.LENGTH_SHORT).show();
        }
    }

    // Default image
    private byte[] getDefaultImageBytes() {
        Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_app_image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        defaultBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}