package com.example.cmina.mycastcast.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.util.RecyclerCastItem;
import com.example.cmina.mycastcast.util.ViewpagerItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by cmina on 2017-02-27.
 */

public class ViewpagerAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;
    public ArrayList<RecyclerCastItem> viewpagerItemArrayList = new ArrayList<RecyclerCastItem>();

    public ViewpagerAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    public class Viewholder {
        ImageView bigimage;
        ImageView smallimage;
        TextView castName;
        TextView castDbName;
        TextView category;
    }

    @Override
    public int getCount() {
        return viewpagerItemArrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= viewpagerItemArrayList.size();

        context = container.getContext();

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = null;

        view = inflater.inflate(R.layout.viewpager_childview, null);

        final Viewholder viewholder = new Viewholder();
        viewholder.bigimage = (ImageView) view.findViewById(R.id.pagerBigImage);
/*        Drawable alpha = ((ImageView) view.findViewById(R.id.pagerBigImage)).getBackground();
        alpha.setAlpha(50);*/
        viewholder.smallimage = (ImageView) view.findViewById(R.id.pagerCastImage);
        viewholder.castName = (TextView) view.findViewById(R.id.pagerCastTitle);
        viewholder.castDbName = (TextView) view.findViewById(R.id.pagerCastDbName);
        viewholder.category = (TextView) view.findViewById(R.id.pagerCategory);


        view.setTag(viewholder);

        RecyclerCastItem item = viewpagerItemArrayList.get(position);
        String url = item.getCastImage();
        String castName = item.getCastTitle();
        String castDbName = item.getCastDbName();
        Integer category = Integer.valueOf(item.getCategory());

        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(viewholder.smallimage);


        Picasso.with(context).load(url).transform(new com.example.cmina.mycastcast.util.BlurTransformation(context, 50)).into(viewholder.bigimage);
        //Glide.with(context).load(url).bitmapTransform(new ColorFilterTransformation(context, Color.argb(100,100,100, 150))).into(viewholder.bigimage);
        viewholder.castName.setText(castName);
        viewholder.castDbName.setText(castDbName);
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
        viewholder.category.setText(cateName);

        container.addView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(context, viewholder.castDbName.getText()+"", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ListActivity.class);
                intent.putExtra("CAST_DB_NUMBER", Integer.parseInt((String) viewholder.castDbName.getText()));
                context.startActivity(intent);
            }
        });

        return view;

    }

    public void addPagerItem(String castimage, String castTitle, String castDbName, String category) {
        RecyclerCastItem item = new RecyclerCastItem(castimage, castTitle, castDbName, category);
        viewpagerItemArrayList.add(item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
