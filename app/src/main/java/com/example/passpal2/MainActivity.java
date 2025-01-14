package com.example.passpal2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {
    String username;
    private RecyclerView appsRecyclerView;
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    private MainAppsAdapter mainAppsAdapter;
    private List<AppsObj> selectedApps = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private Context context;
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

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }


        username = dbHelper.getUsernameByUserId(userId);
        getSupportActionBar().setTitle("Welcome, " + username + "!");

        Main_layout = findViewById(R.id.Main_layout);

        // AsyncTask για την ανάκτηση και εμφάνιση των εφαρμογών
        new FetchAppsTask().execute(userId);

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
        bottomNavigationView.setSelectedItemId(R.id.bottomNavigationView);

        // Handle Navigation Item Clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    // Stay in MainActivity
                    Intent homeIntent = new Intent(MainActivity.this, MainActivity.class);
                    homeIntent.putExtra("user_id", userId); // Pass userId
                    startActivity(homeIntent);
                    overridePendingTransition(0, 0);
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

    private void showMasterPasswordActivity(int userId) {
        Intent intent = new Intent(this, SetMasterPasswordActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }


    // Called when returning from AppSelectionActivity with selected apps
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_APP_REQUEST && resultCode == RESULT_OK) {
            // Ανανέωση των δεδομένων
            new FetchAppsTask().execute(userId);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    private class FetchAppsTask extends AsyncTask<Integer, Void, List<AppsObj>> {
        @Override
        protected List<AppsObj> doInBackground(Integer... userIds) {
            List<AppsObj> apps = dbHelper.getAllSelectedApps(userIds[0]);
            Log.d("FetchAppsTask", "Επιστρεφόμενες εφαρμογές: " + apps.size());
            Log.d("FetchAppsTask", "Ποιες είναι οι εφαρμογές : " + apps);

            return apps;
        }

        @Override
        protected void onPostExecute(List<AppsObj> apps) {
            super.onPostExecute(apps);
            mainAppsAdapter.setSelectedApps(apps);
            attachSwipeToDeleteAndEditHelper();

            Log.d("FetchAppsTask", "Ενημέρωση adapter με " + apps.size() + " εφαρμογές.");
            for (AppsObj app : apps) {
                Log.d("FetchApps", "App: " + app.getAppNames());
            }
        }
    }

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

                    if (swipePercentage >= 0.3f) {
                        AppsObj app = mainAppsAdapter.getAppsList().get(position);

                        Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                        intent.putExtra("APP_DATA", app);
                        intent.putExtra("APP_ID", app.getId());
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("POSITION", position);
                        startActivityForResult(intent, EDIT_APP_REQUEST);
                    } else {
                        mainAppsAdapter.notifyItemChanged(position);
                    }
                }
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(appsRecyclerView);
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