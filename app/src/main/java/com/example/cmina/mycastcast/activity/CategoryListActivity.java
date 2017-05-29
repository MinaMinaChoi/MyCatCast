package com.example.cmina.mycastcast.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.adapter.CastListViewAdapter;
import com.example.cmina.mycastcast.util.CastListItem;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.PlayerConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * Created by cmina on 2017-02-24.
 */

public class CategoryListActivity extends AppCompatActivity {

    private CastListViewAdapter adapter;
    private CastListItem castListItem;

    String myJSON;
    JSONObject casts = null;
    ListView listView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorylist);

     //   getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFE7A5C7));

        listView = (ListView)findViewById(R.id.categoryList);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("cateName");
        Integer category = intent.getIntExtra("category", 0);
        Log.e("category", category+"");

        setTitle(categoryName);


        casts = new JSONObject();
        try {
            casts.put("category", category);
            Log.e("json", casts.toString());

            HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/readCategory.php");
            myJSON = request.execute(casts.toString()).get();
            Log.e("jsonresult", myJSON);

            if (myJSON != null) {
                adapter = new CastListViewAdapter();
                listView.setAdapter(adapter);
                adapter.castListItemArrayList = new ArrayList<CastListItem>();
                try {
                    JSONArray jsonArray = new JSONArray(myJSON);

                    for (int i=0; i<jsonArray.length(); i++) {
                        casts = jsonArray.getJSONObject(i);

                        String castTitle = casts.getString("castTitle");
                        String castImage = casts.getString("castImage");
                        String castName = "cast"+casts.getString("tableName");
                        Integer castFeedcount = casts.getInt("feedcount");
                        String updateDate = casts.getString("updateDate").substring(0, 10);

                        adapter.addItem(castName, castTitle, castImage, updateDate, castFeedcount);
                        adapter.notifyDataSetChanged();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "인터넷 연결하세요", Toast.LENGTH_SHORT).show();
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        castListItem = adapter.castListItemArrayList.get(position);
                        String castName = castListItem.getCastdbname();

                        //castName.replace("cast", "");
                        PlayerConstants.CAST_DB_NUMBER = Integer.parseInt(castName.replace("cast", ""));

                        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });


            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }







}
