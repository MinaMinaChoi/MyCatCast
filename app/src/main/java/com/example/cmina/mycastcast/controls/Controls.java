package com.example.cmina.mycastcast.controls;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ContentActivity;
import com.example.cmina.mycastcast.service.MusicService;
import com.example.cmina.mycastcast.util.PlayerConstants;
import com.example.cmina.mycastcast.util.UtilityFunctions;

import static com.example.cmina.mycastcast.service.MusicService.mp;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by cmina on 2017-02-09.
 */

public class Controls {

    public static void playControl(Context context) {
        sendMessage(context.getResources().getString(R.string.play));
    }

    public static void pauseControl(Context context) {
        sendMessage(context.getResources().getString(R.string.pause));
    }

    public static void pauseControl2() {
        sendMessage("Pause");
    }

    public static void timerControl(String time) {
        timerMessage(time);
    }

    public static void nextControl(Context context) {
        boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (PlayerConstants.SONGS_LIST.size() > 0) {
            if (PlayerConstants.SONG_NUMBER > 0) {
                PlayerConstants.SONG_NUMBER--;
                PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                PlayerConstants.SONG_PAUSED = false;
            } else {

                if (mp.isPlaying()) { //mp제대로 준비되었을 때만...
                    Toast.makeText(context, "마지막 에피소드입니다.", Toast.LENGTH_SHORT).show();
                   /* PlayerConstants.SONG_PAUSED = true;
                    ContentActivity.changeButton();
                    Controls.pauseControl2();*/
                }


            }
        }

    }

    //한곡 반복
    public static void onereplayControl(Context context) {
        boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (PlayerConstants.SONGS_LIST.size() > 0) {
            if (PlayerConstants.SONG_NUMBER >= 0) {
                PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                Log.e("한곡 반복 Controls", PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getEpisodeTitle());
            }
        }
        PlayerConstants.SONG_PAUSED = false;
    }

    public static void previousControl(Context context) {
        boolean isServiceRunning = UtilityFunctions.isServiceRunning(MusicService.class.getName(), context);
        if (!isServiceRunning)
            return;
        if (PlayerConstants.SONGS_LIST.size() > 0) {
            if (PlayerConstants.SONG_NUMBER < (PlayerConstants.SONGS_LIST.size() - 1)) {
                PlayerConstants.SONG_NUMBER++;
                PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
            } else {
                /*if (PlayerConstants.allPlayCount > PlayerConstants.SONGS_LIST.size()) {
                    //로딩할 곡이 더 남아있다면...더 로딩해라.
                    // PlayerConstants.LOAD_MORE_HANDLER.sendMessage(PlayerConstants.LOAD_MORE_HANDLER.obtainMessage());
                    Toast.makeText(context, "로딩모어 해야함...", Toast.LENGTH_SHORT).show();
                }*/
                if (mp.isPlaying()) {
                    Toast.makeText(context, "첫번째 에피소드입니다.", Toast.LENGTH_SHORT).show();
                }

            }
        }
        PlayerConstants.SONG_PAUSED = false;
    }

    private static void sendMessage(String message) {
        try {
            PlayerConstants.PLAY_PAUSE_HANDLER.sendMessage(PlayerConstants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
        } catch (Exception e) {
        }
    }

    private static void timerMessage(String message) {
        try {
            PlayerConstants.TIMER_HANDLER.sendMessage(PlayerConstants.TIMER_HANDLER.obtainMessage(0, message));

        } catch (Exception e) {
        }
    }
}
