package com.example.ecotrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EcoTrack.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_LOGS = "action_logs";

    // Columns
    private static final String COL_DATE = "date"; // Format: YYYY-MM-DD
    private static final String COL_POINTS = "points";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table to store daily points
        String createTable = "CREATE TABLE " + TABLE_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT UNIQUE, " +
                COL_POINTS + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    // Add points to today
    public void addPoints(int points) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getTodayDate();

        // Try to update existing row first
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOGS + " WHERE " + COL_DATE + " = ?", new String[]{today});

        if (cursor.moveToFirst()) {
            int currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POINTS));
            ContentValues values = new ContentValues();
            values.put(COL_POINTS, currentPoints + points);
            db.update(TABLE_LOGS, values, COL_DATE + " = ?", new String[]{today});
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_DATE, today);
            values.put(COL_POINTS, points);
            db.insert(TABLE_LOGS, null, values);
        }
        cursor.close();
    }

    // Get today's points
    public int getTodayPoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDate();
        Cursor cursor = db.rawQuery("SELECT " + COL_POINTS + " FROM " + TABLE_LOGS + " WHERE " + COL_DATE + " = ?", new String[]{today});
        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        return points;
    }

    // Get total historical points
    public int getTotalPoints() {
            SQLiteDatabase db = this.getReadableDatabase();
            int total = 0;
            try {
                Cursor cursor = db.rawQuery("SELECT SUM(" + COL_POINTS + ") FROM " + TABLE_LOGS, null);
                if (cursor.moveToFirst()) {
                    total = cursor.getInt(0);
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace(); // Fail silently return 0
            }
            return total;
        }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Get points for the last 7 days (including today)
    public int[] getLast7DaysPoints() {
        int[] weeklyPoints = new int[7];
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // Loop backwards from today (index 6) to 6 days ago (index 0)
        for (int i = 6; i >= 0; i--) {
            String dateStr = sdf.format(calendar.getTime());
            Cursor cursor = null;
            try {
                cursor = db.rawQuery("SELECT " + COL_POINTS + " FROM " + TABLE_LOGS + " WHERE " + COL_DATE + " = ?", new String[]{dateStr});
                if (cursor.moveToFirst()) {
                    weeklyPoints[i] = cursor.getInt(0);
                } else {
                    weeklyPoints[i] = 0; // No data for this day
                }
            } catch (Exception e) {
                weeklyPoints[i] = 0;
            } finally {
                if (cursor != null) cursor.close();
            }
            // Move calendar back by one day
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return weeklyPoints;
    }
}
