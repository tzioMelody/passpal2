package com.example.passpal2;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private Context context;
    // Database Name and Version
    private static final String DATABASE_NAME = "password_manager.db";
    private static final int DATABASE_VERSION = 1;

    // User Table Columns
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_LAST_LOGIN = "last_login";

// App Info Table Columns
    public static final String TABLE_APPS_INFO = "app_info_table";
    public static final String COLUMN_APP_NAME = "AppName";
    public static final String COLUMN_APP_LINK = "AppLink";
    public static final String COLUMN_IMAGE_RESOURCE = "imageResource";
    public static final String COLUMN_APP_IMAGE_URI = "AppImageUri";
    public static final String COLUMN_IS_SELECTED = "isSelected";
    public static final String COLUMN_USER_ID = "user_id";

    // Constants for App Credentials Table Columns
    public static final String TABLE_APP_CREDENTIALS = "app_credentials";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_APP_NAME_CREDENTIALS = "app_name";
    public static final String COLUMN_APP_LINK_CREDENTIALS = "app_link";
    public static final String COLUMN_USERNAME_CREDENTIALS = "username";
    public static final String COLUMN_EMAIL_CREDENTIALS = "email";
    public static final String COLUMN_PASSWORD_CREDENTIALS = "password";
    public static final String COLUMN_IMAGE_URI_STRING = "image_uri_string";

    // Master Password Table Columns
    public static final String MASTER_PASSWORD_TABLE = "MASTER_PASSWORD_TABLE";
    public static final String COLUMN_MASTER_PASSWORD = "master_password";
    public static final String COLUMN_USERMASTERID = "user_id";


    // SQL Statements for Table Creation
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_LAST_LOGIN + " TEXT)";

    private static final String CREATE_APP_INFO_TABLE = "CREATE TABLE " + TABLE_APPS_INFO + " ("
            + COLUMN_APP_NAME + " TEXT, "
            + COLUMN_APP_LINK + " TEXT, "
            + COLUMN_IMAGE_RESOURCE + " INTEGER, "
            + COLUMN_APP_IMAGE_URI + " TEXT, "
            + COLUMN_IS_SELECTED + " INTEGER, "
            + COLUMN_USER_ID + " INTEGER)";

    private static final String CREATE_APP_CREDENTIALS_TABLE = "CREATE TABLE " + TABLE_APP_CREDENTIALS + " ("
            + COLUMN_USER_ID + " INTEGER, "
            + COLUMN_APP_NAME_CREDENTIALS + " TEXT, "
            + COLUMN_APP_LINK_CREDENTIALS + " TEXT, "
            + COLUMN_USERNAME_CREDENTIALS + " TEXT, "
            + COLUMN_EMAIL_CREDENTIALS + " TEXT, "
            + COLUMN_PASSWORD_CREDENTIALS + " TEXT, "
            + COLUMN_IMAGE_URI_STRING + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";

    private static final String CREATE_MASTER_PASSWORD_TABLE = "CREATE TABLE " + MASTER_PASSWORD_TABLE + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " INTEGER, "
            + COLUMN_MASTER_PASSWORD + " TEXT, "
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_APP_INFO_TABLE);
        db.execSQL(CREATE_APP_CREDENTIALS_TABLE);
        db.execSQL(CREATE_MASTER_PASSWORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_CREDENTIALS);
        db.execSQL("DROP TABLE IF EXISTS " + MASTER_PASSWORD_TABLE);
        onCreate(db);
    }

    // User class
    public static class User {
        private int id;
        private String username;
        private String email;
        private String password;
        private String lastLogin;

        public User(int id, String username, String email, String password, String lastLogin) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.lastLogin = lastLogin;
        }

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

        public String getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(String lastLogin) {
            this.lastLogin = lastLogin;
        }
    }

    // AppInfo class
    public static class AppInfo {
        private String appName;
        private String appLink;
        private int imageResource;
        private String appImageUri;
        private boolean isSelected;

        public AppInfo(String appName, String appLink, int imageResource, String appImageUri, boolean isSelected) {
            this.appName = appName;
            this.appLink = appLink;
            this.imageResource = imageResource;
            this.appImageUri = appImageUri;
            this.isSelected = isSelected;
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

        public int getImageResource() {
            return imageResource;
        }

        public void setImageResource(int imageResource) {
            this.imageResource = imageResource;
        }

        public String getAppImageUri() {
            return appImageUri;
        }

        public void setAppImageUri(String appImageUri) {
            this.appImageUri = appImageUri;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

    // AppCredentials class
    public static class AppCredentials {
        private int userId;
        private String appName;
        private String appLink;
        private String username;
        private String email;
        private String password;
        private String imageUriString;

        public AppCredentials(int userId, String appName, String appLink, String username, String email, String password, String imageUriString) {
            this.userId = userId;
            this.appName = appName;
            this.appLink = appLink;
            this.username = username;
            this.email = email;
            this.password = password;
            this.imageUriString = imageUriString;
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

    // MasterPassword class
    public static class MasterPassword {
        private String masterPassword;

        public MasterPassword(String masterPassword) {
            this.masterPassword = masterPassword;
        }

        public String getMasterPassword() {
            return masterPassword;
        }

        public void setMasterPassword(String masterPassword) {
            this.masterPassword = masterPassword;
        }
    }

    // Method to insert user
    public long insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(USER_TABLE, null, values);
    }

    // Method to check if username is taken
    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean isTaken = cursor.getCount() > 0;
        cursor.close();
        return isTaken;
    }

    // Method to check if email is taken
    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean isTaken = cursor.getCount() > 0;
        cursor.close();
        return isTaken;
    }

    // Μέθοδος για απόκτηση userId από το username
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, new String[]{COLUMN_ID}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            if (idIndex != -1) {
                int userId = cursor.getInt(idIndex);
                cursor.close();
                return userId;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }

    public static int getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_credentials", MODE_PRIVATE);
        return preferences.getInt("userId", -1); // Επιστρέφει -1 αν δεν βρεθεί τιμή
    }

    // Method to check if user exists by email
    public boolean isUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Method to add user
    public boolean addOne(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        long result = db.insert(USER_TABLE, null, values);
        return result != -1;
    }

    // Get user by username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int lastLoginIndex = cursor.getColumnIndex(COLUMN_LAST_LOGIN);

            if (idIndex != -1 && emailIndex != -1 && passwordIndex != -1 && lastLoginIndex != -1) {
                int id = cursor.getInt(idIndex);
                String email = cursor.getString(emailIndex);
                String password = cursor.getString(passwordIndex);
                String lastLogin = cursor.getString(lastLoginIndex);
                cursor.close();
                return new User(id, username, email, password, lastLogin);
            }
        }
        cursor.close();
        return null;
    }

    // Get user by ID
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, null, COLUMN_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int lastLoginIndex = cursor.getColumnIndex(COLUMN_LAST_LOGIN);

            if (usernameIndex != -1 && emailIndex != -1 && passwordIndex != -1 && lastLoginIndex != -1) {
                String username = cursor.getString(usernameIndex);
                String email = cursor.getString(emailIndex);
                String password = cursor.getString(passwordIndex);
                String lastLogin = cursor.getString(lastLoginIndex);
                cursor.close();
                return new User(userId, username, email, password, lastLogin);
            }
        }
        cursor.close();
        return null;
    }

    // Update user
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_LAST_LOGIN, user.getLastLogin());
        int result = db.update(USER_TABLE, values, COLUMN_ID + "=?", new String[]{String.valueOf(user.getId())});
        return result > 0;
    }

    // Delete user
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(USER_TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }
    // Μέθοδος για διαγραφή όλων των δεδομένων του χρήστη
    public boolean deleteUserData(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = true;

        // Διαγραφή από USER_TABLE
        int userResult = db.delete(USER_TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        success &= userResult > 0;

        // Διαγραφή από TABLE_APP_CREDENTIALS
        int credentialsResult = db.delete(TABLE_APP_CREDENTIALS, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        success &= credentialsResult > 0;

        // Διαγραφή από MASTER_PASSWORD_TABLE
        int masterPasswordResult = db.delete(MASTER_PASSWORD_TABLE, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        success &= masterPasswordResult > 0;


        return success;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                String lastLogin = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_LOGIN));
                User user = new User(id, username, email, password, lastLogin);
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }
    public void updatePasswordByEmail(Context context, String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Δημιουργία του hash για τον νέο κωδικό
        String hashedPassword;
        try {
            hashedPassword = PasswordUtil.createPasswordToStore(newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to reset password.", Toast.LENGTH_SHORT).show();
            return;
        }

        values.put(COLUMN_PASSWORD, hashedPassword);

        // Ενημέρωση του πεδίου κωδικού στον πίνακα χρηστών με το νέο hash κωδικού
        int rowsAffected = db.update(USER_TABLE, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();

        if (rowsAffected > 0) {
            // Ενημερώθηκε επιτυχώς
            Toast.makeText(context, "Password reset successful.", Toast.LENGTH_SHORT).show();

            // Μετάβαση στην κεντρική δραστηριότητα
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).finish();
        } else {
            // αν το email δεν βρέθηκε
            Toast.makeText(context, "Email not found. Please check and try again.", Toast.LENGTH_SHORT).show();
            Log.e("UpdatePassword", "Email not found: " + email);
        }
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, new String[]{COLUMN_EMAIL}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }


    // Insert app info
    public long insertAppInfo(AppInfo appInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppName());
        values.put(COLUMN_APP_LINK, appInfo.getAppLink());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        values.put(COLUMN_APP_IMAGE_URI, appInfo.getAppImageUri());
        values.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);
        return db.insert(TABLE_APPS_INFO, null, values);
    }

    // Get app info by name
    public AppInfo getAppInfoByName(String appName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPS_INFO, null, COLUMN_APP_NAME + "=?", new String[]{appName}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int appLinkIndex = cursor.getColumnIndex(COLUMN_APP_LINK);
            int imageResourceIndex = cursor.getColumnIndex(COLUMN_IMAGE_RESOURCE);
            int appImageUriIndex = cursor.getColumnIndex(COLUMN_APP_IMAGE_URI);
            int isSelectedIndex = cursor.getColumnIndex(COLUMN_IS_SELECTED);

            if (appLinkIndex != -1 && imageResourceIndex != -1 && appImageUriIndex != -1 && isSelectedIndex != -1) {
                String appLink = cursor.getString(appLinkIndex);
                int imageResource = cursor.getInt(imageResourceIndex);
                String appImageUri = cursor.getString(appImageUriIndex);
                boolean isSelected = cursor.getInt(isSelectedIndex) == 1;
                cursor.close();
                return new AppInfo(appName, appLink, imageResource, appImageUri, isSelected);
            }
        }
        cursor.close();
        return null;
    }

    // Update app info
    public boolean updateAppInfo(AppInfo appInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppName());
        values.put(COLUMN_APP_LINK, appInfo.getAppLink());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        values.put(COLUMN_APP_IMAGE_URI, appInfo.getAppImageUri());
        values.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);
        int result = db.update(TABLE_APPS_INFO, values, COLUMN_APP_NAME + "=?", new String[]{appInfo.getAppName()});
        return result > 0;
    }

    public boolean isAppSelected(String appName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APP_CREDENTIALS, null, COLUMN_APP_NAME_CREDENTIALS + "=? AND " + COLUMN_USER_ID + "=?", new String[]{appName, String.valueOf(userId)}, null, null, null);
        boolean isSelected = cursor.getCount() > 0;
        cursor.close();
        return isSelected;
    }

    public boolean saveSelectedAppToDatabase(AppsObj app, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_APP_NAME_CREDENTIALS, app.getAppNames());
        cv.put(COLUMN_APP_LINK_CREDENTIALS, app.getAppLinks());
        cv.put(COLUMN_USERNAME_CREDENTIALS, app.getUsername());
        cv.put(COLUMN_EMAIL_CREDENTIALS, app.getEmail());
        cv.put(COLUMN_PASSWORD_CREDENTIALS, app.getPassword());

        // Αν το appImages είναι 0, χρησιμοποιούμε το default_app_icon
        String appImageUri = app.getAppImages() != 0 ? "android.resource://" + context.getPackageName() + "/" + app.getAppImages() : "android.resource://" + context.getPackageName() + "/" + R.drawable.default_app_icon;

        cv.put(COLUMN_IMAGE_URI_STRING, appImageUri);

        long result = db.insert(TABLE_APP_CREDENTIALS, null, cv);
        return result != -1;
    }




    public boolean saveAppCredentials(int userId, String appName, String username, String email, String password, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_APP_NAME_CREDENTIALS, appName);
        cv.put(COLUMN_USERNAME_CREDENTIALS, username);
        cv.put(COLUMN_EMAIL_CREDENTIALS, email);
        cv.put(COLUMN_PASSWORD_CREDENTIALS, password);
        cv.put(COLUMN_APP_LINK_CREDENTIALS, link);

        // Ενημέρωση της εφαρμογής με βάση το appName και το userId
        int rowsAffected = db.update(TABLE_APP_CREDENTIALS, cv, COLUMN_USER_ID + "=? AND " + COLUMN_APP_NAME_CREDENTIALS + "=?", new String[]{String.valueOf(userId), appName});
        db.close();

        return rowsAffected > 0;
    }

    // Delete app info
    public boolean deleteAppInfo(String appName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APPS_INFO, COLUMN_APP_NAME + "=?", new String[]{appName});
        return result > 0;
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
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return selectedApps;
    }

    //Διαγραφή της επιλεγμένης εφαρμογής
    public boolean deleteApp(String appName, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APP_CREDENTIALS, COLUMN_APP_NAME_CREDENTIALS + "=? AND " + COLUMN_USER_ID + "=?", new String[]{appName, String.valueOf(userId)});
        return result > 0;
    }

    // Insert app credentials
    public long insertAppCredentials(AppCredentials appCredentials) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, appCredentials.getUserId());
        values.put(COLUMN_APP_NAME_CREDENTIALS, appCredentials.getAppName());
        values.put(COLUMN_APP_LINK_CREDENTIALS, appCredentials.getAppLink());
        values.put(COLUMN_USERNAME_CREDENTIALS, appCredentials.getUsername());
        values.put(COLUMN_EMAIL_CREDENTIALS, appCredentials.getEmail());
        values.put(COLUMN_PASSWORD_CREDENTIALS, appCredentials.getPassword());
        values.put(COLUMN_IMAGE_URI_STRING, appCredentials.getImageUriString());
        return db.insert(TABLE_APP_CREDENTIALS, null, values);
    }

    // Get app credentials by user ID
    public List<AppCredentials> getAppCredentialsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<AppCredentials> credentialsList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_APP_CREDENTIALS, null, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int appNameIndex = cursor.getColumnIndex(COLUMN_APP_NAME_CREDENTIALS);
                int appLinkIndex = cursor.getColumnIndex(COLUMN_APP_LINK_CREDENTIALS);
                int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME_CREDENTIALS);
                int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL_CREDENTIALS);
                int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD_CREDENTIALS);
                int imageUriStringIndex = cursor.getColumnIndex(COLUMN_IMAGE_URI_STRING);

                if (appNameIndex != -1 && appLinkIndex != -1 && usernameIndex != -1 && emailIndex != -1 && passwordIndex != -1 && imageUriStringIndex != -1) {
                    String appName = cursor.getString(appNameIndex);
                    String appLink = cursor.getString(appLinkIndex);
                    String username = cursor.getString(usernameIndex);
                    String email = cursor.getString(emailIndex);
                    String password = cursor.getString(passwordIndex);
                    String imageUriString = cursor.getString(imageUriStringIndex);
                    credentialsList.add(new AppCredentials(userId, appName, appLink, username, email, password, imageUriString));
                }
            }
            cursor.close();
        }
        return credentialsList;
    }

    // Update app credentials
    public boolean updateAppCredentials(AppCredentials appCredentials) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, appCredentials.getUserId());
        values.put(COLUMN_APP_NAME_CREDENTIALS, appCredentials.getAppName());
        values.put(COLUMN_APP_LINK_CREDENTIALS, appCredentials.getAppLink());
        values.put(COLUMN_USERNAME_CREDENTIALS, appCredentials.getUsername());
        values.put(COLUMN_EMAIL_CREDENTIALS, appCredentials.getEmail());
        values.put(COLUMN_PASSWORD_CREDENTIALS, appCredentials.getPassword());
        values.put(COLUMN_IMAGE_URI_STRING, appCredentials.getImageUriString());
        int result = db.update(TABLE_APP_CREDENTIALS, values, COLUMN_USER_ID + "=? AND " + COLUMN_APP_NAME_CREDENTIALS + "=?", new String[]{String.valueOf(appCredentials.getUserId()), appCredentials.getAppName()});
        return result > 0;
    }

    // Delete app credentials
    public boolean deleteAppCredentials(int userId, String appName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_APP_CREDENTIALS, COLUMN_USER_ID + "=? AND " + COLUMN_APP_NAME_CREDENTIALS + "=?", new String[]{String.valueOf(userId), appName});
        return result > 0;
    }

    // Insert master password
    public long insertMasterPassword(int userId, String masterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MASTER_PASSWORD, masterPassword);
        return db.insert(MASTER_PASSWORD_TABLE, null, values);
    }

    // Get master password
    public String getMasterPassword(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MASTER_PASSWORD_TABLE, new String[]{COLUMN_MASTER_PASSWORD}, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int masterPasswordIndex = cursor.getColumnIndex(COLUMN_MASTER_PASSWORD);
            if (masterPasswordIndex != -1) {
                String masterPassword = cursor.getString(masterPasswordIndex);
                cursor.close();
                return masterPassword;
            }
        }
        cursor.close();
        return null;
    }

    // Update master password
    public boolean updateMasterPassword(String masterPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MASTER_PASSWORD, masterPassword);
        int result = db.update(MASTER_PASSWORD_TABLE, values, null, null);
        return result > 0;
    }

    // Delete master password
    public boolean deleteMasterPassword() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(MASTER_PASSWORD_TABLE, null, null);
        return result > 0;
    }
    // Μέθοδος για έλεγχο αν το username και το password ταιριάζουν
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, new String[]{COLUMN_PASSWORD}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (passwordIndex != -1) {
                String storedPassword = cursor.getString(passwordIndex);
                cursor.close();

                // Split the stored password into hashed password and salt
                String[] parts = storedPassword.split(":");
                String hashedPassword = parts[0];
                String salt = parts[1];

                try {
                    // Check if the hashed password matches
                    boolean isPasswordCorrect = PasswordUtil.hashPassword(password, PasswordUtil.decodeSalt(salt)).equals(hashedPassword);
                    Log.d("checkUser", "Password match: " + isPasswordCorrect);
                    return isPasswordCorrect;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                cursor.close();
            }
        }
        return false;
    }


    public void updateLastLogin(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, System.currentTimeMillis());
        db.update(USER_TABLE, values, COLUMN_USERNAME + "=?", new String[]{username});
    }
    // Method to check if master password exists for a user
    public boolean hasMasterPassword(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MASTER_PASSWORD_TABLE, new String[]{COLUMN_MASTER_PASSWORD}, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        boolean hasMasterPassword = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return hasMasterPassword;
    }

}
