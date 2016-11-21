package com.netroby.daylove.android.daylove;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.netroby.daylove.android.daylove.common.ApiBase;
import com.netroby.daylove.android.daylove.common.Token;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "daylove.main";

    public static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Check if token exists
        Token tk = new Token(getApplicationContext());
        String securityToken = tk.get();
        if (securityToken.equals("")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        } else {
            token = securityToken;
        }


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            Toast.makeText(getApplicationContext(), "Go create blog", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, CreateActivity.class);
            startActivity(intent);
        });

        String listURL = ApiBase.getListUrl(token);
        JSONObject jParams = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                listURL, jParams,
                (JSONObject response) -> {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.mainLinearLayout);
                    try {
                        Log.d(LOG_TAG, response.toString());
                        JSONArray data = response.getJSONArray("data");
                        Integer len = data.length();
                        for (Integer i = 0 ; i < len; i++){
                            JSONObject line = data.getJSONObject(i);
                            WebView wv = new WebView(this);
                            String content = "[" + line.getString("PublishTime") + "]<br />" + line.getString("Content");
                            Log.d(LOG_TAG, content);
                            wv.loadData(content, "text/html;charset=UTF-8", "UTF-8");
                            ll.addView(wv);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, e.getMessage());
                    }
                },
                (VolleyError error) -> {
                    Log.d(LOG_TAG, error.toString());
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonObjectRequest);
        requestQueue.start();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (token.equals("")) {
            MenuItem signInMenu = menu.findItem(R.id.action_signin);
            signInMenu.setVisible(true);
        } else {
            MenuItem logoutMenu = menu.findItem(R.id.action_logout);
            logoutMenu.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_signin) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_logout) {
            Token tk = new Token(getApplicationContext());
            tk.clear();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
