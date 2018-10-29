package com.netroby.daylove.android.daylove

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.netroby.daylove.android.daylove.common.ApiBase
import com.netroby.daylove.android.daylove.common.DLHttpClient
import com.netroby.daylove.android.daylove.common.Token

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Token.registerContext(applicationContext)

        // Set up the login form.
        musernameView = findViewById(R.id.username)

        mPasswordView = findViewById(R.id.password)
        mPasswordView!!.setOnEditorActionListener { _, id: Int, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
            }
            false
        }

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
            val loginURL = ApiBase.loginUrl
            val paramsMap = HashMap<String, String>()
            paramsMap["username"] = username
            paramsMap["password"] = password
            val jParams = JSONObject(paramsMap)
            try {
                DLHttpClient.doPost(loginURL, jParams.toString(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        val handler = Handler(Looper.getMainLooper())
                        handler.post { Toast.makeText(applicationContext, "failed to login", Toast.LENGTH_SHORT).show() }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, resp: Response) {
                        Log.d(LOG_TAG, "Response code: " + resp.code())
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            try {

                                val mhandler = Handler(Looper.getMainLooper())
                                if (resp.code() != 200) {
                                    mhandler.post { Toast.makeText(applicationContext, "Login failed, pleas try again", Toast.LENGTH_SHORT).show() }
                                } else {

                                    val respBodyString = resp.body().string()
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
            } catch (e: IOException) {
                e.printStackTrace()
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

        const val LOG_TAG = "daylove.login"
    }

}

