package com.example.cmina.mycastcast.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.adapter.ListViewAdapter;
import com.example.cmina.mycastcast.util.DbOpenHelper;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.PlayListDbOpenHelper;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.RssItem;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.example.cmina.mycastcast.util.SimpleDividerItemDecoration;
import com.example.cmina.mycastcast.util.UtilityFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.cmina.mycastcast.R.id.feedBtn;
import static com.example.cmina.mycastcast.R.id.recyclerView;
import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;
import static com.example.cmina.mycastcast.util.PlayerConstants.playlistCursor;

/**
 * Created by cmina on 2017-02-09.
 */

public class ListActivity extends AppCompatActivity {

    String TAG = "ListActivity";

    //서버에서 가져올 정보를 담을 변수
    String myJSON;
    JSONObject casts = null;

    //구독하기
    public static DbOpenHelper dbOpenHelper;
    private JSONObject jsonObject;
    private TextView feedText;

    //재생목록
    public static PlayListDbOpenHelper playListDbOpenHelper;

    public ListViewAdapter adapter;
    ImageView castImage;
    ImageView castBigImage;

   public static int limit;

    ProgressDialog dialog;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton floatingActionButton;
    Context context;
    RecyclerView recyclerView;
    MyAdapter recyclerAdapter;
  //  RecyclerView.Adapter recyclerAdapter;
    LinearLayoutManager layoutManager;

    NestedScrollView nestedScrollView;

    RssItem rssItem;
    List<RssItem> rssItemList;


    public class LoadMoreDataTask extends AsyncTask<String, Void, String> {

        private URL url;
        String result;
        MyAdapter adapter;

