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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
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
import java.util.List;

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
            showToast("User ID is invalid");
            finish();
            return;
        }


        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Ορισμός του τίτλου
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.AboutBtn) {
            // About Button
            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Security and organization at your fingertips – this is the vision of PassPal, the ultimate application for managing access credentials and applications. " +
                            "With version 1.0, PassPal offers a perfect combination of simplicity and innovation, allowing you to register, securely store, and manage " +
                            "your access information for your favorite applications and websites.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        } else if (id == R.id.ShareBtn) {
            // Share Button
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Replace with your Google Drive link
            String downloadLink = "https://drive.google.com/file/d/<your-file-id>/view?usp=sharing";
            String shareMessage = "Check out PassPal, the ultimate password manager! Download it here: " + downloadLink;

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            return true;
        } else if (id == R.id.HelpSuppBtn ){
            // Help & Support
            new AlertDialog.Builder(this)
                    .setTitle("Help & Support")
                    .setMessage(getAboutMessage())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        } else if (id == R.id.LogOutBtn) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private SpannableStringBuilder getAboutMessage() {
        SpannableStringBuilder message = new SpannableStringBuilder();
        message.append("Dear user,\n\n");
        message.append("Thank you for choosing the PassPal app! This application is designed to make managing your passwords simple and secure. Below are the main features and how to use them:\n\n");
        message.append("1. You have already taken the first step by creating an account in our app and setting a master password, which will keep your data secure from potential attacks.\n\n");
        message.append("2. Adding Applications: On the home screen, you can add your favorite applications using the buttons located at the bottom of the screen. Simply use the '");

        SpannableString iconSpan = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(this, R.drawable.baseline_apps_24);
        iconSpan.setSpan(imageSpan, 0, 1, 0);
        message.append(iconSpan);
        message.append("' button to get started.\n\n");

        message.append("3. You can also add new applications that are not on the list yourself, which you can securely manage and edit whenever needed.\n\n");
        message.append("4. Edit Profile: In the 'Edit Profile' section, you can change your username and email to keep your profile updated.\n\n");
        message.append("5. Change Master Password: In the 'Change Master Password' section, you can update your master password to enhance the security of your account.\n\n");
        message.append("6. Security: Your passwords are protected with state-of-the-art encryption techniques to ensure the security of your data.\n\n");
        message.append("7. Access Anywhere: With the PassPal app, you can access your passwords from any device at any time.\n\n");
        message.append("If you need further assistance or have any questions, feel free to contact us through the support section in the app.\n\n");
        message.append("Sincerely,\n");
        message.append("The PassPal Team");
        return message;
    }
}
