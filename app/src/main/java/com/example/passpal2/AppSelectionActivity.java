package com.example.passpal2;

import static com.example.passpal2.DataBaseHelper.TABLE_APPS_INFO;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.health.connect.datatypes.AppInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity implements RecyclerViewInterface {
    private static final String SELECTED_APPS_KEY = "selected_apps";
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    AdapterRecycler adapter;
    Button selectionApps,CancelBtn;
    int userId;
    ArrayList<AppsObj> appsObjs = new ArrayList<>();
    ArrayList<AppsObj> selectedApps = new ArrayList<>();
    int[] appImages = {R.drawable.app_icon1, R.drawable.app_icon2, R.drawable.app_icon3, R.drawable.app_icon4,
            R.drawable.app_icon5, R.drawable.app_icon6, R.drawable.app_icon7, R.drawable.app_icon8, R.drawable.app_icon9,
            R.drawable.app_icon10, R.drawable.app_icon11, R.drawable.app_icon12, R.drawable.app_icon13, R.drawable.app_icon14,
            R.drawable.app_icon15, R.drawable.app_icon16, R.drawable.app_icon17, R.drawable.app_icon18, R.drawable.app_icon19, R.drawable.app_icon20,
            R.drawable.app_icon21, R.drawable.app_icon22};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        // Αντληση του userID από το Intent με τη χρήση του σωστού κλειδιού
        userId = getIntent().getIntExtra("USER_ID", -1);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        setUpAppData();

        adapter = new AdapterRecycler(this, appsObjs, this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Choose your favourite apps");
        }


        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

         CancelBtn = findViewById(R.id.cancelBtn);
         selectionApps = findViewById(R.id.selectionApp);

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonClick(v);
            }
        });
        selectionApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectBtnClick(v);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back arrow
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpAppData() {
        // Retrieve default app names and links from resources
        String[] appNames = getResources().getStringArray(R.array.appNames);
        String[] appLinks = getResources().getStringArray(R.array.appLinks);

        // Create a HashSet to track added app names to avoid duplicates
        HashSet<String> addedApps = new HashSet<>();

        // Add apps from the database
        List<AppsObj> dbApps = dbHelper.getAllSelectedApps(userId);
        for (AppsObj app : dbApps) {
            if (!addedApps.contains(app.getAppNames())) {
                appsObjs.add(app);
                addedApps.add(app.getAppNames());
            }
        }

        // Add default apps to the list
        for (int i = 0; i < appNames.length; i++) {
            if (!addedApps.contains(appNames[i])) {
                appsObjs.add(new AppsObj(appNames[i], appLinks[i], appImages[i]));
                addedApps.add(appNames[i]);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("AppName")
                && intent.hasExtra("AppImageUri")) {
            String appName = intent.getStringExtra("AppName");
            String appImageUriString = intent.getStringExtra("AppImageUri");
            Uri appImageUri = appImageUriString != null ? Uri.parse(appImageUriString) : null;

            // Δημιουργία του νέου αντικειμένου AppsObj
            int appImageResId = R.drawable.default_app_image;
            AppsObj newApp = new AppsObj(appName, "App Link Placeholder", appImageResId);
            newApp.setSelected(true);

            adapter.addApp(newApp);

            // Ενημέρωση του adapter
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(int position) {
        if (adapter != null) {
            // Έλεγχος αν η λίστα και η θέση είναι έγκυρες
            if (appsObjs != null && position < appsObjs.size()) {
                AppsObj selectedApp = appsObjs.get(position);
                int userId = getIntent().getIntExtra("USER_ID", -1);

                // Έλεγχος αν το userId είναι έγκυρο
                if (userId == -1) {
                    Log.e("AppSelectionActivity", "Άκυρο USER_ID");
                    Toast.makeText(AppSelectionActivity.this, "There was an error. Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Χρήση του ονόματος της εφαρμογής αντί για String.valueOf(selectedApp)
                if (!dbHelper.isAppSelected(selectedApp.getAppNames(), userId)) {
                    adapter.toggleItemSelection(position);
                    int selectedAppsCount = adapter.getSelectedAppsCount();

                    if (selectedAppsCount > 10) {
                        Toast.makeText(AppSelectionActivity.this, "You can only choose 10 apps", Toast.LENGTH_SHORT).show();
                    } else if (selectedAppsCount == 0) {
                        Log.d("MyApp", "ΛΑΘΟΣ ΕΔΩ αν δεν εχει επιλεξει καμια εφαρμογη " + userId);
                    } else {
                        Log.d("MyApp", "UserID " + userId);
                        boolean isSaved = dbHelper.saveSelectedAppToDatabase(selectedApp, userId);
                        if (isSaved) {
                            Log.d("MyApp", "Η εφαρμογή αποθηκεύτηκε με επιτυχία για τον χρήστη με ID " + userId);
                        } else {
                            Log.e("MyApp", "Σφάλμα κατά την αποθήκευση της εφαρμογής");
                        }
                    }
                } else {
                    Toast.makeText(AppSelectionActivity.this, "This app has already been selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("AppSelectionActivity", "Η θέση είναι εκτός ορίων ή η λίστα είναι άδεια");
            }
        }
    }

    //για να μπορέσει να προσθέσει στην λίστα την νέα εφαμρογή του χρήστη
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String appName = data.getStringExtra("AppName");
            String appLink = data.getStringExtra("AppLink");
            String imageUriString = data.getStringExtra("AppImageUri");
            int imageResId;

            // Αν δεν έχει εικόνα, ορίζουμε default εικόνα
            if (imageUriString == null || imageUriString.isEmpty()) {
                imageResId = R.drawable.default_app_image;
            } else {
                imageResId = Integer.parseInt(imageUriString);
            }

            AppsObj newApp = new AppsObj(appName, appLink, imageResId);
            newApp.setSelected(true);

            // Προσθήκη της νέας εφαρμογής στο adapter
            adapter.addApp(newApp);
            adapter.notifyDataSetChanged();
        }

        if (resultCode == RESULT_OK && data != null) {
            // Get the selected apps - Optional
            ArrayList<AppsObj> selectedApps = data.getParcelableArrayListExtra("SELECTED_APPS");

            // Recreate the activity to reload it (call onCreate again)
            recreate();
        }
    }


    public void SelectBtnClick(View view) {
        ArrayList<AppsObj> selectedApps = adapter.getSelectedApps();

        if (selectedApps.size() <= 10) {
            for (AppsObj app : selectedApps) {
                dbHelper.saveSelectedAppToDatabase(app, userId);
            }

            // Δημιουργία ενός Intent για να περάσει τα δεδομένα στην MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("SELECTED_APPS", selectedApps);
            resultIntent.putExtra("USER_ID", userId);
            setResult(RESULT_OK, resultIntent); // Επιστρέφει τα αποτελέσματα
            finish(); // Τερματισμός της Activity και επιστροφή στην MainActivity
        } else {
            Toast.makeText(this, "You can only choose up to 10 apps", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onBackPressed() {
        if (selectedApps.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("You haven't picked any apps are you sure you want to go?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Αν ο χρήστης επιλέξει να συνεχίσει, καλούμε την super.onBackPressed()
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to continue? Your choosen apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }


    // Λειτουργία κουμπιών
    public void onCancelButtonClick(View view) {
        if (selectedApps.isEmpty()) {
            Intent intentUserId = new Intent(this, MainActivity.class);
            intentUserId.putExtra("USER_ID", userId);
            startActivityForResult(intentUserId, 1);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Continue")
                    .setMessage("Are you sure you want to continue? Your selected apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        Intent intentUserId = new Intent(this, MainActivity.class);
                        intentUserId.putExtra("USER_ID", userId);
                        startActivityForResult(intentUserId, 1);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }
}
