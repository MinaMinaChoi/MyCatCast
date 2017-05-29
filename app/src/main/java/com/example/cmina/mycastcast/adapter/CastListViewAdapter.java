package com.example.cmina.mycastcast.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.util.CastListItem;

import java.util.ArrayList;

/**
 * Created by cmina on 2017-02-09.
 */

public class CastListViewAdapter extends BaseAdapter {
    public ArrayList<CastListItem> castListItemArrayList = new ArrayList<CastListItem>();

    public CastListViewAdapter() {

    }

    public CastListViewAdapter(ArrayList<CastListItem> arrayList) {
        castListItemArrayList = arrayList;
    }

    @Override
    public int getCount() {
        return castListItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return castListItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView castTitle;
        ImageView castImage;
        TextView castDbName;
        TextView castUpdate;
        TextView castFeedCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        // final Bitmap[] bitmap = new Bitmap[1];
        ViewHolder viewHolder;
        //  View listviewItem = convertView;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.castlistitem, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.castTitle = (TextView) convertView.findViewById(R.id.castListTitle);
            viewHolder.castImage = (ImageView) convertView.findViewById(R.id.castListImage);
            viewHolder.castDbName = (TextView) convertView.findViewById(R.id.castDbName);
            viewHolder.castUpdate = (TextView) convertView.findViewById(R.id.castListUpdate);
            viewHolder.castFeedCount = (TextView) convertView.findViewById(R.id.castFeedCount);

            convertView.setTag(viewHolder);
        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CastListItem castListItem = castListItemArrayList.get(position);



        Glide.with(context).load(castListItem.getCastImage()).into(viewHolder.castImage);

        viewHolder.castTitle.setText(castListItem.getCastTitle());
        viewHolder.castDbName.setText(castListItem.getCastdbname());
        viewHolder.castFeedCount.setText(castListItem.getFeedcount()+"");
        viewHolder.castUpdate.setText(castListItem.getUpdateDate());

        return convertView;
    }
    public void addItem(String castdbname, String castTitle, String castImage, String castUpdate, Integer castFeedCount) {
        CastListItem item = new CastListItem(castdbname, castTitle, castImage, castUpdate, castFeedCount);

        item.setCastImage(castImage);
        item.setCastTitle(castTitle);
        item.setCastdbname(castdbname);
        item.setUpdateDate(castUpdate);
        item.setFeedcount(castFeedCount);

        castListItemArrayList.add(item);

    }



}

