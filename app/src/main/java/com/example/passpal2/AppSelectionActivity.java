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
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    AdapterRecycler adapter;
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
        if (adapter != null) {
            AppsObj selectedApp = appsObjs.get(position);
            // Υποθέτουμε ότι αυτή η γραμμή είναι ενεργοποιημένη για να λαμβάνετε το userID
            int userId = getIntent().getIntExtra("USER_ID", -1);

            // Ελέγχουμε αν η εφαρμογή έχει ήδη επιλεγεί
            if (!dbHelper.isAppSelected(String.valueOf(selectedApp), userId)) {
                adapter.toggleItemSelection(position);
                int selectedAppsCount = adapter.getSelectedAppsCount();

                if (selectedAppsCount > 10) {
                    // Εμφάνιση μηνύματος ειδοποίησης αν έχουν επιλεγεί ήδη 10 εφαρμογές
                    Toast.makeText(AppSelectionActivity.this, "Μπορείτε να επιλέξετε μόνο μέχρι 10 εφαρμογές", Toast.LENGTH_SHORT).show();
                } else if (selectedAppsCount == 0) {
                    // Εμφάνιση μηνύματος προειδοποίησης
                    Toast.makeText(this, "Εδω ειναι το λαθος", Toast.LENGTH_SHORT).show();
                } else {
                    // Αποθηκεύουμε την επιλεγμένη εφαρμογή στη βάση δεδομένων
                    Log.d("MyApp", "UserID " + userId);
                    dbHelper.saveSelectedAppToDatabase(selectedApp, userId);
                }
            } else {
                // Εμφάνιση μηνύματος ότι η εφαρμογή έχει ήδη επιλεγεί
                Toast.makeText(AppSelectionActivity.this, "Η εφαρμογή έχει ήδη επιλεγεί", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void SelectBtnClick(View view) {
        ArrayList<AppsObj> selectedApps = adapter.getSelectedApps();

        if (selectedApps.size() <= 10) {
            for (AppsObj app : selectedApps) {
                dbHelper.saveSelectedAppToDatabase(app, userId);
            }

            Toast.makeText(this, "Επιτυχής εισαγωγή επιλεγμένων εφαρμογών!", Toast.LENGTH_SHORT).show();
            // Τερματισμός της Activity και επιστροφή στην MainActivity
            finish();
        } else {
            Toast.makeText(this, "Μπορείτε να επιλέξετε μόνο μέχρι 10 εφαρμογές", Toast.LENGTH_SHORT).show();
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


    // Λειτουργία κουμπιών
    public void onAddUserAppsButtonClick(View view) {
        if (selectedApps.isEmpty()) {
            Intent intentUserId = new Intent(this, AddAppUserActivity.class);
            intentUserId.putExtra("USER_ID", userId);
            startActivityForResult(intentUserId, 1);
        } else {
            // Αν η λίστα δεν είναι άδεια, εμφανίζουμε έναν διάλογο επιβεβαίωσης
            new AlertDialog.Builder(this)
                    .setTitle("Continue")
                    .setMessage("Are you sure you want to continue? Your selected apps will be lost.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Εάν ο χρήστης συνεχίσει, πηγαίνουμε στο AddAppUserActivity
                        Intent intentUserId = new Intent(this, AddAppUserActivity.class);
                        intentUserId.putExtra("USER_ID", userId);
                        startActivityForResult(intentUserId, 1);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }
}
