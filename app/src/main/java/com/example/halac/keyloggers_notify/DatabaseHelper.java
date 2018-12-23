package com.example.halac.keyloggers_notify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
/*Saving the registration Info and the Phone's activity data(event Counts (clicks,scrolls, time spent...) in SQLite database locally
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    //registration data
    public final static String DATABASE_NAME = "User.db";
    public final static String TABLE_NAME = "user";
    public final static String COL_1 = "FirstName";
    public final static String COL_2 = "LastName";
    public final static String COL_3 = "age";
    public final static String COL_4 = "gender";

    //phone's activity data( type is: scrolls, clicks...)
    public final static String Extraction_Table = "Extraction";
    public final static String ECOL_1 = "Type";
    public final static String ECOL_2 = "Context";
    public final static String ECOL_3 = "date";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 23);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," + COL_1 + " TEXT," + COL_2 +
                " TEXT," + COL_3 + " TEXT," + COL_4 + " TEXT);");

        db.execSQL("create table " + Extraction_Table + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," + ECOL_1 + " TEXT," + ECOL_2 +
                " TEXT," + ECOL_3 + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Extraction_Table);
        onCreate(db);
    }

    //Inserting a new user
    public void insertUser(String firstName, String lastName, String age, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, firstName);
        contentValues.put(COL_2, lastName);
        contentValues.put(COL_3, age);
        contentValues.put(COL_4, gender);

        db.insert(TABLE_NAME, null, contentValues);
    }
    //get the data about the user
    public Users getUser() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Users u = null;
        if (cursor.moveToFirst()) {
            do {
                String firstName = cursor.getString(cursor.getColumnIndex(COL_1));
                String lastName = cursor.getString(cursor.getColumnIndex(COL_2));
                String age = cursor.getString(cursor.getColumnIndex(COL_3));
                String gender = cursor.getString(cursor.getColumnIndex(COL_4));
                u = new Users(firstName, lastName, age, gender);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return u;
    }

    public void deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }

    //Inserting the phone activity logs
    public void insertLog(String type, String context, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ECOL_1, type);
        contentValues.put(ECOL_2, context);
        contentValues.put(ECOL_3, date);
        db.insert(Extraction_Table, null, contentValues);
    }

    //Getting the Logs
    public List<Log> getLogs(String where) {
        String selectQuery = "SELECT * FROM " + Extraction_Table + " WHERE " + where;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Log> logs = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndex(ECOL_1));
                String context = cursor.getString(cursor.getColumnIndex(ECOL_2));
                String date = cursor.getString(cursor.getColumnIndex(ECOL_3));
                Log log = new Log();
                log.setType(type);
                log.setContext(context);
                log.setDateAndTime(date);
                logs.add(log);
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return logs;
    }
}