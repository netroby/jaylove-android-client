package com.netroby.jaylove.android.jaylove.common


object ApiBase {
    fun setBaseUrl(s: String) {
        LocalStorage.set("baseUrl", s)
    }
    fun getBaseUrl(): String {
        return LocalStorage.get("baseUrl") + "/api"
    }
    fun getLoginUrl(): String {
        return getBaseUrl() + "/login"
    }

    fun getSaveBlogAddUrl(Token: String): String {
        return getBaseUrl() + "/save-blog-add?token=$Token"
    }

    fun getListUrl(Token: String): String {
        return getBaseUrl() + "/list?token=$Token"
    }

    fun getFileUploadUrl(Token: String): String {
        return getBaseUrl() + "/file-upload?token=$Token"
    }
}
