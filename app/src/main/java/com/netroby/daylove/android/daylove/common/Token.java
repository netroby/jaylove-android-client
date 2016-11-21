package com.netroby.daylove.android.daylove.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huzhifeng on 16-11-19.
 */

public class Token {
    private  Context context;
    public  Token(Context context) {
        this.context = context;
    }
    private static final String SHARED_SETTING_TAG = "daylove.config";
    public  String  get() {
        SharedPreferences setting = context.getSharedPreferences(SHARED_SETTING_TAG, 0);
        return setting.getString("token", "");
    }

    public  boolean set(String token) {
        SharedPreferences setting = context.getSharedPreferences(SHARED_SETTING_TAG, 0);
        SharedPreferences.Editor edit = setting.edit();
        edit.putString("token", token);
        edit.apply();
        return true;
    }
}
