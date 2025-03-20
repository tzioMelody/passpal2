package com.example.passpal2;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {
    String username;
    private RecyclerView appsRecyclerView;
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    private MainAppsAdapter mainAppsAdapter;
    private List<AppsObj> selectedApps = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    RelativeLayout Main_layout;
    private List<AppsObj> apps = new ArrayList<>();
    private static final int EDIT_APP_REQUEST = 2;
    int userId;

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();

        // Λήψη του user ID από το intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
        username = dbHelper.getUsernameByUserId(userId);
        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            finish();
            return;
        }

        // Ορισμός του τίτλου
        username = dbHelper.getUsernameByUserId(userId);
        getSupportActionBar().setTitle("Welcome, " + username + "!");
        Main_layout = findViewById(R.id.Main_layout);

        fetchApps();

        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);

        // Αρχικοποίηση του MainAppsAdapter
        mainAppsAdapter = new MainAppsAdapter(this, selectedApps);

        // Set adapter to RecyclerView
        appsRecyclerView.setAdapter(mainAppsAdapter);

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set Home as default selected item
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Handle Navigation Item Clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    return true;

                case R.id.appsBtn:
                    // Go to AppSelectionActivity
                    Intent appSelectionIntent = new Intent(MainActivity.this, AppSelectionActivity.class);
                    appSelectionIntent.putExtra("USER_ID", userId); // Pass userId
                    startActivity(appSelectionIntent);
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.newApp:
                    // Go to AddAppUserActivity
                    Intent newAppIntent = new Intent(MainActivity.this, AddAppUserActivity.class);
                    newAppIntent.putExtra("USER_ID", userId); // Pass userId
                    startActivity(newAppIntent);
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.profile:
                    // Go to ProfileActivity
                    Log.d("MainActivity", "User ID: " + userId);
                    Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("USER_ID", userId); // Pass userId
                    startActivity(profileIntent);
                    overridePendingTransition(0, 0);
                    return true;


                case R.id.syncData:
                    syncDataToFirestore();
                    return true;
            }
            return false;
        });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    // Called when returning from AppSelectionActivity with selected apps
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_APP_REQUEST && resultCode == RESULT_OK) {
            // Ανανέωση των δεδομένων
            fetchApps();        }

        if (resultCode == RESULT_OK && data != null) {
            // Get the selected apps - Optional
            ArrayList<AppsObj> selectedApps = data.getParcelableArrayListExtra("SELECTED_APPS");

            // Recreate the activity to reload it (call onCreate again)
            recreate();
        }
    }


    // RecyclerViewInterface method implementation
    public void onItemClick(int position) {
        AppsObj selectedApp = selectedApps.get(position);
        // Να πηγαίνει στην αντίστοιχη ιστοσελίδα ή link
        Toast.makeText(MainActivity.this, "Clicked on app: " + selectedApp.getAppNames(), Toast.LENGTH_SHORT).show();
    }

    private void fetchApps() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<AppsObj> apps = dbHelper.getAllSelectedApps(userId); // Ασύγχρονη εργασία
            runOnUiThread(() -> {
                mainAppsAdapter.setSelectedApps(apps); // Ενημέρωσε τον adapter στο κύριο νήμα
                attachSwipeToDeleteAndEditHelper(); // Επαναφόρτωσε τη λειτουργικότητα swipe
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        fetchApps(); // Φόρτωσε τα δεδομένα
    }

       /* @Override
        protected void onPostExecute(List<AppsObj> apps) {
            super.onPostExecute(apps);
            mainAppsAdapter.setSelectedApps(apps);
            attachSwipeToDeleteAndEditHelper();

            Log.d("FetchAppsTask", "Ενημέρωση adapter με " + apps.size() + " εφαρμογές.");
            for (AppsObj app : apps) {
                Log.d("FetchApps", "App: " + app.getAppNames());
            }
        }*/


    // NEW SWIPE TZIO
    private void attachSwipeToDeleteAndEditHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Θέτουμε το όριο στο 30% του πλάτους της οθόνης για την επεξεργασία (δεξιά swipe)
                float limit = recyclerView.getWidth() * 0.3f;

                if (dX > 0 && Math.abs(dX) < limit) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else if (dX > 0) {
                    dX = limit;
                }

                if (dX < 0) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

                View itemView = viewHolder.itemView;
                Paint p = new Paint();

                // Υπολογισμός 15% μικρότερου background
                float backgroundPadding = itemView.getHeight() * 0.15f;
                float cornerRadius = 30f; // Ακτίνα καμπυλότητας

                if (dX > 0) {
                    // Επεξεργασία - Πράσινο background με κυρτές γωνίες
                    p.setColor(Color.parseColor("#388E3C"));
                    RectF background = new RectF(
                            itemView.getLeft(),
                            itemView.getTop() + backgroundPadding, // Κορυφή με περισσότερο padding
                            itemView.getLeft() + dX,
                            itemView.getBottom() - backgroundPadding // Κάτω με περισσότερο padding
                    );
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p);
                } else if (dX < 0) {
                    // Διαγραφή - Κόκκινο background με κυρτές γωνίες
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop() + backgroundPadding, // Κορυφή με περισσότερο padding
                            itemView.getRight(),
                            itemView.getBottom() - backgroundPadding // Κάτω με περισσότερο padding
                    );
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p);
                }

                Drawable icon;
                RectF iconDest;

                if (dX > 0) {
                    // Επεξεργασία - Εικονίδιο
                    icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit);
                    iconDest = new RectF(
                            itemView.getLeft() + 50,
                            itemView.getTop() + 40,
                            itemView.getLeft() + 150,
                            itemView.getBottom() - 40
                    );
                } else {
                    // Διαγραφή - Εικονίδιο
                    icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.deleteappitem);
                    iconDest = new RectF(
                            itemView.getRight() - 150,
                            itemView.getTop() + 40,
                            itemView.getRight() - 50,
                            itemView.getBottom() - 40
                    );
                }

                // Σχεδίαση του εικονιδίου
                icon.setBounds(
                        Math.round(iconDest.left),
                        Math.round(iconDest.top),
                        Math.round(iconDest.right),
                        Math.round(iconDest.bottom)
                );
                icon.draw(c);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    AppsObj app = mainAppsAdapter.getAppsList().get(position);
                    mainAppsAdapter.getAppsList().remove(position);
                    mainAppsAdapter.notifyItemRemoved(position);

                    Snackbar snackbar = Snackbar.make(appsRecyclerView, "App deleted", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", v -> {
                        mainAppsAdapter.getAppsList().add(position, app);
                        mainAppsAdapter.notifyItemInserted(position);
                        appsRecyclerView.scrollToPosition(position);
                    });

                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                dbHelper.deleteApp(app.getAppNames(), dbHelper.getUserIdByUsername(username));
                            }
                        }
                    });

                    snackbar.show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    float swipePercentage = Math.abs(viewHolder.itemView.getTranslationX()) / appsRecyclerView.getWidth();
                    Log.d("SwipeAction", "Swipe detected. Swipe percentage: " + swipePercentage);

                    if (swipePercentage >= 0.3f) {
                        AppsObj app = mainAppsAdapter.getAppsList().get(position);
                        String appName = app.getAppNames();
                        Log.d("SwipeAction", "Swipe detected for app: " + appName);

                        // Παίρνουμε τα credentials για τη συγκεκριμένη εφαρμογή
                        List<DataBaseHelper.AppCredentials> appCredentials = getCredentialsForApp(userId, appName);
                        Log.d("SwipeAction", "Found " + appCredentials.size() + " credentials for app: " + appName);

                        if (appCredentials.isEmpty()) {
                            // Αν δεν υπάρχει λογαριασμός ανοίγουμε κανονικά την Edit
                            Log.d("SwipeAction", "No credentials found. Opening EditSelectedAppActivity.");
                            Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                            intent.putExtra("APP_DATA", app);
                            intent.putExtra("APP_ID", app.getId());
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("POSITION", position);
                            startActivityForResult(intent, EDIT_APP_REQUEST);
                        } else if (appCredentials.size() == 1) {
                            // Αν υπάρχει 1 λογαριασμός, ρωτάμε τον χρήστη τι θέλει να κάνει
                            Log.d("SwipeAction", "1 credential found. Asking user if they want to edit or add new account.");
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Take your pick");
                            builder.setMessage("Do you want to create a new account for this app or edit the one that already exists?");

                            builder.setPositiveButton("Edit", (dialog, which) -> {
                                Log.d("SwipeAction", "User chose to edit existing account.");
                                for (DataBaseHelper.AppCredentials cred : appCredentials) {
                                Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                                    intent.putExtra("APP_DATA", app);
                                    intent.putExtra("CREDENTIAL_ID", cred.getId());
                                    intent.putExtra("APP_ID", app.getId());
                                    intent.putExtra("USER_ID", userId);
                                    intent.putExtra("POSITION", position);

                                    // Νέα πεδία που στέλνουμε στην επεξεργασία
                                    intent.putExtra("USERNAME", cred.getUsername());
                                    intent.putExtra("PASSWORD", cred.getPassword());
                                    intent.putExtra("EMAIL", cred.getEmail());

                                    startActivityForResult(intent, EDIT_APP_REQUEST);
                                }
                            });

                            builder.setNegativeButton("Add new account", (dialog, which) -> {
                                Log.d("SwipeAction", "User chose to add new account.");
                                Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                                intent.putExtra("APP_DATA", app);
                                intent.putExtra("APP_ID", app.getId());
                                intent.putExtra("USER_ID", userId);
                                intent.putExtra("POSITION", position);
                                startActivityForResult(intent, EDIT_APP_REQUEST);
                            });

                            builder.setNeutralButton("Cancel", (dialog, which) -> {
                                Log.d("SwipeAction", "User cancelled action.");
                                dialog.dismiss();
                            });
                            builder.show();
                        } else {
                            // Αν υπάρχουν 2 ή περισσότεροι λογαριασμοί εμφανίζουμε διάλογο με clickable usernames
                            Log.d("SwipeAction", "Multiple credentials found. Showing clickable usernames.");
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Do you want to edit one of the following accounts?");

                            LinearLayout layout = new LinearLayout(MainActivity.this);
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.setPadding(50, 20, 50, 20);

                            for (DataBaseHelper.AppCredentials cred : appCredentials) {
                                Log.d("SwipeAction", "Displaying username: " + cred.getUsername());
                                TextView textView = new TextView(MainActivity.this);
                                textView.setText(cred.getUsername());
                                textView.setTextColor(Color.BLUE);
                                textView.setPadding(10, 10, 10, 10);
                                textView.setTextSize(18);
                                textView.setGravity(Gravity.CENTER);
                                textView.setClickable(true);
                                textView.setOnClickListener(v -> {
                                    Log.d("SwipeAction", "User clicked on username: " + cred.getUsername());

                                    Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                                    intent.putExtra("APP_DATA", app);
                                    intent.putExtra("CREDENTIAL_ID", cred.getId());
                                    intent.putExtra("APP_ID", app.getId());
                                    intent.putExtra("USER_ID", userId);
                                    intent.putExtra("POSITION", position);

                                    // Νέα πεδία που στέλνουμε στην επεξεργασία
                                    intent.putExtra("USERNAME", cred.getUsername());
                                    intent.putExtra("PASSWORD", cred.getPassword());
                                    intent.putExtra("EMAIL", cred.getEmail());

                                    startActivityForResult(intent, EDIT_APP_REQUEST);
                                });
                                layout.addView(textView);
                                Log.d("SwipeAction", "TextView added for username: " + cred.getUsername());
                            }

                            // 🔹 Προσθήκη επιλογής "Προσθήκη Νέου"
                            Log.d("SwipeAction", "Adding 'Add New Account' option.");
                            TextView addNewAccount = new TextView(MainActivity.this);
                            addNewAccount.setText("Αdd new account to app");
                            addNewAccount.setTextColor(Color.BLACK);
                            addNewAccount.setPadding(10, 20, 10, 10);
                            addNewAccount.setTextSize(18);
                            addNewAccount.setGravity(Gravity.LEFT);
                            addNewAccount.setClickable(true);
                            addNewAccount.setOnClickListener(v -> {
                                Log.d("SwipeAction", "User chose to add a new account.");
                                Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                                intent.putExtra("APP_DATA", app);
                                intent.putExtra("APP_ID", app.getId());
                                intent.putExtra("USER_ID", userId);
                                intent.putExtra("POSITION", position);
                                startActivityForResult(intent, EDIT_APP_REQUEST);
                            });

                            layout.addView(addNewAccount);

                            builder.setView(layout);
                            builder.setNegativeButton("Cancel", (dialog, which) -> {
                                Log.d("SwipeAction", "User cancelled multiple accounts dialog.");
                                dialog.dismiss();
                            });
                            builder.show();
                        }
                    } else {
                        Log.d("SwipeAction", "Swipe percentage is too small. Item not swiped.");
                        mainAppsAdapter.notifyItemChanged(position);
                    }
                }
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(appsRecyclerView);
    }

    public List<DataBaseHelper.AppCredentials> getCredentialsForApp(int userId, String appName) {
        Log.d("Credentials", "Getting credentials for user: " + userId + " and app: " + appName);

        // Παίρνουμε όλα τα credentials για τον χρήστη
        List<DataBaseHelper.AppCredentials> allCredentials = dbHelper.getAllCredentialsForUser(userId);
        Log.d("Credentials", "Total credentials found for user " + userId + ": " + allCredentials.size());

        // Φιλτράρουμε τα credentials για την συγκεκριμένη εφαρμογή
        List<DataBaseHelper.AppCredentials> filteredCredentials = new ArrayList<>();
        for (DataBaseHelper.AppCredentials cred : allCredentials)
            if (cred.getAppName().equalsIgnoreCase(appName)) {
                filteredCredentials.add(cred);
                Log.d("Credentials", "Credential found for app: " + appName + " with username: " + cred.getUsername());
            }

        Log.d("Credentials", "Filtered credentials size for app " + appName + ": " + filteredCredentials.size());
        return filteredCredentials;
    }

    private void syncDataToFirestore() {
        String userEmail = dbHelper.getUserEmailByUserId(userId);
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found. Cannot sync data.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<AppsObj> appsToSync = dbHelper.getAllSelectedApps(userId);
        if (appsToSync.isEmpty()) {
            Toast.makeText(this, "No apps to sync.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .document(userEmail)
                .collection("apps")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Batch write to ensure efficient updates
                    firestore.runBatch(batch -> {
                        for (AppsObj app : appsToSync) {
                            batch.set(
                                    firestore.collection("users")
                                            .document(userEmail)
                                            .collection("apps")
                                            .document(app.getAppNames()), // Using app name as document ID
                                    app.toMap() // Convert app to a Map for Firestore
                            );
                        }
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Data synced successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error syncing data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to connect to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