        public LoadMoreDataTask(String url, MyAdapter adapter) throws MalformedURLException {
            this.url = new URL(url);
            this.adapter = adapter;
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

        @Override
        protected String doInBackground(String... params) {

            try {
                String postData = "json_data=" + params[0];
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
                result = readStream(conn.getInputStream());
                conn.disconnect();

                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {
                    rssItem = new RssItem();
                    casts = jsonArray.getJSONObject(i);
                    //  Log.e("로드모어", jsonArray.length() + "");

                    rssItem.setEpisodeTitle(casts.getString("episodeTitle"));
                    rssItem.setDuration(casts.getString("duration"));
                    rssItem.setPubdate(casts.getString("pubDate").substring(0, 10));
                    rssItem.setMusicUrl(casts.getString("musicUrl"));
                    rssItem.setCastTitle(casts.getString("castTitle"));
                    rssItem.setCastImage(casts.getString("castImage"));
                    rssItem.setCategory(casts.getString("category"));

                    //수정
                    //rssItemList.add(rssItem);
                    Log.d("check", rssItem.getEpisodeTitle());

                    recyclerAdapter.addItem(recyclerAdapter.getItemCount()+i, rssItem);

                    PlayerConstants.SONGS_LIST.add(rssItem);

                }

                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            dialog.dismiss();

            if (rssItemList != null) {
                //수정중

/*                layoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(layoutManager);

                recyclerAdapter = new MyAdapter(rssItemList, context);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));*/

            }

            if (nestedScrollView != null) {
                nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        // Log.e("ScrollView", "scrollX_" + scrollX + "_scrollY_" + scrollY + "_oldScrollX_" + oldScrollX + "_oldScrollY_" + oldScrollY);
                        // Log.e("ㅇㅇㅇ", "scrollY = " + scrollY + "비교값 " + (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) + "/" + v.getChildAt(0).getBottom());

                        int diff = (v.getChildAt(0).getBottom() - (nestedScrollView.getHeight() + nestedScrollView
                                .getScrollY()));

                        if (diff == 0) {
                            Log.i(TAG, "BOTTOM SCROLL");

                            Log.e(TAG, v.getChildAt(0).getMeasuredHeight() + ", " + v.getMeasuredHeight());
                            // Log.e(TAG, v.getChildAt(0)+"");
                            Log.e(TAG, scrollY + "");
                            try {
                                LoadMoreDataTask loadMoreDataTask = new LoadMoreDataTask("http://cmina21.cafe24.com/readFromDB.php", recyclerAdapter);

                                limit += 20;

                                jsonObject.put("dbNo", String.valueOf(PlayerConstants.CAST_DB_NUMBER));
                                jsonObject.put("limit", limit);

                                loadMoreDataTask.execute(jsonObject.toString());

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //수정중
        //    PlayerConstants.SONGS_LIST.clear();

        //    rssItemList = new ArrayList<RssItem>();

            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("로딩중...");
            dialog.show();
            // progressBar.setVisibility(View.VISIBLE);

        }
    }

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
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String networkState = UtilityFunctions.getWhatKindOfNetwork(ListActivity.this);

        if (networkState.equals(NONE_STATE)) {
            Toast.makeText(this, "인터넷을 연결해야 어플사용 가능합니다.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            setContentView(R.layout.activity_list_col);

            init();

            limit = 20;

            context = ListActivity.this;
            jsonObject = new JSONObject();

            Intent intent = getIntent();
            if (getIntent().toString().contains("extras")) { //노티에서 클릭하고 들어오는 경우.
                PlayerConstants.CAST_DB_NUMBER = intent.getIntExtra("CAST_DB_NUMBER", 1);
                Log.e("ddd", getIntent().toString());
            }

            //데이터베이스 생성 및 오픈
            dbOpenHelper = new DbOpenHelper(ListActivity.this);
            playListDbOpenHelper = new PlayListDbOpenHelper(ListActivity.this);
            try {
                dbOpenHelper.open();
                playListDbOpenHelper.open();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //처음에만 로딩하도록.....
            if (limit == 20) {
                //서버에 보내는 정보
                JSONObject object = new JSONObject();

                try {

                    object.put("dbNo", String.valueOf(PlayerConstants.CAST_DB_NUMBER));
                    object.put("limit", limit);

                    Log.d("확인", PlayerConstants.CAST_DB_NUMBER + limit + "");

                    PlayerConstants.SONGS_LIST.clear();

                    final HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/readFromDB.php");
                    myJSON = request.execute(object.toString()).get();

                    if (myJSON != null) {
                        Log.d("통신확인", myJSON.toString());

                        JSONArray jsonArray = new JSONArray(myJSON);
                        // casts = new JSONObject();
                        Log.e("로드 첫번째", jsonArray.length() + "");
                        rssItemList = new ArrayList<RssItem>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            rssItem = new RssItem();
                            object = jsonArray.getJSONObject(i);

                            rssItem.setEpisodeTitle(object.getString("episodeTitle"));
                            rssItem.setDuration(object.getString("duration"));

                            rssItem.setPubdate(object.getString("pubDate").substring(0, 10));
                            rssItem.setMusicUrl(object.getString("musicUrl"));
                            rssItem.setCastTitle(object.getString("castTitle"));
                            rssItem.setCastImage(object.getString("castImage"));
                            rssItem.setCategory(object.getString("category"));

                            rssItemList.add(rssItem);
                            PlayerConstants.SONGS_LIST.add(rssItem);

                            if (rssItemList != null) {

                                layoutManager = new LinearLayoutManager(context);
                                recyclerView.setLayoutManager(layoutManager);

                                recyclerAdapter = new MyAdapter(rssItemList, context);
                                recyclerView.setAdapter(recyclerAdapter);
                                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
                            }

                            setcastItem();

                            if (nestedScrollView != null) {

                                nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        int diff = (v.getChildAt(0).getBottom() - (nestedScrollView.getHeight() + nestedScrollView
                                                .getScrollY()));

                                        //824 //scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())
                                        if (diff == 0) {
                                            Log.i(TAG, "BOTTOM SCROLL/" + v.getBottom());
                                            LoadMoreDataTask loadMoreDataTask = null;
                                            try {
                                                loadMoreDataTask = new LoadMoreDataTask("http://cmina21.cafe24.com/readFromDB.php", recyclerAdapter);
                                                limit += 20;
                                                jsonObject.put("dbNo", String.valueOf(PlayerConstants.CAST_DB_NUMBER));
                                                jsonObject.put("limit", limit);
                                                loadMoreDataTask.execute(jsonObject.toString());
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }

                        }

                        //setcastItem();

                        //구독하기...
                        cursor = null;
                        String castTitle =rssItemList.get(0).getCastTitle();
                        castTitle=castTitle.replace('"', '\"');
                        castTitle = castTitle.replace("'", "\'");
                        cursor = dbOpenHelper.getMatchName(castTitle);

                        Log.d("cursor count", cursor.getCount() + "");

                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            if (cursor.getString(0).equals(SaveSharedPreference.getUserUnique(ListActivity.this)) && cursor.getInt(cursor.getColumnIndex("castfeed")) > 0 && SaveSharedPreference.getUserUnique(ListActivity.this).length() > 0) {
                                feedText.setText("구독취소");
                                floatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.colormintAccent));
                              //  floatingActionButton.setBackgroundColor(R.color.colormintAccent);
                            } else {
                                feedText.setText("구독");
                                floatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                            }
                        }

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
    }

    private void init() {
        //list_col
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //액션바 백버튼
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        context = getApplicationContext();
        recyclerView = (RecyclerView) findViewById(R.id.castList);
        recyclerView.setHasFixedSize(true);

        castImage = (ImageView) findViewById(R.id.castImage);
        castBigImage = (ImageView) findViewById(R.id.castBigImage);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        feedText = (TextView) findViewById(R.id.feedText);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SaveSharedPreference.getUserUnique(ListActivity.this).length() > 0) {

                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                    String token = FirebaseInstanceId.getInstance().getToken();

                    jsonObject = new JSONObject();

                    try {
                        jsonObject.put("token", token);
                        jsonObject.put("castName", "cast" + PlayerConstants.CAST_DB_NUMBER);
                        jsonObject.put("uniqueID", SaveSharedPreference.getUserUnique(context));

                        HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/register.php");

                        String result = request.execute(jsonObject.toString()).get();
                        if (result.equals("1")) {
                            //구독하기 등록
                            //정보 넣기
                            dbOpenHelper.insertColumn(SaveSharedPreference.getUserUnique(ListActivity.this), "cast" + PlayerConstants.CAST_DB_NUMBER,
                                    rssItem.getCastTitle(), rssItem.getCastImage(), true, rssItem.getCategory());
                            Log.e("sqlite 넣기", "cast" + PlayerConstants.CAST_DB_NUMBER + ", " + rssItem.getCastTitle() + ", " + rssItem.getCastImage());
                            feedText.setText("구독취소");
                            floatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.colormintAccent));

                           // floatingActionButton.setBackgroundColor(R.color.colormintAccent);
                        } else if (result.equals("2")) {
                            //구독취소
                            dbOpenHelper.deleteColumn(rssItem.getCastTitle());
                            feedText.setText("구독");
                            floatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                        }
                        Log.e("feed check", result);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(context, "구독하기를 사용하려면 로그인하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setListeners() {

   /*     listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                playlistCursor = playListDbOpenHelper.getMatchEpiTitle(rssItemList.get(position).getEpisodeTitle());
                playlistCursor.moveToFirst();

                if (playlistCursor.getCount() > 0) {

                    //이미 추가된 에피소드
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(ListActivity.this);
                    builder2.setMessage("이미 재생목록에 추가되었습니다")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder2.create();
                    alertDialog.show();


                } else {
                    //재생목록에 추가
                    AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                    builder.setTitle("재생목록추가");
                    builder.setMessage("재생목록에 추가하겠습니까?")
                            .setCancelable(true)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //재생목록추가
                                    playListDbOpenHelper.insertColumn(SaveSharedPreference.getUserUnique(getApplicationContext()), rssItemList.get(position).getCastTitle(), rssItemList.get(position).getCastImage(),
                                            rssItemList.get(position).getMusicUrl(), rssItemList.get(position).getEpisodeTitle(), true);
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                return false;
            }
        });*/

    }

    private void setcastItem() {

        collapsingToolbarLayout.setTitle(rssItemList.get(0).getCastTitle());
        Glide.with(ListActivity.this).load(rssItemList.get(0).getCastImage()).thumbnail(0.1f).into(castImage);
        Picasso.with(ListActivity.this).load(rssItemList.get(0).getCastImage()).transform(new com.example.cmina.mycastcast.util.BlurTransformation(context, 50)).into(castBigImage);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (dbOpenHelper != null) {
            dbOpenHelper.close();
        }
        super.onDestroy();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Context context;
        public List<RssItem> items;

        private int lastPosition = -1;

        public MyAdapter(List<RssItem> items, Context context) {
            this.items = items;
            this.context = context;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            holder.episodeTitle.setText(items.get(position).getEpisodeTitle());
            holder.pubDate.setText(items.get(position).getPubdate());
            holder.duration.setText(items.get(position).getDuration());
            holder.castTitle = items.get(position).getCastTitle();
            holder.castImage = items.get(position).getCastImage();
            holder.category = items.get(position).getCategory();
            holder.musicUrl = items.get(position).getMusicUrl();


            //재생목록에 추가 되었는지 판단
            playlistCursor = null;
            playlistCursor = playListDbOpenHelper.getMatchEpiTitle(holder.episodeTitle.getText().toString());

            if (playlistCursor.getCount() > 0) {
                playlistCursor.moveToFirst();
                if (playlistCursor.getString(0).equals(SaveSharedPreference.getUserUnique(context))
                        && playlistCursor.getInt(playlistCursor.getColumnIndex("addList")) > 0) {
                    // holder.playListAdd.setVisibility(View.VISIBLE);
                    holder.addBtn.setVisibility(View.GONE);
                    holder.delBtn.setVisibility(View.VISIBLE);
                }
            }
            //버튼 모양이 섞이는 것을 막기위해서, else문을 꼭 해줘야한다.
            else {
                // holder.playListAdd.setVisibility(View.GONE);
                holder.addBtn.setVisibility(View.VISIBLE);
                holder.delBtn.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }


        public void addItem(final int position, final com.example.cmina.mycastcast.util.RssItem item) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    items.add(position, item);
                    notifyDataSetChanged();
                    //notifyItemInserted(position);
                }
            });


        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView episodeTitle;
            public TextView duration;
            public TextView pubDate;
            public ImageButton addBtn;
            public ImageButton delBtn;
            // public ImageView playListAdd;

            public String castImage;
            public String musicUrl;
            public String castTitle;
            public String category;


            public ViewHolder(View itemView) {
                super(itemView);

                episodeTitle = (TextView) itemView.findViewById(R.id.episode);
                duration = (TextView) itemView.findViewById(R.id.duration);
                pubDate = (TextView) itemView.findViewById(R.id.pubDate);
                addBtn = (ImageButton) itemView.findViewById(R.id.addPlayListBtn);
                delBtn = (ImageButton) itemView.findViewById(R.id.delPlayListBtn);
                //  playListAdd = (ImageView) itemView.findViewById(R.id.addPlayList);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayerConstants.SONG_NUMBER = getPosition();
                        Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
                        startActivity(intent);
                    }
                });

                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(SaveSharedPreference.getUserUnique(ListActivity.this).length() > 0) {
                            //재생목록추가
                            playListDbOpenHelper.insertColumn(SaveSharedPreference.getUserUnique(getApplicationContext()), rssItemList.get(getPosition()).getCastTitle(), rssItemList.get(getPosition()).getCastImage(),
                                    rssItemList.get(getPosition()).getMusicUrl(), rssItemList.get(getPosition()).getEpisodeTitle(), true);

                            Toast.makeText(ListActivity.this, "재생목록에 추가되었습니다", Toast.LENGTH_SHORT).show();

                            addBtn.setVisibility(View.GONE);
                            delBtn.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(ListActivity.this, "재생목록에 추가하려면 로그인하세요", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playListDbOpenHelper.deleteColumn(rssItemList.get(getPosition()).getEpisodeTitle());
                        Toast.makeText(ListActivity.this, "재생목록에서 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        addBtn.setVisibility(View.VISIBLE);
                        delBtn.setVisibility(View.GONE);
                    }
                });

            }
        }

    }
}
