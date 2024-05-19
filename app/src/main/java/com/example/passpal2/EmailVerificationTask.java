package com.example.passpal2;

import android.content.Context;
import android.os.AsyncTask;

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

    public EmailVerificationTask(EmailVerificationListener listener) {
        Context context = null;
        this.contextReference = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... emails) {
        String emailToVerify = emails[0];
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
            return data.getString("result").equals("deliverable");
        } catch (Exception e) {
            e.printStackTrace();
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
}

