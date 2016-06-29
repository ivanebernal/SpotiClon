package com.ivanebernal.pennyapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service {
    MediaPlayer mediaPlayer = new MediaPlayer();
    private final IBinder mBinder = new MyBinder();

    public PlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getStringExtra("url") != null) playStream(intent.getStringExtra("url"));

        if(intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)){
            Log.i("info", "start foreground service");
            showNotification();
        }else if(intent.getAction().equals(Constants.ACTION.PREVIOUS_ACTION)){
            Log.i("info", "Prev pressed");
        }
        else if(intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)){
            Log.i("info", "Stop foreground service");
            stopForeground(true);
            stopSelf();
        }
        else if(intent.getAction().equals(Constants.ACTION.NEXT_ACTION)){
            Log.i("info", "Next pressed");
        }
        else if(intent.getAction().equals(Constants.ACTION.PLAY_ACTION)){
            Log.i("info", "Play pressed");
            togglePlayer();
        }

        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        //notificationIntent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(Constants.ACTION.PREVIOUS_ACTION);
        PendingIntent pPreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pNextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_stat_name);

        int playPauseButtonId = android.R.drawable.ic_media_play;
        String playPauseString = "Play";
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            playPauseButtonId = android.R.drawable.ic_media_pause;
            playPauseString = "Pause";
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Music Player")
                .setTicker("Playing music")
                .setContentText("My Song")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous", pPreviousIntent)
                .addAction(playPauseButtonId, playPauseString, pPlayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pNextIntent)
                .build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
            flipPlayPauseButton(false);
            showNotification();
        }catch (Exception e){
            Log.d("EXCEPTION","Failed to pause media player");
        }
    }

    public void playPlayer(){
        try{
            mediaPlayer.start();
            flipPlayPauseButton(true);
            showNotification();
        }catch (Exception e){
            Log.d("EXCEPTION","Failed to play media player");
        }
    }

    private void flipPlayPauseButton(boolean isPlaying) {
        //code to communicate with main thread
        Intent intent = new Intent("changePlayButton");
        intent.putExtra("isPlaying", isPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void togglePlayer(){
        try {
            if(mediaPlayer.isPlaying()){
                pausePlayer();
            }else{
                playPlayer();
            }
        }catch (Exception e){
            Log.d("EXCEPTION","Failed to toggle media player");

        }
    }

    public class MyBinder extends Binder {
        PlayerService getService(){
            return PlayerService.this;
        }
    }

    private AudioManager am;
    private boolean playingBeforeInterruption = false;

    public void getAudioFocusAndPlay (){
        am = (AudioManager) this.getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mediaPlayer.start();
        }
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                if(mediaPlayer.isPlaying()){
                    playingBeforeInterruption = true;
                }else{
                    playingBeforeInterruption = false;
                }
                pausePlayer();
            }else if(focusChange == AudioManager.AUDIOFOCUS_GAIN){
                if(playingBeforeInterruption) playPlayer();
            }else if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
                pausePlayer();
                am.abandonAudioFocus(afChangeListener);
            }
        }
    };
}
