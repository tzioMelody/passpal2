package com.example.passpal2;

import static android.content.Context.MODE_PRIVATE;

import static com.example.passpal2.PasswordUtil.encodeSalt;
import static com.example.passpal2.PasswordUtil.generateSalt;
import static com.example.passpal2.PasswordUtil.hashPassword;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "passpal.db";

    // User Table Columns
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Master Password Table Columns
    public static final String MASTER_PASSWORD_TABLE = "master_password_table";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_MASTER_PASSWORD = "master_password";

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
    private final Context context;

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

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

        String CREATE_TABLE_APPS_INFO = "CREATE TABLE " + TABLE_APPS_INFO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_APP_NAME + " TEXT, " +
                COLUMN_APP_LINK + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                COLUMN_APP_IMAGE_URI + " TEXT, " +
                COLUMN_IS_SELECTED + " INTEGER, " +
                COLUMN_USER_ID + " INTEGER)";
        db.execSQL(CREATE_TABLE_APPS_INFO);

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
        db.execSQL(createAppCredentialsTableStatement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MASTER_PASSWORD_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_CREDENTIALS);
        onCreate(db);
    }


    // Insert user into the database
    public long insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        byte[] salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        String saltStr = PasswordUtil.encodeSalt(salt);

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedPassword + ":" + saltStr);

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

        try {
            // Δημιουργία salt και hashing του master password
            byte[] salt = PasswordUtil.generateSalt(); // Παράγει νέο salt
            String hashedPassword = PasswordUtil.hashPassword(masterPassword, salt); // Hash του κωδικού
            String saltStr = PasswordUtil.encodeSalt(salt); // Κωδικοποίηση salt σε string

            // Αποθήκευση hashed password και salt στον πίνακα
            values.put(COLUMN_USERID, userId); // User ID
            values.put(COLUMN_MASTER_PASSWORD, hashedPassword + ":" + saltStr); // Συνδυασμός hash και salt

            // Εισαγωγή στη βάση δεδομένων
            long result = db.insert(MASTER_PASSWORD_TABLE, null, values);
            if (result == -1) {
                Log.e("DataBaseHelper", "Failed to insert master password for user ID: " + userId);
            } else {
                Log.d("DataBaseHelper", "Master password stored successfully for user ID: " + userId);
            }
        } finally {
            db.close();
        }
    }


    // REFORMATTED
    // Μέθοδος για έλεγχο αν ο χρήστης υπάρχει με βάση το username και το password
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE,
                new String[]{COLUMN_PASSWORD},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();

            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }

            String storedHash = parts[0];
            byte[] storedSalt = PasswordUtil.decodeSalt(parts[1]);

            String computedHash = PasswordUtil.hashPassword(password, storedSalt);

            return storedHash.equals(computedHash);
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
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
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

    public String getUserEmailByUserId(int userId) {
        String email = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_EMAIL + " FROM " + USER_TABLE + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                if (columnIndex != -1) {
                    email = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }
        db.close();
        return email;
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

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return userId;
        }

        return -1; // Αν δεν βρεθεί ο χρήστης, επιστρέφεται -1
    }

    public void deleteApp(String appName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Διαγραφή της εφαρμογής με βάση το όνομα και το userId από την main activity
        db.delete(TABLE_APPS_INFO, COLUMN_APP_NAME + "=? AND " + COLUMN_USER_ID + "=?", new String[]{appName, String.valueOf(userId)});
        db.close();
    }

    public boolean deleteAppCredentials(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APP_CREDENTIALS, "ID = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public int countAccountsForApp(String appName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM app_credentials WHERE app_name = ?", new String[]{appName});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public void deleteUserData(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Διαγραφή από τον πίνακα app_credentials
        db.delete(TABLE_APP_CREDENTIALS, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        // Διαγραφή από τον πίνακα app_info_table
        db.delete(TABLE_APPS_INFO, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        // Διαγραφή από τον πίνακα user_table
        db.delete(USER_TABLE, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        db.close();
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
                    if (!addedApps.contains(appName)) {
                        int id = cursor.getInt(idColumnIndex);
                        String appLink = cursor.getString(appLinkColumnIndex);
                        int imageResource = cursor.getInt(imageResourceColumnIndex);

                        AppsObj appInfo = new AppsObj(appName, appLink, imageResource);
                        appInfo.setId(id);
                        appInfo.setSelected(true);

                        selectedApps.add(appInfo);
                        addedApps.add(appName);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        //db.close();
        return selectedApps;
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


    public boolean deleteItem(String username, String appName, int userId, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            // 1. Ανάκτηση του κρυπτογραφημένου κωδικού από τη βάση
            cursor = db.query(DataBaseHelper.TABLE_APP_CREDENTIALS,
                    new String[]{DataBaseHelper.COLUMN_PASSWORD_CREDENTIALS},
                    DataBaseHelper.COLUMN_USERNAME_CREDENTIALS + "=? AND " +
                            DataBaseHelper.COLUMN_APP_NAME_CREDENTIALS + "=? AND " +
                            DataBaseHelper.COLUMN_USER_ID + "=? AND " +
                            DataBaseHelper.COLUMN_EMAIL_CREDENTIALS + "=?",
                    new String[]{username, appName, String.valueOf(userId), email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String encryptedPassword = cursor.getString(0);

                String[] parts = encryptedPassword.split(":");
                if (parts.length != 2) {
                    return false;
                }

                // 2. Αποκρυπτογράφηση
                String storedHash = parts[0];
                byte[] storedSalt = PasswordUtil.decodeSalt(parts[1]);
                String computedHash = PasswordUtil.hashPassword(password, storedSalt);

                // 3. Έλεγχος αν οι κωδικοί ταιριάζουν
                if (storedHash.equals(computedHash)) {
                    int rowsDeleted = db.delete(
                            DataBaseHelper.TABLE_APP_CREDENTIALS,
                            DataBaseHelper.COLUMN_USERNAME_CREDENTIALS + "=? AND " +
                                    DataBaseHelper.COLUMN_APP_NAME_CREDENTIALS + "=? AND " +
                                    DataBaseHelper.COLUMN_USER_ID + "=? AND " +
                                    DataBaseHelper.COLUMN_EMAIL_CREDENTIALS + "=?",
                            new String[]{username, appName, String.valueOf(userId), email});

                    return rowsDeleted > 0;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }


    public boolean isLinkTaken(String appLink, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        appLink = appLink.replace("https://", "").replace(".com", "");

        String queryCredentials = "SELECT COUNT(*) FROM " + TABLE_APP_CREDENTIALS + " WHERE " + COLUMN_APP_LINK_CREDENTIALS + "=? AND " + COLUMN_USER_ID + "=?";
        Cursor cursorCredentials = db.rawQuery(queryCredentials, new String[]{appLink, String.valueOf(userId)});
        boolean isTakenInCredentials = false;
        if (cursorCredentials.moveToFirst()) {
            isTakenInCredentials = cursorCredentials.getInt(0) > 0;
        }
        cursorCredentials.close();

        String queryAppsInfo = "SELECT COUNT(*) FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_APP_LINK + "=?";
        Cursor cursorAppsInfo = db.rawQuery(queryAppsInfo, new String[]{appLink});
        boolean isTakenInAppsInfo = false;
        if (cursorAppsInfo.moveToFirst()) {
            isTakenInAppsInfo = cursorAppsInfo.getInt(0) > 0;
        }
        cursorAppsInfo.close();

        db.close();

        return isTakenInCredentials || isTakenInAppsInfo;
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

                    // Decrypt the sensitive fields
                    String encryptedUsername = cursor.getString(usernameColumnIndex);
                    String decryptedUsername = null;
                    try {
                        decryptedUsername = EncryptionHelper.decrypt(encryptedUsername);
                        Log.e("DecryptDebug", "Decrypted: " + decryptedUsername);
                    } catch (Exception e) {
                        Log.e("DataBaseHelper", "Decryption error for username: " + e.getMessage());
                    }

                    String encryptedEmail = cursor.getString(emailColumnIndex);
                    String decryptedEmail = null;
                    try {
                        decryptedEmail = EncryptionHelper.decrypt(encryptedEmail);
                        Log.e("DecryptDebug", "Decrypted: " + decryptedEmail);
                    } catch (Exception e) {
                        Log.e("DataBaseHelper", "Decryption error for email: " + e.getMessage());
                    }

                    String encryptedPassword = cursor.getString(passwordColumnIndex);
                    String decryptedPassword = null;
                    try {
                        decryptedPassword = EncryptionHelper.decrypt(encryptedPassword);
                        Log.e("DecryptDebug", "Decrypted: " + decryptedPassword);
                    } catch (Exception e) {
                        Log.e("DataBaseHelper", "Decryption error for password: " + e.getMessage());
                    }

                    String imageUriString = cursor.getString(imageUriStringColumnIndex);

                    // Create AppCredentials object with decrypted values
                    AppCredentials credentials = new AppCredentials(userId, appName, appLink, decryptedUsername, decryptedEmail, decryptedPassword, imageUriString);
                    credentials.setId(id);

                    credentialsList.add(credentials);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return credentialsList;
    }


    // Μέθοδος για επαλήθευση του master password
    public boolean checkMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_MASTER_PASSWORD};
        String selection = COLUMN_USERID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(MASTER_PASSWORD_TABLE, columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPasswordData = cursor.getString(cursor.getColumnIndex(COLUMN_MASTER_PASSWORD));
            cursor.close();

            // Διαχωρισμός του hash και του salt
            String[] parts = storedPasswordData.split(":");
            if (parts.length != 2) {
                return false; // Invalid format
            }

            String storedHash = parts[0];
            byte[] storedSalt = PasswordUtil.decodeSalt(parts[1]);

            // Hash the entered Master Password with the stored salt
            String hashedInputPassword = PasswordUtil.hashPassword(masterPassword, storedSalt);

            // Σύγκριση hash
            return storedHash.equals(hashedInputPassword);
        }

        db.close();
        return false;
    }


    public boolean saveAppCredentials(int userId, String appName, String username, String email, String password, String link, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {
            // Ensure the encryption key exists
            EncryptionHelper.generateKey();

            // Encrypt sensitive data
            String encryptedUsername = EncryptionHelper.encrypt(username);
            String encryptedEmail = EncryptionHelper.encrypt(email);
            String encryptedPassword = EncryptionHelper.encrypt(password);

            // Add encrypted values to ContentValues
            cv.put(COLUMN_USER_ID, userId);
            cv.put(COLUMN_APP_NAME_CREDENTIALS, appName);
            cv.put(COLUMN_USERNAME_CREDENTIALS, encryptedUsername);
            cv.put(COLUMN_EMAIL_CREDENTIALS, encryptedEmail);
            cv.put(COLUMN_PASSWORD_CREDENTIALS, encryptedPassword);
            cv.put(COLUMN_APP_LINK_CREDENTIALS, link);
            cv.put(COLUMN_IMAGE_URI_STRING, imageUri);

            Log.d("DataBaseHelper", "Attempting to insert new credentials for User ID: " + userId);

            // Insert new record
            long result = db.insert(TABLE_APP_CREDENTIALS, null, cv);
            db.close();

            if (result == -1) {
                Log.e("DataBaseHelper", "Failed to insert new credentials for User ID: " + userId);
                return false;
            } else {
                Log.d("DataBaseHelper", "Inserted new credentials successfully for User ID: " + userId);
                return true;
            }
        } catch (Exception e) {
            Log.e("DataBaseHelper", "Encryption error: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public boolean updateAppCredentials(int id,int userId, String appName, String username, String email, String password, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {
            // Ensure the encryption key exists
            EncryptionHelper.generateKey();

            // Encrypt sensitive data
            String encryptedUsername = EncryptionHelper.encrypt(username);
            String encryptedEmail = EncryptionHelper.encrypt(email);
            String encryptedPassword = EncryptionHelper.encrypt(password);

            // Add encrypted values to ContentValues
            cv.put(COLUMN_APP_NAME_CREDENTIALS, appName);
            cv.put(COLUMN_USERNAME_CREDENTIALS, encryptedUsername);
            cv.put(COLUMN_EMAIL_CREDENTIALS, encryptedEmail);
            cv.put(COLUMN_PASSWORD_CREDENTIALS, encryptedPassword);
            cv.put(COLUMN_APP_LINK_CREDENTIALS, link);

            Log.d("DataBaseHelper", "Attempting to update credentials for ID: " + id);

            // Update the record based on the unique ID
            int result = db.update(TABLE_APP_CREDENTIALS, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();

            if (result > 0) {
                Log.d("DataBaseHelper", "Updated credentials successfully for ID: " + id);
                return true;
            } else {
                Log.e("DataBaseHelper", "Failed to update credentials for ID: " + id);
                return false;
            }
        } catch (Exception e) {
            Log.e("DataBaseHelper", "Encryption error: " + e.getMessage());
            db.close();
            return false;
        }
    }


    // Ενημέρωση Master Password
    public boolean updateMasterPassword(int userId, String newMasterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Δημιουργία salt και hashing του νέου master password
            byte[] salt = PasswordUtil.generateSalt(); // Δημιουργία νέου salt
            String hashedPassword = PasswordUtil.hashPassword(newMasterPassword, salt); // Δημιουργία hash
            String saltStr = PasswordUtil.encodeSalt(salt); // Κωδικοποίηση του salt σε String

            // Προετοιμασία για ενημέρωση στη βάση
            values.put(COLUMN_MASTER_PASSWORD, hashedPassword + ":" + saltStr);

            // Εκτέλεση ενημέρωσης
            int rowsAffected = db.update(
                    MASTER_PASSWORD_TABLE,
                    values,
                    COLUMN_USERID + " = ?",
                    new String[]{String.valueOf(userId)}
            );

            db.close();
            return rowsAffected > 0; // Επιστροφή true αν η ενημέρωση ήταν επιτυχής
        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return false; // Σε περίπτωση αποτυχίας
        }
    }

    public boolean updateUsernameAndEmailWithCheck(int userId, String newUsername, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            // Έλεγχος αν το νέο username υπάρχει ήδη για άλλον χρήστη
            cursor = db.query(USER_TABLE, null, COLUMN_USERNAME + "=? AND " + COLUMN_ID + "!=?",
                    new String[]{newUsername, String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                Log.e("DataBaseHelper", "Username already exists: " + newUsername);
                return false; // Το username υπάρχει ήδη
            }
            cursor.close();

            // Έλεγχος αν το νέο email υπάρχει ήδη για άλλον χρήστη
            cursor = db.query(USER_TABLE, null, COLUMN_EMAIL + "=? AND " + COLUMN_ID + "!=?",
                    new String[]{newEmail, String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                Log.e("DataBaseHelper", "Email already exists: " + newEmail);
                return false; // Το email υπάρχει ήδη
            }
            cursor.close();

            // Ενημέρωση του χρήστη
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, newUsername);
            values.put(COLUMN_EMAIL, newEmail);

            int rowsAffected = db.update(USER_TABLE, values, COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)});
            Log.d("DataBaseHelper", "Rows affected: " + rowsAffected);
            return rowsAffected > 0; // Επιστρέφει true αν έγινε ενημέρωση

        } catch (Exception e) {
            Log.e("DataBaseHelper", "Error updating username and email: " + e.getMessage());
            return false; // Κάποιο σφάλμα συνέβη
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
}