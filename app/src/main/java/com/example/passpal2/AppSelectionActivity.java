package com.example.passpal2;

import static com.example.passpal2.DataBaseHelper.TABLE_APPS_INFO;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity implements RecyclerViewInterface {
    private static final String SELECTED_APPS_KEY = "selected_apps";

    AdapterRecycler adapter;
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

        DataBaseHelper dbHelper = new DataBaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        setUpAppData();

        adapter = new AdapterRecycler(this, appsObjs, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addUserAppsButton = findViewById(R.id.AddUserApps);
        Button selectionApps = findViewById(R.id.selectionApp);

        addUserAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddUserAppsButtonClick(v);
            }
        });
        selectionApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectBtnClick(v);
            }
        });
    }

    private void setUpAppData() {
        String[] appNames = getResources().getStringArray(R.array.appNames);
        String[] appLinks = getResources().getStringArray(R.array.appLinks);

        for (int i = 0; i < appNames.length; i++) {
            appsObjs.add(new AppsObj(appNames[i], appLinks[i], appImages[i]));
        }
    }

    @Override
    public void onItemClick(int position) {
        adapter.toggleItemSelection(position);
        AppsObj selectedApp = appsObjs.get(position);
        DataBaseHelper dbHelper = new DataBaseHelper(this);

        int userId = dbHelper.getUserIdByUsername("exampleUsername");

        if (selectedApp.isSelected()) {
            selectedApps.add(selectedApp);
        } else {
            selectedApps.remove(selectedApp);
        }

        // Εδώ προσθέτουμε κώδικα για να αποθηκεύουμε την επιλεγμένη εφαρμογή στη λίστα selectedApps
        // Πρέπει να έχετε πρόσβαση στην κλάση που διαχειρίζεται τη βάση δεδομένων της εφαρμογής σας
        // Και να χρησιμοποιήσετε τις κατάλληλες μεθόδους για εισαγωγή δεδομένων
        Log.d("MyApp", "UserID " + userId);
        saveSelectedAppToDatabase(selectedApp, userId);
    }

    public void SelectBtnClick(View view) {
        int selectedAppsCount = adapter.getSelectedAppsCount();

        if (selectedAppsCount > 10) {
            //με χρονοκαθυστερηση και δεν αφηνει να προχωρησει στην main οποτε κατι κανουμε
            // Εμφάνιση μηνύματος ειδοποίησης αν έχουν επιλεγεί ήδη 10 εφαρμογές
            Toast.makeText(AppSelectionActivity.this, "Μπορείτε να επιλέξετε μόνο μέχρι 10 εφαρμογές", Toast.LENGTH_SHORT).show();
        } else if (selectedAppsCount == 0) {
            // Εμφανίζουμε μήνυμα προειδοποίησης
            Toast.makeText(this, "Εδω ειναι το λαθος", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "eimaste kala", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onBackPressed() {
        if (selectedApps.isEmpty()) {
            // Αν η λίστα είναι άδεια, εμφανίζουμε ένα διάλογο επιβεβαίωσης
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
            // Αν η λίστα δεν είναι άδεια, εμφανίζουμε ένα διάλογο επιβεβαίωσης
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to continue? Your choosen apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Αν ο χρήστης επιλέξει να συνεχίσει, καλούμε την super.onBackPressed()
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }





    private void saveSelectedAppToDatabase(AppsObj app, int userId) {
        // Ελέγχουμε αν το userId είναι έγκυρο (δηλαδή διαφορετικό του -1)
        if (userId != -1) {
            // Δημιουργούμε ένα αντικείμενο βοηθού βάσης δεδομένων
            DataBaseHelper dbHelper = new DataBaseHelper(this);

            // Εισάγουμε την επιλεγμένη εφαρμογή στη βάση δεδομένων με το συγκεκριμένο userId
            boolean isInserted = dbHelper.addSelectedAppWithUserId(app, userId);
            if (isInserted) {
                // Επιτυχής εισαγωγή
                Log.d("AppSelectionActivity", "App inserted successfully into database");
            } else {
                // Αποτυχία εισαγωγής
                Log.e("AppSelectionActivity", "Failed to insert app into database");
            }
        } else {
            // Εμφανίζουμε μήνυμα λάθους αν το userId δεν είναι έγκυρο
            Log.e("AppSelectionActivity", "Invalid user ID");
        }
    }

    // Λειτουργία κουμπιών
    public void onAddUserAppsButtonClick(View view) {
        if (selectedApps.isEmpty()) {
            // Αν η λίστα είναι άδεια, εμφανίζουμε μήνυμα προειδοποίησης
            Toast.makeText(this, "Παρακαλώ επιλέξτε τουλάχιστον μία εφαρμογή", Toast.LENGTH_SHORT).show();
        } else {
            // Αν η λίστα δεν είναι άδεια, εμφανίζουμε έναν διάλογο επιβεβαίωσης
            new AlertDialog.Builder(this)
                    .setTitle("Continue")
                    .setMessage("Are you sure you want to continue? Your selected apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Εάν ο χρήστης επιλέξει να συνεχίσει, πηγαίνουμε στο AddAppUserActivity
                        Intent intent = new Intent(this, AddAppUserActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }
}
