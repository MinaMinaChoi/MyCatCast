package com.example.cmina.mycastcast.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.RssItem;
import com.example.cmina.mycastcast.util.SaveSharedPreference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.R.drawable.ic_delete;
import static com.example.cmina.mycastcast.R.id.episodeTitle;
import static com.example.cmina.mycastcast.R.id.pubDate;
import static com.example.cmina.mycastcast.activity.ListActivity.dbOpenHelper;
import static com.example.cmina.mycastcast.activity.ListActivity.playListDbOpenHelper;
import static com.example.cmina.mycastcast.util.PlayerConstants.playlistCursor;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by cmina on 2017-02-09.
 */

public class ListViewAdapter extends BaseAdapter {

    private List<RssItem> rssItemList = new ArrayList<>();
    private Context context;

    public ListViewAdapter(List<RssItem> rssItemList, Context context) {
        this.context = context;
        this.rssItemList = rssItemList;
    }


    @Override
    public int getCount() {
        return rssItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return rssItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView episodeTitle;
        TextView pubDate;
        TextView duration;
        ImageButton addBtn;
        ImageButton delBtn;
        boolean addPlayList;
        String category;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        final ViewHolder viewHolder;

        final RssItem item = rssItemList.get(position);
        final LayoutInflater inflater;

        if (view == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.episodeTitle = (TextView) view.findViewById(R.id.episode);
            viewHolder.pubDate = (TextView) view.findViewById(pubDate);
            viewHolder.duration = (TextView) view.findViewById(R.id.duration);
/*            viewHolder.addBtn = (ImageButton) view.findViewById(R.id.addPlayListBtn);
            viewHolder.delBtn = (ImageButton) view.findViewById(R.id.delPlayListBtn);*/

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();

        }

        viewHolder.episodeTitle.setText(item.getEpisodeTitle());
        viewHolder.pubDate.setText(item.getPubdate());
        viewHolder.duration.setText(item.getDuration());
       // viewHolder.category = item.getCategory();
/*
        //재생목록에 추가 되었는지 판단
        playlistCursor = null;
        playlistCursor = playListDbOpenHelper.getMatchEpiTitle(item.getEpisodeTitle());

        if (playlistCursor.getCount() > 0) {
            playlistCursor.moveToFirst();
            if (playlistCursor.getString(0).equals(SaveSharedPreference.getUserUnique(context))
                    && playlistCursor.getInt(playlistCursor.getColumnIndex("addList")) > 0 ) {

                viewHolder.delBtn.setVisibility(View.VISIBLE);
                viewHolder.addBtn.setVisibility(View.GONE);

            }
        }
        //버튼 모양이 섞이는 것을 막기위해서, else문을 꼭 해줘야한다.
        else {
            viewHolder.delBtn.setVisibility(View.GONE);
            viewHolder.addBtn.setVisibility(View.VISIBLE);
        }


        viewHolder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //재생목록추가
                playListDbOpenHelper.insertColumn(SaveSharedPreference.getUserUnique(context), item.getCastTitle(), item.getCastImage(),
                        item.getMusicUrl(), item.getEpisodeTitle(), true);

                viewHolder.delBtn.setVisibility(View.VISIBLE);
                viewHolder.addBtn.setVisibility(View.GONE);
                viewHolder.addPlayList = true;
                Toast.makeText(context, "재생목록에 추가했습니다", Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.delBtn.setVisibility(View.GONE);
                viewHolder.addBtn.setVisibility(View.VISIBLE);
                playListDbOpenHelper.deleteColumn(item.getEpisodeTitle());
                viewHolder.addPlayList = false;
                Toast.makeText(context, "재생목록에서 삭제했습니다", Toast.LENGTH_SHORT).show();
            }
        });

*/

        return view;
    }

}

