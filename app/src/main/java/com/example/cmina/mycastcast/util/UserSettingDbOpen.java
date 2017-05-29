package com.example.cmina.mycastcast.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by cmina on 2017-02-21.
 */

public class UserSettingDbOpen {
    private static final String DATABASE_NAME = "userSetting.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;
    private DataBaseHelper dataBaseHelper;
    private Context context;

    public UserSettingDbOpen(Context context) {
        this.context = context;
    }

    private class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(UserSetting.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+UserSetting.CreateDB._TABLENAME);
        }
    }

    public UserSettingDbOpen open() throws SQLException {
        dataBaseHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = dataBaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        database.close();
    }

    public long insertColumn(String uniqueId, boolean pushNoti, boolean onlywifi) {
        ContentValues values = new ContentValues();
        values.put(UserSetting.CreateDB.UNIQUE_ID, uniqueId);
        values.put(UserSetting.CreateDB.PUSH_NOTI, pushNoti);
        values.put(UserSetting.CreateDB.ONLY_WIFI, onlywifi);
        return database.insert(UserSetting.CreateDB._TABLENAME, null, values);
    }

    public void updateWifi(String uniqueId, boolean onlywifi) {

        database.execSQL("UPDATE "+UserSetting.CreateDB._TABLENAME+" SET "+UserSetting.CreateDB.ONLY_WIFI+"='"+onlywifi+"' WHERE "
        +UserSetting.CreateDB.UNIQUE_ID+"='"+uniqueId+"'");
        //database.close();
    }

    public void updatePush(String uniqueId, boolean pushNoti) {

        database.execSQL("UPDATE "+UserSetting.CreateDB._TABLENAME+" SET push_noti='"+pushNoti+"' WHERE "
                +UserSetting.CreateDB.UNIQUE_ID+"='"+uniqueId+"'");
       // database.close();
    }

    public Cursor getWIFI(String uniqueId) {
        Cursor c = database.rawQuery("Select * from "+UserSetting.CreateDB._TABLENAME+" where "+
                UserSetting.CreateDB.UNIQUE_ID+"='"+uniqueId+"'", null);
        return c;
    }

    public Cursor getPush(String uniqueId) {
        Cursor c = database.rawQuery("Select * from "+UserSetting.CreateDB._TABLENAME+" where "+
                UserSetting.CreateDB.UNIQUE_ID+"='"+uniqueId+"'", null);
        return c;
    }

    public Cursor getUniqueID(String uniqueId) {
        Cursor c = database.rawQuery("Select * from "+
        UserSetting.CreateDB._TABLENAME+" WHERE "+ UserSetting.CreateDB.UNIQUE_ID+"='"+uniqueId+"'", null);
        return c;
    }
}
