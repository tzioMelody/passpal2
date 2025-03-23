package com.example.passpal2;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.ScrollView;
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
        mainAppsAdapter = new MainAppsAdapter(this, selectedApps,this);

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
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.about_button:
                showAboutDialog();
                return true;
            case R.id.helpSupportButton:
                showHelpDialog();
                return true;
            case R.id.shareButton:
                shareApp();
                return true;
            case R.id.log_out_button:
                performLogout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Security and organization at your fingertips – this is the vision of PassPal, the ultimate application for managing access credentials and applications. " +
                        " PassPal offers a perfect combination of simplicity and innovation, allowing you to register, securely store, and manage " +
                        "your access information for your favorite applications and websites.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help & Support");

        // Χρήση ScrollView για μεγάλα μηνύματα
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(getAboutMessage());
        Log.d("HelpDialog", "Message: " + getAboutMessage());
        textView.setPadding(20, 20, 20, 20);
        scrollView.addView(textView);

        builder.setView(scrollView);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String downloadLink = "https://drive.google.com/file/d/<your-file-id>/view?usp=sharing";
        String shareMessage = "Check out PassPal, the ultimate password manager! Download it here: " + downloadLink;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
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
    @Override
    public void onItemClick(int position) {
        if (mainAppsAdapter != null) {
            Log.d("onItemClick", "in on item click");

            AppsObj app = mainAppsAdapter.getAppsList().get(position);

            if (dbHelper.isAppSelected(app.getAppNames(), userId)) {
                Log.d("onItemClick", "in the if " + app);

                // Αναζήτηση εγκατεστημένων εφαρμογών για αντιστοιχία με το όνομα της εφαρμογής
                PackageManager packageManager = getPackageManager();
                Intent launchIntent = null;
                String appName = app.getAppNames();

                for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
                    String appLabel = packageManager.getApplicationLabel(appInfo).toString();
                    if (appLabel.equalsIgnoreCase(appName)) {
                        // Βρέθηκε αντιστοιχία, λήψη του intent εκκίνησης της εφαρμογής
                        launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
                        break;
                    }
                }

                if (launchIntent != null) {
                    // Εκκίνηση της εφαρμογής αν βρέθηκε
                    startActivity(launchIntent);
                } else {
                    // Αν δεν βρεθεί, fallback στο άνοιγμα του URL
                    String url = app.getAppLinks() != null ? app.getAppLinks().toString() : "";
                    if (!url.isEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }

                    if (!url.isEmpty()) {
                        // Άνοιγμα του URL με intent
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);

                        Toast.makeText(MainActivity.this, "Opening website", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "No app or URL found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

    }
    private void fetchApps() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<AppsObj> apps = dbHelper.getAllSelectedApps(userId);
            runOnUiThread(() -> {
                mainAppsAdapter.setSelectedApps(apps);
                attachSwipeToDeleteAndEditHelper();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        fetchApps();
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
                                fetchApps();
                                dialog.dismiss();
                            });
                            builder.show();
                        }else if (appCredentials.size() > 1) {
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

                                    intent.putExtra("USERNAME", cred.getUsername());
                                    intent.putExtra("PASSWORD", cred.getPassword());
                                    intent.putExtra("EMAIL", cred.getEmail());

                                    startActivityForResult(intent, EDIT_APP_REQUEST);
                                });
                                layout.addView(textView);
                                Log.d("SwipeAction", "TextView added for username: " + cred.getUsername());
                            }

                            builder.setView(layout);

                            builder.setNeutralButton("NEW", (dialog, which) -> {
                                Log.d("SwipeAction", "User chose to add a new account.");
                                Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                                intent.putExtra("APP_DATA", app);
                                intent.putExtra("APP_ID", app.getId());
                                intent.putExtra("USER_ID", userId);
                                intent.putExtra("POSITION", position);
                                startActivityForResult(intent, EDIT_APP_REQUEST);
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> {
                                Log.d("SwipeAction", "User cancelled multiple accounts dialog.");
                                fetchApps();
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

    private void performLogout() {
        // Καθαρίζουμε τα SharedPreferences
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Toast.makeText(this, "Logout successful!" , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private SpannableStringBuilder getAboutMessage() {
        SpannableStringBuilder message = new SpannableStringBuilder();
        message.append("Dear user,\n\n");
        message.append("Thank you for choosing PassPal! Our app is designed to make managing your passwords both simple and highly secure. Here’s a quick guide to help you get the most out of it:\n\n");

        // Step 1
        message.append("1. You have successfully created your account and set a master password to keep your data protected from unauthorized access.\n\n");

        // Step 2 - Add Apps
        message.append("2. Adding Applications: On the home screen, you can easily add your favorite apps using the apps button ");
        SpannableString iconSpan1 = new SpannableString(" ");
        ImageSpan imageSpan1 = new ImageSpan(this, R.drawable.baseline_apps_24);
        iconSpan1.setSpan(imageSpan1, 0, 1, 0);
        message.append(iconSpan1);
        message.append(". Simply tap it to get started.\n\n");

        // Step 3 - Add custom apps
        message.append("3. Adding Custom Applications: Want to add an app that's not listed? Tap the plus button in the middle, to create and manage your own custom apps securely.\n\n");

        // Step 4 - Sync Data
        message.append("4. Sync Data: Keep your data safe and up-to-date by using the sync button ");
        SpannableString iconSpan3 = new SpannableString(" ");
        ImageSpan imageSpan3 = new ImageSpan(this, R.drawable.baseline_sync_lock_24);
        iconSpan3.setSpan(imageSpan3, 0, 1, 0);
        message.append(iconSpan3);
        message.append(" which allows you to back up and restore your encrypted data across devices.\n\n");

        // Step 5 - Edit Profile
        message.append("5. Edit Profile: In the 'Edit Profile' section ");
        SpannableString iconSpan4 = new SpannableString(" ");
        ImageSpan imageSpan4 = new ImageSpan(this,  R.drawable.ic_person);
        iconSpan4.setSpan(imageSpan4, 0, 1, 0);
        message.append(iconSpan4);
        message.append(", you can update your username and email anytime.\n\n");

        // Step 6 - Change Master Password
        message.append("6. Change Master Password: Visit the 'Change Master Password' section to enhance your account’s security by updating your master password.\n\n");

        // Step 7 - Security
        message.append("7. Security: Your passwords are protected using advanced encryption methods to keep your data safe and private. You can see them in the password vault where you can either copy them for your use or you can delete them.\n\n");

        // Step 8 - Manage favorite apps
        message.append("8. Managing Your Favorite Apps: Once you select your favorite applications, they will appear on the home screen. From there, you can:\n");
        message.append("- Edit your credentials and add as many accounts as you want.\n");
        message.append("- Delete them whenever you want.\n");
        message.append("- Tap on them to quickly visit their official website.\n\n");

        message.append("9. \n");

        message.append("Sincerely,\nThe PassPal Team");

        return message;
    }

}
