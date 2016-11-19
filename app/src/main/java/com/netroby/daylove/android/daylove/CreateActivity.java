package com.netroby.daylove.android.daylove;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.netroby.daylove.android.daylove.common.Token;

public class CreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //Check if token exists
        String securityToken = Token.get();
        if (securityToken.equals("")) {
            Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
