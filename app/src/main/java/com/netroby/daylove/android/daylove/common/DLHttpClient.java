package com.netroby.daylove.android.daylove.common;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DLHttpClient {
    private static DLHttpClient _instance;
    private OkHttpClient client;
    private static final MediaType JSON =
            MediaType.parse("application/json;charset=utf-8");
    private DLHttpClient() {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
                .build();
    }
    public static DLHttpClient getInstance()
    {
        if (_instance == null) {
            _instance = new DLHttpClient();
        }
        return _instance;
    }
    public void doPost(String url, String json, Callback callback) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public void fileUpload(String url, String fileUri, Callback callback) throws IOException {
        MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

            File file = new File(fileUri);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MEDIA_TYPE_JPG, file))
                    .build();
            client.newCall(request).enqueue(callback);
    }
}
