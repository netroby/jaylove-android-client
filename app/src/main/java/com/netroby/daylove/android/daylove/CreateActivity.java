package com.netroby.daylove.android.daylove;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class CreateActivity extends AppCompatActivity {
    private static final String LOG_TAG = "daylove.create";
    private String uploadedImageUrl = "";
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //Check if token exists
        Token tk = Token.getInstance(getApplicationContext());
        String securityToken = tk.get();
        if (securityToken.equals("")) {
            startActivity(new Intent(CreateActivity.this, LoginActivity.class));
        } else {
            token = securityToken;
        }


    }
    public void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                EditText contentEditText = (EditText) findViewById(R.id.editText);
                contentEditText.setText(sharedText);
            });
        }
    }

    public void selectImage(View v) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 54);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            try {
                if (requestCode == 54 && resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null,null);
                        cursor.moveToFirst();
                        int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picPath = cursor.getString(colIndex);
                        cursor.close();
                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(BitmapFactory.decodeFile(picPath));
                        DLHttpClient httpClient = DLHttpClient.getInstance();
                        String url = ApiBase.getFileUploadUrl(token);
                        httpClient.fileUpload(url, picPath, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    JSONObject resp = new JSONObject(response.body().string());
                                    uploadedImageUrl = resp.getString("url");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), "Can not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPost(View v) {
        EditText contentEditText = (EditText) findViewById(R.id.editText);
        String content = contentEditText.getText().toString();
        String loginURL = ApiBase.getSaveBlogAddUrl(token);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("content", content.replaceAll("\r?\n", "<br />"));
        JSONObject jParams = new JSONObject(paramsMap);

        DLHttpClient httpClient = DLHttpClient.getInstance();
        try {
            httpClient.doPost(loginURL, jParams.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(getApplicationContext(), "failed to create", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                            try {
                                JSONObject response = new JSONObject(resp.body().string());
                                String msg = response.getString("msg");
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } catch (Exception e) {
                                Token tk = Token.getInstance(getApplicationContext());
                                tk.clear();
                                Toast.makeText(getApplicationContext(), "Create failed", Toast.LENGTH_SHORT).show();
                                Log.d(LOG_TAG, e.getMessage());
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }

                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
