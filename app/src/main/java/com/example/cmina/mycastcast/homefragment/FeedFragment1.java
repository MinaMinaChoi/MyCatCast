package com.example.cmina.mycastcast.homefragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.activity.MainActivity;
import com.example.cmina.mycastcast.adapter.CastListViewAdapter;
import com.example.cmina.mycastcast.fragment.SettingFragment;
import com.example.cmina.mycastcast.util.CastListItem;
import com.example.cmina.mycastcast.util.PlayerConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by cmina on 2017-02-09.
 */

public class FeedFragment1 extends ListFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
//    public static ArrayList<CastListItem> castListItemList ;
    public CastListViewAdapter adapter;

    //서버에서 가져올 정보를 담을 변수 선언
    String myJSON;
    private static final String TAG_RESULT = "result";
    private static final String TAG_CASTTITLE = "castTitle";
    private static final String TAG_CASTIMAGE = "castImage";

    JSONObject casts = null;
    public static CastListItem castListItem;

    private String mParam1;
    private String mParam2;

    public FeedFragment1() {

    }

    public static FeedFragment1 newInstance(String mParam1, String mParam2) {
        FeedFragment1 fragment = new FeedFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("inner frag 1111", "onCreate");

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("inner frag 1111", "onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("inner frag 1111", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("inner frag 1111", "onStart");
    }

    private void getDbData(String string) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

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
                            while ((json=bufferedReader.readLine())!= null) {
                                sb.append(json+"\n");
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
                myJSON=result;
                Log.d("통신확인", myJSON+"");
                showList();
            }


        }

        GetDataJSON g = new GetDataJSON();
        g.execute(string);
    }

    private void showList() {
        adapter = new CastListViewAdapter();
        setListAdapter(adapter);
        adapter.castListItemArrayList = new ArrayList<CastListItem>();
        try {
            JSONArray jsonArray = new JSONArray(myJSON);

            // JSONObject jsonObj = new JSONObject(myJSON);
            for (int i=0; i<jsonArray.length(); i++) {
                casts = jsonArray.getJSONObject(i);

                String castTitle = casts.getString(TAG_CASTTITLE);
                String castImage = casts.getString(TAG_CASTIMAGE);
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
            //Toast.makeText(getContext(), "인터넷 연결하세요", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("현재 와이파이에서만 사용가능합니다. 와이파이를 연결하거나 설정을 변경하십쇼").setCancelable(false)
                    .setPositiveButton("wifi 연결", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).setNegativeButton("설정변경", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //설정으로...
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    SettingFragment settingFragment = new SettingFragment();
                    transaction.replace(R.id.content_main, settingFragment);
                    transaction.commit();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.homefragment1, container, false);
        Log.e("inner frag 1111", "onCreateView");
        getDbData("http://cmina21.cafe24.com/readCastList.php");

        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        castListItem = adapter.castListItemArrayList.get(position);
        Log.e("castListItem", String.valueOf(castListItem));
        String castName = castListItem.getCastdbname();
      //  Toast.makeText(getContext(), castName+castListItem.getCastTitle()+"", Toast.LENGTH_SHORT).show();

       // Log.e("castNAMe", castName);
        castName.replace("cast", "");
        PlayerConstants.CAST_DB_NUMBER = Integer.parseInt(castName.replace("cast", ""));

        Intent intent = new Intent(getContext(), ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }




}

