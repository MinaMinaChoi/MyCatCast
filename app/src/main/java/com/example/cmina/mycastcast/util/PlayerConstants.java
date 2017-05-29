package com.example.cmina.mycastcast.util;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by cmina on 2017-02-09.
 */

public class PlayerConstants {

    //List of Songs
    //  public static ArrayList<MediaItem> SONGS_LIST = new ArrayList<MediaItem>();
    public static ArrayList<RssItem> SONGS_LIST = new ArrayList<RssItem>();

    //public static ArrayList<RssItem> SONGS_LIST2 = new ArrayList<>()
    //song number which is playing right now from SONGS_LIST
    public static int SONG_NUMBER = 0;

    // 디비선택해서 리스트뷰에 뿌려주기
    public static int CAST_DB_NUMBER = 0;

    //이전곡으로도 load more 하기 위해
    public static int loadingCount = 0;

    public static int allPlayCount = 0;

    //셔플여부. 1이면 전체반복., 2이면 한곡반복,
    public static int repeat = 1;

    //타이머
    public static int timer = 0;

    public static boolean timerUse = false;

    //재생속도
    public static int speed = 0;

    public static String NOW_SONG_URL = null;

    //song is playing or paused
    public static boolean SONG_PAUSED = true;

    //song changed (next, previous)
    public static boolean SONG_CHANGED = false;

    //handler for song changed(next, previous) defined in service(SongService)
    public static Handler SONG_CHANGE_HANDLER;

    //handler for song play/pause defined in service(SongService)
    public static Handler PLAY_PAUSE_HANDLER;

    //handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
    public static Handler PROGRESSBAR_HANDLER;

    public static Handler LOAD_MORE_HANDLER;

    public static Handler TIMER_HANDLER;

    public static final String WIFI_STATE = "WIFI";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";

   // public static String playlistString = null;

    public static Cursor cursor;
    public static Cursor playlistCursor;

   // public static int PlayCase = 0; //0이면 일반, 1이면 재생리스트에서

}
