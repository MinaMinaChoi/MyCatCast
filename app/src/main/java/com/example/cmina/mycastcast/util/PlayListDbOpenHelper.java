package com.example.cmina.mycastcast.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.name;
import static com.example.cmina.mycastcast.util.DbOpenHelper.database;

/**
 * Created by cmina on 2017-03-02.
 */

public class PlayListDbOpenHelper {

    private static final String DATABASE_NAME = "playlist.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase playlistDatabase;
    private DataBaseHelper dataBaseHelper;
    private Context context;

    public PlayListDbOpenHelper(Context context) {
        this.context = context;
    }

    private class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PlayListDataBase.CreateDB._CREATE);
            Log.d("PlaylistDbhelper", "oncreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+PlayListDataBase.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public PlayListDbOpenHelper open() throws SQLException {
        dataBaseHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        playlistDatabase = dataBaseHelper.getWritableDatabase();
        return this;
    }

    //에피소드 타이틀로 검색
    public Cursor getMatchEpiTitle(String name) {
        name = name.replace("'", "\'");
        name = name.replace('"', '\"');
        Cursor c = playlistDatabase.rawQuery("Select * from playlist where episodeTitle='"+name+"'", null);
        return c;
    }

    public void close() {
        playlistDatabase.close();
    }

    public long insertColumn(String uniqueID, String castTitle, String castImage, String musicUrl, String episodeTitle, boolean listadd) {

        episodeTitle = episodeTitle.replace("'", "\'");
        episodeTitle = episodeTitle.replace('"', '\"');

        castTitle = castTitle.replace("'", "\'");
        castTitle = castTitle.replace('"', '\"');

        ContentValues values = new ContentValues();
        values.put(PlayListDataBase.CreateDB.UNIQUE_ID, uniqueID);
        values.put(PlayListDataBase.CreateDB.CAST_TITLE, castTitle);
        values.put(PlayListDataBase.CreateDB.CAST_IMAGE, castImage);
        values.put(PlayListDataBase.CreateDB.MUSIC_URL, musicUrl);
        values.put(PlayListDataBase.CreateDB.EPISODE_TITLE, episodeTitle);
        values.put(PlayListDataBase.CreateDB.PLAY_LIST, listadd);


        return playlistDatabase.insert(PlayListDataBase.CreateDB._TABLENAME, null, values);
    }

    public boolean deleteColumn(String episodeTitle) {
        episodeTitle = episodeTitle.replace("'", "\'");
        episodeTitle = episodeTitle.replace('"', '\"');
        return playlistDatabase.delete(PlayListDataBase.CreateDB._TABLENAME, "episodeTitle='"+episodeTitle+"'", null)>0;
    }

    public void deleteAll() {
        playlistDatabase.delete(PlayListDataBase.CreateDB._TABLENAME, null, null);
    }

    public Cursor getPlayList(String unigueID) {
        Cursor cursor = playlistDatabase.rawQuery("Select * from playlist where uniqueID ='"+unigueID+"'", null);
        return cursor;
    }

}
