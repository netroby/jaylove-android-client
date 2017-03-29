package com.netroby.daylove.android.daylove.common


object ApiBase {
    val API_BASE_URL = "https://love.netroby.com/api"
    //public static String API_BASE_URL="http://10.0.12.125:8080/api";
    val loginUrl: String
        get() = API_BASE_URL + "/login"

    fun getSaveBlogAddUrl(Token: String?): String {
        return API_BASE_URL + "/save-blog-add?token=" + Token
    }

    fun getListUrl(Token: String?): String {
        return API_BASE_URL + "/list?token=" + Token
    }

    fun getFileUploadUrl(Token: String?): String {
        return API_BASE_URL + "/file-upload?token=" + Token
    }
}
