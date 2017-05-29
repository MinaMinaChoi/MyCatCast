package com.example.cmina.mycastcast.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.controls.Controls;
import com.example.cmina.mycastcast.service.MusicService;
import com.example.cmina.mycastcast.util.NetworkUtil;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.cmina.mycastcast.R.id.episode;
import static com.example.cmina.mycastcast.service.MusicService.mp;
import static com.example.cmina.mycastcast.util.PlayerConstants.NONE_STATE;

/**
 * Created by cmina on 2017-02-09.
 */

public class ContentActivity extends AppCompatActivity {

    //팟캐스트 다운로드
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ImageButton downBtn;
    private ImageButton downCancelBtn;
    private static ImageButton timermintBtn;
    private static ImageButton timerBtn;
    private ImageButton allplayBtn;
    private ImageButton oneplayBtn;

    //download manager
    private DownloadManager downloadManager;
    private DownloadManager.Request request;
    private long latestId = -1;
    private int downCase = 0; //다운로드케이스 초기값,

    private boolean hasPermission(String[] permissions) {
        int res = 0;
        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false; //퍼미션허가 안된 경우
            }
        }
        return true; //허가
    }

    //마시멜로이상 런타임퍼미션요청
    private void requestNecessaryPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    static Bitmap bitmap;

    AlertDialog alertDialog;

    static TextView castTitle, episodeTitle;
    static ImageView castImage;

    static String musicUrl;

    public static SeekBar seekBar;
    public static TextView durationText;

    public static ImageButton playButton, pauseButton, backwardButton, forwardButton, prevButton, nextButton;

    // private Button repeatBtn, timerBtn;

    final String alertOptions[] = {"사용안함", "현재 에피소드까지", "10분", "30분", "1시간", "2시간"};

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //다운로드 버튼 클릭시 네트워크 사용을 위해.
        NetworkUtil.setNetworkPolicy();

        String networkState = UtilityFunctions.getWhatKindOfNetwork(ContentActivity.this);

        if (networkState.equals(NONE_STATE)) {
            Toast.makeText(this, "인터넷을 연결해야 재생할 수 있습니다.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            setContentView(R.layout.activity_content);

            getViews();
            //액션바 백버튼 추가
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            timerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("PlayerConstants", PlayerConstants.timer + "");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);

                    builder.setTitle("취침 타이머");

                    builder.setSingleChoiceItems(alertOptions, 0, new DialogInterface.OnClickListener() { //-1은 아직 선택된게 없음을 의미. 선택한 곳의 넘버
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    PlayerConstants.timer = 0;
                                    return;

                                case 1:
                                    PlayerConstants.timer = 1;
                                    return;
                                case 2:
                                    // 600000
                                    PlayerConstants.timer = 2;
                                    return;
                                case 3:
                                    PlayerConstants.timer = 3;
                                    return;
                                case 4:
                                    PlayerConstants.timer = 4;
                                    return;
                                case 5:
                                    PlayerConstants.timer = 5;
                                    return;

                            }

                        }
                    }).setPositiveButton("확인", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.e("PlayerConstants 확인", PlayerConstants.timer + "");
                            switch (PlayerConstants.timer) {
                                case 0:
                                    PlayerConstants.timerUse = false;
                                    timerBtn.setVisibility(View.VISIBLE);
                                    timermintBtn.setVisibility(View.GONE);
                                    return;
                                case 1:
                                    Toast.makeText(ContentActivity.this, "현재 에피소드까지만 재생됩니다.", Toast.LENGTH_SHORT).show();
                                    PlayerConstants.timerUse = true;
                                    timerBtn.setVisibility(View.GONE);
                                    timermintBtn.setVisibility(View.VISIBLE);
                                    return;
                                case 2: //600000
                                    Toast.makeText(ContentActivity.this, "10분 타이머(일단 10초)가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                    Controls.timerControl("10000");
                                    PlayerConstants.timerUse = true;
                                    timerBtn.setVisibility(View.GONE);
                                    timermintBtn.setVisibility(View.VISIBLE);
                                    return;
                                case 3:
                                    Toast.makeText(ContentActivity.this, "30분 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                    Controls.timerControl("1800000");
                                    PlayerConstants.timerUse = true;
                                    timerBtn.setVisibility(View.GONE);
                                    timermintBtn.setVisibility(View.VISIBLE);
                                    return;
                                case 4:
                                    Toast.makeText(ContentActivity.this, "1시간 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                    Controls.timerControl("3600000");
                                    PlayerConstants.timerUse = true;
                                    timerBtn.setVisibility(View.GONE);
                                    timermintBtn.setVisibility(View.VISIBLE);

                                    return;
                                case 5:
                                    Toast.makeText(ContentActivity.this, "2시간 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                    Controls.timerControl("7200000");
                                    PlayerConstants.timerUse = true;
                                    timerBtn.setVisibility(View.GONE);
                                    timermintBtn.setVisibility(View.VISIBLE);
                                    return;
                            }

                        }
                    }).create().show();

                }
            });


            timermintBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("PlayerConstants", PlayerConstants.timer + "");
                    //타이머를 취소하겠습니까 다이얼로그.
                    AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);
                    builder.setMessage("취침 타이머를 취소하시겠습니까?").setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PlayerConstants.timerUse = false;
                            PlayerConstants.timer = 0;
                            timerBtn.setVisibility(View.VISIBLE);
                            timermintBtn.setVisibility(View.GONE);

                            //타이머 취소
                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });

