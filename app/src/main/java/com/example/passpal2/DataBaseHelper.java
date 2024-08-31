package com.example.passpal2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "passpal.db";

    // User Table Columns
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_REGISTRATION_DATE = "registration_date";

    // Master Password Table Columns
    public static final String MASTER_PASSWORD_TABLE = "master_password_table";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_MASTER_PASSWORD = "master_password";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_MASTER_PASSWORD_TABLE = "CREATE TABLE " + MASTER_PASSWORD_TABLE + " (" +
                COLUMN_USERID + " INTEGER PRIMARY KEY, " +
                COLUMN_MASTER_PASSWORD + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_USERID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_MASTER_PASSWORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MASTER_PASSWORD_TABLE);
        onCreate(db);
    }


    // Insert user into the database
    public long insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_REGISTRATION_DATE, getCurrentDate());

        return db.insert(USER_TABLE, null, values);
    }

    // Check if username exists
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Check if email exists
    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Generate the current date in the desired format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Μέθοδος για την αποθήκευση του master password
    public void insertMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERID, userId);
        values.put(COLUMN_MASTER_PASSWORD, masterPassword);
        db.insert(MASTER_PASSWORD_TABLE, null, values);
        db.close();
    }

    // Μέθοδος για έλεγχο αν ο χρήστης υπάρχει με βάση το username και το password
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_PASSWORD};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String[] parts = storedPassword.split(":");

            if (parts.length == 2) {
                String hash = parts[0];
                String salt = parts[1];
                String hashedInputPassword = hashPassword(password, decodeSalt(salt));

                if (hash.equals(hashedInputPassword)) {
                    cursor.close();
                    return true;
                }
            }

            cursor.close();
        }

        return false;
    }

    // Μέθοδος για την απόκτηση του ID του χρήστη με βάση το username
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return userId;
        }

        return -1; // Αν δεν βρεθεί ο χρήστης, επιστρέφεται -1
    }
    public String getUsernameByUserId(int userId) {
        String username = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USERNAME + " FROM " + USER_TABLE + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                if (columnIndex != -1) {
                    username = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }
        db.close();
        return username;
    }

    public static int getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_credentials", MODE_PRIVATE);
        return preferences.getInt("userId", -1); // Επιστρέφει -1 αν δεν βρεθεί τιμή
    }
    // Μέθοδος για την ενημέρωση του password με βάση το email
    public boolean updatePasswordByEmail(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        // Χρήση του SQLite update μεθόδου
        int rowsAffected = db.update(USER_TABLE, values, COLUMN_EMAIL + " = ?", new String[]{email});

        db.close(); // Κλείνουμε τη βάση δεδομένων μετά την ενημέρωση

        return rowsAffected > 0; // Επιστρέφει true αν ενημερώθηκε τουλάχιστον μία σειρά
    }

    // Μέθοδος για την παραγωγή ενός salt
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
    //  ΚΡΥΠΤΟΓΡΑΦΗΣΗ *** ΚΡΥΠΤΟΓΡΑΦΗΣΗ *** ΚΡΥΠΤΟΓΡΑΦΗΣΗ *** ΚΡΥΠΤΟΓΡΑΦΗΣΗ *** ΚΡΥΠΤΟΓΡΑΦΗΣΗ
    // Μέθοδος για το hashing του κωδικού με salt
    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Μέθοδος για την κωδικοποίηση του salt σε String
    public static String encodeSalt(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    // Μέθοδος για την αποκωδικοποίηση του salt από String
    public static byte[] decodeSalt(String saltStr) {
        return Base64.getDecoder().decode(saltStr);
    }

    // AES encryption/decryption constants
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    // Method to generate a random AES key
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    // Method to encrypt data using AES-256
    public static String encryptAES(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedBytes = new byte[GCM_IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedBytes, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedBytes, GCM_IV_LENGTH, encryptedData.length);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Method to decrypt data using AES-256
    public static String decryptAES(String encryptedData, SecretKey key) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] iv = Arrays.copyOfRange(encryptedBytes, 0, GCM_IV_LENGTH);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decryptedData = cipher.doFinal(encryptedBytes, GCM_IV_LENGTH, encryptedBytes.length - GCM_IV_LENGTH);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}



