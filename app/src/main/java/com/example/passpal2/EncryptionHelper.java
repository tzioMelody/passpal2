package com.example.passpal2;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class EncryptionHelper {
    private static final String KEY_ALIAS = "MySecureKeyAlias";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    // Generate a key in the Keystore
    public static void generateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
            );
            keyGenerator.generateKey();
        }
    }

    // Encrypt data
    public static String encrypt(String data) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptionIv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Combine IV and encrypted data
        byte[] combined = new byte[encryptionIv.length + encryptedData.length];
        System.arraycopy(encryptionIv, 0, combined, 0, encryptionIv.length);
        System.arraycopy(encryptedData, 0, combined, encryptionIv.length, encryptedData.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }


    // Decrypt data
    // Decrypt data
    public static String decrypt(String encryptedData) throws Exception {
        Log.d("DecryptionDebug", "Raw Encrypted Data: " + encryptedData);  // Log the raw encrypted data

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);

        // Log combined data length
        Log.d("DecryptionDebug", "Combined Data Length: " + combined.length);

        // Extract IV and encrypted data
        byte[] encryptionIv = new byte[12]; // GCM standard IV length
        System.arraycopy(combined, 0, encryptionIv, 0, 12); // First 12 bytes are IV
        byte[] encryptedBytes = new byte[combined.length - 12];
        System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.length); // Rest is encrypted data

        // Debug logs for encrypted data and IV length
        Log.d("DecryptionDebug", "Encrypted Data Length: " + encryptedBytes.length);
        Log.d("DecryptionDebug", "IV Length: " + encryptionIv.length);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, encryptionIv));
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData);
    }


}