package com.netroby.daylove.android.daylove.common

import java.io.IOException
import java.util.concurrent.TimeUnit

import okhttp3.Callback
import okhttp3.ConnectionPool
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object DLHttpClient {

    private val JSON = MediaType.parse("application/json;charset=utf-8")
    val client = OkHttpClient.Builder()
                .connectionPool(ConnectionPool(10, 60, TimeUnit.SECONDS))
                .build()


    @Throws(IOException::class)
    fun doPost(url: String, json: String, callback: Callback) {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        client.newCall(request).enqueue(callback)
    }

    @Throws(IOException::class)
    fun fileUpload(url: String, fileUri: String, imageByteArray: ByteArray, callback: Callback) {
        val MEDIA_TYPE_JPG = MediaType.parse("image/jpg")

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploadfile", fileUri, RequestBody.create(MEDIA_TYPE_JPG, imageByteArray))
                .build()
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        client.newCall(request).enqueue(callback)
    }
}
