package com.example.Rythemica;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.Rythemica.com.rythemica.event.Listener;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class InPlay extends AppCompatActivity {
    ImageView img ;
    TextView txt ;
    ArrayList<Song> arrayList ;
    Uri imageUri ;
    ImageButton play ;
    ImageButton next ;
    ImageButton previous ;
    MediaPlayerServices mediaPlayerServices ;
    public  static final String ACTION_PAUSE= "com.Rythemica.ACTION_PAUSE_SONGADAPTER";
    public static final  String ACTION_RESUME = "com.Rythemica.ACTION_RESUME_SONGADAPTER";
    Intent intent ;
    public  static  final  String ACTION_NEXT = "com.Rythemica.ACTION_NEXT_MAINACTIVITY" ;
    public  static  final  String ACTION_PREVIOUS = "com.Rythemic.ACTION_PREVIOUS_MAINACTIVITY" ;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerServices.LocalBinder binder = (MediaPlayerServices.LocalBinder) service;
            mediaPlayerServices = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

   /* @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        play = findViewById(R.id.playon);
        play.setOnClickListener(onClickListener);
        return super.onCreateView(name, context, attrs);

    }*/

    Song song  ;
    PlaybackStatus state = PlaybackStatus.PLAYING;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_play);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        img = findViewById(R.id.sora);
        txt = findViewById(R.id.klam) ;
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        updateUI(storageUtil.loadSongIndex());
       intent = new Intent(this , MediaPlayerServices.class) ;
       bindService(intent , serviceConnection , Context.BIND_AUTO_CREATE) ;
       play = findViewById(R.id.playon);
       next = findViewById(R.id.nexton);
       previous = findViewById(R.id.previousone);
       play.setOnClickListener(onClickListener);
       next.setOnClickListener(nextClicked);
       previous.setOnClickListener(previousClicked);

    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (state == PlaybackStatus.PLAYING){
                Intent intent = new Intent(ACTION_PAUSE);
                sendBroadcast(intent);
               changeBtnIconToply();
                state = PlaybackStatus.PAUSED;
            }
            else if (state ==PlaybackStatus.PAUSED){
                Intent intent = new Intent(ACTION_RESUME) ;
                sendBroadcast(intent);
                changeBtnIconToPause();
                state = PlaybackStatus.PLAYING ;
            }
        }
    };
    View.OnClickListener nextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ACTION_NEXT);
            sendBroadcast(intent);
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            updateUI(storageUtil.loadSongIndex() + 1);
        }
    };
    View.OnClickListener previousClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ACTION_PREVIOUS);
            sendBroadcast(intent);
           StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            updateUI(storageUtil.loadSongIndex()-1);
        }

    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void changeBtnIconToPause() {
        play.setImageResource(R.drawable.circle_pause);
    }

    private void changeBtnIconToply() {
        play.setImageResource(R.drawable.circle_play);
    }

    private void updateUI(int audioIndex){
        Uri uri= Uri.parse("content://media/external/audio/albumart");
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        arrayList= storageUtil.loadSong();
       if(audioIndex == arrayList.size()){
           song = arrayList.get(0);
       }else if (audioIndex == -1){
           song = arrayList.get(arrayList.size() -1 );
       }

        else{song = arrayList.get(audioIndex);}
        Log.d("tmam", "updateUI: " + audioIndex);
        imageUri = ContentUris.withAppendedId(uri,song.getAlbum_id());
        Log.d("tmam", "updateUI:  " + imageUri);
        Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(img);
        txt.setText(song.getName());
        txt.setSelected(true);
    }

        class UpdateUi extends AsyncTask<String ,String[], Integer>{
            @Override
            protected Integer doInBackground(String...strings) {
                StorageUtil storageUtil = new StorageUtil(getApplicationContext());

                return storageUtil.loadSongIndex() +1;
            }

            @Override
            protected void onPostExecute(Integer audioIndex) {
                updateUI(audioIndex);
            }
        }
}
