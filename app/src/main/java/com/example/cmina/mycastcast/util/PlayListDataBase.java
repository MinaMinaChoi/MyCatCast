package com.example.cmina.mycastcast.util;

import android.provider.BaseColumns;

public class PlayListDataBase {

    public static final class CreateDB implements BaseColumns {
        public static final String UNIQUE_ID = "uniqueID";
        // public static final String CAST_DB_NAME = "castdbname";
        public static final String CAST_TITLE = "casttitle";
        public static final String CAST_IMAGE = "castimage";
        public static final String MUSIC_URL = "musicUrl";
        public static final String EPISODE_TITLE = "episodeTitle";
        public static final String PLAY_LIST = "addList";
        public static final String _TABLENAME = "playlist";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                        + UNIQUE_ID + " text not null, "
                        + CAST_TITLE + " text not null, "
                        + CAST_IMAGE + " text not null, "
                        + MUSIC_URL + " text not null, "
                        + EPISODE_TITLE + " text not null,"
                        + PLAY_LIST + " boolean not null); ";
    }

}
