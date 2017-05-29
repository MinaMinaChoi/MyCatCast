package com.example.cmina.mycastcast.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.MainActivity;
import com.example.cmina.mycastcast.service.MusicService;
import com.example.cmina.mycastcast.util.DbOpenHelper;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.RecyclerCastItem;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.example.cmina.mycastcast.util.SimpleDividerItemDecoration;
import com.example.cmina.mycastcast.util.UtilityFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static android.R.attr.fragment;
import static com.example.cmina.mycastcast.R.id.feedBtn;
import static com.example.cmina.mycastcast.R.id.textView;
import static com.example.cmina.mycastcast.activity.ListActivity.dbOpenHelper;
import static com.example.cmina.mycastcast.activity.ListActivity.playListDbOpenHelper;
import static com.example.cmina.mycastcast.activity.MainActivity.userSettingDbOpen;

import static com.example.cmina.mycastcast.util.PlayerConstants.MOBILE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.WIFI_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by cmina on 2017-02-09.
 */

public class FeedFragment extends Fragment {

    TextView textview;

    //recyclerview
    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FeedFragment() {

    }

    public static FeedFragment newInstance(String mParam1, String mParam2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feedlistfragment, menu);
        //  super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.del_feedlist:
/*                boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), getApplicationContext());
                // PlayCase == 1 && NOW_SONG_URL.length() > 0
                if (*//*PlayerConstants.PlayCase != 1 && *//*!isServiceRunning ) { //재생목록에서 플레이중이 아닐때만*/
                dbOpenHelper.deleteAll();

                //화면갱신
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                FeedFragment fragment = new FeedFragment();
                transaction.replace(R.id.content_main, fragment);
                transaction.commit();
               /* } else {
                    Toast.makeText(context, "재생목록이 플레이중일때는 목록지우기 할 수 없습니다", Toast.LENGTH_SHORT).show();
                }*/

                return true;

        }
        return false;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("구독목록");
        setHasOptionsMenu(true);

        String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());

        View view = inflater.inflate(R.layout.fragment_feedlist, null);

        context = getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        textview = (TextView) view.findViewById(R.id.feedText);

        //sqlite에 목록이 있으면 리스트뷰로 보여주기
        try {
            dbOpenHelper = new DbOpenHelper(getActivity().getApplicationContext());
            dbOpenHelper.open();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        //wifi에서만 플레이 할 경우, 와이파이 체크해서 아니면 와이파이 연결하라고. 안내.
        cursor = userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext()));
        cursor.moveToFirst();

        if (SaveSharedPreference.getUserUnique(context).length() > 0) { //로그인했을 떄
            //와이파이에서만 설정
            if (cursor.getString(1).toString().equals("true") || cursor.getString(1).toString().equals("1")) {

                if (networkState.equals(WIFI_STATE)) {
                    //오케이
                    doWhileCursorToArray();
                    return view;
                } else if (networkState.equals(MOBILE_STATE)) {
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
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            SettingFragment settingFragment = new SettingFragment();
                            transaction.replace(R.id.content_main, settingFragment);
                            transaction.commit();

                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return null;
                }
                return null;
            } else {
                //와이파이에서만 아닐때!
                doWhileCursorToArray();
                return view;
            }
        } else {  //로그인 안했을 때
            doWhileCursorToArray();
            return view;
        }


    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void doWhileCursorToArray() {
        cursor = null;
        //uniqueID에 해당하는 칼럼..가져오기
        cursor = dbOpenHelper.getFeedList(SaveSharedPreference.getUserUnique(getContext()));

        Log.e("칼럼의 개수확인", "Count = " + cursor.getCount());

        if (cursor.getCount() != 0) {
            textview.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayList<RecyclerCastItem> items = new ArrayList<>();

        while (cursor.moveToNext()) {

            items.add(new RecyclerCastItem(cursor.getString(cursor.getColumnIndex("castimage")),
                    cursor.getString(cursor.getColumnIndex("casttitle")),
                    cursor.getString(cursor.getColumnIndex("castdbname")), cursor.getString(cursor.getColumnIndex("category"))
            ));

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);

            recyclerAdapter = new MyAdapter(items, context);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));

        }

        cursor.close();

    }


    @Override
    public void onDestroyView() {
        if (dbOpenHelper != null)
            dbOpenHelper.close();
        super.onDestroyView();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Context context;
        private ArrayList<RecyclerCastItem> items;

        private int lastPosition = -1;

        public MyAdapter(ArrayList<RecyclerCastItem> items, Context context) {
            this.items = items;
            this.context = context;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_listview_item, parent, false);
            ViewHolder holder = new ViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Glide.with(context).load(items.get(position).getCastImage()).thumbnail(0.1f).into(holder.imageView);
            holder.castTitle.setText(items.get(position).getCastTitle());
            holder.castDbName.setText(items.get(position).getCastDbName());
            Integer category = Integer.valueOf(items.get(position).getCategory());
            String cateName = null;
            switch (category) {
                case 0:
                    cateName = "코미디";
                    break;
                case 1:
                    cateName = "영화/음악";
                    break;
                case 2:
                    cateName = "어학/교육";
                    break;
                case 3:
                    cateName = "정치/시사";
                    break;
                case 4:
                    cateName = "문화/교양";
                    break;
                case 5:
                    cateName = "건강/의학";
                    break;
                case 6:
                    cateName = "종교";
                    break;
                case 7:
                    cateName = "여행";
                    break;

            }

            holder.category.setText(cateName);

            //  setAnimation(holder.imageView, position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView castTitle;
            public TextView castDbName;
            public TextView category;


            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.castImage);
                castTitle = (TextView) itemView.findViewById(R.id.episodeTitle);
                castDbName = (TextView) itemView.findViewById(R.id.castDbName);
                category = (TextView) itemView.findViewById(R.id.castTitle);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(getContext(), getPosition()+"", Toast.LENGTH_SHORT).show();
                        PlayerConstants.CAST_DB_NUMBER = Integer.parseInt(items.get(getPosition()).getCastDbName().replace("cast", ""));
                        Intent intent = new Intent(getActivity(), com.example.cmina.mycastcast.activity.ListActivity.class);
                        intent.putExtra("CAST_DB_NUMBER", Integer.parseInt(items.get(getPosition()).getCastDbName().replace("cast", "")));
                        startActivity(intent);
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("구독목록삭제");
                        builder.setMessage("구독목록에서 삭제하겠습니까?")
                                .setCancelable(true)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        FirebaseMessaging.getInstance().subscribeToTopic("news");
                                        String token = FirebaseInstanceId.getInstance().getToken();

                                        JSONObject jsonObject = new JSONObject();

                                        try {
                                            jsonObject.put("token", token);
                                            jsonObject.put("castName", "cast" + PlayerConstants.CAST_DB_NUMBER);
                                            jsonObject.put("uniqueID", SaveSharedPreference.getUserUnique(context));

                                            HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/register.php");

                                            String result = request.execute(jsonObject.toString()).get();
                                            if (result.equals("2")) {
                                                //구독취소
                                                dbOpenHelper.deleteColumn(items.get(getPosition()).getCastTitle());
                                                //화면갱신
                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                FeedFragment fragment = new FeedFragment();
                                                transaction.replace(R.id.content_main, fragment);
                                                transaction.commit();
                                            } else {
                                                Toast.makeText(getContext(), "다시 시도해주세요", Toast.LENGTH_SHORT).show();
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


                                    }
                                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        return false;
                    }
                });
            }
        }

        private void setAnimation(View viewToAnimate, int position) {
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }
    }
}
