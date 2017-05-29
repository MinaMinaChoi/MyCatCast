package com.example.cmina.mycastcast.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by cmina on 2017-02-09.
 */

public class SaveSharedPreference {

    static final String PREF_USER_NAME = "username";
    static final String PREF_USER_IMAGE = "userimage";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserName(Context context, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    //유니크한 값..
    public static void setUserUnique (Context context, String unique, String logincase) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("unique", unique);
        editor.putString("logincase", logincase);
        editor.commit();
    }


    public static String getUserUnique(Context context) {
        return getSharedPreferences(context).getString("unique", "");
    }

    public static String getLoginCase (Context context) {
        return getSharedPreferences(context).getString("logincase", "");
    }

    public static void setUserImage(Context context, String userImage) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_IMAGE, userImage);
        editor.commit();
    }

    public static String getUserID(Context context) {
        return getSharedPreferences(context).getString("user_id", "");
    }
    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_NAME, "");
    }

    public static String getUserImage(Context context ) {
        return getSharedPreferences(context).getString(PREF_USER_IMAGE, "");
    }

    public static String getUserEmail(Context context) {
        return getSharedPreferences(context).getString("user_email", "");
    }

    public static void clearUserInfo (Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }


}
