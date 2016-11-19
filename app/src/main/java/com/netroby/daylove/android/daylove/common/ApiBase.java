package com.netroby.daylove.android.daylove.common;

/**
 * Created by huzhifeng on 16-11-19.
 */

public class ApiBase {
    public static String API_BASE_URL="https://love.netroby.com/api/";
    public static String getLoginUrl()
    {
        return API_BASE_URL + "/login";
    }
}
