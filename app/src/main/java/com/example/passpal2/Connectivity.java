package com.example.passpal2;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;


import java.util.List;

public class Connectivity extends AsyncTask<Void, Void, Boolean> {
    private Context context;

    Connectivity(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Ελέγχει τη σύνδεση στο διαδίκτυο πριν από την εκτέλεση της εργασίας στο background
        return isOnline();
    }

    @Override
    protected void onPostExecute(Boolean isOnline) {
        if (isOnline) {
            // Αν υπάρχει σύνδεση στο διαδίκτυο, εκτέλεσε λειτουργίες βάσης δεδομένων
            performDatabaseOperation();
        } else {
            // Αν ΔΕΝ υπάρχει σύνδεση, εμφάνισε μήνυμα στον χρήστη
            showToast("No internet connection. Please check your network settings.");
        }
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void performDatabaseOperation() {
        // Εκτέλεση λειτουργιών βάσης δεδομένων
        DataBaseHelper userDB = new DataBaseHelper(context);

        List<DataBaseHelper.User> userList = userDB.getAllUsers();
        for (DataBaseHelper.User user : userList) {
            // Κάνε ό,τι χρειάζεται με τον κάθε χρήστη
        }
    }

}
