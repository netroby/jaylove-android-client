package com.netroby.daylove.android.daylove.common;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DLHttpClient {
    private OkHttpClient client;
    private static final MediaType JSON =
            MediaType.parse("application/json;charset=utf-8");
    public DLHttpClient() {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(10, 60, TimeUnit.SECONDS))
                .build();
    }
    public void doPost(String url, String json, Callback callback) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
