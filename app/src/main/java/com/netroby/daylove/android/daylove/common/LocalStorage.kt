package com.netroby.daylove.android.daylove.common

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {

    private var context: Context? = null

    private val SHARED_SETTING_TAG = "daylove.config"

    fun getSetting() : SharedPreferences? {
        return context?.getSharedPreferences(SHARED_SETTING_TAG, 0)
    }

    fun registerContext(ctx: Context) {
        context = ctx
    }

    fun get(key: String): String {
        return getSetting()?.getString(key, "") as String
    }

    fun set(key: String, value: String): Boolean {
        val edit = getSetting()?.edit()
        edit?.putString(key, value)
        edit?.apply()
        return true
    }

    fun clear(key: String): Boolean {
        val edit = getSetting()?.edit()
        edit?.putString(key, "")
        edit?.apply()
        return true
    }
}
