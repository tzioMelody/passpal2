package com.example.passpal2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Credentials;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private int userId;
    private TableLayout passwordsTableLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_table);

        // να φανει το ονομα της εφαρμογης πανω στην μπαρα
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Passwords vault");
        }


        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID από το intent
        userId = getIntent().getIntExtra("user_id", -1);

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Αρχικοποίηση του TableLayout
        passwordsTableLayout = findViewById(R.id.passwordsTableLayout);
        passwordsTableLayout.setStretchAllColumns(true);


        // Φόρτωση δεδομένων
        loadCredentials();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu); // Φόρτωση του μενού από το XML

        // Αναφορά στο SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        // Ρύθμιση του ακροατή (listener) για την αναζήτηση
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Εκτέλεση αναζήτησης όταν ο χρήστης πατάει "Submit"
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Καλούμε τη μέθοδο filterCredentials με το νέο κείμενο
                filterCredentials(newText);
                return true;
            }
        });

        return true;
    }

    // Φόρτωση credentials από τη βάση δεδομένων και προσθήκη δυναμικών γραμμών στο TableLayout
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void loadCredentials() {
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);
        if (credentialsList != null && !credentialsList.isEmpty()) {
            for (DataBaseHelper.AppCredentials credential : credentialsList) {
                // Δημιουργία νέου TableRow
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));
                Log.d("SwipeAction", "credentials List : " + credentialsList);

                // Δημιουργία και ρύθμιση TextView για το όνομα της εφαρμογής
                TextView appNameTextView = new TextView(this);
                appNameTextView.setText(credential.getAppName());
                row.addView(appNameTextView);

                // Δημιουργία και ρύθμιση TextView για το όνομα χρήστη
                TextView usernameTextView = new TextView(this);
                usernameTextView.setText(credential.getUsername());
                row.addView(usernameTextView);

                // Δημιουργία και ρύθμιση TextView για τον κωδικό
                TextView passwordTextView = new TextView(this);
                passwordTextView.setText("••••••••");  // Κρυμμένος κωδικός
                row.addView(passwordTextView);

                // Δημιουργία και ρύθμιση TextView για το κουμπί Copy
                TextView copyTextView = new TextView(this);
                copyTextView.setTextColor(getResources().getColor(R.color.blue));
                copyTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                copyTextView.setPadding(0, 5, 12, 5);
                copyTextView.setGravity(Gravity.CENTER);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    copyTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_copy, 0, 0, 0);
                } else {
                    copyTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy, 0, 10, 0);
                }
                copyTextView.setCompoundDrawablePadding(2);

                copyTextView.setOnClickListener(view -> {
                    String password = credential.getPassword();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Password", password);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
                });
                row.addView(copyTextView);

                // Δημιουργία και ρύθμιση TextView για το κουμπί Delete
                TextView deleteTextView = new TextView(this);
                deleteTextView.setTextColor(getResources().getColor(R.color.red));
                deleteTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                deleteTextView.setPadding(0, 5, 2, 5);
                deleteTextView.setGravity(Gravity.CENTER);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    deleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
                } else {
                    deleteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 10, 0);
                }
                deleteTextView.setCompoundDrawablePadding(0);

                final TableRow currentRow = row;
                deleteTextView.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PasswordsTableActivity.this);
                    builder.setTitle("Delete Item");
                    builder.setMessage("Are you sure you want to delete this item? Once deleted, " +
                                    "it will be permanently removed from the database and cannot be restored.");

                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        boolean isDeleted = dbHelper.deleteAppCredentials(credential.getId());

                        if (isDeleted) {
                            Toast.makeText(PasswordsTableActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            if (currentRow.getParent() != null) {
                                ((TableLayout) currentRow.getParent()).removeView(currentRow);
                            }
                        } else {
                            Toast.makeText(PasswordsTableActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                    builder.show();
                });

                row.addView(deleteTextView);

                // Δημιουργία και ρύθμιση TextView για το κουμπί Show/Hide
                TextView showHideTextView = new TextView(this);
                showHideTextView.setText("Show");
                showHideTextView.setTextColor(getResources().getColor(R.color.purple_500));

                // Προσθήκη λειτουργικότητας εμφάνισης/απόκρυψης
                showHideTextView.setOnClickListener(view -> {
                    if (showHideTextView.getText().equals("Show")) {
                        passwordTextView.setText(credential.getPassword()); // Εμφάνιση κωδικού
                        showHideTextView.setText("Hide");
                    } else {
                        passwordTextView.setText("••••••••");  // Απόκρυψη κωδικού
                        showHideTextView.setText("Show");
                    }
                });
                row.addView(showHideTextView);

                passwordsTableLayout.addView(row);
            }
        } else {
            showToast("No saved credentials found.");
        }
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


    private void filterCredentials(String query) {
        passwordsTableLayout.removeAllViews();

        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);

        for (DataBaseHelper.AppCredentials credential : credentialsList) {
            if (credential.getAppName().toLowerCase().contains(query.toLowerCase()) ||
                    credential.getUsername().toLowerCase().contains(query.toLowerCase())) {
                addCredentialRow(credential);
            }
        }
    }
        // Μέθοδος για την προσθήκη μιας γραμμής (TableRow) για ένα credential
        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        private void addCredentialRow(DataBaseHelper.AppCredentials credential) {
            TableRow row = createTableRow();
            addAppNameToRow(row, credential.getAppName());
            addUsernameToRow(row, credential.getUsername());
            addPasswordToRow(row, credential.getPassword());
            addCopyButtonToRow(row, credential);
            addDeleteButtonToRow(row, credential);
            addShowHideButtonToRow(row, credential);
            passwordsTableLayout.addView(row);
        }

    private TableRow createTableRow() {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return row;
    }

    private void addAppNameToRow(TableRow row, String appName) {
        TextView appNameTextView = new TextView(this);
        appNameTextView.setText(appName);
        row.addView(appNameTextView);
    }

    private void addUsernameToRow(TableRow row, String username) {
        TextView usernameTextView = new TextView(this);
        usernameTextView.setText(username);
        row.addView(usernameTextView);
    }

    private void addPasswordToRow(TableRow row, String password) {
        TextView passwordTextView = new TextView(this);
        passwordTextView.setText("••••••••");  // Κρυμμένος κωδικός
        row.addView(passwordTextView);
    }

    private void addCopyButtonToRow(TableRow row, DataBaseHelper.AppCredentials credential) {
        TextView copyTextView = new TextView(this);
        copyTextView.setTextColor(getResources().getColor(R.color.blue));
        copyTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        copyTextView.setPadding(0, 5, 12, 5);
        copyTextView.setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            copyTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_copy, 0, 0, 0);
        } else {
            copyTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy, 0, 10, 0);
        }
        copyTextView.setCompoundDrawablePadding(2);

        copyTextView.setOnClickListener(view -> {
            String password = credential.getPassword();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Password", password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        row.addView(copyTextView);
    }

    private void addDeleteButtonToRow(TableRow row, DataBaseHelper.AppCredentials credential) {
        TextView deleteTextView = new TextView(this);
        deleteTextView.setTextColor(getResources().getColor(R.color.red));
        deleteTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        deleteTextView.setPadding(0, 5, 2, 5);
        deleteTextView.setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            deleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        } else {
            deleteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 10, 0);
        }
        deleteTextView.setCompoundDrawablePadding(0);

        deleteTextView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PasswordsTableActivity.this);
            builder.setTitle("Delete Item");
            builder.setMessage("Are you sure you want to delete this item? Once deleted, " +
                    "it will be permanently removed from the database and cannot be restored.");

            builder.setPositiveButton("Yes", (dialog, which) -> {
                boolean isDeleted = dbHelper.deleteAppCredentials(credential.getId());

                if (isDeleted) {
                    Toast.makeText(PasswordsTableActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    passwordsTableLayout.removeView(row);
                } else {
                    Toast.makeText(PasswordsTableActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
        row.addView(deleteTextView);
    }

    private void addShowHideButtonToRow(TableRow row, DataBaseHelper.AppCredentials credential) {
        TextView showHideTextView = new TextView(this);
        showHideTextView.setText("Show");
        showHideTextView.setTextColor(getResources().getColor(R.color.purple_500));

        TextView passwordTextView = (TextView) row.getChildAt(2); // Το password TextView είναι το τρίτο στοιχείο της γραμμής

        showHideTextView.setOnClickListener(view -> {
            if (showHideTextView.getText().equals("Show")) {
                passwordTextView.setText(credential.getPassword()); // Εμφάνιση κωδικού
                showHideTextView.setText("Hide");
            } else {
                passwordTextView.setText("••••••••");  // Απόκρυψη κωδικού
                showHideTextView.setText("Show");
            }
        });
        row.addView(showHideTextView);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}