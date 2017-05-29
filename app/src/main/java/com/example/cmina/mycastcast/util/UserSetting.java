package com.example.cmina.mycastcast.util;

import android.provider.BaseColumns;

/**
 * Created by cmina on 2017-02-21.
 */

public class UserSetting {

    public UserSetting() {

    }

    public static final class CreateDB implements BaseColumns {
        public static final String UNIQUE_ID = "uniqueID";
        public static final String ONLY_WIFI = "only_wifi";
        public static final String PUSH_NOTI = "push_noti";
       // public static final String FEEDLIST = "feedlist";
        public static final String _TABLENAME = "userSetting";
        public static final String _CREATE =
                "create table " +_TABLENAME +"("
                + UNIQUE_ID+ " text not null, "
                +ONLY_WIFI+" boolean not null, "
                +PUSH_NOTI+" boolean not null); ";
               // +FEEDLIST+" text not null);";
    }
}
