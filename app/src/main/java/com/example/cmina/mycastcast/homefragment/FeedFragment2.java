package com.example.cmina.mycastcast.homefragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.adapter.CastListViewAdapter;
import com.example.cmina.mycastcast.util.CastListItem;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.PlayerConstants;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;


/**
 * Created by cmina on 2017-02-09.
 */

public class FeedFragment2 extends ListFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    //서버에서 가져올 정보를 담을 변수
    String myJSON;
    JSONObject casts = null;

    private CastListViewAdapter adapter;
  //  private ArrayList<CastListItem> castListItemList;
    private CastListItem castListItem;

    private String mParam1;
    private String mParam2;

    public FeedFragment2() {

    }

    public static FeedFragment2 newInstance(String mParam1, String mParam2) {
        FeedFragment2 fragment = new FeedFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("inner frag 222", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("inner frag 222", "onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("inner frag 222", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("inner frag 222", "onStart");
    }

    private void showList() {
        adapter = new CastListViewAdapter();
        setListAdapter(adapter);
        adapter.castListItemArrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(myJSON);

            // JSONObject jsonObj = new JSONObject(myJSON);
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
            //Toast.makeText(getContext(), "인터넷 연결하세요", Toast.LENGTH_SHORT).show();
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());

       // getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(0xFFE7A5C7));

        if (networkState.equals(NONE_STATE)) {
            View view = inflater.inflate(R.layout.fragment_none, container, false);
            return view;
        } else {
            View view =  inflater.inflate(R.layout.homefragment2, container, false);

            try {
                HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/readTopCast.php");
                casts = new JSONObject();
                casts.put("limit", 10);
                myJSON = request.execute(casts.toString()).get();
                Log.e("feedfragment2", myJSON);
                showList();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        castListItem = adapter.castListItemArrayList.get(position);
        String castName = castListItem.getCastdbname();

        PlayerConstants.CAST_DB_NUMBER = Integer.parseInt(castName.replace("cast", ""));

        Intent intent = new Intent(getContext(), ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
}