/*            timerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PlayerConstants.timerUse) //타이머 사용중이면
                    {
                        Log.e("PlayerConstants", PlayerConstants.timer + "");
                        //타이머를 취소하겠습니까 다이얼로그.
                        AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);
                        builder.setMessage("취침 타이머를 취소하시겠습니까?").setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PlayerConstants.timerUse = false;
                                PlayerConstants.timer = 0;

                                //타이머 취소
                            }
                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });

                        alertDialog = builder.create();
                        alertDialog.show();

                    } else { //타이머 설정하러러
                        Log.e("PlayerConstants", PlayerConstants.timer + "");
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);

                        builder.setTitle("취침 타이머");


                        builder.setSingleChoiceItems(alertOptions, 0, new DialogInterface.OnClickListener() { //-1은 아직 선택된게 없음을 의미. 선택한 곳의 넘버
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        PlayerConstants.timer = 0;
                                        return;

                                    case 1:
                                        PlayerConstants.timer = 1;
                                        return;
                                    case 2:
                                        // 600000
                                        PlayerConstants.timer = 2;
                                        return;
                                    case 3:
                                        PlayerConstants.timer = 3;
                                        return;
                                    case 4:
                                        PlayerConstants.timer = 4;
                                        return;
                                    case 5:
                                        PlayerConstants.timer = 5;
                                        return;

                                }

                            }
                        }).setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("PlayerConstants 확인", PlayerConstants.timer + "");
                                switch (PlayerConstants.timer) {
                                    case 0:
                                        PlayerConstants.timerUse = false;
                                        return;
                                    case 1:
                                        Toast.makeText(ContentActivity.this, "현재 에피소드까지만 재생됩니다.", Toast.LENGTH_SHORT).show();
                                        PlayerConstants.timerUse = true;
                                        timerBtn.setBackgroundColor(R.color.colorHighlight);
                                        return;
                                    case 2: //600000
                                        Toast.makeText(ContentActivity.this, "10분 타이머(일단 10초)가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                        Controls.timerControl("10000");
                                        PlayerConstants.timerUse = true;
                                        timerBtn.setBackgroundColor(R.color.colorHighlight);
                                        return;
                                    case 3:
                                        Toast.makeText(ContentActivity.this, "30분 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                        Controls.timerControl("1800000");
                                        PlayerConstants.timerUse = true;
                                        timerBtn.setBackgroundColor(R.color.colorHighlight);
                                        return;
                                    case 4:
                                        Toast.makeText(ContentActivity.this, "1시간 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                        Controls.timerControl("3600000");
                                        PlayerConstants.timerUse = true;
                                        timerBtn.setBackgroundColor(R.color.colorHighlight);
                                        return;
                                    case 5:
                                        Toast.makeText(ContentActivity.this, "2시간 타이머가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                        Controls.timerControl("7200000");
                                        PlayerConstants.timerUse = true;
                                        timerBtn.setBackgroundColor(R.color.colorHighlight);
                                        return;
                                }

                            }
                        }).create().show();

                    }

                }
            });*/

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Toast.makeText(ContentActivity.this, "터치터치", Toast.LENGTH_SHORT).show();
                    Log.d("시크바터치", seekBar + "");
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mp != null) {
                        int totalDuration = mp.getDuration();
                        int currentPosition = UtilityFunctions.progressToTimer(seekBar.getProgress(), totalDuration);

                        mp.seekTo(currentPosition);

                    }

                }
            });

            setListeners();
            PlayerConstants.SONG_PAUSED = false;
            Log.d("송포지션확인", PlayerConstants.SONG_NUMBER + "");
            Log.d("서비스 NOW SONG URL", PlayerConstants.NOW_SONG_URL + "");

            boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), getApplicationContext());


            if (!isServiceRunning) {
                Log.d("서비스 처음 시작", musicUrl + "");
                Intent i = new Intent(getApplicationContext(), MusicService.class);
                startService(i);
                updateUI();
            } else {
                if (PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl().equals(PlayerConstants.NOW_SONG_URL)) {
                    updateUI();
                } else {
                    try {
                        PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                        Log.d("서비스 새롭게 시작", musicUrl + ", " + PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                        Log.d("서비스 비교", PlayerConstants.NOW_SONG_URL + ", " + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl());
                        updateUI();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            changeButton();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "확인");

        PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Integer i[] = (Integer[]) msg.obj;

                durationText.setText(UtilityFunctions.getDuration(i[0]) + " / " + UtilityFunctions.getDuration(i[1]));
                seekBar.setProgress(i[2]);
            }
        };


        //  //downloadmanager
        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(completeReceiver, completeFilter);

    }

    @Override
    protected void onPause() {
        Log.d("onPause", "확인");
        super.onPause();
        //downloadmanager
        unregisterReceiver(completeReceiver);
    }


    public static void changeButton() {
        if (PlayerConstants.SONG_PAUSED) {
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);

        } else {
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
        }

        if (PlayerConstants.timerUse) {
            timerBtn.setVisibility(View.GONE);
            timermintBtn.setVisibility(View.VISIBLE);
        } else {
            timerBtn.setVisibility(View.VISIBLE);
            timermintBtn.setVisibility(View.GONE);
        }
    }

    private void getViews() {
        castTitle = (TextView) findViewById(R.id.title);
        episodeTitle = (TextView) findViewById(episode);
        castImage = (ImageView) findViewById(R.id.castImage);
        // castBigIamge = (ImageView) findViewById(R.id.castBigImage);
        durationText = (TextView) findViewById(R.id.duration);

        playButton = (ImageButton) findViewById(R.id.playButton);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        backwardButton = (ImageButton) findViewById(R.id.backwardButton);
        forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        prevButton = (ImageButton) findViewById(R.id.prevButton);
        nextButton = (ImageButton) findViewById(R.id.nextButton);
        seekBar = (SeekBar) findViewById(R.id.musicSeekBar);

        //  repeatBtn = (Button) findViewById(R.id.repeatBtn);
        // timerBtn = (Button) findViewById(R.id.timerButton);

        timermintBtn = (ImageButton) findViewById(R.id.timermintBtn);
        timerBtn = (ImageButton) findViewById(R.id.timerBtn);
        oneplayBtn = (ImageButton) findViewById(R.id.oneplayBtn);
        allplayBtn = (ImageButton) findViewById(R.id.allplayBtn);

        downBtn = (ImageButton) findViewById(R.id.castDown);
        downCancelBtn = (ImageButton) findViewById(R.id.cancelDown);


        if (PlayerConstants.repeat == 1) {
            allplayBtn.setVisibility(View.VISIBLE);
            oneplayBtn.setVisibility(View.GONE);
           /* repeatBtn.setText("전체 재생");
            repeatBtn.setTextColor(R.color.textColorPrimary);
            repeatBtn.setBackgroundColor(0xcfcfcf);*/
        } else if (PlayerConstants.repeat == 2) {
            allplayBtn.setVisibility(View.GONE);
            oneplayBtn.setVisibility(View.VISIBLE);
            /*repeatBtn.setText("한곡 반복");
            repeatBtn.setTextColor(R.color.textColorPrimary);
            repeatBtn.setBackgroundColor(R.color.colorHighlight);*/
        }


        if (PlayerConstants.timerUse) {//전제재생
            timerBtn.setVisibility(View.GONE);
            timermintBtn.setVisibility(View.VISIBLE);
        } else {
            timerBtn.setVisibility(View.VISIBLE);
            timermintBtn.setVisibility(View.GONE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!readAccepted || !writeAccepted) {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다. ");
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(ContentActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
            }
        });

        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        myDialog.show();
    }


    private void setListeners() {

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasPermission(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
                    requestNecessaryPermissions(PERMISSIONS); //허가 안됐으면 사용자에게 요청
                } else {
                    //이미 허가

                    String realURL = null;

                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    // urlToDownload = Uri.parse(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl());

                    //redirectURL을 realURL로
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl()).openConnection();
                        connection.setInstanceFollowRedirects(false);
                        connection.connect();

                        if (connection.getResponseCode() >= 300 && connection.getResponseCode() < 400) { //리다이렉트 주소일경우
                            realURL = connection.getHeaderField("Location").toString();

                            Log.e("realURL", realURL);
                        } else {
                            realURL = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl();
                            Log.e("realURL", realURL);
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    request = new DownloadManager.Request(Uri.parse(realURL));
                    request.setTitle(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getCastTitle());
                    request.setDescription(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getEpisodeTitle());
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getEpisodeTitle() + ".mp3");
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
                    latestId = downloadManager.enqueue(request);

                }
                downBtn.setVisibility(View.GONE);
                downCancelBtn.setVisibility(View.VISIBLE);
                downCase = 1; //다운로드중.

            }
        });

        downCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.remove(latestId);
                downBtn.setVisibility(View.VISIBLE);
                downCancelBtn.setVisibility(View.GONE);
                downCase = 2; //다운로드 아님.
            }
        });

