package com.example.Rythemica;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import java.io.IOException;
import java.util.ArrayList;
import android.support.v4.media.session.MediaSessionCompat ;
import android.support.v4.media.MediaMetadataCompat ;

import com.example.myapplication.R;

public class MediaPlayerServices extends Service implements MediaPlayer.OnCompletionListener
        ,MediaPlayer.OnPreparedListener
        , MediaPlayer.OnSeekCompleteListener
        , MediaPlayer.OnErrorListener
        , AudioManager.OnAudioFocusChangeListener
        , MediaPlayer.OnInfoListener


{
    private final String changeToPlay = "com.Rythemica.mainActivity_changeToPly" ;
    private final String changeToPause = "com.Rythemica.mainActivity_changeToPause" ;
    public  static final String ACTION_PLAY = "com.Rythemica.ACTION_PLAY";
    public  static final String ACTION_PAUSE = "com.Rythemica.ACTION_PAUSE";
    public  static final String ACTION_PREVIOUS = "com.Rythemica.ACTION_PREVIOUS";
    public  static final String ACTION_NEXT = "com.Rythemic.ACTION_NEXT";
    public  static final String ACTION_STOP = "com.Rythemica.ACTION_STOP" ;
    private MediaSessionManager mediaSessionManager ;
    private MediaSessionCompat mediaSession ;
    private MediaControllerCompat.TransportControls transportControls ;
    private static final int notification_id = 101 ;
    ArrayList<Song> arrayList ;
    LocalBinder localBinder = new LocalBinder();
    Boolean isItPlaying ;
    private AudioManager audioManager ;
    public  View v ;
    ImageButton btn ;
    PlaybackStatus playbackStatus ;
    String TAG = "MediaplayerServices";
    private PhoneStateListener phoneStateListener ;
    private Boolean onGoingCall = false ;
    private TelephonyManager telephonyManager ;
    private int audioIndex = -1;
    private  Song song ;

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null)return;
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE) ;
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer") ;
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        updateMetaData();
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                play();
                resume();
                sendBroadcast(new Intent(changeToPlay));
                buildNotification(PlaybackStatus.PLAYING);
                playbackStatus = PlaybackStatus.PLAYING;

            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
                sendBroadcast(new Intent(changeToPause));
                buildNotification(PlaybackStatus.PAUSED);
                playbackStatus = PlaybackStatus.PAUSED ;
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext() ;
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }
    BroadcastReceiver skipToNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToNext() ; updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
        private  void register_skipTONext(){
            IntentFilter intentFilter = new IntentFilter(MainActivity.ACTION_NEXT);
            registerReceiver(skipToNext , intentFilter);
        }
    BroadcastReceiver skipToPrevious = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skipToPrevious();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
        private  void register_skipTOPrevious(){
    IntentFilter intentFilter = new IntentFilter(MainActivity.ACTION_PREVIOUS);
    registerReceiver(skipToPrevious , intentFilter);
}
    private void updateMetaData(){
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.music123);
        try {
            initMediaSession();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART , albumArt).
                 putString(MediaMetadataCompat.METADATA_KEY_ARTIST , song.getSongArtist() ).
                 putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.getName()).build());
    }
    private void buildNotification(PlaybackStatus playbackStatus){
        int notificationAction = android.R.drawable.ic_media_pause ;
        int notificationActionPlay = android.R.drawable.ic_media_play ;
        int notifictaionActionPause = android.R.drawable.ic_media_pause ;
        PendingIntent play_pauseAction = null ;

        if (playbackStatus == PlaybackStatus.PLAYING){
            notificationAction = notifictaionActionPause ;
            play_pauseAction = playBackAction(1) ;
        }else if(playbackStatus == PlaybackStatus.PAUSED){
            notificationAction = notificationActionPlay ;
            play_pauseAction = playBackAction(0);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.music123);
        NotificationCompat.Builder  notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this).
                setShowWhen(false).
                setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0 , 1 , 2))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(song.getName())
                .setContentInfo(song.getSongArtist())
                .addAction(android.R.drawable.ic_media_previous,"previous",playBackAction(3))
                .addAction(notificationAction , "pause" , play_pauseAction)
                .addAction(android.R.drawable.ic_media_next , "next" , playBackAction(2));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notification_id ,notificationBuilder.build());
    }
    private PendingIntent playBackAction(int actionNumber){
      Intent playBackAction = new Intent(this,MediaPlayerServices.class);
       switch (actionNumber) {
           case 0: playBackAction.setAction(ACTION_PLAY);
                    playbackStatus = PlaybackStatus.PLAYING ;
               return PendingIntent.getService(this,actionNumber , playBackAction , 0);
           case 1: playBackAction.setAction(ACTION_PAUSE);
                    playbackStatus = PlaybackStatus.PAUSED ;
               return PendingIntent.getService(this , actionNumber , playBackAction , 0) ;
           case 2 : playBackAction.setAction(ACTION_NEXT);
               return PendingIntent.getService(this , actionNumber , playBackAction , 0);
           case 3 : playBackAction.setAction(ACTION_PREVIOUS) ;
               return PendingIntent.getService(this , actionNumber , playBackAction , 0);
            default: break;

       }
    return null ; }
    private void handleIncomingActions(Intent playbackAction){
        if (playbackAction == null || playbackAction.getAction() == null) return;
        String actionString = playbackAction.getAction() ;
        if (actionString.equalsIgnoreCase(ACTION_PLAY)){
            transportControls.play();
        }else if (actionString.equalsIgnoreCase(ACTION_PAUSE)){
            transportControls.pause();
        }else if (actionString.equalsIgnoreCase(ACTION_NEXT)){
            transportControls.skipToNext();
        }else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)){
            transportControls.skipToPrevious();
        }else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)){
            transportControls.stop();
        }
    }

    private  void removeNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_id);
    }
    private BroadcastReceiver playNewAudio= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StorageUtil storageUtil = new StorageUtil(getApplicationContext()) ;
            audioIndex = storageUtil.loadSongIndex();
            arrayList = storageUtil.loadSong() ;
            if(audioIndex != -1 && audioIndex < arrayList.size()){
               song = arrayList.get(audioIndex) ;
            }
            else {stopSelf();}
            stop();
            mediaPlayer.reset();
            createPlayer();
            try {
                initMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            playbackStatus = PlaybackStatus.PLAYING ;

        }
    };
    private void register_playNewAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio,filter);
    }
    private BroadcastReceiver pausePlayingAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
            updateMetaData();
            buildNotification(PlaybackStatus.PAUSED);
            playbackStatus = PlaybackStatus.PAUSED ;

        }
    };
    private void register_pausePlayingAudio(){
        IntentFilter intentFilter = new IntentFilter(SongAdapter.ACTION_PAUSE) ;
        registerReceiver(pausePlayingAudio , intentFilter) ;
    }
    BroadcastReceiver resumeAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                play();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                playbackStatus = PlaybackStatus.PLAYING ;
        }
    };
    private  void register_resumeAudioReciever(){
        IntentFilter intentFilter = new IntentFilter(SongAdapter.ACTION_RESUME);
        registerReceiver(resumeAudioReceiver, intentFilter) ;

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            arrayList = storageUtil.loadSong();
            audioIndex = storageUtil.loadSongIndex();
            if (audioIndex != -1 && audioIndex<arrayList.size()){
                song = arrayList.get(audioIndex);
            }else {
                stopSelf();
            }

        }catch (NullPointerException e){stopSelf();}
        if (requestFocus() == false){
            stopSelf();
        }
        if (mediaSessionManager == null){
            try {
                initMediaSession();

            createPlayer();
            }catch (RemoteException e){
                e.printStackTrace();
                stopSelf();
            }
        buildNotification(PlaybackStatus.PLAYING);

        }
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);

    }
    @Override
  public void  onCreate(){
        super.onCreate();
            regesterBecomingNoisyReciever();
            PhoneStateListener();
            register_playNewAudio();
            register_pausePlayingAudio();
            register_resumeAudioReciever();
            register_skipTONext();
            register_skipTOPrevious();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            stop();
            mediaPlayer.release();
        }
        unregisterReceiver(becomingNoisyReciever);
        unregisterReceiver(pausePlayingAudio);
        unregisterReceiver(resumeAudioReceiver);
        unregisterReceiver(skipToNext);
        unregisterReceiver(skipToPrevious);
        removeAudioFocus();
         if (phoneStateListener != null){
             telephonyManager.listen(phoneStateListener , phoneStateListener.LISTEN_NONE);
         }
            removeNotification() ;
            unregisterReceiver(playNewAudio);
            new StorageUtil(getApplicationContext()).clearCachedSongList();
            removeNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
           stop();
           stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
            play();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN :
            if (mediaPlayer == null){
                createPlayer();
            }else if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                mediaPlayer.setVolume(1.0f ,1.0f);

            }
                break;
            case AudioManager.AUDIOFOCUS_LOSS :
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null ;
                }break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT : if (playbackStatus == PlaybackStatus.PLAYING) mediaPlayer.pause();
            break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK : if (playbackStatus == PlaybackStatus.PLAYING) {
                mediaPlayer.setVolume(0.1f, 0.1f);
                Log.d("tmam", "onAudioFocusChange: ana etnadaht ");
                break;
               /* try {
                    wait(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

        }
    }

        private boolean requestFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int keep = audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if (keep ==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            return true;
        }
        return false;
        }
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

     MediaPlayer mediaPlayer = new MediaPlayer();
    String songPath  = "/storage/emulated/0/Music/bluetooth/Skillet_-_Monster_Official_Video[SaveFrom.online].mp3";
    private BroadcastReceiver becomingNoisyReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                pause();
                 buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private  void regesterBecomingNoisyReciever(){
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReciever,filter);
    }
    private  void PhoneStateListener(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                switch(state){
                    case TelephonyManager.CALL_STATE_OFFHOOK :
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null){
                            pause();
                            onGoingCall = true ;
                        }
                        break;
                    case  TelephonyManager.CALL_STATE_IDLE :
                      if (mediaPlayer != null){
                        if(onGoingCall){
                            resume();
                            onGoingCall = false;
                        }}
                      break;

                }
            }
        };
        telephonyManager.listen(phoneStateListener,phoneStateListener.LISTEN_CALL_STATE);
    }
    private void skipToNext() {
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        audioIndex = storageUtil.loadSongIndex();
        if (audioIndex == arrayList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            song = arrayList.get(audioIndex);
        } else {
            //get next in playlist
            song = arrayList.get(++audioIndex);

        }


        new StorageUtil(getApplicationContext()).storeSongIndex(audioIndex);

        stop();
        //reset mediaPlayer
        mediaPlayer.reset();
        createPlayer();
    }

    private void skipToPrevious() {
        StorageUtil storageUtil = new StorageUtil(getApplicationContext()) ;
        audioIndex = storageUtil.loadSongIndex();
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = arrayList.size() - 1;
            song = arrayList.get(audioIndex);
        } else {
            //get previous in playlist
            song = arrayList.get(--audioIndex);

        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeSongIndex(audioIndex);

        stop();
        //reset mediaPlayer
        mediaPlayer.reset();
        createPlayer();
    }
    public void createPlayer(){
        Log.d(TAG, "createPlayer: i was called" + songPath);
        mediaPlayer = new MediaPlayer() ;
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(song.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
            mediaPlayer.prepareAsync();
    }
        public void play(){
            Log.d("URI", "play: "+songPath);
           if (!mediaPlayer.isPlaying()){mediaPlayer.start();
            isItPlaying = true ;
            isPaused = false ;}
            Log.d(TAG, "play: i was called");

          //   btn = (ImageButton) v ;
           //  btn.setImageResource(R.drawable.circle_pause);
    }
        public void stop(){
          if (mediaPlayer == null)return;
          else if (mediaPlayer.isPlaying()){mediaPlayer.stop();
            isItPlaying = false ;}
            Log.d(TAG, "stop: i was called ");
            //btn = (ImageButton) v ;
            //btn.setImageResource(R.drawable.circle_play);

        }
        int resumePosition ;
        boolean isPaused = false ;
            public void pause(){
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                   resumePosition= mediaPlayer.getCurrentPosition();
                    isPaused = true ;
                   // btn = (ImageButton) v ;
                   // btn.setImageResource(R.drawable.circle_play);
                }
            }
         public void resume(){
              if (!mediaPlayer.isPlaying()){ mediaPlayer.seekTo(resumePosition);
              isPaused= false ;}
        //     btn = (ImageButton) v ;
          //   btn.setImageResource(R.drawable.circle_pause);
            }
    public class LocalBinder extends Binder {
        public MediaPlayerServices getService() {
            return MediaPlayerServices.this;
        }
    }


}
