package com.demotxt.droidsrce.homedashboard.Utils;

import android.media.MediaPlayer;

import com.demotxt.droidsrce.homedashboard.Drive;
import com.demotxt.droidsrce.homedashboard.R;

public class Alarm {
    private MediaPlayer mediaPlayer;

    public Alarm(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void play() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(Drive.getAppContext(), R.raw.beep);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayer();
                }
            });
        }
        mediaPlayer.start();
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.pause();
        }
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}