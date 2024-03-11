package com.example.passpal2;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.function.Consumer;

public class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
    private WeakReference<Context> contextRef;
    private Consumer<String> passwordConsumer;

    public GeneratePasswordTask(Context context, Consumer<String> passwordConsumer) {
        this.contextRef = new WeakReference<>(context);
        this.passwordConsumer = passwordConsumer;
    }

    @Override
    protected String doInBackground(Void... voids) {
        int len = 12;
        return generatePswd(len);
    }

    @Override
    protected void onPostExecute(String result) {
        if (contextRef.get() != null && passwordConsumer != null) {
            passwordConsumer.accept(result);
        }
    }

    private String generatePswd(int length) {
        String charsCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String chars = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String symbols = "!@#$%^&*_=+-/€.?<>)";

        String passSymbols = charsCaps + chars + nums + symbols;
        SecureRandom rnd = new SecureRandom();

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(passSymbols.charAt(rnd.nextInt(passSymbols.length())));
        }
        return password.toString();
    }
}
