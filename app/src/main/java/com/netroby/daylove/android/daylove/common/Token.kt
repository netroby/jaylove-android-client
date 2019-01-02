package com.netroby.daylove.android.daylove.common

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by huzhifeng on 16-11-19.
 */

object Token {
    private const val KEY: String = "token"
    fun get(): String {
        return LocalStorage.get(KEY)
    }

    fun set(token: String): Boolean {
        LocalStorage.set(KEY, token)
        return true
    }

    fun clear(): Boolean {
        LocalStorage.clear(KEY)
        return true
    }
}
