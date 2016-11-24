package com.netroby.daylove.android.daylove.common;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    public OkHttpClient getClient() {
        return client;
    }
    public void doPost(String url, String json, Callback callback) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public void fileUpload(String url, String fileUri, byte[] imageByteArray, Callback callback) throws IOException {
        MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadfile", fileUri, RequestBody.create(MEDIA_TYPE_JPG, imageByteArray))
                .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(callback);
    }
}
