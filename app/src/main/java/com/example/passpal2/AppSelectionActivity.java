package com.example.passpal2;

import static com.example.passpal2.DataBaseHelper.TABLE_APPS_INFO;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.health.connect.datatypes.AppInfo;
import android.net.Uri;
import androidx.appcompat.widget.SearchView;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
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
import java.util.Locale;

public class AppSelectionActivity extends AppCompatActivity implements RecyclerViewInterface {
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    AdapterRecycler adapter;
    Button selectionApps, CancelBtn;
    int userId;
    ArrayList<AppsObj> appsObjs = new ArrayList<>();
    private List<AppsObj> appsObjsList;
    int[] appImages = {R.drawable.app_icon1, R.drawable.app_icon2, R.drawable.app_icon3, R.drawable.app_icon4,
            R.drawable.app_icon5, R.drawable.app_icon6, R.drawable.app_icon7, R.drawable.app_icon8, R.drawable.app_icon9,
            R.drawable.app_icon10, R.drawable.app_icon11, R.drawable.app_icon12, R.drawable.app_icon13, R.drawable.app_icon14,
            R.drawable.app_icon15, R.drawable.app_icon16, R.drawable.app_icon17, R.drawable.app_icon18, R.drawable.app_icon19, R.drawable.app_icon20,
            R.drawable.app_icon21, R.drawable.app_icon22};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Apps");
        }
        userId = getIntent().getIntExtra("USER_ID", -1);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        setUpAppData();

        if (appsObjs == null) {
            Log.e("AppSelectionActivity", "appsObjs is null!");
            appsObjs = new ArrayList<>();
        }
        adapter = new AdapterRecycler(this, appsObjs, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appsObjsList = new ArrayList<>();

        CancelBtn = findViewById(R.id.cancelBtn);
        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonClick(v);
            }
        });
        selectionApps = findViewById(R.id.selectionApp);
        selectionApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectBtnClick(v);
            }
        });
    }

    //action bar items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back arrow
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                Log.e("AppSelectionActivity", "kaleitai h filterlist");
                return true;
            }
        });

        return true;
    }


    // Μέθοδος για φιλτράρισμα των δεδομένων
    private void filterList(String text) {
        Log.e("AppSelectionActivity", "text : " + text);
        Log.e("AppSelectionActivity", "Μεσα στην filterlist");

        List<AppsObj> dbApps = dbHelper.getAllSelectedApps(userId);
        // Δημιουργία μιας νέας λίστας για τα φιλτραρισμένα αποτελέσματα
        List<AppsObj> filteredList = new ArrayList<>();

        for (AppsObj appsObj : dbApps) {
            Log.e("AppSelectionActivity", "appName: " + appsObj.getAppNames());
            Log.e("AppSelectionActivity", "Ποια ειναι η appsObjsList" + appsObjsList);

            if (appsObj.getAppNames().toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                filteredList.add(appsObj); // Προσθήκη στη νέα λίστα
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No apps found", Toast.LENGTH_SHORT).show();
        }

        // Ενημέρωση της λίστας appsObjs με τα φιλτραρισμένα αποτελέσματα
        appsObjs.clear();
        appsObjs.addAll(filteredList);
        adapter.notifyDataSetChanged();
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

        // Ενημέρωση του adapter με τα νέα δεδομένα
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            Log.e("AppSelectionActivity", "Chose an app");
        } else {
            Log.e("AppSelectionActivity", "AdapterRecycler is null!");
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

           adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(int position) {
        if (adapter != null) {
            // Έλεγχος αν η λίστα και η θέση είναι έγκυρες
            if (appsObjs != null && position < appsObjs.size()) {
                AppsObj selectedApp = appsObjs.get(position);

                // Εναλλαγή της επιλογής στο UI (χωρίς αποθήκευση στη βάση δεδομένων)
                adapter.toggleItemSelection(position);

                int selectedAppsCount = adapter.getSelectedAppsCount();

                if (selectedAppsCount > 10) {
                    Toast.makeText(AppSelectionActivity.this, "You can only choose 10 apps", Toast.LENGTH_SHORT).show();
                    adapter.toggleItemSelection(position); // Αναίρεση επιλογής
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

            // Αν δεν έχει εικόνα ορίζεται default εικόνα
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
            ArrayList<AppsObj> selectedApps = data.getParcelableArrayListExtra("SELECTED_APPS");
            recreate();
        }
    }


    // Λειτουργία κουμπιών

    public void SelectBtnClick(View view) {
        ArrayList<AppsObj> selectedApps = adapter.getSelectedApps();

        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "No apps selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedApps.size() <= 10) {
            int userId = getIntent().getIntExtra("USER_ID", -1);

            if (userId == -1) {
                Toast.makeText(this, "There was an error. Please try again", Toast.LENGTH_SHORT).show();
                return;
            }

            // Αποθήκευση στη βάση δεδομένων μόνο εδώ!
            for (AppsObj app : selectedApps) {
                dbHelper.saveSelectedAppToDatabase(app, userId);
            }

            // Μεταφορά στην MainActivity
            Intent resultIntent = new Intent(AppSelectionActivity.this, MainActivity.class);
            resultIntent.putExtra("USER_ID", userId);
            startActivity(resultIntent);
            finish();
        } else {
            Toast.makeText(this, "You can only choose up to 10 apps", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        ArrayList<AppsObj> selectedApps = adapter.getSelectedApps();

        if (selectedApps == null || selectedApps.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("You haven't picked any apps. Are you sure you want to go?")
                    .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();

            dialog.show();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to continue? Your chosen apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();

            dialog.show();
        }
    }


    public void onCancelButtonClick(View view) {
        finish();
    }

}
