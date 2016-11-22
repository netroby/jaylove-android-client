package com.netroby.daylove.android.daylove.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huzhifeng on 16-11-19.
 */

public class Token {
    private static Token _instance;
    private SharedPreferences setting;
    private  Token(Context context) {
        this.setting = context.getSharedPreferences(SHARED_SETTING_TAG, 0);
    }

    public static Token getInstance(Context context)
    {
        if (_instance == null) {
            _instance = new Token(context);
        }
        return _instance;
    }

    private static final String SHARED_SETTING_TAG = "daylove.config";
    public  String  get() {
        return setting.getString("token", "");
    }

    public  boolean set(String token) {
        SharedPreferences.Editor edit = setting.edit();
        edit.putString("token", token);
        edit.apply();
        return true;
    }

    public boolean clear() {
        SharedPreferences.Editor edit = setting.edit();
        edit.putString("token", "");
        edit.apply();
        return true;
    }
}
