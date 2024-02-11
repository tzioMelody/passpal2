package com.example.passpal2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        db.execSQL("CREATE TABLE " + AppsInfoDB.TABLE_APP_INFO + " (" +
                AppsInfoDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AppsInfoDB.KEY_USER_ID + " INTEGER," +
                AppsInfoDB.COLUMN_APP_NAME + " TEXT, " +
                AppsInfoDB.COLUMN_APP_LINK + " TEXT, " +
                AppsInfoDB.COLUMN_IMAGE_RESOURCE + " INTEGER, " +
                AppsInfoDB.COLUMN_IS_SELECTED + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Εδώ μπορείτε να προσθέσετε λογική αναβάθμισης της βάσης δεδομένων σας,
        // όπως το DROP του πίνακα και τη δημιουργία του ξανά.
    }
    public void addUserApp(AppsObj.UserApp userApp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, userApp.getAppName());
        values.put(COLUMN_APP_LINK, userApp.getAppLink());
        // Μπορείτε να προσθέσετε εδώ περαιτέρω στήλες ανάλογα με την ανάγκη

        db.insert(TABLE_APP_INFO, null, values);
        db.close();
    }

}
