package com.example.passpal2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.passpal2.Data.Entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 1;

    // Σταθερές για το όνομα του πίνακα και των στηλών
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_LOGIN_DATE = "loginDate";
    public static final String COLUMN_LOGIN_TIME = "loginTime";

    public UserDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία του πίνακα χρηστών (users)
        db.execSQL("CREATE TABLE " + UserDB.TABLE_USERS + " (" +
                UserDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserDB.COLUMN_USERNAME + " TEXT, " +
                UserDB.COLUMN_EMAIL + " TEXT, " +
                UserDB.COLUMN_PASSWORD + " TEXT, " +
                UserDB.COLUMN_LOGIN_DATE + " TEXT, " +
                UserDB.COLUMN_LOGIN_TIME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Αν χρειαστεί να αναβαθμίσετε τη βάση δεδομένων σε μια νεότερη έκδοση,
        // μπορείτε να υλοποιήσετε την αναβάθμιση εδώ.
    }
    // Ελέγχος εάν ο χρήστης υπάρχει με βάση το email
    public boolean isUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean userExists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return userExists;
    }

    // Εισαγωγή χρήστη αν δεν υπάρχει ήδη
    public boolean addUserIfNotExists(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Έλεγχος εάν το email υπάρχει ήδη
        if (!isUserExists(user.getEmail())) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, user.getUsername());
            cv.put(COLUMN_EMAIL, user.getEmail());
            cv.put(COLUMN_PASSWORD, user.getPassword());
            cv.put(COLUMN_LOGIN_DATE, user.getLoginDate());
            cv.put(COLUMN_LOGIN_TIME, user.getLoginTime());

            long insert = db.insert(TABLE_USERS, null, cv);
            db.close();

            // Επιστροφή true αν η εισαγωγή ήταν επιτυχής.
            return insert != -1;
        } else {
            // Το email υπάρχει ήδη στη βάση δεδομένων.
            db.close();
            return false;
        }
    }
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { email });

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                int loginDateColumnIndex = cursor.getColumnIndex(COLUMN_LOGIN_DATE);
                int loginTimeColumnIndex = cursor.getColumnIndex(COLUMN_LOGIN_TIME);

                int id = cursor.getInt(idColumnIndex);
                String username = cursor.getString(usernameColumnIndex);
                String userEmail = cursor.getString(emailColumnIndex);
                String password = cursor.getString(passwordColumnIndex);
                String loginDate = cursor.getString(loginDateColumnIndex);
                String loginTime = cursor.getString(loginTimeColumnIndex);

                user = new User(id, username, userEmail, password, loginDate, loginTime);
            }

            cursor.close();
        }

        db.close();

        return user;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
                int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
                int emailColumnIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                int loginDateColumnIndex = cursor.getColumnIndex(COLUMN_LOGIN_DATE);
                int loginTimeColumnIndex = cursor.getColumnIndex(COLUMN_LOGIN_TIME);

                int id = cursor.getInt(idColumnIndex);
                String username = cursor.getString(usernameColumnIndex);
                String userEmail = cursor.getString(emailColumnIndex);
                String password = cursor.getString(passwordColumnIndex);
                String loginDate = cursor.getString(loginDateColumnIndex);
                String loginTime = cursor.getString(loginTimeColumnIndex);

                User user = new User(id, username, userEmail, password, loginDate, loginTime);
                userList.add(user);
            }
            cursor.close();
        }

        db.close();

        return userList;
    }

}
