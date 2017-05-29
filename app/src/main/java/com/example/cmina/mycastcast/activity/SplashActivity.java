package com.example.cmina.mycastcast.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.cmina.mycastcast.util.PlayerConstants.MOBILE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.WIFI_STATE;

/**
 * Created by cmina on 2017-03-15.
 */

public class SplashActivity extends AppCompatActivity {

    String myJSON;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String networkState = UtilityFunctions.getWhatKindOfNetwork(SplashActivity.this);

        Log.d("network", networkState);

        if (networkState.equals(NONE_STATE)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SplashActivity.this,"인터넷을 연결해야 어플이용가능합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, 2000);

        } else {
            //카테고리별로 top1, 업데이트순 같이 가져오기
            getDbData("http://cmina21.cafe24.com/readByCategory.php");
        }

    }

    private class splashhandler implements Runnable {
        public void run() {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("myJSON", myJSON);
            startActivity(intent);
            SplashActivity.this.finish(); // 로딩페이지 Activity Stack에서 제거
        }
    }


    private void getDbData(String string) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];
                BufferedReader bufferedReader = null;

                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //연결 객체 생성
                    StringBuilder sb = new StringBuilder();

                    if (conn != null) { //연결되었으면
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String json;
                            while ((json = bufferedReader.readLine()) != null) {
                                sb.append(json + "\n");
                            }
                        }
                    }
                    return sb.toString().trim();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                Log.d("splash", myJSON + "");
                //showList();
               /* Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("myJSON", myJSON);
                startActivity(intent);*/
                Handler handler = new Handler();
                handler.postDelayed(new splashhandler(), 1000);

            }


        }

        GetDataJSON g = new GetDataJSON();
        g.execute(string);
    }

}
