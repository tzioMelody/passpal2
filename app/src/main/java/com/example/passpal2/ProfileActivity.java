package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ProfileActivity extends AppCompatActivity {

    private Button aboutButton, deleteAccountButton, logOutButton;
    private TextView helpSupportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Συνδέουμε τα στοιχεία από το XML
        aboutButton = findViewById(R.id.about_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        helpSupportButton = findViewById(R.id.help_support_button);
        logOutButton = findViewById(R.id.log_out_button);

        // Ρύθμιση λειτουργικότητας κουμπιών
        setupButtons();
    }

    private void setupButtons() {
        // About the App
        aboutButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Security and organization at your fingertips – this is the vision of PassPal, the ultimate application for managing access credentials and applications. " +
                            "With version 1.0, PassPal offers a perfect combination of simplicity and innovation, allowing you to register, securely store, and manage " +
                            "your access information for your favorite applications and websites. Thanks to state-of-the-art encryption technology, your data is well-protected, " +
                            "while the integrated connection with the Hunter API ensures that the email addresses you register are always valid. " +
                            "Download PassPal today and enhance the management of your digital profiles!")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Help & Support
        helpSupportButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Help & Support")
                    .setMessage(getAboutMessage())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Delete Account
        deleteAccountButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        // Log Out
        logOutButton.setOnClickListener(view -> performLogout());
    }

    private SpannableStringBuilder getAboutMessage() {
        SpannableStringBuilder message = new SpannableStringBuilder();
        message.append("Dear user,\n\n");
        message.append("Thank you for choosing the PassPal app! This application is designed to make managing your passwords simple and secure. Below are the main features and how to use them:\n\n");
        message.append("1. You have already taken the first step by creating an account in our app and setting a master password, which will keep your data secure from potential attacks.\n\n");
        message.append("2. Adding Applications: On the home screen, you can add your favorite applications using the buttons located at the bottom of the screen. Simply use the '");

        // Προσθήκη εικονιδίου στο μήνυμα
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

    private void deleteUserAccount() {
        // Προσθήκη λογικής διαγραφής λογαριασμού
        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

        // Επιστροφή στο LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void performLogout() {
        // Επιστροφή στο LoginActivity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
