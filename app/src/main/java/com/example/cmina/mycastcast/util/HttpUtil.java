package com.example.cmina.mycastcast.util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cmina on 2017-02-21.
 */

public class HttpUtil extends AsyncTask<String, Void, String> {
    private URL url;
    String result;

    public HttpUtil(String url) throws MalformedURLException {
        this.url = new URL(url);
    }
    private String readStream(InputStream in) throws IOException {
        StringBuilder jsonHtml = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = null;

        while ((line = reader.readLine()) !=null ) {
            jsonHtml.append(line);
        }
        reader.close();
        return jsonHtml.toString();
    }


    public String PhPtest(final String json_data) {
        try {

            String postData = "json_data=" + json_data;
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postData.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            result = readStream(conn.getInputStream());
            conn.disconnect();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("PHPRequest", "request was failed");
            return null;
        }

    }

    @Override
    protected String doInBackground(String... params) {
        PhPtest(params[0]);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

    }

}

