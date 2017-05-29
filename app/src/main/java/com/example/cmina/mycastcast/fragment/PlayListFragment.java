package com.example.cmina.mycastcast.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ContentActivity;
import com.example.cmina.mycastcast.activity.MainActivity;
import com.example.cmina.mycastcast.service.MusicService;
import com.example.cmina.mycastcast.util.PlayListDbOpenHelper;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.RssItem;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.example.cmina.mycastcast.util.SimpleDividerItemDecoration;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.cmina.mycastcast.activity.ListActivity.playListDbOpenHelper;
import static com.example.cmina.mycastcast.activity.MainActivity.userSettingDbOpen;
import static com.example.cmina.mycastcast.service.MusicService.mp;
import static com.example.cmina.mycastcast.util.PlayerConstants.MOBILE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.NOW_SONG_URL;
import static com.example.cmina.mycastcast.util.PlayerConstants.WIFI_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;
import static com.example.cmina.mycastcast.util.PlayerConstants.playlistCursor;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by cmina on 2017-02-09.
 */

public class PlayListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextView textView;
    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerAdapter;
    RssItem rssItem;

    public PlayListFragment() {

    }

    public static PlayListFragment newInstance(String mParam1, String mParam2) {
        PlayListFragment fragment = new PlayListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   Toast.makeText(getContext(), "N", Toast.LENGTH_SHORT).show();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.playlistfragment, menu);
      //  super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.del_playlist:
                boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), getApplicationContext());
               // PlayCase == 1 && NOW_SONG_URL.length() > 0
                if (/*PlayerConstants.PlayCase != 1 && */!isServiceRunning ) { //재생목록에서 플레이중이 아닐때만
                    playListDbOpenHelper.deleteAll();
                    //화면갱신
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    PlayListFragment fragment = new PlayListFragment();
                    transaction.replace(R.id.content_main, fragment);
                    transaction.commit();
                } else {
                    Toast.makeText(context, "재생목록이 플레이중일때는 목록지우기 할 수 없습니다", Toast.LENGTH_SHORT).show();
                }

                return true;

        }
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("재생목록");
        //액션바 메뉴
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        PlayerConstants.SONGS_LIST.clear();

        context = getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        textView = (TextView) view.findViewById(R.id.playlistText);

        try {
            playListDbOpenHelper = new PlayListDbOpenHelper(getActivity().getApplicationContext());
            playListDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //wifi에서만 플레이 할 경우, 와이파이 체크해서 아니면 와이파이 연결하라고. 안내.
        cursor = userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext()));
        cursor.moveToFirst();
        if (SaveSharedPreference.getUserUnique(context).length() > 0) { //로그인해서 설정값이 저장되어있을경우

            if (cursor.getString(1).toString().equals("true") || cursor.getString(1).toString().equals("1")) {
                String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());
                if (networkState.equals(WIFI_STATE)) {
                    //오케이
                    doWhileCursorToArray();
                    return view;
                } else if (networkState.equals(MOBILE_STATE)){
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
                            //return;
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return null;
                } else { //여기 왜 안되징??
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("인터넷이 연결되어 있지 않습니다. 모바일데이터나 wifi를 연결하세요").setCancelable(false)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivity(intent);
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    return null;
                }
            } else {
                doWhileCursorToArray();
                return view;
            }
        } else  {
            doWhileCursorToArray();
            return view;
        }

    }

    private void doWhileCursorToArray() {

        playlistCursor = null;

        playlistCursor = playListDbOpenHelper.getPlayList(SaveSharedPreference.getUserUnique(context));

        Log.e("칼럼의 개수확인", "Count = " + playlistCursor.getCount());

        if (playlistCursor.getCount() !=0) {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayList<RssItem> items = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        while (playlistCursor.moveToNext()) {
            //송리스트에도.
            rssItem = new RssItem();

            rssItem.setCastImage(playlistCursor.getString(playlistCursor.getColumnIndex("castimage")));
            rssItem.setCastTitle(playlistCursor.getString(playlistCursor.getColumnIndex("casttitle")));
            rssItem.setEpisodeTitle(playlistCursor.getString(playlistCursor.getColumnIndex("episodeTitle")));
            rssItem.setMusicUrl(playlistCursor.getString(playlistCursor.getColumnIndex("musicUrl")));

            items.add(rssItem);

            //플레이리스트에 추가
            PlayerConstants.SONGS_LIST.add(rssItem);

            recyclerAdapter = new MyAdapter(items, context);
            recyclerView.setAdapter(recyclerAdapter);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        }

        playlistCursor.close();
    }

    @Override
    public void onDestroyView() {
        if (playListDbOpenHelper != null) {
            playListDbOpenHelper.close();
        }
        super.onDestroyView();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private Context context;
        private ArrayList<RssItem> items;

        private int lastPosition =  -1;

        public MyAdapter(ArrayList<RssItem> items, Context context) {
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
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {

            Glide.with(context).load(items.get(position).getCastImage()).into(holder.imageView);
            holder.episodeTitle.setText(items.get(position).getEpisodeTitle());
            holder.castTitle.setText(items.get(position).getCastTitle());
            holder.musicUrl.setText(items.get(position).getMusicUrl());

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView episodeTitle;
            public TextView musicUrl;
            public TextView castTitle;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = (ImageView) itemView.findViewById(R.id.castImage);
                episodeTitle = (TextView) itemView.findViewById(R.id.episodeTitle);
                castTitle = (TextView) itemView.findViewById(R.id.castTitle);
                musicUrl = (TextView) itemView.findViewById(R.id.castDbName);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayerConstants.SONG_NUMBER = getPosition();
                        Intent intent = new Intent(context, ContentActivity.class);
                       // Toast.makeText(getContext(), items.get(getPosition()).getEpisodeTitle()+"", Toast.LENGTH_SHORT).show();
                        startActivity(intent);

                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), getApplicationContext());
                        if (/*PlayCase == 1 && */isServiceRunning) {
                            Toast.makeText(context, "재생목록 플레이 중에는 삭제할 수 없습니다", Toast.LENGTH_SHORT).show();
                        } else  {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("재생목록삭제");
                            builder.setMessage("재생목록에서 삭제하겠습니까?")
                                    .setCancelable(true)
                                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            playListDbOpenHelper.deleteColumn(items.get(getPosition()).getEpisodeTitle());
                                            //화면갱신
                                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                            PlayListFragment fragment = new PlayListFragment();
                                            transaction.replace(R.id.content_main, fragment);
                                            transaction.commit();

                                            //PlayerConstants.SONGS_LIST.remove(getPosition());

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
                });

            }
        }
    }
}
