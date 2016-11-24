package com.netroby.daylove.android.daylove.common;

/**
 * Created by huzhifeng on 16-11-19.
 */

public class ApiBase {
    public static String API_BASE_URL="https://love.netroby.com/api";
    //public static String API_BASE_URL="http://10.0.12.125:8080/api";
    public static String getLoginUrl()
    {
        return API_BASE_URL + "/login";
    }
    public static String getSaveBlogAddUrl(String Token)
    {
        return API_BASE_URL + "/save-blog-add?token=" + Token;
    }
    public static String getListUrl(String Token)
    {
        return API_BASE_URL + "/list?token=" + Token;
    }
    public static String getFileUploadUrl(String Token) {
        return API_BASE_URL + "/file-upload?token=" + Token;
    }
}
