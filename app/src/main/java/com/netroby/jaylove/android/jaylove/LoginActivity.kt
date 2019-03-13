package com.netroby.jaylove.android.jaylove

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

import com.netroby.jaylove.android.jaylove.common.ApiBase
import com.netroby.jaylove.android.jaylove.common.DLHttpClient
import com.netroby.jaylove.android.jaylove.common.LocalStorage
import com.netroby.jaylove.android.jaylove.common.Token

import org.json.JSONObject

import java.io.IOException
import java.util.HashMap

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {


    // UI references.
    private var musernameView: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null

    var MY_PERMISSIONS_STORAGE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the u
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_STORAGE);
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        LocalStorage.registerContext(applicationContext) //设置LocalStorage context

        // Set up the login form.
        musernameView = findViewById(R.id.username)

        mPasswordView = findViewById(R.id.password)

        Log.i(LOG_TAG, "Here is the login form")

        var remHost = LocalStorage.get("baseUrl");
        Log.i(LOG_TAG, "Remembered host : " + remHost)
        findViewById<EditText>(R.id.host).setText(remHost)

        val musernameSignInButton = findViewById<Button>(R.id.username_sign_in_button)
        musernameSignInButton.setOnClickListener { _ -> attemptLogin() }

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        musernameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val username = musernameView!!.text.toString()
        val password = mPasswordView!!.text.toString()
        val host = findViewById<EditText>(R.id.host).text.toString()
        ApiBase.setBaseUrl(host)

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            musernameView!!.error = getString(R.string.error_field_required)
            focusView = musernameView
            cancel = true
        } else if (!isusernameValid(username)) {
            musernameView!!.error = getString(R.string.error_invalid_username)
            focusView = musernameView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Log.d(LOG_TAG, "Username:$username")
            Log.d(LOG_TAG, "Password:$password")
            val loginURL = ApiBase.getLoginUrl()
            val paramsMap = HashMap<String, String>()
            paramsMap["username"] = username
            paramsMap["password"] = password
            val jParams = JSONObject(paramsMap)
            try {
                Log.d(LOG_TAG, "Now will post to LoginUrl: " + loginURL)
                DLHttpClient.doPost(loginURL, jParams.toString(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(LOG_TAG, e.message)
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "failed to login", Toast.LENGTH_SHORT).show() }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, resp: Response) {
                        Log.d(LOG_TAG, "Response code: " + resp.code())
                        Log.d(LOG_TAG, "Response body: " + resp.body())
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            try {

                                val mhandler = Handler(Looper.getMainLooper())
                                if (resp.code() != 200) {
                                    mhandler.post { Toast.makeText(applicationContext, "Login failed, pleas try again", Toast.LENGTH_SHORT).show() }
                                } else {

                                    val respBodyString = resp.body()?.string()
                                    Log.d(LOG_TAG, "Response body String : $respBodyString")
                                    val response = JSONObject(respBodyString)
                                    val token = response.getString("token")
                                    Token.set(token)
                                    Log.d(LOG_TAG, response.get("token").toString())
                                    mhandler.post {
                                        Toast.makeText(applicationContext, "Success login", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(applicationContext, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                }

                            } catch (e: Exception) {
                                Log.d(LOG_TAG, e.message)
                                e.printStackTrace()
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.message)
            }

        }
    }

    private fun isusernameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    companion object {

        const val LOG_TAG = "jaylove.login"
    }

}

