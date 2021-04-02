package com.example.project.utils;

import android.app.Activity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageUpload {
    private static final String IMGUR_CLIENT_ID = "123";
    private static final MediaType MEDIA_TYPE_JPG = MediaType.Companion.parse("image/jpg");

    private static final OkHttpClient client = new OkHttpClient();

    public static void run(File f, Activity activity, TextView tv1, TextView tv2, TextView tv3,
                           TextView tv4, TextView tv5, TextView tv6) throws Exception {
        final File file=f;
        new Thread() {
            @Override
            public void run() {
                //子线程需要做的工作
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", UUID.randomUUID().toString()+".jpg",
                                RequestBody.Companion.create(file, MEDIA_TYPE_JPG))
                        .build();
                //设置为自己的ip地址
                Request request = new Request.Builder()
                        .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .url("https://lzf.chengguo.plus:5000/classification")
                        .post(requestBody)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                        String result0 = jsonObject.optString("result0", null);
                        String name0 = new JSONObject(result0).optString("name", null);
                        float score0 = (float) new JSONObject(result0).optDouble("score", 0);
                        String result1 = jsonObject.optString("result1", null);
                        String name1 = new JSONObject(result1).optString("name", null);
                        float score1 = (float) new JSONObject(result1).optDouble("score", 0);
                        String result2 = jsonObject.optString("result2", null);
                        String name2 = new JSONObject(result2).optString("name", null);
                        float score2 = (float) new JSONObject(result2).optDouble("score", 0);
                        ResultHelper.INSTANCE.setResult0(name0);
                        ResultHelper.INSTANCE.setResult1(name1);
                        ResultHelper.INSTANCE.setResult2(name2);
                        ResultHelper.INSTANCE.setScore0(score0);
                        ResultHelper.INSTANCE.setScore1(score1);
                        ResultHelper.INSTANCE.setScore2(score2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv1.setText(ResultHelper.INSTANCE.getResult0());
                            tv2.setText(ResultHelper.INSTANCE.getScore0());
                            tv3.setText(ResultHelper.INSTANCE.getResult1());
                            tv4.setText(ResultHelper.INSTANCE.getScore1());
                            tv5.setText(ResultHelper.INSTANCE.getResult2());
                            tv6.setText(ResultHelper.INSTANCE.getScore2());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
