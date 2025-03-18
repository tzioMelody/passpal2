package com.example.passpal2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {

    private DataBaseHelper dbHelper;
    private int userId;
    private TableLayout passwordsTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_table);


        // Εμφάνιση του τίτλου στην ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Passwords Vault");
        }

        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID από το Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Αρχικοποίηση του TableLayout
        passwordsTableLayout = findViewById(R.id.passwordsTableLayout);
        passwordsTableLayout.setStretchAllColumns(true);

        // Φόρτωση των credentials
        loadCredentials();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Ρύθμιση του SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCredentials(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void loadCredentials() {
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);
        if (credentialsList != null && !credentialsList.isEmpty()) {
            for (DataBaseHelper.AppCredentials credential : credentialsList) {
                addCredentialRow(credential);
            }
        } else {
            showToast("No saved credentials found.");
        }
    }

    private void filterCredentials(String query) {
        passwordsTableLayout.removeAllViews();
        addHeaderRow();
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);

        for (DataBaseHelper.AppCredentials credential : credentialsList) {
            if (credential.getAppName().toLowerCase().contains(query.toLowerCase()) ||
                    credential.getUsername().toLowerCase().contains(query.toLowerCase())) {
                addCredentialRow(credential);
            }
        }
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.light_purple));
        headerRow.setPadding(8, 8, 8, 8);

        String[] headers = {"App name", "Username", "Password", "Copy", "Delete", "Show/Hide"};
        int[] textColors = {
                R.color.black, R.color.black, R.color.black,
                R.color.blue, R.color.red, R.color.purple_500
        };

        for (int i = 0; i < headers.length; i++) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            textView.setGravity(Gravity.CENTER);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setText(headers[i]);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(ContextCompat.getColor(this, textColors[i]));
            headerRow.addView(textView);
        }

        passwordsTableLayout.addView(headerRow);
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void addCredentialRow(DataBaseHelper.AppCredentials credential) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Προσθήκη App Name
        TextView appNameTextView = new TextView(this);
        appNameTextView.setText(truncateAppName(credential.getAppName(), 12));
        appNameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        appNameTextView.setGravity(Gravity.CENTER);
        row.addView(appNameTextView);

        // Προσθήκη Username
        TextView usernameTextView = new TextView(this);
        usernameTextView.setText(credential.getUsername());
        usernameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        usernameTextView.setGravity(Gravity.CENTER);
        row.addView(usernameTextView);

        // Προσθήκη Password
        TextView passwordTextView = new TextView(this);
        passwordTextView.setText("••••••••");
        passwordTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        passwordTextView.setGravity(Gravity.CENTER);
        row.addView(passwordTextView);

        // Προσθήκη Copy Button
        // Προσθήκη Copy Button
        TextView copyTextView = new TextView(this);
        copyTextView.setTextColor(getResources().getColor(R.color.blue));
        copyTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        copyTextView.setGravity(Gravity.CENTER);

// Αλλαγή χρώματος του εικονιδίου copy και μετακίνηση προς τα δεξια
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            copyTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    getResources().getDrawable(R.drawable.ic_copy, getTheme()),
                    null
            );
        } else {
            copyTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    getResources().getDrawable(R.drawable.ic_copy),
                    null
            );
        }
        copyTextView.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue))); // Μπλε εικονίδιο
        copyTextView.setCompoundDrawablePadding(8); // Μετακίνηση εικονιδίου προς τα δεξια
        copyTextView.setPadding(0, 0, 16, 0); // Μετακίνηση κειμένου προς τα δεξια
        copyTextView.setOnClickListener(v -> copyToClipboard(credential.getPassword()));
        row.addView(copyTextView);

// Προσθήκη Delete Button
        TextView deleteTextView = new TextView(this);
        deleteTextView.setTextColor(getResources().getColor(R.color.red));
        deleteTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        deleteTextView.setGravity(Gravity.CENTER);

// Αλλαγή χρώματος του εικονιδίου delete και μετακίνηση προς τα δεξια
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            deleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    getResources().getDrawable(R.drawable.ic_delete, getTheme()),
                    null
            );
        } else {
            deleteTextView.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    getResources().getDrawable(R.drawable.ic_delete),
                    null
            );
        }
        deleteTextView.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.red))); // Κόκκινο εικονίδιο
        deleteTextView.setCompoundDrawablePadding(8); // Μετακίνηση εικονιδίου προς τα δεξια
        deleteTextView.setPadding(0, 0, 16, 0); // Μετακίνηση κειμένου προς τα δεξια
        deleteTextView.setOnClickListener(v -> deleteCredential(credential, row));
        row.addView(deleteTextView);

        // Προσθήκη Show/Hide Button
        TextView showHideTextView = new TextView(this);
        showHideTextView.setText("Show");
        showHideTextView.setTextColor(getResources().getColor(R.color.purple_500));
        showHideTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        showHideTextView.setGravity(Gravity.CENTER);
        showHideTextView.setOnClickListener(v -> togglePasswordVisibility(passwordTextView, showHideTextView, credential.getPassword()));
        row.addView(showHideTextView);

        passwordsTableLayout.addView(row);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", text);
        clipboard.setPrimaryClip(clip);
        showToast("Password copied to clipboard");
    }

    private void deleteCredential(DataBaseHelper.AppCredentials credential, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            boolean isDeleted = dbHelper.deleteAppCredentials(credential.getId());
            if (isDeleted) {
                passwordsTableLayout.removeView(row);
                showToast("Item deleted successfully");
            } else {
                showToast("Failed to delete item");
            }
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void togglePasswordVisibility(TextView passwordTextView, TextView showHideTextView, String password) {
        if (showHideTextView.getText().toString().equals("Show")) {
            passwordTextView.setText(password);
            showHideTextView.setText("Hide");
        } else {
            passwordTextView.setText("••••••••");
            showHideTextView.setText("Show");
        }
    }

    private String truncateAppName(String appName, int maxLength) {
        return appName.length() > maxLength ? appName.substring(0, maxLength - 3) + "..." : appName;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}