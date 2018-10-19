package com.example.jek.whenyouwashme.model.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jek on 15.06.2017.
 */

public class MessageSQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.messages";
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TITLE = "title";

    public MessageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public MessageSQLiteHelper(Context context, String name,
                               SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MessageSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDbStatement = "create table "
                + TABLE_NAME + "( " + COLUMN_TIME
                + " integer primary key, " + COLUMN_MESSAGE
                + " text not null, " + COLUMN_TITLE
                + " text not null);";
        db.execSQL(createDbStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
