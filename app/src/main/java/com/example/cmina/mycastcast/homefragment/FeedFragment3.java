package com.example.cmina.mycastcast.homefragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.CategoryListActivity;
import com.example.cmina.mycastcast.util.CardViewItem;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import java.util.ArrayList;

import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;

/**
 * Created by cmina on 2017-02-09.
 */

public class FeedFragment3 extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    public FeedFragment3() {

    }


    public static FeedFragment3 newInstance(String mParam1, String mParam2) {
        FeedFragment3 fragment = new FeedFragment3();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("inner frag 33", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("inner frag 33", "onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("inner frag 33", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("inner frag 33", "onStart");
    }


    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());

        if (networkState.equals(NONE_STATE)) {
            View view = inflater.inflate(R.layout.fragment_none, container, false);
            return view;
        } else {
            View view = inflater.inflate(R.layout.homefragment3, null);

            context = getContext();
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            ArrayList<CardViewItem> items = new ArrayList<>();

            items.add(new CardViewItem(R.drawable.comedy, "코미디"));
            items.add(new CardViewItem(R.drawable.music, "영화/음악"));
            items.add(new CardViewItem(R.drawable.langstudy, "어학/교육"));
            items.add(new CardViewItem(R.drawable.politics, "정치/시사"));
            items.add(new CardViewItem(R.drawable.culture, "문화/교양"));
            items.add(new CardViewItem(R.drawable.health, "건강/의학"));
            items.add(new CardViewItem(R.drawable.religion, "종교"));
            items.add(new CardViewItem(R.drawable.travel, "여행"));

            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            // layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new MyAdapter(items, context);
            recyclerView.setAdapter(adapter);

            return view;
        }

    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Context context;
        private ArrayList<CardViewItem> items;

        private int lastPosition = -1;

        public MyAdapter(ArrayList<CardViewItem> items, Context context) {
            this.items = items;
            this.context = context;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, parent, false);
            ViewHolder holder = new ViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

           // Picasso.with(getContext()).load(items.get(position).getImage()).transform(new com.example.cmina.mycastcast.util.BlurTransformation(getContext(), 25))
                //    .into(holder.imageView);
            holder.imageView.setImageResource(items.get(position).getImage());
            holder.textView.setText(items.get(position).getImagetitle());

          //  setAnimation(holder.imageView, position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.castImage);
                textView = (TextView) itemView.findViewById(R.id.castTitle);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(getContext(), getPosition()+"", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                        intent.putExtra("cateName", items.get(getPosition()).getImagetitle());
                        intent.putExtra("category", getPosition());
                        startActivity(intent);
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