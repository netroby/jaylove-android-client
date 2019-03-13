package com.netroby.jaylove.android.jaylove

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.netroby.jaylove.android.jaylove.common.ApiBase
import com.netroby.jaylove.android.jaylove.common.DLHttpClient
import com.netroby.jaylove.android.jaylove.common.LocalStorage
import com.netroby.jaylove.android.jaylove.common.Token
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*

@Suppress("unused")
class MainActivity : AppCompatActivity() {
    companion object {
        const val LOG_TAG = "jaylove.main"
    }

    var context: Context? = null
    var MY_PERMISSIONS_STORAGE = 0
    private var page = 1
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val permission = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the u
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_STORAGE);
        }

        context = applicationContext
        LocalStorage.registerContext(applicationContext)
        super.onCreate(savedInstanceState)

        //Check if token exists
        val securityToken = Token.get()
        if (securityToken == "") {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            return
        } else {
            token = securityToken
        }


        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        Glide.get(this)
                .register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(DLHttpClient.client))


        loadList()

    }



    fun goCreateActivity(v: View) {
        startActivity(Intent(this@MainActivity, CreateActivity::class.java))
        finish()
    }

    fun buttonReEnable() {
        val btnPrev = findViewById<Button>(R.id.nav_prev)
        btnPrev.setText(R.string.string_nav_prev)
        btnPrev.isEnabled = true
        val btnNext = findViewById<Button>(R.id.nav_next)
        btnNext.setText(R.string.string_nav_next)
        btnNext.isEnabled = true
    }

    fun goPrev(v: View) {
        page -= 1
        if (page < 1) {
            page = 1
        }
        val btnPrev = findViewById<Button>(R.id.nav_prev)
        btnPrev.setText(R.string.string_loading)
        btnPrev.isEnabled = false
        loadList(page)
    }

    fun goNext(v: View) {
        page += 1
        val btnNext = findViewById<Button>(R.id.nav_next)
        btnNext.setText(R.string.string_loading)
        btnNext.isEnabled = false
        loadList(page)
    }

    @JvmOverloads fun loadList(page: Int = 1) {
        val listURL = ApiBase.getListUrl(token)
        val params = HashMap<String, String>()
        params["page"] = Integer.toString(page)
        Log.d(LOG_TAG, "Try to load page: $page")
        val jParams = JSONObject(params)

        try {

                DLHttpClient.doPost(listURL, jParams.toString(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { buttonReEnable() }
                        e.printStackTrace()
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, resp: Response) {
                        Log.d(LOG_TAG, "Response code: " + resp.code())
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { buttonReEnable() }

                        val respBodyString = resp.body()?.string()
                        Log.d(LOG_TAG, "Response body: $respBodyString")
                        val response = JSONObject(respBodyString)

                        handler.post {
                            try {
                                if (resp.code() != 200) {
                                    val additionMsg = response.getString("msg")
                                    Handler(Looper.getMainLooper()).post { Toast.makeText(context, "Can not load data, please re login then try again$additionMsg", Toast.LENGTH_SHORT).show() }
                                }


                                val data = response.getJSONArray("data")
                                val len = data.length()
                                val ll = findViewById<LinearLayout>(R.id.mainLinearLayout)
                                ll.removeAllViews()
                                for (i in 0 until len) {
                                    val line = data.getJSONObject(i)
                                    Log.d(LOG_TAG, line.toString())
                                    val wv = WebView(context)
                                    val content = "[" + line.getString("publishTime") + "]<br />" + line.getString("content")
                                    Log.d(LOG_TAG, content)
                                    wv.loadData(content, "text/html;charset=UTF-8", "UTF-8")
                                    ll.addView(wv)
                                    val imageList = JSON.parseArray(line.getString("images"))
                                    if (imageList != null) {
                                        val imagesNums = imageList.size
                                        for (j in 0 until imagesNums) {
                                            val iv = ImageView(context)

                                            ll.addView(iv)
                                            val imageUrl = imageList.getString(j) + "?act=resize&x=1024"
                                            Log.d(LOG_TAG, "Try to display image: $imageUrl")
                                            Glide.with(context).load(imageUrl).into(iv)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(LOG_TAG, e.toString() + e.stackTrace.asList().toString())
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        if (token == "") {
            val signInMenu = menu.findItem(R.id.action_signin)
            signInMenu.isVisible = true
        } else {
            val logoutMenu = menu.findItem(R.id.action_logout)
            logoutMenu.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }
        if (id == R.id.action_signin) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            return true
        }
        if (id == R.id.action_logout) {
            Token.clear()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            return true
        }
        if (id == R.id.action_reload_list) {
            loadList()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
