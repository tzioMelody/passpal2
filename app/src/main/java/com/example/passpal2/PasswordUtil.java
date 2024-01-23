package com.example.passpal2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {
    public static String getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(android.util.Base64.decode(salt, android.util.Base64.NO_WRAP));
            byte[] hashedPassword = md.digest(password.getBytes());
            return android.util.Base64.encodeToString(hashedPassword, android.util.Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
