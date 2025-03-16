package com.example.passpal2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AddAppUserActivity extends AppCompatActivity {

    private TextInputEditText newAppName,newAppLink,newAppUsername,newAppEmail,newAppPassword;
    private ImageButton addAppPhoto;
    private Button cancelNewApp;
    private Uri appImageUri;
    private Bitmap appImageBitmap;
    private DataBaseHelper dbHelper;
    private int userId;
    private static final int REQUEST_CAMERA_PERMISSION = 100;


    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    addAppPhoto.setImageURI(appImageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    appImageUri = uri;
                    appImageBitmap = null;
                    addAppPhoto.setImageURI(appImageUri);
                }
            }
    );

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

        addAppPhoto.setOnClickListener(v -> showImageSourceDialog());

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            File photoFile = createImageFile();
            if (photoFile != null) {
                appImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                takePictureLauncher.launch(appImageUri);
            } else {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImageChooser() {
        pickPhotoLauncher.launch("image/*");
    }

    private File createImageFile() {
        File storageDir = getExternalFilesDir("Pictures");
        try {
            return File.createTempFile("JPEG_", ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void saveNewApp() {
        String appName = newAppName.getText().toString().trim();
        String appLink = newAppLink.getText().toString().trim();
        String username = newAppUsername.getText().toString().trim();
        String email = newAppEmail.getText().toString().trim();
        String password = newAppPassword.getText().toString().trim();
        String imageUriString = appImageUri != null ? appImageUri.toString() : null;

        if (appName.isEmpty() || appLink.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        //αν υπάρχει το όνομα της εφαρμογής
        if (dbHelper.isAppSelected(appName, userId)) {
            Toast.makeText(this, "Application name already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        //έλεγχος αν υπάρχει το link
        if (dbHelper.isLinkTaken(appLink, userId)) {
            Toast.makeText(this, "Application with the same link already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Επαλήθευση URL Format
        if (!Patterns.WEB_URL.matcher(appLink).matches()) {
            Toast.makeText(this, "Please enter a valid URL format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Επαλήθευση Εmail
        EmailVerificationTask verificationTask = new EmailVerificationTask(AddAppUserActivity.this, isEmailValid -> {
            if (!isEmailValid) {
                Toast.makeText(AddAppUserActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Επαλήθευση Αντικειμενικής Υπαρξης URL
            new UrlValidationTask(isValid -> {
                if (!isValid) {
                    Toast.makeText(this, "The URL is not reachable. Please enter a valid URL.", Toast.LENGTH_SHORT).show();
                } else {
                    // Αν περάσουν όλοι οι έλεγχοι, προχωράμε με την αποθήκευση της εφαρμογής
                    Toast.makeText(this, "Your app and your credentials are saved!", Toast.LENGTH_SHORT).show();
                    proceedWithAppSave(appName, appLink, username, email, password, imageUriString);
                }
            }).execute(appLink);
        });

        verificationTask.execute(email);
    }

    private void proceedWithAppSave(String appName, String appLink, String username, String email, String password, String imageUriString) {
        byte[] appImageBytes = getImageBytes();

        boolean result = dbHelper.saveSelectedAppToDatabase(
                new AppsObj(appName, appLink, 0, username, email, password, appImageBytes),
                userId
        ) && dbHelper.saveAppCredentials(userId, appName, username, email, password, appLink, imageUriString);

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

    private byte[] getImageBytes() {
        if (appImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            appImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } else if (appImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), appImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getDefaultImageBytes();
    }

    private byte[] getDefaultImageBytes() {
        Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_app_image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        defaultBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}