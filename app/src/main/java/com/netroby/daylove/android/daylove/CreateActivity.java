package com.netroby.daylove.android.daylove;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.netroby.daylove.android.daylove.common.ApiBase;
import com.netroby.daylove.android.daylove.common.Token;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {
    private static final String LOG_TAG = "daylove.create";
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //Check if token exists
        Token tk = new Token(getApplicationContext());
        String securityToken = tk.get();
        if (securityToken.equals("")) {
            Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            token = securityToken;
        }
    }

    public void sendPost(View v) {
        EditText contentEditText = (EditText) findViewById(R.id.editText);
        String content = contentEditText.getText().toString();
        String loginURL = ApiBase.getSaveBlogAddUrl(token);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("content", content);
        JSONObject jParams = new JSONObject(paramsMap);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                loginURL, jParams,
                (JSONObject response) -> {
                    try {
                        String msg = response.getString("msg");
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Token tk = new Token(getApplicationContext());
                        tk.clear();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        Log.d(LOG_TAG, e.getMessage());
                    }
                },
                (VolleyError error) -> {
                    Log.d(LOG_TAG, error.toString());
                    Toast.makeText(getApplicationContext(), "Can not login", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonObjectRequest);
        requestQueue.start();
    }
}
