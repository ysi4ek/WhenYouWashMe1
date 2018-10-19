package com.example.jek.whenyouwashme.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.jek.whenyouwashme.model.firebase.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jek on 15.06.2017.
 */

public class MessageDataSource {
    private MessageSQLiteHelper messageSQLiteHelper;
    private SQLiteDatabase sqLiteDatabase;


    public MessageDataSource(Context context) {
        messageSQLiteHelper = new MessageSQLiteHelper(context);
    }

    public void open(){
        sqLiteDatabase = messageSQLiteHelper.getWritableDatabase();
    }

    public void close(){
        messageSQLiteHelper.close();
    }

    public void addMessage(Message message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageSQLiteHelper.COLUMN_TIME, message.getId());
        contentValues.put(MessageSQLiteHelper.COLUMN_MESSAGE, message.getMessage());
        contentValues.put(MessageSQLiteHelper.COLUMN_TITLE, message.getTitle());
        sqLiteDatabase.insert(MessageSQLiteHelper.TABLE_NAME, null, contentValues);
    }

    public void delMessage(){

    }

    public List<Message> getAllMessages(){
        List<Message> messages = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(MessageSQLiteHelper.TABLE_NAME,
                new String[] {MessageSQLiteHelper.COLUMN_TIME,
                        MessageSQLiteHelper.COLUMN_MESSAGE,
                        MessageSQLiteHelper.COLUMN_TITLE},
                null, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Message message = new Message();
            message.setTime(String.valueOf(cursor.getLong(0)));
            message.setMessage(cursor.getString(1));
            message.setTitle(cursor.getString(2));
            message.setId(cursor.getLong(0));
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }
}
