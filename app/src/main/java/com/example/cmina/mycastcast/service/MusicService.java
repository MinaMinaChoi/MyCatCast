package com.example.cmina.mycastcast.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ContentActivity;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.controls.Controls;
import com.example.cmina.mycastcast.receiver.NotificationBroadcast;
import com.example.cmina.mycastcast.util.HttpUtil;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.RssItem;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.cmina.mycastcast.activity.ListActivity.limit;

/**
 * Created by cmina on 2017-02-09.
 */

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    // RssItem data;

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.stop();
        mp.release();
        Toast.makeText(this, "에러발생", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp.getDuration() < 0) {
            //if (mp == null ) {
            Log.d("엠피준비안됨", mp.getDuration() + "");
            mp.reset();
            MediaPlayer.create(MusicService.this, Uri.parse(data.getMusicUrl()));
            //  mp.prepareAsync();
        } else {
            Log.d("mp준비완료", mp.getDuration() + "");
            mp.start();
            updateProgressBar();
        }

    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("서비스 핸들 메시지", "확인");
            //여기서 할일...
            if (PlayerConstants.SONGS_LIST.size() <= 0) {
                PlayerConstants.SONGS_LIST = RssItem.getItemList();
            }

            data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
            String songPath = data.getMusicUrl();

            if (currentVersionSupportLockScreenControls) {
                RegisterRemoteClient();
            }


            try {
                playSong(songPath, data);
                PlayerConstants.NOW_SONG_URL = songPath;
                newNotification();

            } catch (ArithmeticException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    String LOG_CLASS = "MusicService";
    public static MediaPlayer mp;

    //MediaPlayer notification ID
    int NOTIFICATION_ID = 1111;

    //Used to pause/resume MediaPlayer
    private int resumePosition;

    public static final String NOTIFY_PREVIOUS = "com.example.cmina.mypodcast.previous";
    public static final String NOTIFY_DELETE = "com.example.cmina.mypodcast.delete";
    public static final String NOTIFY_PAUSE = "com.example.cmina.mypodcast.pause";
    public static final String NOTIFY_PLAY = "com.example.cmina.mypodcast.play";
    public static final String NOTIFY_NEXT = "com.example.cmina.mypodcast.next";

    private ComponentName remoteComponentName;
    private RemoteControlClient remoteControlClient;

    //AudioFocus
    AudioManager audioManager;

    //Binder given to clients
//    private final IBinder iBinder = new LocalBinder();

    RssItem data;
    Bitmap mDummyAlbumArt;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;


    private static boolean currentVersionSupportBigNotification = false;
    private static boolean currentVersionSupportLockScreenControls = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("서비스 onCreate", "확인");


        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        currentVersionSupportBigNotification = UtilityFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = UtilityFunctions.currentVersionSupportLockScreenControls();

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                //현재 에피소드까지만 듣기
                if (PlayerConstants.timer == 1) {
                    stopSelf();
                    PlayerConstants.SONG_PAUSED = true;
                    ContentActivity.changeButton();
                    Log.e("현재에피소드", "노래끝낫을때");
                    PlayerConstants.timerUse = false;
                    PlayerConstants.timer = 0;

                }

                //전체반복
                if (PlayerConstants.repeat == 1) {
                    Controls.nextControl(getApplicationContext());
                    Log.e("전체반복", "노래끝낫을때");
                }
                //한곡반복
                else if (PlayerConstants.repeat == 2) {
                    Controls.onereplayControl(getApplicationContext());
                    Log.e("한곡반복", "노래끝낫을때");
                }
            }
        });

    }

    Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
            PlayerConstants.PROGRESSBAR_HANDLER.postDelayed(this, 1000);
        }
    };

    //타이머설정시.
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (PlayerConstants.timerUse) {
                stopSelf();
                PlayerConstants.SONG_PAUSED = true;
                PlayerConstants.timerUse = false; //타이머적용된 후에는 원래 설정으로.
                ContentActivity.changeButton();
            }

        }
    };

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mp != null) {
                int progress = UtilityFunctions.getProgressPercentage(mp.getCurrentPosition(), mp.getDuration());//(mp.getCurrentPosition() * 100) / mp.getDuration();
                Integer i[] = new Integer[3];
                i[0] = mp.getCurrentPosition();
                i[1] = mp.getDuration();
                i[2] = progress;

                Log.d("핸들러", i[1] + "");
                try {
                    PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
                } catch (Exception e) {
                }
            }

        }
    };


    //the system calls this method when an activity, requests the service be started.
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        //mediasession

        try {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            ContentActivity.updateUI();
            ContentActivity.changeButton();
            Log.d("온스타트", msg + "");

            PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Handler.Callback() {
                //이전곡, 다음곡을 눌렀을 때, 바로 이곳으로...클릭해서 들어올때도 마찬가지네..그럼왜...
                @Override
                public boolean handleMessage(Message msg) {
                    //Toast.makeText(MusicService.this, "핸들러수거", Toast.LENGTH_SHORT).show();
                    Log.d("송체인지 핸들러", "확인");
                    PlayerConstants.PROGRESSBAR_HANDLER.removeCallbacks(mUpdateTimeTask);

                    msg = mServiceHandler.obtainMessage();
                    msg.arg1 = startId;
                    mServiceHandler.sendMessage(msg);
                    ContentActivity.updateUI();
                    ContentActivity.changeButton();
                    return false;
                }
            });

