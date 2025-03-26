package com.example.passpal2;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.security.SecureRandom;

public class ChangeMasterPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmNewPasswordEditText,emailConfirmAccount;
    private Button submitButton, cancelButton, generatePasswordButton;
    private DataBaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_master_password);

        dbHelper = new DataBaseHelper(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.dark_blue)));
        getSupportActionBar().setTitle("Change master password!");

        emailConfirmAccount = findViewById(R.id.emailInputForVerif);
        newPasswordEditText = findViewById(R.id.newMasterPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewMasterPassword);
        submitButton = findViewById(R.id.submitNewMasterPassword);
        cancelButton = findViewById(R.id.cancelChangeMasterPassword);
        generatePasswordButton = findViewById(R.id.generatePasswordButton);


        // Retrieve user ID from Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            showToast("Invalid user ID");
            finish();
            return;
        }

        setupButtons();
    }

    private void setupButtons() {
        submitButton.setOnClickListener(v -> changeMasterPassword());
        cancelButton.setOnClickListener(v -> finish());
        generatePasswordButton.setOnClickListener(v -> generateNewPassword());
    }

    private void changeMasterPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();
        String email = emailConfirmAccount.getText().toString().trim();

        // Έλεγχος αν όλα τα πεδία είναι συμπληρωμένα
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword) || TextUtils.isEmpty(email)) {
            showToast("All fields are required");
            return;
        }

        // Ανάκτηση του email του συνδεδεμένου χρήστη
        String loggedInUserEmail = dbHelper.getUserEmailByUserId(userId);

        // Έλεγχος αν το email που εισήγαγε ο χρήστης ταιριάζει με το email του συνδεδεμένου χρήστη
        if (!email.equals(loggedInUserEmail)) {
            showToast("The email does not match the logged-in user's email");
            return;
        }

        // Έλεγχος αν τα νέα passwords ταιριάζουν
        if (!newPassword.equals(confirmNewPassword)) {
            showToast("Passwords do not match");
            return;
        }

        // Έλεγχος αν το νέο password έχει ακριβώς 4 χαρακτήρες
        if (newPassword.length() < 4) {
            showToast("Password must be at least 4 characters long");
            return;
        }

        // Ενημέρωση του master password στη βάση δεδομένων
        boolean success = dbHelper.updateMasterPassword(userId, newPassword);
        if (success) {
            showToast("Master Password updated successfully");
            finish();
        } else {
            showToast("Failed to update Master Password. Try again.");
        }
    }

    private void generateNewPassword() {
        // Generate a 4-character random password
        String generatedPassword = generateRandomPassword();
        newPasswordEditText.setText(generatedPassword);
        confirmNewPasswordEditText.setText(generatedPassword);
    }
    private String generateRandomPassword() {
        String allowedChars = "0123456789"; // Μόνο αριθμοί
        SecureRandom random = new SecureRandom();
        StringBuilder passwordBuilder = new StringBuilder();

        // Δημιουργία τυχαίου κωδικού με τουλάχιστον 4 χαρακτήρες
        int passwordLength = 4 + random.nextInt(5); // Από 4 έως 8 χαρακτήρες

        while (true) {
            passwordBuilder.setLength(0); // Εκκαθάριση του StringBuilder

            // Δημιουργία τυχαίου 4ψήφιου κωδικού
            for (int i = 0; i < passwordLength; i++) {
                int randomIndex = random.nextInt(allowedChars.length());
                passwordBuilder.append(allowedChars.charAt(randomIndex));
            }

            String password = passwordBuilder.toString();

            // Έλεγχος ότι ο κωδικός δεν είναι "1234" ή 4 συνεχόμενα ψηφία
            if (!isConsecutiveSequence(password) && !password.equals("1234")) {
                return password;
            }
        }
    }

    // Μέθοδος για έλεγχο αν ο κωδικός είναι 4 συνεχόμενα ψηφία (αύξοντα ή φθίνοντα)
    private boolean isConsecutiveSequence(String password) {
        boolean isIncreasing = true;
        boolean isDecreasing = true;

        for (int i = 0; i < password.length() - 1; i++) {
            int currentDigit = Character.getNumericValue(password.charAt(i));
            int nextDigit = Character.getNumericValue(password.charAt(i + 1));

            // Έλεγχος για αύξουσα σειρά
            if (nextDigit != currentDigit + 1) {
                isIncreasing = false;
            }

            // Έλεγχος για φθίνουσα σειρά
            if (nextDigit != currentDigit - 1) {
                isDecreasing = false;
            }

            // Αν δεν είναι ούτε αύξουσα ούτε φθίνουσα, σταμάτα τον έλεγχο
            if (!isIncreasing && !isDecreasing) {
                break;
            }
        }

        // Αν είναι είτε αύξουσα είτε φθίνουσα σειρά, επιστροφή true
        return isIncreasing || isDecreasing;
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
