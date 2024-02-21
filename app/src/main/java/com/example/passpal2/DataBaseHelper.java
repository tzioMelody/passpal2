package com.example.passpal2;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.passpal2.Data.Entities.AppsInfo;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
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
    public static final String COLUMN_IS_SELECTED = "isSelected";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String password;


        // Κατασκευαστής
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
                COLUMN_IS_SELECTED + " INTEGER)";

        db.execSQL(createUserTableStatement);
        db.execSQL(createAppsInfoTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS_INFO);
            onCreate(db);
        }
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

    // Μέθοδος για το hashing του κωδικού με SHA-256
    public static String hashPassword(String passwordToHash, byte[] salt) {
        String hashedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            hashedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
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
            cv.put(COLUMN_PASSWORD, hashedPassword + ":" + saltStr); // Αποθηκεύουμε το hashed password και το salt μαζί
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }

        long insert = db.insert(USER_TABLE, null, cv);
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

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") User user = new User(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD))
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }


    // Κώδικας για την εισαγωγή μιας επιλεγμένης εφαρμογής με συγκεκριμένο user_id
    public boolean addSelectedAppWithUserId(AppsInfo appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppName());
        values.put(COLUMN_APP_LINK, appInfo.getAppLink());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        values.put(COLUMN_IS_SELECTED, 1);  // Θέστε το isSelected σε 1 για τις επιλεγμένες εφαρμογές
        values.put("user_id", userId);  // Ορίστε το user_id για την επιλεγμένη εφαρμογή

        long insert = db.insert(TABLE_APPS_INFO, null, values);
        db.close();

        // Επιστρέφει true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }


    public User getUserByUsername(String username) {
        User user = null;
        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);


            int id = cursor.getInt(idColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String password = cursor.getString(passwordColumnIndex);

            user = new User(id, username, email, password);
            cursor.close();
        }
        db.close();

        return user;
    }
    public String getUsernameByUserId(int userId) {
        String username = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USERNAME + " FROM " + USER_TABLE + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            }
            cursor.close();
        }
        db.close();
        return username;
    }

    public boolean addSelectedAppWithUserId(AppsObj app, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("app_name", app.getAppNames());
        values.put("app_link", app.getAppLinks());
        // Προσθέστε τα υπόλοιπα πεδία ανάλογα με τις ανάγκες σας

        long result = db.insert("selected_apps_table", null, values);
        db.close();

        return result != -1;
    }

    public int getUserIdByUsername(String username) {
        // Αρχικοποίηση με τιμή που υποδηλώνει ότι δεν βρέθηκε χρήστης
        int userId = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_ID);
                if (columnIndex != -1) {
                    userId = cursor.getInt(columnIndex);
                }
            }
            cursor.close();
        }
        db.close();
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
            // Χειριστείτε το σφάλμα κατάλληλα
        }

        db.update(USER_TABLE, values, COLUMN_EMAIL + " = ?", new String[]{email});
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


    ///////APP GETTERS AND SETTERS


    // Κώδικας για την εισαγωγή μιας επιλεγμένης εφαρμογής
    // Κώδικας για την εισαγωγή μιας επιλεγμένης εφαρμογής
    public boolean addSelectedApp(AppsInfo appInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppName());
        values.put(COLUMN_APP_LINK, appInfo.getAppLink());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        values.put(COLUMN_IS_SELECTED, 1);  // Θέστε το isSelected σε 1 για τις επιλεγμένες εφαρμογές

        long insert = db.insert(TABLE_APPS_INFO, null, values);
        db.close();

        // Επιστρέφει true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }

    // Κώδικας για την ανάκτηση όλων των επιλεγμένων εφαρμογών
    public List<AppsInfo> getAllSelectedApps() {
        List<AppsInfo> selectedApps = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_IS_SELECTED + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int appNameColumnIndex = cursor.getColumnIndex(COLUMN_APP_NAME);
                int appLinkColumnIndex = cursor.getColumnIndex(COLUMN_APP_LINK);
                int imageResourceColumnIndex = cursor.getColumnIndex(COLUMN_IMAGE_RESOURCE);

                do {
                    int id = cursor.getInt(idColumnIndex);
                    String appName = cursor.getString(appNameColumnIndex);
                    String appLink = cursor.getString(appLinkColumnIndex);
                    int imageResource = cursor.getInt(imageResourceColumnIndex);

                    AppsInfo appInfo = new AppsInfo(appName, appLink, imageResource);
                    appInfo.setId(id);
                    appInfo.setSelected(true);

                    selectedApps.add(appInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return selectedApps;
    }


    //  αφαιρεί όλες τις επιλεγμένες εφαρμογές
    public void removeAllSelectedApps() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS_INFO, COLUMN_IS_SELECTED + " = 1", null);
        db.close();
    }

    // Κώδικας για την εισαγωγή δεδομένων στον πίνακα app_info_table
    public boolean addAppInfo(AppsInfo appInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APP_NAME, appInfo.getAppName());
        cv.put(COLUMN_APP_LINK, appInfo.getAppLink());
        cv.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        // Μετατροπή της τιμής isSelected σε 1 ή 0.
        cv.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);

        long insert = db.insert(TABLE_APPS_INFO, null, cv);
        // Κλείνουμε τη βάση δεδομένων μετά την εισαγωγή.
        db.close();

        // Επιστρέφει true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }

    // Κώδικας για την ανάκτηση όλων των πληροφοριών εφαρμογών από τον πίνακα app_info_table
    public List<AppsInfo> getAllAppInfo() {
        List<AppsInfo> appInfoList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_APPS_INFO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

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

                    AppsInfo appInfo = new AppsInfo(appName, appLink, imageResource);
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


}