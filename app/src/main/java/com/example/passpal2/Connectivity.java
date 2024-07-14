package com.example.passpal2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public class Connectivity extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private ConnectivityListener listener;

    public interface ConnectivityListener {
        void onConnectionChecked(boolean isConnected);
    }

    private static Boolean cachedIsOnline = null;
    private static long lastChecked = 0;
    private static final long CACHE_DURATION = 60000;

    public Connectivity(Context context, ConnectivityListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return isOnline();
    }

    @Override
    protected void onPostExecute(Boolean isOnline) {
        if (listener != null) {
            listener.onConnectionChecked(isOnline);
        }
        if (!isOnline) {
            showToast("No internet connection. Please check your network settings.");
        }
    }

    private boolean isOnline() {
        long currentTime = System.currentTimeMillis();
        if (cachedIsOnline != null && (currentTime - lastChecked) < CACHE_DURATION) {
            return cachedIsOnline;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            try {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                cachedIsOnline = networkInfo != null && networkInfo.isConnected();
                lastChecked = currentTime;
                return cachedIsOnline;
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Failed to check internet connection.");
            }
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
