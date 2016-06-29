package com.ivanebernal.pennyapp;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ivan on 22/06/16.
 */
public class Player {
    MediaPlayer mediaPlayer = new MediaPlayer();
    public static Player player;
    String url = "";

    public Player(){
        this.player = this;
    }

    public void playStream(String url){
        if(mediaPlayer != null){
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                mediaPlayer.stop();
            }catch (Exception e){
            }
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playPlayer();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    MainActivity.flipPlayPauseButton(false);
                }
            });
            mediaPlayer.prepareAsync();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void pausePlayer(){
        try{
            mediaPlayer.pause();

        }catch (Exception e){
            Log.d("EXCEPTION","Failed to pause media player");
        }
    }

    public void playPlayer(){
        try{
            mediaPlayer.start();
        }catch (Exception e){
            Log.d("EXCEPTION","Failed to play media player");
        }
     }

    public void togglePlayer(){
        try {
            if(mediaPlayer.isPlaying()){
                pausePlayer();
            }else{
                playPlayer();
            }
            MainActivity.flipPlayPauseButton(mediaPlayer.isPlaying());
        }catch (Exception e){
            Log.d("EXCEPTION","Failed to toggle media player");

        }
    }

}
