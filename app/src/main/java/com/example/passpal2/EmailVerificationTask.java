package com.example.passpal2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailVerificationTask extends AsyncTask<String, Void, Boolean> {
    private WeakReference<Context> contextReference;
    private EmailVerificationListener listener;

    public interface EmailVerificationListener {
        void onEmailVerified(boolean isEmailValid);
    }

    // Constructor που δέχεται το context και τον listener
    public EmailVerificationTask(Context context, EmailVerificationListener listener) {
        this.contextReference = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        Context context = contextReference.get();
        if (context != null) {
            new Connectivity(context, new Connectivity.ConnectivityListener() {
                @Override
                public void onConnectionChecked(boolean isConnected) {
                    if (!isConnected) {
                        cancel(true);
                        if (listener != null) {
                            listener.onEmailVerified(false);
                        }
                    }
                }
            }).execute();
        }
    }

    @Override
    protected Boolean doInBackground(String... emails) {
        if (isCancelled()) {
            return false;
        }

        String emailToVerify = emails[0];

        // Τοπική επαλήθευση format email πριν καλέσουμε το API
        if (!isValidEmail(emailToVerify)) {
            Log.d("EmailVerification", "Invalid email format: " + emailToVerify);
            return false; // Επιστρέφουμε false εάν το format είναι άκυρο
        }

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://api.hunter.io/v2/email-verifier?email=" + emailToVerify + "&api_key=9f387e4dfb8a839b9b246089137cc92244ad5562");
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONObject jsonObject = new JSONObject(result.toString());
            JSONObject data = jsonObject.getJSONObject("data");

            // Καταγραφή της απόκρισης για έλεγχο
            Log.d("EmailVerification", "API Response: " + data.toString());

            // Έλεγχος του αποτελέσματος αν είναι "deliverable"
            return data.getString("result").equals("deliverable");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EmailVerification", "Error: " + e.getMessage());
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean isEmailValid) {
        if (listener != null) {
            listener.onEmailVerified(isEmailValid);
        }
    }

    // Μέθοδος για τοπική επαλήθευση format email με regular expression
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}