/*
package com.example.passpal2;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;
import android.util.Log;


public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "passpal.db";

    // User Table Columns
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // App Info Table Columns
    public static final String TABLE_APPS_INFO = "app_info_table";
    public static final String COLUMN_APP_NAME = "AppName";
    public static final String COLUMN_APP_LINK = "AppLink";
    public static final String COLUMN_IMAGE_RESOURCE = "imageResource";
    public static final String COLUMN_APP_IMAGE_URI = "AppImageUri";
    public static final String COLUMN_IS_SELECTED = "isSelected";

    // Constants for App Credentials Table Columns
    public static final String TABLE_APP_CREDENTIALS = "app_credentials";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_APP_NAME_CREDENTIALS = "app_name";
    public static final String COLUMN_APP_LINK_CREDENTIALS = "app_link";
    public static final String COLUMN_USERNAME_CREDENTIALS = "username";
    public static final String COLUMN_EMAIL_CREDENTIALS = "email";
    public static final String COLUMN_PASSWORD_CREDENTIALS = "password";
    public static final String COLUMN_IMAGE_URI_STRING = "image_uri_string";

    // Master Password Table Columns
    public static final String MASTER_PASSWORD_TABLE = "master_password_table";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_MASTER_PASSWORD = "master_password";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createUserTableStatement = "CREATE TABLE " + USER_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";

        String createAppsInfoTableStatement = "CREATE TABLE " + TABLE_APPS_INFO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_APP_NAME + " TEXT, " +
                COLUMN_APP_LINK + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                COLUMN_APP_IMAGE_URI + " TEXT, " +
                COLUMN_IS_SELECTED + " INTEGER, " +
                COLUMN_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";



        String createAppCredentialsTableStatement = "CREATE TABLE " + TABLE_APP_CREDENTIALS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_APP_NAME_CREDENTIALS + " TEXT, " +
                COLUMN_APP_LINK_CREDENTIALS + " TEXT, " +
                COLUMN_USERNAME_CREDENTIALS + " TEXT, " +
                COLUMN_EMAIL_CREDENTIALS + " TEXT, " +
                COLUMN_PASSWORD_CREDENTIALS + " TEXT, " +
                COLUMN_IMAGE_URI_STRING + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";

        db.execSQL(createUserTableStatement);
        db.execSQL(createAppsInfoTableStatement);
        db.execSQL(createAppCredentialsTableStatement);

        // Προσθήκη αρχικών δεδομένων για τον χρήστη
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "demoUser");
        userValues.put(COLUMN_EMAIL, "demo@example.com");

        try {
            byte[] salt = DataBaseHelper.generateSalt();
            String hashedPassword = DataBaseHelper.hashPassword("demoPassword123", salt);
            userValues.put(COLUMN_PASSWORD, hashedPassword + ":" + DataBaseHelper.encodeSalt(salt));
            db.insert(USER_TABLE, null, userValues);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Προσθήκη αρχικών δεδομένων για μια εφαρμογή
        ContentValues appValues = new ContentValues();
        appValues.put(COLUMN_APP_NAME, "Demo App");
        appValues.put(COLUMN_APP_LINK, "https://demoapp.com");
        appValues.put(COLUMN_IMAGE_RESOURCE, R.drawable.applogomain);
        appValues.put(COLUMN_IS_SELECTED, 1); // Επιλεγμένη εφαρμογή
        appValues.put(COLUMN_USER_ID, 1);
        db.insert(TABLE_APPS_INFO, null, appValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String password;

        // Κατασκευαστής χωρίς το master password
        public User(int id, String username, String email, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
        }

        // Getters και Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    public class AppCredentials {
        private int id;
        private int userId;
        private String appName;
        private String appLink;
        private String username;
        private String email;
        private String password;
        private String imageUriString;

        // Constructor
        public AppCredentials(int userId, String appName, String appLink, String username, String email, String password, String imageUriString) {
            this.userId = userId;
            this.appName = appName;
            this.appLink = appLink;
            this.username = username;
            this.email = email;
            this.password = password;
            this.imageUriString = imageUriString;
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppLink() {
            return appLink;
        }

        public void setAppLink(String appLink) {
            this.appLink = appLink;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getImageUriString() {
            return imageUriString;
        }

        public void setImageUriString(String imageUriString) {
            this.imageUriString = imageUriString;
        }
    }


    // Μέθοδος για την αποθήκευση του master password
    public void saveMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERID, userId);
        values.put(COLUMN_MASTER_PASSWORD, masterPassword);
        db.insert(MASTER_PASSWORD_TABLE, null, values);
        db.close();
    }


    // Μέθοδος για να ελέγξει αν ο χρήστης έχει master password
    public boolean hasMasterPassword(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MASTER_PASSWORD + " FROM " + MASTER_PASSWORD_TABLE + " WHERE " + COLUMN_USERID + " = ?", new String[]{String.valueOf(userId)});
        boolean hasPassword = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasPassword = cursor.getString(0) != null;
            cursor.close();
        }
        db.close();
        return hasPassword;
    }

    // Μέθοδος για την επαλήθευση του master password
    public boolean checkMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MASTER_PASSWORD + " FROM " + MASTER_PASSWORD_TABLE + " WHERE " + COLUMN_USERID + " = ?", new String[]{String.valueOf(userId)});
        boolean isPasswordCorrect = false;
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            isPasswordCorrect = storedPassword.equals(masterPassword);
            cursor.close();
        }
        db.close();
        return isPasswordCorrect;
    }


    public void addUserApp(AppsObj userApp, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, userApp.getAppNames());
        values.put(COLUMN_APP_LINK, userApp.getAppLinks());
        values.put(COLUMN_USER_ID, userId);
        db.insert(TABLE_APPS_INFO, null, values);
        db.close();
    }

    private static final String SALT_ALGORITHM = "SHA1PRNG";
    private static final int SALT_LENGTH = 16;

    // Μέθοδος για την παραγωγή ενός salt
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance(SALT_ALGORITHM);
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }

    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    // Μέθοδος για την κωδικοποίηση του salt σε String για αποθήκευση
    public static String encodeSalt(byte[] salt) {
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    // Μέθοδος για την αποκωδικοποίηση του salt από String
    public static byte[] decodeSalt(String saltStr) {
        return Base64.decode(saltStr, Base64.DEFAULT);
    }

    // Κώδικας για την εισαγωγή του χρήστη στον πίνακα
    public boolean addOne(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            byte[] salt = generateSalt();
            String hashedPassword = hashPassword(user.getPassword(), salt);
            String saltStr = encodeSalt(salt);
            cv.put(COLUMN_USERNAME, user.getUsername());
            cv.put(COLUMN_EMAIL, user.getEmail());
            cv.put(COLUMN_PASSWORD, hashedPassword + ":" + saltStr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        long insert = db.insert(USER_TABLE, null, cv);

        if (insert != -1) {
            // Δημιουργία του πίνακα master_password_table μόνο για αυτόν τον χρήστη
            String createMasterPasswordTableStatement = "CREATE TABLE IF NOT EXISTS " + MASTER_PASSWORD_TABLE + " (" +
                    COLUMN_USERID + " INTEGER PRIMARY KEY, " +
                    COLUMN_MASTER_PASSWORD + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_USERID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";
            db.execSQL(createMasterPasswordTableStatement);
        }

        db.close();
        return insert != -1;
    }


    // Ελέγχος εάν ο χρήστης υπάρχει με βάση το email
    public boolean isUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }


    // Κώδικας για την εισαγωγή χρήστη αν δεν υπάρχει ήδη
    public boolean addUserIfNotExists(User user) {
        if (!isUserExists(user.getEmail())) {
            return addOne(user);
        } else {
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") User user = new User(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD))
                );
                userList.add(user);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return userList;
    }

    public boolean addSelectedAppWithUserId(AppsObj appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppNames());
        values.put(COLUMN_APP_LINK, appInfo.getAppLinks());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getAppImages());
        values.put(COLUMN_IS_SELECTED, 1);  //  isSelected σε 1 για τις επιλεγμένες εφαρμογές
        values.put(COLUMN_USER_ID, userId);  // το user_id για την επιλεγμένη εφαρμογή

        long insert = db.insert(TABLE_APPS_INFO, null, values);
        db.close();

        // true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }

    public static int getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_credentials", MODE_PRIVATE);
        return preferences.getInt("userId", -1); // Επιστρέφει -1 αν δεν βρεθεί τιμή
    }



    public String getUsernameByUserId(int userId) {
        String username = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USERNAME + " FROM " + USER_TABLE + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                if (columnIndex != -1) {
                    username = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }
        db.close();
        return username;
    }

    public boolean updateAppInfo(AppsObj appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APP_NAME, appInfo.getAppNames());
        cv.put(COLUMN_APP_LINK, appInfo.getAppLinks());
        cv.put(COLUMN_IMAGE_RESOURCE, appInfo.getAppImages());
        // αν εχει επιλεχθει η εφαρμογη ή οχι
        cv.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);

        // Ενημέρωση βάσει του appId και του userId
        int rowsAffected = db.update(TABLE_APPS_INFO, cv, COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?", new String[]{String.valueOf(appInfo.getId()), String.valueOf(userId)});
        db.close();

        return rowsAffected > 0;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                USER_TABLE,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, // group by
                null, // having
                null // order by
        );

        boolean isAuthenticated = false;

        if (cursor != null && cursor.moveToFirst()) {
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (passwordColumnIndex != -1) {
                String storedPassword = cursor.getString(passwordColumnIndex);
                String[] parts = storedPassword.split(":");
                if (parts.length == 2) {
                    String hash = parts[0];
                    String salt = parts[1];
                    String hashedInputPassword = hashPassword(password, decodeSalt(salt));
                    if (hash.equals(hashedInputPassword)) {
                        isAuthenticated = true;
                    }
                }
            }
            cursor.close();
        }

        db.close();
        return isAuthenticated;
    }

    public void updateLastLogin(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("last_login", getCurrentDateTime());
        db.update(USER_TABLE, cv, COLUMN_USERNAME + "=?", new String[]{username});
        db.close();
    }


    // Πρόσθεσε τη μέθοδο insertUser
    public long insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);

        long result = db.insert(USER_TABLE, null, cv);
        db.close();
        return result;
    }


    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }


    @SuppressLint("Range")
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
        }

        return userId;
    }

    public User getUserByEmail(String email) {
        User user = null;
        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);

            int id = cursor.getInt(idColumnIndex);
            String username = cursor.getString(usernameColumnIndex);
            String password = cursor.getString(passwordColumnIndex);

            user = new User(id, username, email, password);

            cursor.close();
        }
        db.close();

        return user;
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            byte[] salt = generateSalt();
            String hashedPassword = hashPassword(newPassword, salt);
            String saltStr = encodeSalt(salt);

            values.put(COLUMN_PASSWORD, hashedPassword + ":" + saltStr);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        db.update(USER_TABLE, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
    }

    public void deleteUserData(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Διαγραφή από τον πίνακα app_credentials
        db.delete(TABLE_APP_CREDENTIALS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

        // Διαγραφή από τον πίνακα app_info_table
        db.delete(TABLE_APPS_INFO, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

        // Διαγραφή από τον πίνακα user_table
        db.delete(USER_TABLE, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        db.close();
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + " = ?";
            cursor = db.rawQuery(query, new String[]{username});

            if (cursor != null) {
                exists = cursor.getCount() > 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exists;
    }

    public String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    public boolean saveAppCredentials(int appId, int userId, String appName, String username, String email, String password, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_APP_NAME_CREDENTIALS, appName);
        cv.put(COLUMN_USERNAME_CREDENTIALS, username);
        cv.put(COLUMN_EMAIL_CREDENTIALS, email);
        cv.put(COLUMN_PASSWORD_CREDENTIALS, password);
        cv.put(COLUMN_APP_LINK_CREDENTIALS, link);

        // Ενημέρωση της εφαρμογής με βάση το appId
        int rowsAffected = db.update(TABLE_APP_CREDENTIALS, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(appId)});
        db.close();

        return rowsAffected > 0;
    }

    public List<AppCredentials> getAllCredentialsForUser(int userId) {
        List<AppCredentials> credentialsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_APP_CREDENTIALS + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int appNameColumnIndex = cursor.getColumnIndex(COLUMN_APP_NAME_CREDENTIALS);
                int appLinkColumnIndex = cursor.getColumnIndex(COLUMN_APP_LINK_CREDENTIALS);
                int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME_CREDENTIALS);
                int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL_CREDENTIALS);
                int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD_CREDENTIALS);
                int imageUriStringColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_URI_STRING);

                do {
                    int id = cursor.getInt(idColumnIndex);
                    String appName = cursor.getString(appNameColumnIndex);
                    String appLink = cursor.getString(appLinkColumnIndex);
                    String username = cursor.getString(usernameColumnIndex);
                    String email = cursor.getString(emailColumnIndex);
                    String password = cursor.getString(passwordColumnIndex);
                    String imageUriString = cursor.getString(imageUriStringColumnIndex);

                    AppCredentials credentials = new AppCredentials(userId, appName, appLink, username, email, password, imageUriString);
                    credentials.setId(id);

                    credentialsList.add(credentials);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return credentialsList;
    }

    // Κώδικας για την ανάκτηση όλων των επιλεγμένων εφαρμογών
    public List<AppsObj> getAllSelectedApps(int userId) {
        List<AppsObj> selectedApps = new ArrayList<>();
        HashSet<String> addedApps = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_IS_SELECTED + " = 1 AND " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int appNameColumnIndex = cursor.getColumnIndex(COLUMN_APP_NAME);
                int appLinkColumnIndex = cursor.getColumnIndex(COLUMN_APP_LINK);
                int imageResourceColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_RESOURCE);

                do {
                    String appName = cursor.getString(appNameColumnIndex);
                    // Έλεγχος αν το όνομα της εφαρμογής έχει ήδη προστεθεί
                    if (!addedApps.contains(appName)) {
                        int id = cursor.getInt(idColumnIndex);
                        String appLink = cursor.getString(appLinkColumnIndex);
                        int imageResource = cursor.getInt(imageResourceColumnIndex);

                        AppsObj appInfo = new AppsObj(appName, appLink, imageResource);
                        appInfo.setId(id);
                        appInfo.setSelected(true);

                        selectedApps.add(appInfo);
                        addedApps.add(appName);

                        Log.d("DataBaseHelper", "Retrieved App: " + appName + " for User ID: " + userId);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return selectedApps;
    }


    //  αφαιρεί όλες τις επιλεγμένες εφαρμογές
    public void removeAllSelectedApps(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS_INFO, COLUMN_IS_SELECTED + " = 1 AND " + COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public void deleteApp(String appName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Διαγραφή της εφαρμογής με βάση το όνομα, το userId και τη θέση
        db.delete(TABLE_APPS_INFO, COLUMN_APP_NAME + "=? AND " + COLUMN_USER_ID + "=?", new String[]{appName, String.valueOf(userId)});
        db.close();
    }

    // Κώδικας για την εισαγωγή δεδομένων στον πίνακα app_credentials
    public boolean addAppCredential(AppCredentials credential) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, credential.getUserId());
        values.put(COLUMN_APP_NAME_CREDENTIALS, credential.getAppName());
        values.put(COLUMN_APP_LINK_CREDENTIALS, credential.getAppLink());
        values.put(COLUMN_USERNAME_CREDENTIALS, credential.getUsername());
        values.put(COLUMN_EMAIL_CREDENTIALS, credential.getEmail());
        values.put(COLUMN_PASSWORD_CREDENTIALS, credential.getPassword());
        values.put(COLUMN_IMAGE_URI_STRING, credential.getImageUriString());

        long result = db.insert(TABLE_APP_CREDENTIALS, null, values);
        db.close();
        return result != -1;
    }

    // Κώδικας για την εισαγωγή δεδομένων στον πίνακα app_info_table
    public boolean addAppInfo(AppsObj appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APP_NAME, appInfo.getAppNames());
        cv.put(COLUMN_APP_LINK, appInfo.getAppLinks());
        cv.put(COLUMN_IMAGE_RESOURCE, appInfo.getAppImages());
        // Μετατροπή της τιμής isSelected σε 1 ή 0.
        cv.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);
        cv.put(COLUMN_USER_ID, userId); // Προσθήκη του πεδίου user_id

        long insert = db.insert(TABLE_APPS_INFO, null, cv);
        db.close();

        //  true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }

    // Μέθοδος για την αποθήκευση του master password
    public void insertMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("master_password", masterPassword);
        db.insert("master_password_table", null, values);
        db.close();
    }


    // Κώδικας για την ανάκτηση όλων των πληροφοριών εφαρμογών από τον πίνακα app_info_table
    public List<AppsObj> getAllAppInfo(int userId) {
        List<AppsObj> appInfoList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_USER_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int appNameColumnIndex = cursor.getColumnIndex(COLUMN_APP_NAME);
                int appLinkColumnIndex = cursor.getColumnIndex(COLUMN_APP_LINK);
                int imageResourceColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_RESOURCE);
                int isSelectedColumnIndex = cursor.getColumnIndex(COLUMN_IS_SELECTED);

                do {
                    int id = cursor.getInt(idColumnIndex);
                    String appName = cursor.getString(appNameColumnIndex);
                    String appLink = cursor.getString(appLinkColumnIndex);
                    int imageResource = cursor.getInt(imageResourceColumnIndex);
                    boolean isSelected = cursor.getInt(isSelectedColumnIndex) == 1;

                    AppsObj appInfo = new AppsObj(appName, appLink, imageResource);
                    appInfo.setId(id);
                    appInfo.setSelected(isSelected);

                    appInfoList.add(appInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return appInfoList;
    }

    public boolean isAppSelected(String appName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_APP_NAME + "=? AND " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{appName, String.valueOf(userId)});
        boolean isSelected = false;
        if (cursor.moveToFirst()) {
            // true αν βρεθεί έστω και μία εγγραφή
            isSelected = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return isSelected;
    }

    public boolean appExists(String appName, String appLink) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_APP_NAME + "=? OR " + COLUMN_APP_LINK + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{appName, appLink});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // νεα εφαρμογη
    public boolean addNewAppWithDetails(int userId, String appName, String appLink, String username, String email, String password, String imageUriString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APP_NAME, appName);
        cv.put(COLUMN_APP_LINK, appLink);
        cv.put("app_username", username);
        cv.put("app_email", email);
        cv.put("app_password", password);
        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_IS_SELECTED, 1);
        cv.put(COLUMN_APP_IMAGE_URI, imageUriString);

        long result = db.insert(TABLE_APPS_INFO, null, cv);
        db.close();

        return result != -1;
    }

    public boolean saveSelectedAppToDatabase(AppsObj appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APP_NAME, appInfo.getAppNames());
        cv.put(COLUMN_APP_LINK, appInfo.getAppLinks());
        cv.put(COLUMN_IMAGE_RESOURCE, appInfo.getAppImages());
        cv.put(COLUMN_IS_SELECTED, 1);
        cv.put(COLUMN_USER_ID, userId);

        long result = db.insert(TABLE_APPS_INFO, null, cv);
        db.close();

        if (result == -1) {
            Log.e("DataBaseHelper", "Failed to insert app for User ID: " + userId);
            return false; //  Η εισαγωγή απέτυχε
        } else {
            Log.d("DataBaseHelper", "App inserted successfully for User ID: " + userId + " App Name: " + appInfo.getAppNames());
            return true; //  Η εισαγωγή είναι επιτυχής
        }
    }

}
*/
