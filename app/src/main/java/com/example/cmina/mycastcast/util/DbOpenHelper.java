package com.example.cmina.mycastcast.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.cmina.mycastcast.R.id.castTitle;
import static com.example.cmina.mycastcast.R.id.casttitle;
import static com.example.cmina.mycastcast.R.id.episodeTitle;

/**
 * Created by cmina on 2017-02-10.
 */

public class DbOpenHelper {
    private static final String DATABASE_NAME = "feedlist.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase database;
    private DataBaseHelper dataBaseHelper;
    private Context context;

    public DbOpenHelper(Context context) {
        this.context = context;
    }


    private class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //최초 디비를 만들때만 호출.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);
            Log.e("DbOpenHelper", "onCreate");
        }

        //버전이 업데이트 되었을 때 디비를 다시 만들어주는 메소드
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper open() throws SQLException {
        dataBaseHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = dataBaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        database.close();
    }

    public long insertColumn(String uniqueID, String castdbname, String casttitle, String castimage, boolean castfeed, String category) {

        casttitle = casttitle.replace("'", "\'");
        casttitle = casttitle.replace('"', '\"');

        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.UNIQUE_ID, uniqueID);
        values.put(DataBases.CreateDB.CAST_DB_NAME, castdbname);
        values.put(DataBases.CreateDB.CAST_TITLE, casttitle);
        values.put(DataBases.CreateDB.CAST_IMAGE, castimage);
        values.put(DataBases.CreateDB.CAST_FEED, castfeed);
        values.put(DataBases.CreateDB.CATEGORY, category);
        return database.insert(DataBases.CreateDB._TABLENAME, null, values);
    }

    public boolean deleteColumn(String casttitle) {
        return database.delete(DataBases.CreateDB._TABLENAME, "casttitle='"+casttitle+"'", null) >0;
    }

    public void deleteAll() {
        database.delete(DataBases.CreateDB._TABLENAME, null, null);
    }

    //커서 전체를 선택하는 메소드
    public Cursor getAllColumns() {
        return database.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    //이름으로 검색
    public Cursor getMatchName(String name) {

        Cursor c = null;
        if(name.contains("'")) {
             c = database.rawQuery("Select * from feedlist where casttitle= \"" +name+'"', null);
        } else  {
             c = database.rawQuery("Select * from feedlist where casttitle= '"+name+"'", null);
        }

        return c;
    }

    public Cursor getFeedList(String uniqueID) {
        Cursor cursor = database.rawQuery("Select * from feedlist where uniqueID='"+uniqueID+"'", null);
        return cursor;
    }

}