/*        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PlayerConstants.repeat == 1) { //전체반복이면
                    repeatBtn.setText("한곡 반복");
                    PlayerConstants.repeat = 2;
                    Log.e("repeat", PlayerConstants.repeat + "");

                } else if (PlayerConstants.repeat == 2) { //한곡반복이면
                    repeatBtn.setText("전체 재생");
                    PlayerConstants.repeat = 1;
                    Log.e("repeat", PlayerConstants.repeat + "");
                }
            }
        });*/

        allplayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //한곡재생으로 변경
                PlayerConstants.repeat = 2;
                oneplayBtn.setVisibility(View.VISIBLE);
                allplayBtn.setVisibility(View.GONE);
            }
        });

        oneplayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //w전체재생으로변경
                PlayerConstants.repeat = 1;
                oneplayBtn.setVisibility(View.GONE);
                allplayBtn.setVisibility(View.VISIBLE);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    Log.d("일시정지 후 재생", PlayerConstants.SONG_PAUSED + "");
                    changeButton();
                    Controls.playControl(getApplicationContext());
                } else {
                    //노티에서 플레이 삭제하고, 서비스 중단하고 다시 시작할때.
                    PlayerConstants.SONG_PAUSED = false;
                    Intent i = new Intent(getApplicationContext(), MusicService.class);
                    startService(i);

                }


            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeButton();
                Controls.pauseControl(getApplicationContext());
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.nextControl(getApplicationContext());
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.previousControl(getApplicationContext());
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {

                    Toast.makeText(ContentActivity.this, "30초 앞으로", Toast.LENGTH_SHORT).show();
                    // PlayerConstants.PROGRESSBAR_HANDLER.removeCallbacksAndMessages(0);
                    int movePosition = mp.getCurrentPosition() + 30000;
                    if (mp.getDuration() >= movePosition) {
                        mp.seekTo(movePosition);
                    } else {
                        mp.seekTo(mp.getDuration());
                    }

                }


            }
        });

        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {


                    Toast.makeText(ContentActivity.this, "30초 뒤로", Toast.LENGTH_SHORT).show();

                    int movePosition = mp.getCurrentPosition() - 30000;
                    if (mp.getCurrentPosition() >= movePosition) {
                        mp.seekTo(movePosition);
                    } else {
                        mp.seekTo(0);
                    }
                }

            }
        });
    }

    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (downCase == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);
                builder.setMessage("다운로드가 완료되었습니다. 다운로드 목록으로 이동하겠습니까?").setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();

            } else if (downCase == 2) {
                Toast.makeText(context, "다운로드 취소되었습니다", Toast.LENGTH_SHORT).show();
            }
            downBtn.setVisibility(View.VISIBLE);
            downCancelBtn.setVisibility(View.GONE);

        }

    };

    public static void updateUI() {

        try {
            String episodetitle = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getEpisodeTitle();
            String casttitle = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getCastTitle();
            String musicurl = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicUrl();
            String duration = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getDuration();
            final String imageurl = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getCastImage();

            Log.d("updateUI", episodetitle + casttitle + musicurl + imageurl + duration + "");
            castTitle.setText(casttitle);
            episodeTitle.setText(episodetitle);
            episodeTitle.setSelected(true);
            /*episodeTitle.setSingleLine();
            episodeTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            episodeTitle.setMarqueeRepeatLimit(-1);  // marquee_forever
            */


            musicUrl = musicurl;
           // Glide.with(ContentActivity.this).load(imageurl).thumbnail(0.1f).into(castImage);

            //웹이미지 읽어들이는 쓰레드.
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(imageurl);

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream is = conn.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

            try {
                thread.join();
                castImage.setImageBitmap(bitmap);
                //   castBigIamge.setImageBitmap(bitmap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();

            }

        } catch (Exception e) {
        }
    }

}
