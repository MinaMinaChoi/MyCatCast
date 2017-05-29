package com.example.cmina.mycastcast.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.cmina.mycastcast.util.HttpUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cmina on 2017-02-14.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        try {
            PushRequest request = new PushRequest("http://cmina21.cafe24.com/register2.php");
           // JSONObject object = new JSONObject();
            //object.put("token", token);


            String result = request.PushRequest(token);
            if (result.equals("-1")){
                Log.e("push request", "실패");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

/*        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
                .build();

        //request
        Request request = new Request.Builder()
                .url("http://cmina21.cafe24.com/register2.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


    public class PushRequest {
        private URL url;

        public PushRequest(String url) throws MalformedURLException {
            this.url = new URL(url);
        }

        private String readStream(InputStream in) throws IOException {
            StringBuilder jsonHtml = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = null;

            while ((line = reader.readLine()) != null) {
                jsonHtml.append(line);
            }
            reader.close();
            return jsonHtml.toString();
        }

        public String PushRequest(final String token) {
            try {

                String postData = "Token=" + token;
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setConnectTimeout(5000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                String result = readStream(conn.getInputStream());
                conn.disconnect();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("PushRequest", "request was failed");
                return null;
            }

        }
    }

    private class DeleteTokenTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                Log.d(TAG, "Exception deleting token", e);
            }
            return null;
        }
    }

}
