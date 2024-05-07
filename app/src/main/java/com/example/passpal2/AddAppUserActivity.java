package com.example.passpal2;

import static com.example.passpal2.DataBaseHelper.COLUMN_APP_IMAGE_URI;
import static com.example.passpal2.DataBaseHelper.TABLE_APP_CREDENTIALS;
import static com.example.passpal2.DataBaseHelper.getUserId;

import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class AddAppUserActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1; 
    private static final int PICK_IMAGE = 2;
    private ImageView passGenButton;
    private FloatingActionButton saveNewApp;
    private EditText newAppPassword;

    // μεταβλητή για να παρακολουθούμε την ορατότητα του κωδικού
    private boolean isPasswordVisible = false;
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    int userId;
    String username;
    String email;
    String password;
    ImageButton addAppPhotoButton;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appadduser);

        // Ορισμός των στοιχείων UI από το layout
        passGenButton = findViewById(R.id.passGnrt);
        saveNewApp = findViewById(R.id.saveNewApp);
        newAppPassword = findViewById(R.id.newAppPassword);
        ImageButton showHideButton = findViewById(R.id.showHideBtn);
        MaterialButton generatePasswordButton = findViewById(R.id.generatePasswordButton);
        addAppPhotoButton = findViewById(R.id.addAppPhoto);

        userId = getIntent().getIntExtra("USER_ID", -1);

        generatePasswordButton.setOnClickListener(v -> {
            new GeneratePasswordTask(this, password -> newAppPassword.setText(password)).execute();
        });

        addAppPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

        saveNewApp.setOnClickListener(v -> saveNewApp());

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Log.d("MyApp", "on activity  result");

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("MyApp", "image capture " );

                Bundle extras = data.getExtras();
                if (extras != null) {
                    Log.d("NewApp", "image SAVED " );

                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    addAppPhotoButton.setImageBitmap(imageBitmap);
                    // Αποθηκεύει την εικόνα και επιστρέφει τη διαδρομή
                    String imagePath = saveImageToFile(imageBitmap);
                    saveImageUriInDatabase(imagePath, "App Name");
                }
            } else if (requestCode == PICK_IMAGE) {
                Log.d("MyApp", "image pick " );

                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    addAppPhotoButton.setImageURI(selectedImageUri);
                    saveImageUriInDatabase(selectedImageUri.toString(), "App Name");  // Αντικαταστήστε με την πραγματική όνομα της εφαρμογής αν χρειάζεται
                }
            }
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }


    private String saveImageToFile(Bitmap bitmap) {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, "UniqueFileName" + ".jpg");  // Δημιουργία ενός μοναδικού ονόματος αρχείου

        try {
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        } catch (IOException e) // Εξαίρεση εάν δεν μπορεί να γράψει
        {
            e.printStackTrace();
        }

        Uri savedImageURI = Uri.parse(file.getAbsolutePath());
        return savedImageURI.toString();
    }


    private void saveImageUriInDatabase(String imageUri, String appName) {
        // Κώδικας για την αποθήκευση του URI ή διαδρομής στη βάση δεδομένων
        if (imageUri != null && appName != null && !appName.isEmpty()) {
            boolean isUpdated = updateAppImageUri(userId, appName, imageUri);
            if (isUpdated) {
                Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save the image.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "App name or image URI is missing.", Toast.LENGTH_SHORT).show();
        }
    }

   /* private void saveNewApp() {
        EditText newAppUsername = findViewById(R.id.newAppUsername);
        EditText newAppEmail = findViewById(R.id.newAppEmail);
        EditText newAppname = findViewById(R.id.newAppname);
        EditText newAppLink = findViewById(R.id.newAppLink);
        EditText newAppPassword = findViewById(R.id.newAppPassword);

        String appName = newAppname.getText().toString();
        String appLink = newAppLink.getText().toString();
        username = newAppUsername.getText().toString();
        email = newAppEmail.getText().toString();
        password = newAppPassword.getText().toString();

        if (dbHelper.appExists(appName, appLink)) {
            Toast.makeText(this, "This app name or link already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Έλεγχος των πεδίων
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(appLink) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(password)) {
            Toast.makeText(AddAppUserActivity.this, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show();
            return;
        }
        String imageUriString = imageUri != null ? imageUri.toString() : null;
        boolean success = dbHelper.addNewAppWithDetails(userId, appName, appLink, username, email, password, imageUriString);

        if (success) {
            Toast.makeText(AddAppUserActivity.this, "App added successfully", Toast.LENGTH_SHORT).show();

            AppsObj.UserApp newUserApp = new AppsObj.UserApp(appName, appLink);

            Intent intent = new Intent(AddAppUserActivity.this, AppSelectionActivity.class);
            startActivity(intent);
            finish();
        }


        // Ελέγχουμε την εγκυρότητα του email
        new EmailVerificationTask(isEmailValid -> {
            if (isEmailValid) {
                // Ελέγχουμε την εγκυρότητα του link
                new CheckAppLinkValidityTask().execute(appLink, appName);
            } else {
                Toast.makeText(AddAppUserActivity.this, "Email is not valid.", Toast.LENGTH_SHORT).show();
            }
        }).execute(email);
    }
*/
   private void saveNewApp() {
       String appName = ((EditText)findViewById(R.id.newAppname)).getText().toString();
       String appLink = ((EditText)findViewById(R.id.newAppLink)).getText().toString();
       username = ((EditText)findViewById(R.id.newAppUsername)).getText().toString();
       email = ((EditText)findViewById(R.id.newAppEmail)).getText().toString();
       password = ((EditText)findViewById(R.id.newAppPassword)).getText().toString();
       String imageUriString = imageUri != null ? imageUri.toString() : null;

       if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(appLink) || TextUtils.isEmpty(username) ||
               TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(password)) {
           Toast.makeText(this, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show();
           return;
       }

       if (dbHelper.appExists(appName, appLink)) {
           Toast.makeText(this, "This app name or link already exists.", Toast.LENGTH_SHORT).show();
           return;
       }

       boolean success = dbHelper.addNewAppWithDetails(userId, appName, appLink, username, email, password, imageUriString);

       if (success) {
           Toast.makeText(this, "App added successfully", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(this, AppSelectionActivity.class);
           intent.putExtra("newAppName", appName);
           intent.putExtra("newAppLink", appLink);
           intent.putExtra("newImageUri", imageUriString);
           startActivity(intent);
           finish();
       } else {
           Toast.makeText(this, "Failed to add the app", Toast.LENGTH_SHORT).show();
       }
   }

    private void openImageSelector() {
        final CharSequence[] options = { "Τραβήξτε φωτογραφία", "Επιλέξτε από την συλλογή", "Ακύρωση" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddAppUserActivity.this);
        builder.setTitle("Προσθέστε την εικόνα σας");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Τραβήξτε φωτογραφία")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                } else if (options[item].equals("Επιλέξτε από την συλλογή")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                } else if (options[item].equals("Ακύρωση")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    public boolean updateAppImageUri(int userId, String appName, String imageUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APP_IMAGE_URI, imageUri);

        // Ενημέρωση της εικόνας με βάση το userId και το όνομα της εφαρμογής
        int rowsAffected = db.update(TABLE_APP_CREDENTIALS, cv, "user_id = ? AND app_name = ?", new String[]{String.valueOf(userId), appName});
        db.close();
        return rowsAffected > 0;
    }


    private class CheckAppLinkValidityTask extends AsyncTask<String, Void, Integer> {
        private String appName;
        private String appLink;

        @Override
        protected Integer doInBackground(String... params) {
            appLink = params[0];
            appName = params[1];
            try {
                URL url = new URL(appLink);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                return connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Αποθήκευση της εφαρμογής στη βάση
                AppsObj newApp = new AppsObj(appName, appLink, R.drawable.default_app_icon);
                newApp.setUsername(username);
                newApp.setEmail(email);
                newApp.setPassword(password);

                String imageUriString = imageUri != null ? imageUri.toString() : null;
                boolean success = dbHelper.addNewAppWithDetails(userId, appName, appLink, username, email, password, imageUriString);

                if (success) {
                    Toast.makeText(AddAppUserActivity.this, "App added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAppUserActivity.this, "Failed to add the app", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddAppUserActivity.this, "The app link is not valid.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void saveNewUserAppToDatabase(AppsObj.UserApp newUserApp) {
        AppsObj.USER_APPS.add(newUserApp);

        Intent intent = new Intent(AddAppUserActivity.this, AppSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Αποθήκευση Αλλαγών")
                .setMessage("Είστε σίγουροι ότι θέλετε να φύγετε; Όλες οι αλλαγές που δεν έχουν αποθηκευτεί θα χαθούν.")
                .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddAppUserActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}






