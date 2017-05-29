package com.example.cmina.mycastcast.util;

import android.provider.BaseColumns;

/**
 * Created by cmina on 2017-02-10.
 */

public class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String UNIQUE_ID = "uniqueID";
        public static final String CAST_DB_NAME = "castdbname";
        public static final String CAST_TITLE = "casttitle";
        public static final String CAST_IMAGE = "castimage";
        public static final String CAST_FEED = "castfeed";
        public static final String CATEGORY = "category";
        public static final String _TABLENAME = "feedlist";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                        + UNIQUE_ID + " text not null, "
                        + CAST_DB_NAME + " text not null,  "
                        + CAST_TITLE + " text not null, "
                        + CAST_IMAGE + " text not null, "
                        + CATEGORY +" text not null, "
                        //+ "feedcount integer not null, "*/
                        + CAST_FEED + " boolean not null);";
    }

}

