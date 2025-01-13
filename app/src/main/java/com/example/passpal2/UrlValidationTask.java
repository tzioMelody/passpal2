package com.example.passpal2;

import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlValidationTask extends AsyncTask<String, Void, Boolean> {

    private final UrlValidationListener listener;

    public interface UrlValidationListener {
        void onUrlValidationResult(boolean isValid);
    }

    public UrlValidationTask(UrlValidationListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        String urlString = urls[0];
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // Χρησιμοποιούμε HEAD για λιγότερα δεδομένα
            connection.setConnectTimeout(5000); // Timeout 5 δευτερόλεπτα
            connection.setReadTimeout(5000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400); // 2xx και 3xx θεωρούνται επιτυχία

        } catch (Exception e) {
            Log.e("UrlValidationTask", "URL validation error: " + e.getMessage());
            return false;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean isValid) {
        if (listener != null) {
            listener.onUrlValidationResult(isValid);
        }
    }
}

