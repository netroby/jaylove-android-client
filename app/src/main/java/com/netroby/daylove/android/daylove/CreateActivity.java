package com.netroby.daylove.android.daylove;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.netroby.daylove.android.daylove.common.ApiBase;
import com.netroby.daylove.android.daylove.common.DLHttpClient;
import com.netroby.daylove.android.daylove.common.Token;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateActivity extends AppCompatActivity {
    private static final String LOG_TAG = "daylove.create";
    private String uploadedImageUrl = "";
    private String token;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        context = getApplicationContext();
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
                if (requestCode == 54) {
                    if (data != null) {

                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picPath = cursor.getString(colIndex);
                        cursor.close();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.getLayoutParams().height = 250;
                            imageView.requestLayout();
                            imageView.setImageBitmap(BitmapFactory.decodeFile(picPath));
                        });
                        //Make toast
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Begin to upload image , please wait", Toast.LENGTH_SHORT).show();
                        });
                        DLHttpClient httpClient = DLHttpClient.getInstance();
                        String url = ApiBase.getFileUploadUrl(token);
                        Log.d(LOG_TAG, "Upload to url" + url);
                        Glide.with(context)
                                .load(picPath)
                                .asBitmap()
                                .override(1024, 768)
                                .into(new SimpleTarget<Bitmap>(1024, 768) {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                        // Do something with bitmap here.
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                                        byte[] byteArray = stream.toByteArray();
                                        try {
                                            httpClient.fileUpload(url, picPath, byteArray, new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    try {
                                                        Handler handler = new Handler(Looper.getMainLooper());
                                                        if (response.code() != 200) {
                                                            handler.post(()->{
                                                                Toast.makeText(getApplicationContext(), "Image uploaded Failed, try to logout then login", Toast.LENGTH_SHORT).show();
                                                            });
                                                            return;
                                                        }
                                                        JSONObject resp = new JSONObject(response.body().string());
                                                        uploadedImageUrl = resp.getString("url");
                                                        handler.post(() -> {
                                                            Toast.makeText(getApplicationContext(), "Image uploaded success, then you can send post", Toast.LENGTH_SHORT).show();
                                                        });
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), "Can not load image", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPost(View v) {
        Button sendBtn = (Button) findViewById(R.id.button);
        sendBtn.setText(R.string.create_submit_sending);
        sendBtn.setEnabled(false);
        EditText contentEditText = (EditText) findViewById(R.id.editText);
        String content = contentEditText.getText().toString();
        String loginURL = ApiBase.getSaveBlogAddUrl(token);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("content", content.replaceAll("\r?\n", "<br />"));
        ArrayList<String> list = new ArrayList<>();
        list.add(uploadedImageUrl);
        String imagesJsonString = new JSONArray(list).toString();
        paramsMap.put("images", imagesJsonString);
        JSONObject jParams = new JSONObject(paramsMap);

        DLHttpClient httpClient = DLHttpClient.getInstance();
        try {
            httpClient.doPost(loginURL, jParams.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sendBtn.setText(R.string.create_submit);
                    sendBtn.setEnabled(true);
                    e.printStackTrace();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(getApplicationContext(), "failed to create", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        try {
                            if (resp.code() != 200) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                   Toast.makeText(context, "Can not post, logout then login again !", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }
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
