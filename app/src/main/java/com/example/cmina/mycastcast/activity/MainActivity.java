package com.example.cmina.mycastcast.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.fragment.FeedFragment;
import com.example.cmina.mycastcast.fragment.MainFragment;
import com.example.cmina.mycastcast.fragment.PlayListFragment;
import com.example.cmina.mycastcast.fragment.SettingFragment;
import com.example.cmina.mycastcast.service.MyFirebaseMessagingService;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.NetworkUtil;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.example.cmina.mycastcast.util.UserSettingDbOpen;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.example.cmina.mycastcast.util.PlayerConstants.cursor;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //이미지저장을 위한...일반로그인시.
    public static SharedPreferences settingShared;
    public static SharedPreferences.Editor editor;

    ImageView profileImageview;
    TextView profileName;
    private Fragment mainfragment;
    private Fragment feedfragment;
    private Fragment playlistfragment;
    private Fragment settingfragment;

    String myJSON = "";
    //userSetting값 저장을 위한 sqlite
    public static UserSettingDbOpen userSettingDbOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkUtil.setNetworkPolicy();

        //splash 액티비티에서 로딩데이터 받음.
        Intent intent = getIntent();
        if (getIntent().toString().contains("extras")) {
           myJSON = intent.getStringExtra("myJSON");
        }

        settingShared = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = settingShared.edit();

        //setting데이터베이스 생성및 오픈.
        userSettingDbOpen = new UserSettingDbOpen(MainActivity.this);
        try {
            userSettingDbOpen.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //fcm토큰 받기.
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();

        //해시키구하기
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo( "com.example.cmina.mycastcast", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("KeyHash:", "name not found");
        } catch (NoSuchAlgorithmException e) {
            Log.d("KeyHash:", "no such");
        }*/


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setSubtitleTextColor(Color.BLACK);

        mainfragment = new MainFragment();

        Bundle bundle = new Bundle();
        bundle.putString("myJSON", myJSON);
        mainfragment.setArguments(bundle);

        feedfragment = new FeedFragment();
        playlistfragment = new PlayListFragment();
        settingfragment = new SettingFragment();

        final android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //첫화면에 mainfragment를 보여주겠다.
        transaction.add(R.id.content_main, mainfragment);
        //  transaction.addToBackStack(null); 백키 먹지 않게
        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        profileImageview = (ImageView) header.findViewById(R.id.profileImage);
        profileName = (TextView) header.findViewById(R.id.profileName);

        navigationView.setNavigationItemSelectedListener(this);

        //헤더에 이름, 이미지 보여주기. 저장된 이메일값으로 디비가져 이름, 이미지 가져오기.

        if (SaveSharedPreference.getUserUnique(MainActivity.this).length() == 0) {

            Log.e("Mainactivity", SaveSharedPreference.getUserUnique(this)+"///");
            //call loginactivity
            profileImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toast.makeText(MainActivity.this, "로그인화면으로 이동", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
        } else { //로그인이 되어 있는 상태면..디비가서 유니크한 값에 맞는 이름과 이미지 가져오기.
            Log.e("Mainactivity", SaveSharedPreference.getUserUnique(this));
            try {
                //로그인하면...세팅정보 sqlite에. unique비교해서. 없으면....
                cursor = null;
                cursor = userSettingDbOpen.getUniqueID(SaveSharedPreference.getUserUnique(MainActivity.this));
                if (cursor.getCount() <= 0) {
                    userSettingDbOpen.insertColumn(SaveSharedPreference.getUserUnique(MainActivity.this),
                            true, false);
                    Log.e("sqlite", "setting sqlite ");
                }

                //푸시받을지 말지에 따라서. 초기세팅
                cursor = userSettingDbOpen.getPush(SaveSharedPreference.getUserUnique(MainActivity.this));
                cursor.moveToFirst();
                if (cursor.getString(2).toString().equals("true") || cursor.getString(2).toString().equals("1")) {
                    PackageManager pm = getApplicationContext().getPackageManager();
                    ComponentName componentName = new ComponentName(getApplicationContext(), MyFirebaseMessagingService.class);
                    pm.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                } else {

                    PackageManager pm = getApplicationContext().getPackageManager();
                    ComponentName componentName = new ComponentName(getApplicationContext(), MyFirebaseMessagingService.class);
                    pm.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }

                if (SaveSharedPreference.getLoginCase(MainActivity.this).toString().equals("1")) {
                    //자체 로그인
                    HttpUtil validateRequest = new HttpUtil("http://cmina21.cafe24.com/validateEmail.php");
                    JSONObject jsonobject = new JSONObject();
                    jsonobject.put("user_email", SaveSharedPreference.getUserUnique(MainActivity.this));
                    String result = validateRequest.PhPtest(jsonobject.toString());
                    if (result.equals("1")) {
                        Toast.makeText(this, "회원가입하세요", Toast.LENGTH_SHORT).show();
                    } else {
                        JSONObject object = new JSONObject(result);
                        JSONArray array = object.getJSONArray("result");

                        profileName.setText(array.getJSONObject(0).getString("user_name"));

                        if (settingShared.getString(SaveSharedPreference.getUserUnique(getApplicationContext()), "") != "") {
                            Glide.with(MainActivity.this).load(settingShared.getString(SaveSharedPreference.getUserUnique(this), "")).bitmapTransform(new CropCircleTransformation(this)).into(profileImageview);
                        }
                    }


                } else if (SaveSharedPreference.getLoginCase(MainActivity.this).toString().equals("2")) {
                    //연동 로그인
                    profileName.setText(SaveSharedPreference.getUserName(MainActivity.this));

                    //연동로그인사용시, 만약 쉐어드에 저장된 이미지가 있으면 그 이미지를 보여주고, 아니면 api에서 받은 이미지를...
                    if (settingShared.getString(SaveSharedPreference.getUserUnique(getApplicationContext()), "") != "") {
                        Glide.with(MainActivity.this).load(settingShared.getString(SaveSharedPreference.getUserUnique(this), "")).bitmapTransform(new CropCircleTransformation(this)).into(profileImageview);
                    } else {
                        String link = SaveSharedPreference.getUserImage(MainActivity.this);
                        Glide.with(MainActivity.this).load(link).bitmapTransform(new CropCircleTransformation(this)).into(profileImageview);
                    }

                    Log.e("main", SaveSharedPreference.getUserImage(MainActivity.this));
                    Log.e("main", SaveSharedPreference.getUserUnique(MainActivity.this));

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(this, "인터넷을 연결하세요", Toast.LENGTH_SHORT).show();
            }


        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        int id = item.getItemId();

        if (id == R.id.nav_main) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getSupportFragmentManager().popBackStack();

        } else if (id == R.id.nav_feedlist) {

            transaction.replace(R.id.content_main, feedfragment);
        } else if (id == R.id.nav_playlist) {
            transaction.replace(R.id.content_main, playlistfragment);

        } else if (id == R.id.nav_setting) {

            if (SaveSharedPreference.getUserUnique(MainActivity.this).length() != 0) {
                transaction.replace(R.id.content_main, settingfragment); //로그인상태에서만 설정에 들어갈 수 있다.

            } else {
                Toast.makeText(this, "로그인을 하세요", Toast.LENGTH_SHORT).show();
            }

        }

        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
