package com.example.cmina.mycastcast.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.MainActivity;
import com.example.cmina.mycastcast.homefragment.FeedFragment1;
import com.example.cmina.mycastcast.homefragment.FeedFragment2;
import com.example.cmina.mycastcast.homefragment.FeedFragment3;
import com.example.cmina.mycastcast.homefragment.FeedFragment4;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import static com.example.cmina.mycastcast.activity.MainActivity.userSettingDbOpen;
import static com.example.cmina.mycastcast.util.PlayerConstants.MOBILE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.WIFI_STATE;
import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;

/**
 * Created by cmina on 2017-02-09.
 */

public class MainFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ViewPager pager;
    private PagerSlidingTabStrip tabStrip;

    //  private ListFragment feedFragment1;
    private Fragment feedFragment4;
    private Fragment feedFragment2;
    private Fragment feedFragment3;

    public MainFragment() {

    }

    public static MainFragment newInstance(String mParam1, String mParam2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, mParam1);
        args.putString(ARG_PARAM2, mParam2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("Mainfrag", "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Mainfrag", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("Mainfrag", "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("Mainfrag", "onStart");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("Mainfrag", "onCreateView");
        //다른 네비메뉴 눌렀다가 다시 눌렀을 때 onCreateView부터...
        getActivity().setTitle("MyCatCast");

        String networkState = UtilityFunctions.getWhatKindOfNetwork(getContext());

        if (networkState.equals(NONE_STATE)) {
            View view = inflater.inflate(R.layout.fragment_none, container, false);
            return view;
        } else {

            View view = inflater.inflate(R.layout.fragment_home, container, false);

            feedFragment4 = new FeedFragment4();
            String myJSON = getArguments().getString("myJSON");
            Log.e("myJSON Mainfrag", myJSON);
            Bundle bundle = new Bundle();
            bundle.putString("myJSON", myJSON);
            feedFragment4.setArguments(bundle);


            feedFragment2 = new FeedFragment2();
            feedFragment3 = new FeedFragment3();

            pager = (ViewPager) view.findViewById(R.id.pager);
            pager.setAdapter(new MainFragment.PageAdapter(getActivity().getSupportFragmentManager()));
            pager.setCurrentItem(0);
            pager.setOffscreenPageLimit(3);

            tabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
            tabStrip.setViewPager(pager);

            //wifi에서만 플레이 할 경우, 와이파이 체크해서 아니면 와이파이 연결하라고. 안내.
            cursor = userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext()));
            cursor.moveToFirst();
        /*Log.e("MainFrag", userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext())).isFirst() + ";;");
        Log.e("MainFrag", cursor.getString(1).toString() + ";;");*/
            //  if (userSettingDbOpen.getWIFI(SaveSharedPreference.getUserUnique(getContext())).isFirst()) { //로그인해서 설정값이 저장되어있을경우
            if (SaveSharedPreference.getUserUnique(getContext()).length() > 0) {

                if (cursor.getString(1).toString().equals("true") || cursor.getString(1).toString().equals("1")) {

                    if (networkState.equals(WIFI_STATE)) {
                        //오케이
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
                    return view;
                }
            } else {
                return view;
            }

        }

    }

    private String[] pageTitle = {"홈", "Top", "카테고리"};

    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return pageTitle[position];
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return feedFragment4;
                //return feedFragment1;
            } else if (position == 1) {
                // return new FeedFragment2();
                return feedFragment2;
            } else {
                // return new FeedFragment3();
                return feedFragment3;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
