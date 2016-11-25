package com.netroby.daylove.android.daylove;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netroby.daylove.android.daylove.common.ApiBase;
import com.netroby.daylove.android.daylove.common.DLHttpClient;
import com.netroby.daylove.android.daylove.common.Token;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String LOG_TAG = "daylove.login";


    // UI references.
    private AutoCompleteTextView musernameView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        musernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((TextView textView, int id, KeyEvent keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button musernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        musernameSignInButton.setOnClickListener((View view) -> {
            attemptLogin();
        });

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        musernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = musernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            musernameView.setError(getString(R.string.error_field_required));
            focusView = musernameView;
            cancel = true;
        } else if (!isusernameValid(username)) {
            musernameView.setError(getString(R.string.error_invalid_username));
            focusView = musernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Log.d(LOG_TAG, "Username:" + username);
            Log.d(LOG_TAG, "Password:" + password);
            String loginURL = ApiBase.getLoginUrl();
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("username", username);
            paramsMap.put("password", password);
            JSONObject jParams = new JSONObject(paramsMap);
            DLHttpClient httpClient = DLHttpClient.getInstance();
            try {
                httpClient.doPost(loginURL, jParams.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> Toast.makeText(getApplicationContext(), "failed to login", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response resp) throws IOException {
                        Log.d(LOG_TAG, "Response code: "  + resp.code());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            try {

                                Handler mhandler = new Handler(Looper.getMainLooper());
                                if (resp.code() != 200) {
                                    mhandler.post(() -> {
                                        Toast.makeText(getApplicationContext(), "Login failed, pleas try again", Toast.LENGTH_SHORT).show();
                                    });
                                } else {

                                    String respBodyString = resp.body().string();
                                    Log.d(LOG_TAG, "Response body String : " + respBodyString);
                                    JSONObject response = new JSONObject(respBodyString);
                                    String token = response.getString("token");
                                    Token tk = Token.getInstance(getApplicationContext());
                                    tk.set(token);
                                    Log.d(LOG_TAG, response.get("token").toString());
                                    mhandler.post(() -> {
                                        Toast.makeText(getApplicationContext(), "Success login", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    });
                                }

                            } catch (Exception e) {
                                Log.d(LOG_TAG, e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isusernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}

