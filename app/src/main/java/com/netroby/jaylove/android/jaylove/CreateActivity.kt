package com.netroby.jaylove.android.jaylove

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.netroby.jaylove.android.jaylove.common.ApiBase
import com.netroby.jaylove.android.jaylove.common.DLHttpClient
import com.netroby.jaylove.android.jaylove.common.LocalStorage
import com.netroby.jaylove.android.jaylove.common.Token
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class CreateActivity : AppCompatActivity() {
    private var uploadedImageUrl = ""
    private var token: String = ""
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        val action = intent.action
        val type = intent.type
        context = applicationContext
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(intent)
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        //Check if token exists
        LocalStorage.registerContext(applicationContext)
        val securityToken = Token.get()
        if (securityToken == "") {
            startActivity(Intent(this@CreateActivity, LoginActivity::class.java))
        } else {
            token = securityToken
        }


    }

    private fun handleSendText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val contentEditText = findViewById<EditText>(R.id.editText)
                contentEditText.setText(sharedText)
            }
        }
    }

    fun selectImage(v: View) {

        val container = findViewById<TextView>(R.id.uploadResultContainer)
        container.clearComposingText()
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 54)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED) {
            try {
                if (requestCode == 54) {
                    if (data != null) {

                        val selectedImage = data.data
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
                        cursor!!.moveToFirst()
                        val colIndex = cursor.getColumnIndex(filePathColumn[0])
                        val picPath = cursor.getString(colIndex)
                        cursor.close()
                        Handler(Looper.getMainLooper()).post {
                            val imageView = findViewById<ImageView>(R.id.imageView)
                            imageView.visibility = View.VISIBLE
                            imageView.layoutParams.height = 250
                            imageView.requestLayout()
                            imageView.setImageBitmap(BitmapFactory.decodeFile(picPath))
                        }
                        //Make toast
                        Handler(Looper.getMainLooper()).post {

                            val container = findViewById<TextView>(R.id.uploadResultContainer)
                            container.setText(R.string.image_uploading)
                            Toast.makeText(context, "Begin to upload image , please wait", Toast.LENGTH_SHORT).show() }
                        val url = ApiBase.getFileUploadUrl(token)
                        Log.d(LOG_TAG, "Upload to url$url")
                        val uploadTarget = object: SimpleTarget<Bitmap>(1024, 768) {
                            override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                                // Do something with bitmap here.
                                val stream = ByteArrayOutputStream()
                                resource?.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                                val byteArray = stream.toByteArray()
                                try {
                                    DLHttpClient.fileUpload(url, picPath, byteArray, object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            e.printStackTrace()
                                        }

                                        @Throws(IOException::class)
                                        override fun onResponse(call: Call, response: Response) {
                                            try {
                                                val handler = Handler(Looper.getMainLooper())
                                                if (response.code() != 200) {
                                                    handler.post { Toast.makeText(applicationContext, "Image uploaded Failed, try to logout then login", Toast.LENGTH_SHORT).show() }
                                                    return
                                                }
                                                val resp = JSONObject(response.body()?.string())
                                                uploadedImageUrl = resp.getString("url")
                                                handler.post {
                                                    val container = findViewById<TextView>(R.id.uploadResultContainer)
                                                    container.setText(R.string.image_success_uploaded)
                                                    Toast.makeText(applicationContext, "Image uploaded success, then you can send post", Toast.LENGTH_SHORT).show() }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                        }
                                    })
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }
                        }
                        Glide.with(context)
                                .load(picPath)
                                .asBitmap()
                                .override(1024, 768)
                                .into(uploadTarget)

                    } else {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "Can not load image", Toast.LENGTH_SHORT).show() }
                    }
                }
            } catch (e: Exception) {
                val handler = Handler(Looper.getMainLooper())
                handler.post { Toast.makeText(applicationContext, "Can not load image" + e.message, Toast.LENGTH_SHORT).show() }
                e.printStackTrace()
            }

        }
    }

    fun sendPost(v: View) {
        val sendBtn = findViewById<Button>(R.id.button)
        sendBtn.setText(R.string.create_submit_sending)
        sendBtn.isEnabled = false
        val contentEditText = findViewById<EditText>(R.id.editText)
        val content = contentEditText.text.toString()
        val loginURL = ApiBase.getSaveBlogAddUrl(token)
        val paramsMap = HashMap<String, String>()
        paramsMap["content"] = content.replace("\r?\n".toRegex(), "<br />")
        val list = ArrayList<String>()
        list.add(uploadedImageUrl)
        val imagesJsonString = JSONArray(list).toString()
        paramsMap["images"] = imagesJsonString
        val jParams = JSONObject(paramsMap)

        try {
            DLHttpClient.doPost(loginURL, jParams.toString(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    sendBtn.setText(R.string.create_submit)
                    sendBtn.isEnabled = true
                    e.printStackTrace()
                    val handler = Handler(Looper.getMainLooper())
                    handler.post { Toast.makeText(applicationContext, "failed to create", Toast.LENGTH_SHORT).show() }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, resp: Response) {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        try {
                            if (resp.code() != 200) {
                                Handler(Looper.getMainLooper()).post { Toast.makeText(context, "Can not post, logout then login again !", Toast.LENGTH_SHORT).show() }
                            }
                            val response = JSONObject(resp.body()?.string())
                            val msg = response.getString("msg")
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        } catch (e: Exception) {
                            Token.clear()
                            Toast.makeText(applicationContext, "Create failed", Toast.LENGTH_SHORT).show()
                            Log.d(LOG_TAG, e.message)
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                        }


                    }
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private const val LOG_TAG = "jaylove.create"
    }
}
