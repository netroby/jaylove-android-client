package com.netroby.daylove.android.daylove.common

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object DLHttpClient {

    private val JSON = MediaType.parse("application/json;charset=utf-8")
    val client: OkHttpClient? = OkHttpClient.Builder()
                .connectionPool(ConnectionPool(5, 300, TimeUnit.SECONDS))
                .build()

    @Throws(IOException::class)
    fun preparePool()  {
        client!!.newCall(Request.Builder().url(ApiBase.API_BASE_URL).head().build()).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call?, response: Response?) {

            }
        })
    }

    @Throws(IOException::class)
    fun doPost(url: String, json: String, callback: Callback) {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        client!!.newCall(request).enqueue(callback)
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
        client!!.newCall(request).enqueue(callback)
    }
}
