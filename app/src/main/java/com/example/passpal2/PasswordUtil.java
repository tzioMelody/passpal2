package com.example.passpal2;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import android.util.Base64;

public class PasswordUtil {

    // Μέθοδος για τη δημιουργία salt
    public static byte[] generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    // Μέθοδος για το hashing του κωδικού με salt
    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.encodeToString(hashedPassword, Base64.DEFAULT).trim();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Κωδικοποίηση του salt για αποθήκευση
    public static String encodeSalt(byte[] salt) {
        return Base64.encodeToString(salt, Base64.DEFAULT).trim();
    }

    // Αποκωδικοποίηση του salt από τη βάση
    public static byte[] decodeSalt(String saltStr) {
        return Base64.decode(saltStr, Base64.DEFAULT);
    }
}
