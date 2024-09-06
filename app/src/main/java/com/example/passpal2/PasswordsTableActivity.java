package com.example.passpal2;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class PasswordsTableActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords_table);
        getSupportActionBar().setTitle("Passwords Table");

        dbHelper = new DataBaseHelper(this);

        // Λήψη του user ID από το intent
        userId = getIntent().getIntExtra("user_id", -1);

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        // Ανάκτηση δεδομένων και εμφάνιση στον πίνακα
        displayPasswordsTable();
    }

    private void displayPasswordsTable() {
        TableLayout tableLayout = findViewById(R.id.passwordsTableLayout);
        List<DataBaseHelper.AppCredentials> credentialsList = dbHelper.getAllCredentialsForUser(userId);

        for (DataBaseHelper.AppCredentials credentials : credentialsList) {
            TableRow tableRow = new TableRow(this);

            // Δημιουργία και ρύθμιση των TextView για κάθε στήλη
            TextView appNameTextView = createTextView(credentials.getAppName(), R.color.barbie_pink);
            TextView usernameTextView = createTextView(credentials.getUsername(), R.color.smoothBlue);
            TextView passwordTextView = createTextView("••••••••", R.color.transWhite); // αρχικά κρυφό
            TextView showHideTextView = createTextView("Show", R.color.black);

            // Προσθήκη λειτουργικότητας εμφάνισης/απόκρυψης του κωδικού πρόσβασης
            showHideTextView.setOnClickListener(new View.OnClickListener() {
                boolean isPasswordVisible = false;

                @Override
                public void onClick(View v) {
                    if (isPasswordVisible) {
                        passwordTextView.setText("••••••••");
                        showHideTextView.setText("Show");
                    } else {
                        passwordTextView.setText(credentials.getPassword());
                        showHideTextView.setText("Hide");
                    }
                    isPasswordVisible = !isPasswordVisible;
                }
            });

            // Προσθήκη των TextView στο TableRow
            tableRow.addView(appNameTextView);
            tableRow.addView(usernameTextView);
            tableRow.addView(passwordTextView);
            tableRow.addView(showHideTextView);

            // Προσθήκη του TableRow στο TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private TextView createTextView(String text, int backgroundColorResourceId) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setBackgroundColor(getResources().getColor(backgroundColorResourceId));
        textView.setTextColor(getResources().getColor(R.color.white)); // Χρώμα κειμένου
        textView.setPadding(8, 8, 8, 8); // Padding για εμφάνιση
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Ομοιόμορφη κατανομή πλάτους
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER); // Κεντράρισμα κειμένου
        return textView;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
