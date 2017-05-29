package com.example.cmina.mycastcast.homefragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;

import com.example.cmina.mycastcast.adapter.ViewpagerAdapter;

import com.example.cmina.mycastcast.util.RecyclerCastItem;
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

import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;


/**
 * Created by cmina on 2017-02-27.
 */

public class FeedFragment4 extends Fragment {

    ViewpagerAdapter viewpagerAdapter;
    ViewPager viewpager;
    int pos;

    //카테고리별 탑
    Context context;
    RecyclerView recyclerView2;
    RecyclerView.Adapter recyclerAdapter2;
    RecyclerView.LayoutManager layoutManager2;

    ProgressDialog dialog;

    //서버에서 가져올 정보를 담을 변수 선언
    String myJSON;

    JSONObject casts = null;

    public FeedFragment4() {
        // Required empty public constructor
    }

    Handler handler = new Handler();

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
           viewpager.setCurrentItem(pos, false); //페이저 변경할 때 부드럽게 변경 X

            if (pos >= 8)
                pos = 0;
            else  {
              //  Log.e("현재 pos", pos+"");
                pos++;
            }
            handler.postDelayed(runnable, 3500);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 1000);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getDbData(String string) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = new ProgressDialog(getContext());
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("loading...");
                dialog.show();

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
                dialog.dismiss();
                Log.d("feedfrag4 ", myJSON + "");
                showList();
            }


        }

        GetDataJSON g = new GetDataJSON();
        g.execute(string);
    }

    private void showList() {

        try {
            JSONObject object = new JSONObject(myJSON);

            Log.e("json", object.toString());

            JSONArray jsonArray =object.getJSONArray("byupdate");
            ArrayList<RecyclerCastItem> items2 = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                casts = jsonArray.getJSONObject(i);

                items2.add(new RecyclerCastItem(casts.getString("castImage"), casts.getString("castTitle"), casts.getString("tableName"), casts.getString("category")));
                //Log.e("byupdate", casts.toString());
            }

            layoutManager2 = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            recyclerView2.setLayoutManager(layoutManager2);

            recyclerAdapter2 = new MyAdapter2(items2, context);
            recyclerView2.setAdapter(recyclerAdapter2);

            JSONArray array = object.getJSONArray("bycategory");
            JSONObject json ;

            for (int i = 0; i<array.length(); i++) {
                json = array.getJSONObject(i);
                viewpagerAdapter.addPagerItem(json.getString("castImage") , json.getString("castTitle"), json.getString("tableName"), json.getString("category"));
            }
            viewpagerAdapter.notifyDataSetChanged();


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "인터넷 연결하세요", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());

        if (networkState.equals(NONE_STATE)) {
            View view = inflater.inflate(R.layout.fragment_none, container, false);
            return view;
        } else {
            View view = inflater.inflate(R.layout.homefragment, container, false);

            pos =0;

            viewpager = (ViewPager)view.findViewById(R.id.homePager);
            viewpagerAdapter = new ViewpagerAdapter(getActivity().getLayoutInflater());
            viewpager.setAdapter(viewpagerAdapter);
            //뷰페이저를 움직였을 때, 움직인 위치를 position에 저장.
            viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    pos = position;
                    //Log.e("change", position+"");
                    //viewpager.setCurrentItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            context = getContext();

            recyclerView2 = (RecyclerView) view.findViewById(R.id.recyclerView2);
            recyclerView2.setHasFixedSize(true);

            if (getArguments().getString("myJSON", "").length() >0) {
                myJSON = getArguments().getString("myJSON");
                showList();
            } else {
                //카테고리별로 top1, 업데이트순 같이 가져오기
                getDbData("http://cmina21.cafe24.com/readByCategory.php");
            }


            return view;
        }

    }

    //어댑터 따로......
    private class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.ViewHolder> {
        private Context mContext;
        private ArrayList<RecyclerCastItem> items;

        private int lastPosition = -1;

        public MyAdapter2(ArrayList<RecyclerCastItem> items, Context context) {
            this.items = items;
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_cardview_item, parent, false);
            ViewHolder holder = new ViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

           try {
               Glide.with(mContext).load(items.get(position).getCastImage()).thumbnail(0.1f).into(holder.castImage);
           } catch (Exception e) {
               e.printStackTrace();
           }

            holder.titleText.setText(items.get(position).getCastTitle());
            holder.dbNameText.setText(items.get(position).getCastDbName());

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView castImage;
            public TextView titleText, dbNameText ;

            public ViewHolder(View itemView) {
                super(itemView);

                castImage = (ImageView) itemView.findViewById(R.id.castImage);
                titleText = (TextView)itemView.findViewById(R.id.castTitle);
                dbNameText = (TextView)itemView.findViewById(R.id.castDbName);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ListActivity.class);
                        intent.putExtra("CAST_DB_NUMBER", Integer.parseInt(items.get(getPosition()).getCastDbName()));
                        startActivity(intent);
                    }
                });
            }
        }

    }

}
