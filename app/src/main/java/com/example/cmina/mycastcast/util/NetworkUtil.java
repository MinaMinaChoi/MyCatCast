package com.example.cmina.mycastcast.util;

import android.os.Build;
import android.os.StrictMode;

/**
 * Created by cmina on 2017-02-09.
 */

public class NetworkUtil {
    static public void setNetworkPolicy() {
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
}
