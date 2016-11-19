package com.netroby.daylove.android.daylove.common;

/**
 * Created by huzhifeng on 16-11-19.
 */

public class Token {
    public static String TOKEN = "";
    public static String  get() {
        return TOKEN;
    }

    public  static  boolean set(String token) {
        TOKEN = token;
        return true;
    }
}
