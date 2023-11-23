package com.example.passpal2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.passpal2.Data.Entities.AppsInfo;

import java.util.ArrayList;
import java.util.List;

public class AppsInfoDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "apps.db";
    private static final int DATABASE_VERSION = 1;

    // Σταθερές για το όνομα του πίνακα και των στηλών
    public static final String TABLE_APP_INFO = "app_info_table";
    public static final String COLUMN_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    public static final String COLUMN_APP_NAME = "appName";
    public static final String COLUMN_APP_LINK = "appLink";
    public static final String COLUMN_IMAGE_RESOURCE = "imageResource";
    public static final String COLUMN_IS_SELECTED = "isSelected";

    public AppsInfoDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία του πίνακα εφαρμογών (app_info_table)
        db.execSQL("CREATE TABLE " + TABLE_APP_INFO + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_USER_ID + " INTEGER," +
                COLUMN_APP_NAME + " TEXT, " +
                COLUMN_APP_LINK + " TEXT, " +
                COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                COLUMN_IS_SELECTED + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Εδώ μπορείτε να προσθέσετε λογική αναβάθμισης της βάσης δεδομένων σας,
        // όπως το DROP του πίνακα και τη δημιουργία του ξανά.
    }

    // Μέθοδος για εισαγωγή νέας εφαρμογής
    public void addAppInfo(AppsInfo appInfo, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userId);
        values.put(COLUMN_APP_NAME, appInfo.getAppName());
        values.put(COLUMN_APP_LINK, appInfo.getAppLink());
        values.put(COLUMN_IMAGE_RESOURCE, appInfo.getImageResource());
        values.put(COLUMN_IS_SELECTED, appInfo.isSelected() ? 1 : 0);

        db.insert(TABLE_APP_INFO, null, values);
        db.close();
    }


    // Μέθοδος για ανάκτηση όλων των εφαρμογών για έναν συγκεκριμένο χρήστη
    public List<AppsInfo> getAllAppsForUser(int userId) {
        List<AppsInfo> appList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_APP_INFO, null, KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                AppsInfo appInfo = new AppsInfo();
                appInfo.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                appInfo.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                appInfo.setAppName(cursor.getString(cursor.getColumnIndex(COLUMN_APP_NAME)));
                appInfo.setAppLink(cursor.getString(cursor.getColumnIndex(COLUMN_APP_LINK)));
                appInfo.setImageResource(cursor.getInt(cursor.getColumnIndex(COLUMN_IMAGE_RESOURCE)));
                appInfo.setSelected(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SELECTED)) == 1);

                appList.add(appInfo);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();
        return appList;
    }
}
