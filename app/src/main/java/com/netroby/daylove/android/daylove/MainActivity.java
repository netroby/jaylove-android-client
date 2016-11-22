package com.netroby.daylove.android.daylove;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.netroby.daylove.android.daylove.common.ApiBase;
import com.netroby.daylove.android.daylove.common.DLHttpClient;
import com.netroby.daylove.android.daylove.common.Token;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    public Context context;
    public static final String LOG_TAG = "daylove.main";

    public static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        super.onCreate(savedInstanceState);

        //Check if token exists
        Token tk = new Token(getApplicationContext());
        String securityToken = tk.get();
        if (securityToken.equals("")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return;
        } else {
            token = securityToken;
        }


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> startActivity(new Intent(MainActivity.this, CreateActivity.class)));

        String listURL = ApiBase.getListUrl(token);


        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        JSONObject jParams = new JSONObject(params);

        DLHttpClient httpClient = new DLHttpClient();
        try {
            httpClient.doPost(listURL, jParams.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        try {
                            JSONObject response = new JSONObject(resp.body().string());
                            Log.d(LOG_TAG, response.toString());
                            JSONArray data = response.getJSONArray("data");
                            Integer len = data.length();
                            for (Integer i = 0; i < len; i++) {
                                LinearLayout ll = (LinearLayout) findViewById(R.id.mainLinearLayout);
                                JSONObject line = data.getJSONObject(i);
                                Log.d(LOG_TAG, line.toString());
                                WebView wv = new WebView(context);
                                String content = "[" + line.getString("PublishTime") + "]<br />" + line.getString("Content");
                                Log.d(LOG_TAG, content);
                                wv.loadData(content, "text/html;charset=UTF-8", "UTF-8");
                                ll.addView(wv);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }
        if (id == R.id.action_logout) {
            Token tk = new Token(getApplicationContext());
            tk.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
