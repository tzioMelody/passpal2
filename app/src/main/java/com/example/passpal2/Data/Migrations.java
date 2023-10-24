package com.example.passpal2.Data;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Εκτελέστε τα SQL εδώ για τη μετάβαση από έκδοση 1 σε έκδοση 2 για τον πίνακα users
            // Παράδειγμα:
             database.execSQL("ALTER TABLE users ADD COLUMN new_column_name TEXT");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Εκτελέστε τα SQL εδώ για τη μετάβαση από έκδοση 3 σε έκδοση 4 για τον πίνακα app_info_table
            // Παράδειγμα:
            database.execSQL("ALTER TABLE app_info_table ADD COLUMN new_column_name TEXT");
        }
    };
}
