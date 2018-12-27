package com.netroby.daylove.android.daylove.common

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by huzhifeng on 16-11-19.
 */

object Token {

    private var context: Context? = null

    private val SHARED_SETTING_TAG = "daylove.config"

    fun getSetting() : SharedPreferences? {
        return context?.getSharedPreferences(SHARED_SETTING_TAG, 0)
    }

    fun registerContext(ctx: Context) {
        context = ctx
    }

    fun get(): String {
        return getSetting()?.getString("token", "") as String
    }

    fun set(token: String): Boolean {
        val edit = getSetting()?.edit()
        edit?.putString("token", token)
        edit?.apply()
        return true
    }

    fun clear(): Boolean {
        val edit = getSetting()?.edit()
        edit?.putString("token", "")
        edit?.apply()
        return true
    }
}