/*            PlayerConstants.LOAD_MORE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    //어떻게....리스트뷰 더 불러오는 핸들러를 구현하지...

                    PlayerConstants.SONGS_LIST.clear();

                    JSONObject object = new JSONObject();
                    try {
                        object.put("dbNo", String.valueOf(PlayerConstants.CAST_DB_NUMBER));
                        object.put("limit", limit);

                        HttpUtil request = new HttpUtil("http://cmina21.cafe24.com/readFromDB.php");
                        String myJSON = request.execute(object.toString()).get();

                        if (myJSON != null) {
                            JSONArray array = new JSONArray(myJSON);

                            ArrayList<RssItem> rssItemArrayList = new ArrayList<RssItem>();
                            RssItem rssItem;
                            for (int i = 0; i < array.length(); i++) {
                                rssItem = new RssItem();
                                object = array.getJSONObject(i);

                                rssItem.setEpisodeTitle(object.getString("episodeTitle"));
                                rssItem.setDuration(object.getString("duration"));

                                rssItem.setPubdate(object.getString("pubDate").substring(0, 10));
                                rssItem.setMusicUrl(object.getString("musicUrl"));
                                rssItem.setCastTitle(object.getString("castTitle"));
                                rssItem.setCastImage(object.getString("castImage"));
                                rssItem.setCategory(object.getString("category"));

                                rssItemArrayList.add(rssItem);
                                PlayerConstants.SONGS_LIST.add(rssItem);

                                if (rssItemArrayList != null) {
                                    //어떻게...리스트액티비티의 리사이클러뷰에 셋팅을 해주지.

                                }
                            }
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });*/

            //timer handler
            PlayerConstants.TIMER_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String) msg.obj;
                    if (message != null) {
                        PlayerConstants.TIMER_HANDLER.postDelayed(timerRunnable, Long.parseLong(message));
                    }
                    return false;
                }
            });

            PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String) msg.obj;
                    if (mp == null) {
                        return false;
                    }

                    if (message.equalsIgnoreCase("Play")) {
                        PlayerConstants.SONG_PAUSED = false;
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                        }

                        Log.d("핸들러재생", message + "");
                        mp.start();

                    } else if (message.equalsIgnoreCase("Pause")) {
                        PlayerConstants.SONG_PAUSED = true;
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                        }
                        Log.d("핸들러일시정지", message + "");
                        mp.pause();

                    }

                    newNotification();
                    try {
                        ContentActivity.changeButton();
                    } catch (Exception e) {
                    }

                    return false;
                }
            });


        } catch (Exception e) {
        }

        return START_STICKY;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void newNotification() {
        Log.d("뉴노티", "확인");
        String episodeTitle = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getEpisodeTitle();
        String castTitle = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getCastTitle();

        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.caticon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Notification notification = notificationBuilder.build();

        setListeners(simpleContentView);
        setListeners(expandedView);

        notification.contentView = simpleContentView;
        if (currentVersionSupportBigNotification) {
            notification.bigContentView = expandedView;
        }

        try {
            Bitmap albumArt = UtilityFunctions.getCastImage(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getCastImage());
            if (albumArt != null) {
                notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                }
            } else {
                notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, null);
                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //일시정지일 때, 버튼
        if (PlayerConstants.SONG_PAUSED) {
            notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            }
        } else {
            notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
            }
        }

        notification.contentView.setTextViewText(R.id.textSongName, episodeTitle);
        notification.contentView.setTextViewText(R.id.textAlbumName, castTitle);
        if (currentVersionSupportBigNotification) {
            Log.d("확인", episodeTitle + castTitle + "");
            notification.bigContentView.setTextViewText(R.id.textSongName, episodeTitle);
            notification.bigContentView.setTextViewText(R.id.textAlbumName, castTitle);
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);

    }

    //noti의 클릭 리스너
    public void setListeners(RemoteViews view) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);
        Intent intent1 = new Intent(MusicService.this, ContentActivity.class);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

        PendingIntent pActivity = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.noti, pActivity);
    }


    @Override
    public void onDestroy() {
        if (mp != null) {
            Toast.makeText(this, "service destroy", Toast.LENGTH_SHORT).show();
            if (mp.isPlaying())
                mp.stop();
            PlayerConstants.PROGRESSBAR_HANDLER.removeCallbacks(mUpdateTimeTask);
            mp.reset();
            mp.release();
            mp = null;
            stopForeground(true);

            Log.d("엠피stop확인", mp + "");
        }
        super.onDestroy();
    }

    public void updateProgressBar() {
        try {
            Log.d("업데이트프로그레스바", "확인");
            //  PlayerConstants.PROGRESSBAR_HANDLER.removeCallbacks(mUpdateTimeTask);
            PlayerConstants.PROGRESSBAR_HANDLER.postDelayed(mUpdateTimeTask, 1000);
        } catch (Exception e) {
        }
    }

    private void playSong(String value, RssItem item) {
        Log.d("playSong", mp + "확인");
        try {
            if (currentVersionSupportLockScreenControls) {
                UpdateMetadata(item);
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            }
            mp.reset();
            mp.setDataSource(value);
            mp.prepareAsync();

        } catch (IOException e) {
            Toast.makeText(this, "에러발생", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void RegisterRemoteClient() {
        remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception ex) {
        }
    }

    private void UpdateMetadata(RssItem item) {
        if (remoteControlClient == null) {
            return;
        }

        RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //  metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, item.getImageUrl());
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, item.getCastTitle());
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, item.getEpisodeTitle());
            mDummyAlbumArt = UtilityFunctions.getCastImage(item.getCastImage());
            if (mDummyAlbumArt == null) {
                mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
            }
            metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
            metadataEditor.apply();
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }


}
