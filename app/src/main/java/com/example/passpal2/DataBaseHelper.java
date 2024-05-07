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

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "passpal.db";

    // User Table Columns
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_MASTER_PASSWORD = "master_password";


    // App Info Table Columns
    public static final String TABLE_APPS_INFO = "app_info_table";
    public static final String COLUMN_APP_NAME = "AppName";
    public static final String COLUMN_APP_LINK = "AppLink";
    public static final String COLUMN_IMAGE_RESOURCE = "imageResource";
    public static final String COLUMN_APP_IMAGE_URI = "AppImageUri";
    public static final String COLUMN_IS_SELECTED = "isSelected";

    // Constants for App Credentials Table Columns
    public static final String TABLE_APP_CREDENTIALS = "app_credentials"; // Added constant for the table name
    public static final String COLUMN_USER_ID = "user_id"; // Use consistent naming for columns
    public static final String COLUMN_APP_NAME_CREDENTIALS = "app_name";
    public static final String COLUMN_APP_LINK_CREDENTIALS = "app_link";
    public static final String COLUMN_USERNAME_CREDENTIALS = "username";
    public static final String COLUMN_EMAIL_CREDENTIALS = "email";
    public static final String COLUMN_PASSWORD_CREDENTIALS = "password";
    public static final String COLUMN_IMAGE_URI_STRING = "image_uri_string";



    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String password;
        private String masterPassword;


        // Κατασκευαστής
        public User(int id, String username, String email, String password) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.masterPassword = masterPassword;

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
        public String getMasterPassword() {return masterPassword;}
        public void setMasterPassword(String masterPassword) {this.masterPassword = masterPassword;}


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


    @Override
        public void onCreate(SQLiteDatabase db) {
            String createUserTableStatement = "CREATE TABLE " + USER_TABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_MASTER_PASSWORD + " TEXT)";

            String createAppsInfoTableStatement = "CREATE TABLE " + TABLE_APPS_INFO + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_APP_NAME + " TEXT, " +
                    COLUMN_APP_LINK + " TEXT, " +
                    COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                    COLUMN_APP_IMAGE_URI + " TEXT, " +
                    COLUMN_IS_SELECTED + " INTEGER, " +
                    "user_id INTEGER, " +
                    "FOREIGN KEY(user_id) REFERENCES " + USER_TABLE + "(" + COLUMN_ID + "))";

        String CREATE_APP_CREDENTIALS_TABLE = "CREATE TABLE " + TABLE_APP_CREDENTIALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_APP_NAME_CREDENTIALS + " TEXT, "
                + COLUMN_APP_LINK_CREDENTIALS + " TEXT, "
                + COLUMN_USERNAME_CREDENTIALS + " TEXT, "
                + COLUMN_EMAIL_CREDENTIALS + " TEXT, "
                + COLUMN_PASSWORD_CREDENTIALS + " TEXT, "
                + COLUMN_IMAGE_URI_STRING + " TEXT" + ")";

        db.execSQL(createUserTableStatement);
        db.execSQL(createAppsInfoTableStatement);
        db.execSQL(CREATE_APP_CREDENTIALS_TABLE);

        // Προσθήκη αρχικών δεδομένων για τον χρήστη
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "demoUser");
        userValues.put(COLUMN_EMAIL, "demo@example.com");

        try {
            byte[] salt = DataBaseHelper.generateSalt();
            String hashedPassword = DataBaseHelper.hashPassword("demoPassword123", salt);
            userValues.put(COLUMN_PASSWORD, hashedPassword + ":" + DataBaseHelper.encodeSalt(salt));
            db.insert(USER_TABLE, null, userValues);
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // Προσθήκη αρχικών δεδομένων για μια εφαρμογή
        ContentValues appValues = new ContentValues();
        appValues.put(COLUMN_APP_NAME, "Demo App");
        appValues.put(COLUMN_APP_LINK, "https://demoapp.com");
        appValues.put(COLUMN_IMAGE_RESOURCE, R.drawable.applogomain);
        appValues.put(COLUMN_IS_SELECTED, 1); // Επιλεγμένη εφαρμογή
        appValues.put("user_id", 1);
        db.insert(TABLE_APPS_INFO, null, appValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            String addUsernameColumn = "ALTER TABLE " + TABLE_APPS_INFO + " ADD COLUMN app_username TEXT";
            String addEmailColumn = "ALTER TABLE " + TABLE_APPS_INFO + " ADD COLUMN app_email TEXT";
            String addPasswordColumn = "ALTER TABLE " + TABLE_APPS_INFO + " ADD COLUMN app_password TEXT";
            db.execSQL(addUsernameColumn);
            db.execSQL(addEmailColumn);
            db.execSQL(addPasswordColumn);

        }
    }

    public void addUserApp(AppsObj userApp, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, userApp.getAppNames());
        values.put(COLUMN_APP_LINK, userApp.getAppLinks());
        values.put("user_id", userId);

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

    // Μέθοδος για το hashing του κωδικού με SHA-256
    public static String hashPassword(String passwordToHash, byte[] salt) throws NoSuchAlgorithmException {
        String hashedPassword = null;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] bytes = md.digest(passwordToHash.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        hashedPassword = sb.toString();
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
            cv.put(COLUMN_PASSWORD, hashedPassword + ":" + saltStr);
            cv.put(COLUMN_MASTER_PASSWORD, user.getMasterPassword());

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


    public boolean addSelectedAppWithUserId(AppsObj appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, appInfo.getAppNames());
        values.put(COLUMN_APP_LINK, appInfo.getAppLinks());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getAppImages());
        values.put(COLUMN_IS_SELECTED, 1);  //  isSelected σε 1 για τις επιλεγμένες εφαρμογές
        values.put("user_id", userId);  // το user_id για την επιλεγμένη εφαρμογή

        long insert = db.insert(TABLE_APPS_INFO, null, values);
        db.close();

        //true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }


    public static int getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_credentials", MODE_PRIVATE);
        return preferences.getInt("userId", -1); // Επιστρέφει -1 αν δεν βρεθεί τιμή
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
            int masterPasswordColumnIndex = cursor.getColumnIndex(COLUMN_MASTER_PASSWORD);


            int id = cursor.getInt(idColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String masterPassword = cursor.getString(masterPasswordColumnIndex);

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
        //αν εχει επιλεχθει η εφαρμογη ή οχι
        cv.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);

        // Ενημέρωση βάσει του appId και του userId
        int rowsAffected = db.update(TABLE_APPS_INFO, cv, COLUMN_ID + " = ? AND user_id = ?", new String[]{String.valueOf(appInfo.getId()), String.valueOf(userId)});
        db.close();

        return rowsAffected > 0;
    }


    public boolean checkUserLogin(String username, String password) {
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
            String storedPassword = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
            String[] parts = storedPassword.split(":");
            if (parts.length == 2) {
                String hash = parts[0];
                String salt = parts[1];
                try {
                    String hashedInputPassword = hashPassword(password, decodeSalt(salt));
                    if (hash.equals(hashedInputPassword)) {
                        isAuthenticated = true;
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        db.close();
        return isAuthenticated;
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

    public boolean saveAppCredentials(int appId, int userId, String username, String email, String password, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_APP_NAME_CREDENTIALS, username);
        cv.put(COLUMN_EMAIL_CREDENTIALS, email);
        cv.put(COLUMN_PASSWORD_CREDENTIALS, password);
        cv.put(COLUMN_APP_LINK_CREDENTIALS, link);

        // Ενημέρωση της εφαρμογής με βάση το appId
        int rowsAffected = db.update(TABLE_APP_CREDENTIALS, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(appId)});
        db.close();

        return rowsAffected > 0;
    }



    // Κώδικας για την ανάκτηση όλων των επιλεγμένων εφαρμογών
    public List<AppsObj> getAllSelectedApps(int userId) {
        List<AppsObj> selectedApps = new ArrayList<>();
        HashSet<String> addedApps = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_IS_SELECTED + " = 1 AND user_id = ?";
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


    //  αφαιρεί όλες τις επιλεγμένες εφαρμογές
    public void removeAllSelectedApps(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS_INFO, COLUMN_IS_SELECTED + " = 1 AND user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }


    public void deleteApp(String appName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Διαγραφή της εφαρμογής με βάση το όνομα, το userId και τη θέση
        db.delete(TABLE_APPS_INFO, COLUMN_APP_NAME + "=? AND user_id=? ", new String[]{appName, String.valueOf(userId)});
        db.close();
    }

    // Κώδικας για την εισαγωγή δεδομένων στον πίνακα app_credentials

    public boolean addAppCredential(AppCredentials credential) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", credential.getUserId());
        values.put("app_name", credential.getAppName());
        values.put("app_link", credential.getAppLink());
        values.put("username", credential.getUsername());
        values.put("email", credential.getEmail());
        values.put("password", credential.getPassword());
        values.put("image_uri_string", credential.getImageUriString());

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
        cv.put("user_id", userId); // Προσθήκη του πεδίου user_id

        long insert = db.insert(TABLE_APPS_INFO, null, cv);
        db.close();

        //  true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }


    // Κώδικας για την ανάκτηση όλων των πληροφοριών εφαρμογών από τον πίνακα app_info_table
    public List<AppsObj> getAllAppInfo(int userId) {
        List<AppsObj> appInfoList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_APPS_INFO + " WHERE user_id = ?";

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
        String query = "SELECT COUNT(*) FROM " + TABLE_APPS_INFO + " WHERE " + COLUMN_APP_NAME + "=? AND user_id=?";
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

    //νεα εφαρμογη
    public boolean addNewAppWithDetails(int userId, String appName, String appLink, String username, String email, String password, String imageUriString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APP_NAME, appName);
        cv.put(COLUMN_APP_LINK, appLink);
        cv.put("app_username", username);
        cv.put("app_email", email);
        cv.put("app_password", password);
        cv.put("user_id", userId);
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
        cv.put("user_id", userId);

        long result = db.insert(TABLE_APPS_INFO, null, cv);
        db.close();

      /*  if (userId == -1) {
            // false αν το userId δεν είναι έγκυρο.
            Log.e("DataBaseHelper", "Invalid User ID. Cannot save the app.");
            return false;
        }*/
        if (result == -1) {
            Log.e("DataBaseHelper", "Failed to insert app for User ID: " + userId);
            return false; //  εισαγωγή απετυχε
        } else {
            Log.d("DataBaseHelper", "App inserted successfully for User ID: " + userId);
            return true; //  εισαγωγή είναι επιτυχής
        }
    }



}