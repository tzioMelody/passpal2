package com.example.passpal2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.passpal2.Data.Entities.User;
import com.example.passpal2.Data.Entities.AppsInfo;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_LOGINDATE = "loginDate";
    public static final String COLUMN_LOGINTIME = "loginTime";
    public static final String TABLE_APPS_INFO = "app_info_table";
    public static final String COLUMN_APP_NAME = "AppName";
    public static final String COLUMN_APP_LINK = "AppLink";
    public static final String COLUMN_IMAGE_RESOURCE = "imageResource";
    public static final String COLUMN_IS_SELECTED = "isSelected";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "passpal.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTableStatement = "CREATE TABLE " + USER_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_LOGINDATE + " TEXT, " +
                COLUMN_LOGINTIME + " TEXT" +
                ")";
        db.execSQL(createUserTableStatement);

        String createAppsInfoTableStatement = "CREATE TABLE " + TABLE_APPS_INFO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_APP_NAME + " TEXT, " +
                COLUMN_APP_LINK + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                COLUMN_IS_SELECTED + " INTEGER" +
                ")";
        db.execSQL(createAppsInfoTableStatement);
    }

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

    // Κώδικας για τον αφαιρεί όλες τις επιλεγμένες εφαρμογές
    public void removeAllSelectedApps() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS_INFO, COLUMN_IS_SELECTED + " = 1", null);
        db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // Εδώ προσθέστε τον κώδικα για τη διαχείριση της αναβάθμισης
            switch (oldVersion) {
                case 1:
                    // Προσθέστε τον κώδικα για την αναβάθμιση από έκδοση 1 σε έκδοση 2 (παράδειγμα).
                    break;
                case 2:
                    // Προσθέστε τον κώδικα για την αναβάθμιση από έκδοση 2 σε έκδοση 3 (παράδειγμα).
                    break;
                // Συνεχίστε με τις άλλες περιπτώσεις για υπάρχουσες αναβαθμίσεις.

                default:
                    break;
            }
        }
    }

    // Κώδικας για την εισαγωγή του χρήστη στον πίνακα
    public boolean addOne(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, user.getUsername());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPassword());
        cv.put(COLUMN_LOGINDATE, user.getLoginDate());
        cv.put(COLUMN_LOGINTIME, user.getLoginTime());

        long insert = db.insert(USER_TABLE, null, cv);
        // Κλείνουμε τη βάση δεδομένων μετά την εισαγωγή.
        db.close();

        // Επιστρέφει true αν η εισαγωγή ήταν επιτυχής.
        return insert != -1;
    }
    // Ελέγχος εάν ο χρήστης υπάρχει με βάση το email
    public boolean isUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean userExists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return userExists;
    }
    // Κώδικας για την εισαγωγή χρήστη αν δεν υπάρχει ήδη
    public boolean addUserIfNotExists(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Έλεγχος εάν το email υπάρχει ήδη
        if (!isUserExists(user.getEmail())) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, user.getUsername());
            cv.put(COLUMN_EMAIL, user.getEmail());
            cv.put(COLUMN_PASSWORD, user.getPassword());
            cv.put(COLUMN_LOGINDATE, user.getLoginDate());

            long insert = db.insert(USER_TABLE, null, cv);
            db.close();

            // Επιστροφή true αν η εισαγωγή ήταν επιτυχής.
            return insert != -1;
        } else {
            // Το email υπάρχει ήδη στη βάση δεδομένων.
            db.close();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM " + USER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                int loginDateColumnIndex = cursor.getColumnIndex(COLUMN_LOGINDATE);
                int loginTimeColumnIndex = cursor.getColumnIndex(COLUMN_LOGINTIME);

                do {
                    int id = cursor.getInt(idColumnIndex);
                    String username = cursor.getString(usernameColumnIndex);
                    String email = cursor.getString(emailColumnIndex);
                    String password = cursor.getString(passwordColumnIndex);
                    String loginDate = cursor.getString(loginDateColumnIndex);
                    String loginTime = cursor.getString(loginTimeColumnIndex);

                    User user = new User(id, username, email, password, loginDate, loginTime);
                    userList.add(user);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        db.close();

        return userList;
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
    public User getUserByUsername(String username) {
        User user = null;
        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_USERNAME + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int loginDateColumnIndex = cursor.getColumnIndex(COLUMN_LOGINDATE);
            int loginTimeColumnIndex = cursor.getColumnIndex(COLUMN_LOGINTIME);

            int id = cursor.getInt(idColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String loginDate = cursor.getString(loginDateColumnIndex);
            String loginTime = cursor.getString(loginTimeColumnIndex);

            user = new User(id, username, email, password, loginDate, loginTime);

            cursor.close();
        }
        db.close();

        return user;
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
            int loginDateColumnIndex = cursor.getColumnIndex(COLUMN_LOGINDATE);
            int loginTimeColumnIndex = cursor.getColumnIndex(COLUMN_LOGINTIME);

            int id = cursor.getInt(idColumnIndex);
            String username = cursor.getString(usernameColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String loginDate = cursor.getString(loginDateColumnIndex);
            String loginTime = cursor.getString(loginTimeColumnIndex);

            user = new User(id, username, email, password, loginDate, loginTime);

            cursor.close();
        }
        db.close();

        return user;
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

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

}
